#!/usr/bin/env python3
"""BloodHelper MPRIS bridge. Python 3 stdlib + busctl >= 245, no shell or pip modules.

Reads only the current user's session bus. Each control targets a unique D-Bus owner
and an observed token; discovery never activates or starts an application.
"""
import base64
import hashlib
import json
import math
import os
import queue
import re
import selectors
import shutil
import subprocess
import sys
import threading
import time
import urllib.parse
import urllib.request

PLAYER = "org.mpris.MediaPlayer2.Player"
PROPERTIES = "org.freedesktop.DBus.Properties"
OBJECT = "/org/mpris/MediaPlayer2"
NO_TRACK = "/org/mpris/MediaPlayer2/TrackList/NoTrack"
MAX_TIME = 365 * 24 * 60 * 60 * 1000
MAX_ART = 2 * 1024 * 1024
SERVICE_RE = re.compile(r"org\.mpris\.MediaPlayer2\.[A-Za-z0-9_.-]{1,220}\Z")
TRACK_RE = re.compile(r"/(?:[A-Za-z0-9_]+(?:/[A-Za-z0-9_]+)*)?\Z")
TOKEN_RE = re.compile(r"[a-f0-9]{32}\Z")


def number(value, fallback=0.0):
    try:
        result = float(value)
        return result if math.isfinite(result) else fallback
    except (ValueError, TypeError, OverflowError):
        return fallback


def clean(value, limit=1024):
    if not isinstance(value, str):
        return ""
    return "".join(c if ord(c) >= 32 else " " for c in value)[:limit]


def unwrap(value):
    if isinstance(value, dict):
        if set(value) == {"type", "data"}:
            return unwrap(value["data"])
        return {key: unwrap(item) for key, item in value.items()}
    if isinstance(value, list):
        return [unwrap(item) for item in value]
    return value


class BusError(Exception):
    pass


class Bus:
    def __init__(self):
        self.executable = shutil.which("busctl")
        if not self.executable:
            raise BusError("missing_busctl: install systemd busctl >= 245")

    def call(self, *arguments):
        # Bounding output while reading prevents a broken/malicious local player from
        # forcing communicate() to retain an arbitrarily large D-Bus metadata reply.
        process = subprocess.Popen([self.executable, "--user", "--json=short", "--timeout=1",
                                    "--auto-start=no", "--no-pager", *arguments],
                                   stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                   stderr=subprocess.DEVNULL)
        selector = selectors.DefaultSelector()
        result = bytearray()
        deadline = time.monotonic() + 1.5
        try:
            selector.register(process.stdout, selectors.EVENT_READ)
            while selector.get_map():
                remaining = deadline - time.monotonic()
                if remaining <= 0:
                    raise BusError("mpris_timeout")
                for key, _ in selector.select(min(remaining, .1)):
                    chunk = os.read(key.fd, 65536)
                    if not chunk:
                        selector.unregister(key.fileobj)
                        continue
                    result.extend(chunk)
                    if len(result) > 256 * 1024:
                        raise BusError("mpris_reply_too_large")
            if process.wait(timeout=max(.01, deadline - time.monotonic())):
                raise BusError("mpris_request_unavailable")
            if not result.strip():
                return None
            response = json.loads(result)
            data = response.get("data", [])
            return unwrap(data[0]) if data else None
        except (OSError, ValueError, subprocess.TimeoutExpired) as failure:
            raise BusError("mpris_request_unavailable") from failure
        finally:
            selector.close()
            if process.poll() is None:
                process.kill()
            process.wait()
            process.stdout.close()

    def daemon(self, method, *args):
        return self.call("call", "org.freedesktop.DBus", "/org/freedesktop/DBus",
                         "org.freedesktop.DBus", method, *args)

    def properties(self, owner):
        result = self.call("call", owner, OBJECT, PROPERTIES, "GetAll", "s", PLAYER)
        if not isinstance(result, dict):
            raise BusError("invalid_mpris_properties")
        return result

    def transport(self, owner, method):
        return self.call("call", owner, OBJECT, PLAYER, method)

    def seek(self, owner, track, position_ms):
        return self.call("call", owner, OBJECT, PLAYER, "SetPosition", "ox", track, str(position_ms * 1000))

    def volume(self, owner, value):
        return self.call("set-property", owner, OBJECT, PLAYER, "Volume", "d", str(value))


class ArtworkRedirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, response, code, message, headers, new_url):
        parsed = urllib.parse.urlsplit(new_url)
        if parsed.scheme not in ("http", "https") or parsed.username:
            raise ValueError("unsupported_artwork_redirect")
        return super().redirect_request(request, response, code, message, headers, new_url)


class Artwork:
    """One daemon worker and one outstanding URL; artwork never delays commands."""
    def __init__(self):
        self.lock = threading.Lock()
        self.url = ""
        self.result = None
        self.pending = ""
        self.retry_at = 0

    def get(self, url):
        url = clean(url, 8192)
        with self.lock:
            if url == self.url:
                result = self.result
            else:
                result = None
            if url and not self.pending and (url != self.url or time.monotonic() >= self.retry_at):
                if url != self.url or self.result is None:
                    self.pending = url
                    threading.Thread(target=self.load, args=(url,), daemon=True,
                                     name="bloodhelper-mpris-cover").start()
            return result

    def load(self, url):
        result = None
        try:
            parsed = urllib.parse.urlsplit(url)
            if parsed.scheme == "file" and parsed.netloc in ("", "localhost"):
                path = urllib.request.url2pathname(parsed.path)
                # Do not read FIFOs/devices: they can block indefinitely even in a worker.
                if not os.path.isfile(path) or os.path.getsize(path) > MAX_ART:
                    raise ValueError("unsupported_artwork_file")
                with open(path, "rb") as stream:
                    result = stream.read(MAX_ART + 1)
            elif parsed.scheme in ("http", "https") and parsed.hostname and not parsed.username:
                request = urllib.request.Request(url, headers={"User-Agent": "BloodHelper-MPRIS/1"})
                with urllib.request.build_opener(ArtworkRedirects()).open(request, timeout=3) as stream:
                    deadline = time.monotonic() + 3
                    data = bytearray()
                    while len(data) <= MAX_ART and time.monotonic() < deadline:
                        chunk = stream.read1(min(65536, MAX_ART + 1 - len(data)))
                        if not chunk:
                            result = bytes(data)
                            break
                        data.extend(chunk)
            if not result or len(result) > MAX_ART:
                result = None
            # NativeImage accepts images, never expose arbitrary file data to its decoder.
            if result and not (result.startswith(b"\x89PNG\r\n\x1a\n") or result.startswith(b"\xff\xd8\xff")
                               or result.startswith((b"GIF87a", b"GIF89a"))
                               or result.startswith(b"RIFF") and result[8:12] == b"WEBP"):
                result = None
        except (OSError, ValueError):
            result = None
        finally:
            with self.lock:
                self.url = url
                self.result = base64.b64encode(result).decode("ascii") if result else None
                self.pending = ""
                self.retry_at = time.monotonic() + 30


class Bridge:
    def __init__(self, bus, preferred=""):
        self.bus = bus
        self.preferred = preferred if SERVICE_RE.fullmatch(preferred) else ""
        self.players = {}
        self.active = ""
        self.discovery_at = 0
        self.artwork = Artwork()
        self.seek_reply = (0, False)
        self.volume_reply = (0, False)
        self.volume_failed = set()
        self.duck_epoch = 0
        self.duck_factor = 1.0
        self.duck_until = 0
        self.duck = None
        self.duck_suppressed = None
        self.error = ""

    def discover(self):
        names = self.bus.daemon("ListNames")
        if not isinstance(names, list):
            raise BusError("session_bus_unavailable")
        names = sorted(name for name in names if isinstance(name, str) and SERVICE_RE.fullmatch(name))[:16]
        players = {}
        # Limit slow unresponsive players to a bounded discovery slice. A selected
        # player is queried first, so a broken unrelated application cannot starve it.
        names.sort(key=lambda name: (name != self.preferred, name != self.active, name))
        deadline = time.monotonic() + 3
        for name in names:
            if time.monotonic() > deadline:
                break
            try:
                owner = self.bus.daemon("GetNameOwner", "s", name)
                if not isinstance(owner, str) or not re.fullmatch(r":[0-9]+\.[0-9]+", owner):
                    continue
                properties = self.bus.properties(owner)
                players[name] = (owner, properties)
            except BusError:
                continue
        self.players = players
        self.volume_failed.intersection_update(owner for owner, _ in players.values())
        self.discovery_at = time.monotonic() + 2

    def select(self):
        if self.preferred:
            selected = self.preferred if self.preferred in self.players else ""
        else:
            playing = sorted(name for name, (_, p) in self.players.items() if p.get("PlaybackStatus") == "Playing")
            selected = self.active if self.active in playing else playing[0] if playing else (
                self.active if self.active in self.players else next(iter(self.players), ""))
        if selected != self.active:
            self.restore_duck()
            self.seek_reply = (0, False)
            self.volume_reply = (0, False)
            self.active = selected
        return self.players.get(self.active)

    def observe(self):
        now = time.monotonic()
        try:
            if now >= self.discovery_at:
                self.discover()
            current = self.select()
            if current:
                owner, _ = current
                properties = self.bus.properties(owner)
                self.players[self.active] = (owner, properties)
                self.apply_duck(owner, properties)
            self.error = ""
        except BusError:
            self.error = "session_bus_unavailable"
            self.restore_duck()
            self.players.pop(self.active, None)
            self.active = ""
            self.discovery_at = now + 2
        return self.packet()

    def identity(self):
        current = self.players.get(self.active)
        if not current:
            return "", "", "", {}, {}
        owner, properties = current
        metadata = properties.get("Metadata", {})
        metadata = metadata if isinstance(metadata, dict) else {}
        track = clean(metadata.get("mpris:trackid"), 1024)
        identity = [owner, track, metadata.get("xesam:url", ""), metadata.get("xesam:title", ""), metadata.get("xesam:artist", [])]
        token = hashlib.sha256(json.dumps(identity, ensure_ascii=True, sort_keys=True).encode()).hexdigest()[:32]
        target = hashlib.sha256(owner.encode()).hexdigest()[:32]
        return owner, token, target, properties, metadata

    def packet(self):
        owner, token, target, properties, metadata = self.identity()
        if not owner:
            return {"kind": "snapshot", "available": False}
        title = clean(metadata.get("xesam:title"))
        artists = metadata.get("xesam:artist", [])
        artist = clean(", ".join(str(item) for item in artists[:16]) if isinstance(artists, list) else artists)
        # Some radio streams provide only a URL. Keep the player selectable but don't
        # invent a title or leak file paths into the HUD.
        duration = int(max(0, min(MAX_TIME, number(metadata.get("mpris:length")) / 1000)))
        position = int(max(0, min(MAX_TIME, number(properties.get("Position")) / 1000)))
        control = properties.get("CanControl") is True
        track = metadata.get("mpris:trackid", "")
        can_seek = control and properties.get("CanSeek") is True and duration > 0 and isinstance(track, str) and bool(TRACK_RE.fullmatch(track)) and track != NO_TRACK
        volume = number(properties.get("Volume"), -1)
        can_volume = control and volume >= 0 and owner not in self.volume_failed
        if self.duck and self.duck["owner"] == owner:
            volume = self.duck["baseline"]
        packet = {"kind": "snapshot", "available": bool(title or artist), "title": title, "artist": artist,
                  "playing": properties.get("PlaybackStatus") == "Playing", "durationMs": duration,
                  "positionMs": position, "observedAtEpochMs": int(time.time() * 1000),
                  "playbackRate": max(0, min(16, number(properties.get("Rate"), 1))),
                  "canSeek": can_seek, "trackToken": token,
                  "seekRequestId": self.seek_reply[0], "seekAccepted": self.seek_reply[1],
                  "canControlVolume": can_volume, "volume": max(0, min(1, volume)),
                  "volumeTargetToken": target, "volumeRequestId": self.volume_reply[0], "volumeAccepted": self.volume_reply[1],
                  "canFavorite": False}
        packet["artworkBase64"] = self.artwork.get(metadata.get("mpris:artUrl", ""))
        return packet

    def sources(self):
        return {"kind": "sources", "activeId": self.active,
                "apps": [{"id": name, "playing": props.get("PlaybackStatus") == "Playing"}
                         for name, (_, props) in self.players.items()]}

    def command(self, value):
        fields = value.split(":")
        if len(fields) == 4 and fields[0] == "duck":
            try:
                epoch, active, remaining = map(int, fields[1:])
                if epoch <= 0 or active not in (0, 1) or not 0 <= remaining <= 1000:
                    return
                if epoch != self.duck_epoch:
                    self.restore_duck()
                    self.duck_suppressed = None
                self.duck_epoch = epoch
                self.duck_factor = remaining / 1000
                self.duck_until = time.monotonic() + 3 if active else 0
                if not active:
                    self.restore_duck()
            except ValueError:
                pass
            return
        # Refresh before every action. A stale UI gesture cannot affect the next track
        # or a replacement application which reused the well-known service name.
        self.observe()
        owner, token, target, properties, metadata = self.identity()
        if not owner or properties.get("CanControl") is not True:
            return
        try:
            if len(fields) == 3 and fields[0] == "transport" and fields[1] == target:
                action = fields[2]
                if action == "toggle":
                    action = "Pause" if properties.get("PlaybackStatus") == "Playing" else "Play"
                    if properties.get("CanPause" if action == "Pause" else "CanPlay") is True:
                        self.bus.transport(owner, action)
                elif action in ("next", "previous") and properties.get("CanGoNext" if action == "next" else "CanGoPrevious") is True:
                    self.bus.transport(owner, "Next" if action == "next" else "Previous")
            elif len(fields) == 4 and fields[0] in ("seek", "volume") and TOKEN_RE.fullmatch(fields[1]):
                request_id, amount = int(fields[2]), int(fields[3])
                if not 0 < request_id <= 9223372036854775807:
                    return
                if fields[0] == "seek":
                    self.seek_reply = (request_id, False)
                    if fields[1] != token or not self.packet().get("canSeek"):
                        return
                    amount = max(0, min(self.packet()["durationMs"], amount))
                    self.bus.seek(owner, metadata["mpris:trackid"], amount)
                    self.observe()
                    self.seek_reply = (request_id, self.identity()[1] == token and abs(self.packet().get("positionMs", -10000) - amount) <= 1500)
                else:
                    self.volume_reply = (request_id, False)
                    if fields[1] != target or not 0 <= amount <= 1000 or not self.packet().get("canControlVolume"):
                        return
                    requested = amount / 1000
                    actual = requested
                    if self.duck and self.duck["owner"] == owner:
                        self.duck["baseline"] = requested
                        actual *= self.duck_factor
                        self.duck["written"] = actual
                    try:
                        self.bus.volume(owner, actual)
                    except BusError:
                        self.volume_failed.add(owner)
                        raise
                    self.observe()
                    self.volume_reply = (request_id, self.identity()[2] == target and abs(self.packet().get("volume", -10) - requested) <= .015)
        except (ValueError, BusError):
            # Unsupported controls fail in the selected application; never fall back
            # to media keys, global mixer volume, or another player.
            pass

    def restore_duck(self):
        old, self.duck = self.duck, None
        if not old:
            return
        try:
            volume = number(self.bus.properties(old["owner"]).get("Volume"), -1)
            if abs(volume - old["written"]) <= .015:
                self.bus.volume(old["owner"], old["baseline"])
        except BusError:
            pass

    def apply_duck(self, owner, properties):
        if self.duck and self.duck["owner"] != owner:
            self.restore_duck()
        if time.monotonic() >= self.duck_until:
            self.restore_duck()
            return
        volume = number(properties.get("Volume"), -1)
        if properties.get("CanControl") is not True or volume < 0 or owner in self.volume_failed:
            return
        if self.duck_suppressed == (self.duck_epoch, owner):
            return
        if self.duck:
            if abs(volume - self.duck["written"]) > .015:
                # An external/user adjustment wins; don't overwrite it or restore an
                # obsolete baseline when the screen closes.
                self.duck = None
                self.duck_suppressed = (self.duck_epoch, owner)
                return
        else:
            self.duck = {"owner": owner, "baseline": volume, "written": volume}
        wanted = self.duck["baseline"] * self.duck_factor
        if abs(wanted - volume) > .001:
            try:
                self.bus.volume(owner, wanted)
                self.duck["written"] = wanted
                properties["Volume"] = wanted
            except BusError:
                self.volume_failed.add(owner)
                self.duck = None


def run():
    preferred = ""
    if len(sys.argv) == 2:
        try:
            preferred = base64.b64decode(sys.argv[1], validate=True).decode("utf-8")
        except (ValueError, UnicodeError):
            pass
    bridge = Bridge(Bus(), preferred)
    commands = queue.Queue(maxsize=32)
    eof = threading.Event()

    def read_commands():
        try:
            while True:
                line = sys.stdin.buffer.readline(513)
                if not line or len(line) > 512:
                    break
                try:
                    command = line.decode("ascii").strip()
                    commands.put_nowait((time.monotonic(), command))
                except (UnicodeError, queue.Full):
                    continue
        finally:
            eof.set()

    threading.Thread(target=read_commands, daemon=True, name="bloodhelper-mpris-input").start()
    last_sources = None
    last_error = ""
    try:
        while not eof.is_set():
            try:
                created, command = commands.get(timeout=.25)
                if time.monotonic() - created <= 2:
                    bridge.command(command)
            except queue.Empty:
                pass
            packet = bridge.observe()
            sources = bridge.sources()
            if sources != last_sources:
                print(json.dumps(sources, ensure_ascii=True, separators=(",", ":")), flush=True)
                last_sources = sources
            if bridge.error != last_error:
                print(json.dumps({"kind": "diagnostic", "message": bridge.error}, separators=(",", ":")), flush=True)
                last_error = bridge.error
            print(json.dumps(packet, ensure_ascii=True, separators=(",", ":")), flush=True)
    finally:
        bridge.restore_duck()


if __name__ == "__main__":
    try:
        run()
    except BrokenPipeError:
        pass
    except (BusError, OSError) as error:
        print(str(error)[:160], file=sys.stderr, flush=True)
        sys.exit(1)
