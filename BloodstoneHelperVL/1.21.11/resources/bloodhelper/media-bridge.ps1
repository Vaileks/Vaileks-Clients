# BLOODHELPER Windows GSMTC bridge, Windows PowerShell 5.1 / Windows 10 1809+.
# This script is bundled with the mod. It never downloads or executes external code.
# -ReadOnlySamples N is a diagnostic mode: emit N observations and accept no controls.
param([ValidateRange(0, 120)][int]$ReadOnlySamples = 0, [string]$PreferredSourceBase64 = '')
if ($PreferredSourceBase64.Length -gt 4096) { throw 'Invalid media source' }
$PreferredSource = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($PreferredSourceBase64))
if ($PreferredSource.Length -gt 512 -or $PreferredSource -match '[\x00-\x1f\x7f]') { throw 'Invalid media source' }
. (Join-Path $PSScriptRoot 'media-sources.ps1')

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
[Console]::InputEncoding = New-Object System.Text.UTF8Encoding($false)
$MaxArtworkBytes = 2 * 1024 * 1024

function Write-Packet($Packet) {
    [Console]::Out.WriteLine(($Packet | ConvertTo-Json -Compress -Depth 4))
    [Console]::Out.Flush()
}

function Write-Diagnostic([string]$Message) {
    $text = ($Message -replace '[\r\n\t]', ' ')
    if ($text.Length -gt 512) { $text = $text.Substring(0, 512) }
    [Console]::Error.WriteLine($text)
}

try {
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $ManagerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
    $PropertiesType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]
    $StreamType = [Windows.Storage.Streams.IRandomAccessStreamWithContentType, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $RandomAccessStreamType = [Windows.Storage.Streams.IRandomAccessStream, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $null = [Windows.Storage.Streams.DataReader, Windows.Storage.Streams, ContentType = WindowsRuntime]

    $script:AsTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
        $_.Name -eq 'AsTask' -and $_.IsGenericMethodDefinition -and
        $_.GetGenericArguments().Length -eq 1 -and $_.GetParameters().Length -eq 1 -and
        $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
    } | Select-Object -First 1
    if ($null -eq $script:AsTask) { throw 'WinRT task adapter is unavailable' }

    function Await-WinRT($Operation, [Type]$ResultType, [int]$TimeoutMs = 1000) {
        $task = $script:AsTask.MakeGenericMethod($ResultType).Invoke($null, @($Operation))
        if (-not $task.Wait($TimeoutMs)) {
            try { $Operation.Cancel() } catch {}
            throw 'Windows media operation timed out'
        }
        # Unary comma prevents PowerShell from enumerating projected WinRT return values.
        return ,$task.Result
    }

    # Console.In.ReadLineAsync can synchronously block in Windows PowerShell. A small .NET
    # background thread reads a bounded command queue, without touching PowerShell runspaces.
    Add-Type -TypeDefinition @'
using System;
using System.Collections.Concurrent;
using System.Text.RegularExpressions;
using System.Threading;
public static class BloodHelperMediaInput {
    private sealed class Command {
        internal string Text;
        internal long Created;
    }
    private static readonly ConcurrentQueue<Command> Pending = new ConcurrentQueue<Command>();
    private static readonly AutoResetEvent Signal = new AutoResetEvent(false);
    public static volatile bool Closed;
    public static bool HasPending { get { return !Pending.IsEmpty; } }
    public static void Wait(int milliseconds) {
        if (!Closed && Pending.IsEmpty) Signal.WaitOne(Math.Max(0, milliseconds));
    }
    public static void Start() {
        var reader = new Thread(() => {
            try {
                string line;
                while ((line = Console.ReadLine()) != null) {
                    if (line.Length <= 128 && Pending.Count < 16 && (line == "toggle" || line == "next" || line == "previous"
                        || Regex.IsMatch(line, @"\Aseek:[a-f0-9]{32}:[1-9][0-9]{0,18}:[0-9]{1,11}\z")
                        || Regex.IsMatch(line, @"\Avolume:[a-f0-9]{32}:[1-9][0-9]{0,18}:(?:[0-9]{1,3}|1000)\z")
                        || Regex.IsMatch(line, @"\Afavorite:[a-f0-9]{32}:[1-9][0-9]{0,18}\z")
                        || Regex.IsMatch(line, @"\Aduck:[1-9][0-9]{0,18}:[01]:(?:[0-9]{1,3}|1000)\z")))
                    {
                        Pending.Enqueue(new Command { Text = line, Created = System.Diagnostics.Stopwatch.GetTimestamp() });
                        Signal.Set();
                    }
                }
            } catch { }
            finally { Closed = true; Signal.Set(); }
        });
        reader.IsBackground = true;
        reader.Start();
    }
    public static string Take() {
        Command value;
        while (Pending.TryDequeue(out value)) {
            if (System.Diagnostics.Stopwatch.GetTimestamp() - value.Created < System.Diagnostics.Stopwatch.Frequency * 2)
                return value.Text;
        }
        return null;
    }
 }
// Yandex Music's desktop seekto handler uses `seekTime && setProgress(seekTime)`.
// Give only its exact zero target a one-millisecond epsilon, retaining the 00:00 display.
// A full millisecond survives Chromium's time conversions; every nonzero target is intact.
public static class BloodHelperMediaSeek {
    public static long TargetTicks(string source, long relativeMs, long start, long end) {
        decimal target = Math.Min((decimal)end, Math.Max((decimal)start,
            (decimal)start + (decimal)relativeMs * TimeSpan.TicksPerMillisecond));
        bool yandex = Regex.IsMatch(source ?? "", @"\A(?:yandexmusic(?:\.exe)?|yandex music(?:\.exe)?|\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430(?:\.exe)?|ru\.yandex\.desktop\.music|ru\.yandex\.music\.desktop|com\.squirrel\.yandexmusic\.yandexmusic)\z", RegexOptions.IgnoreCase | RegexOptions.CultureInvariant);
        if (target == 0 && start == 0 && end > 0 && yandex)
            target = Math.Min((decimal)end, TimeSpan.TicksPerMillisecond);
        return (long)target;
    }
}
// GSMTC playback status and timeline notifications can arrive separately. An unchanged
// timeline must not add the entire paused interval when playback resumes.
public sealed class BloodHelperMediaClock {
    private string track = "";
    private long timelineStamp;
    private double rawPosition;
    private double projected;
    private bool playing;
    private double rate = 1;
    private long observed;
    public double Observe(string identity, double raw, long updatedEpoch, bool nowPlaying,
                          double nowRate, double duration, long nowEpoch, long nowMonotonic) {
        if (track == identity && timelineStamp == updatedEpoch && rawPosition == raw) {
            double elapsed = Math.Max(0, Math.Min(1500,
                (nowMonotonic - observed) * 1000.0 / System.Diagnostics.Stopwatch.Frequency));
            if (playing) projected += elapsed * rate;
        } else {
            projected = raw;
            // Some desktop players replace Position before advancing LastUpdatedTime.
            // Such a changed raw position is fresh now; adding the old timestamp's age
            // would jump forward again immediately after a backwards seek.
            bool freshPositionWithOldStamp = track == identity && timelineStamp == updatedEpoch;
            if (nowPlaying && updatedEpoch > 946684800000L && !freshPositionWithOldStamp)
                projected += Math.Max(0, nowEpoch - updatedEpoch) * nowRate;
        }
        projected = Math.Max(0, projected);
        if (duration > 0) projected = Math.Min(duration, projected);
        track = identity;
        timelineStamp = updatedEpoch;
        rawPosition = raw;
        observed = nowMonotonic;
        playing = nowPlaying;
        rate = nowRate;
        return projected;
    }
}
// Pure envelope shared by the real native target and synthetic tests. A baseline belongs
// to one exact audio-session instance. External/manual changes surrender ownership for
// this combat cycle, including restoration, instead of fighting the user's volume slider.
public sealed class BloodHelperDuckEnvelope {
    private bool armed, manual, restoring;
    private float baseline, written, from, goal;
    private long transitionAt;
    public bool Armed { get { return armed; } }
    public bool Transitioning { get { return armed && Math.Abs(written - goal) > 0.001f; } }
    public void BeginCycle() { manual = false; }
    public void Cancel() { armed = false; manual = true; }
    public float? Update(float level, bool muted, bool requested, double factor, long nowMs) {
        if (float.IsNaN(level) || float.IsInfinity(level)) { Cancel(); return null; }
        if (armed && (muted || Math.Abs(level - written) > 0.004f)) { Cancel(); return null; }
        if (manual) return null;
        if (!armed) {
            if (!requested || muted || factor >= 1) return null;
            baseline = written = from = level;
            goal = (float)(baseline * Math.Max(0, Math.Min(1, factor)));
            armed = true; restoring = false; transitionAt = nowMs;
        }
        float target = requested ? (float)(baseline * Math.Max(0, Math.Min(1, factor))) : baseline;
        if (Math.Abs(target - goal) > 0.00001f) {
            from = level; goal = target; transitionAt = nowMs;
        }
        restoring = !requested;
        double progress = Math.Max(0, Math.Min(1, (nowMs - transitionAt) / 650.0));
        double eased = progress * progress * (3 - 2 * progress);
        float next = progress >= 1 ? goal : (float)(from + (goal - from) * eased);
        if (Math.Abs(next - level) < 0.0001f) {
            if (restoring && progress >= 1) armed = false;
            return null;
        }
        return next;
    }
    // Called only after the native write succeeds. A failed write never becomes our baseline.
    public void Applied(float value) {
        written = value;
        if (restoring && Math.Abs(value - baseline) < 0.0001f) armed = false;
    }
    public float? Restore(float level, bool muted) {
        bool safe = armed && !manual && !muted && Math.Abs(level - written) <= 0.004f;
        armed = false;
        return safe && Math.Abs(level - baseline) > 0.0001f ? (float?)baseline : null;
    }
}
// Core Audio is deliberately session-scoped. No endpoint/master-volume interface exists here.
public sealed class BloodHelperVolume : IDisposable {
    public bool NativeReadSucceeded { get; private set; }
    public bool DuckTransitioning { get; private set; }
    public sealed class Observation {
        public bool Available;
        public double Level;
        public string Token = "";
    }
    private sealed class Target {
        internal AudioControl2 Control;
        internal SimpleVolume Volume;
        internal uint Pid;
        internal string Instance;
        internal string AppIdentity;
        internal BloodHelperDuckEnvelope Duck = new BloodHelperDuckEnvelope();
    }
    private readonly System.Collections.Generic.List<Target> targets = new System.Collections.Generic.List<Target>();
    private string source = "", track = "", signature = "", token = "";
    private long refreshed;
    private long duckEpoch, duckPulse;
    private bool duckActive;
    private double duckFactor = 0.4;
    private static readonly Guid Context = new Guid("af6db6cd-ce7d-4e8d-a7de-03e3f0db7d62");
    private static readonly Guid ManagerId = new Guid("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F");

    private static long Milliseconds() {
        return (long)(System.Diagnostics.Stopwatch.GetTimestamp() * 1000.0 / System.Diagnostics.Stopwatch.Frequency);
    }
    // Only a validated Java module intent can arm this. Repeated heartbeat updates retain
    // the original baseline; an expired lease naturally fades back even if Java stalls.
    public void RequestDucking(long epoch, bool active, int remainingThousandths) {
        if (epoch <= 0 || remainingThousandths < 0 || remainingThousandths > 1000) return;
        if (epoch != duckEpoch) {
            foreach (Target target in targets) RestoreTarget(target, source);
        }
        if (epoch != duckEpoch || active && !duckActive) {
            foreach (Target target in targets) target.Duck.BeginCycle();
        }
        duckEpoch = epoch; duckActive = active; duckFactor = remainingThousandths / 1000.0;
        duckPulse = Milliseconds();
    }

    public Observation Observe(string app, string currentTrack) {
        try {
            long now = System.Diagnostics.Stopwatch.GetTimestamp();
            if (source != app || track != currentTrack || now - refreshed >= System.Diagnostics.Stopwatch.Frequency) {
                Refresh(app, currentTrack);
                refreshed = now;
            }
            if (targets.Count == 0) return new Observation();
            double sum = 0;
            DuckTransitioning = false;
            bool requested = duckActive && Milliseconds() - duckPulse <= 3500;
            foreach (Target target in targets) {
                float level; bool muted;
                if (target.Volume.GetMasterVolume(out level) != 0 || target.Volume.GetMute(out muted) != 0)
                    throw new InvalidOperationException();
                float? desired = target.Duck.Update(level, muted, requested, duckFactor, Milliseconds());
                if (desired.HasValue) {
                    Guid context = Context;
                    if (Valid(target, app) && target.Volume.SetMasterVolume(desired.Value, ref context) == 0) {
                        target.Duck.Applied(desired.Value); level = desired.Value;
                    } else target.Duck.Cancel();
                }
                DuckTransitioning |= target.Duck.Transitioning;
                sum += muted ? 0 : level;
            }
            return new Observation { Available = true, Level = sum / targets.Count, Token = token };
        } catch { Reset(); return new Observation(); }
    }

    // Called only after an explicit validated stdin volume command and a fresh GSMTC source check.
    public bool Apply(string app, string currentTrack, string expectedToken, int thousandths) {
        if (thousandths < 0 || thousandths > 1000 || expectedToken.Length != 32) return false;
        try {
            // Session routing or process changes must invalidate even a command already in the pipe.
            Refresh(app, currentTrack);
            refreshed = System.Diagnostics.Stopwatch.GetTimestamp();
            if (targets.Count == 0 || token != expectedToken) return false;
            Guid context = Context;
            bool success = true;
            foreach (Target target in targets) {
                uint pid;
                if (target.Control.GetProcessId(out pid) != 0 || pid != target.Pid
                    || Identity(app, pid) != target.AppIdentity) return false;
            }
            foreach (Target target in targets) {
                // This is ISimpleAudioVolume for the selected app, NOT IAudioEndpointVolume.
                bool applied = target.Volume.SetMasterVolume(thousandths / 1000.0f, ref context) == 0;
                if (applied) target.Duck.Cancel(); // Explicit user intent wins for this combat.
                success &= applied;
                bool muted;
                if (thousandths > 0 && target.Volume.GetMute(out muted) == 0 && muted)
                    success &= target.Volume.SetMute(false, ref context) == 0;
            }
            return success;
        } catch { Reset(); return false; }
    }

    private void Refresh(string app, string currentTrack) {
        var found = new System.Collections.Generic.List<Target>();
        DeviceEnumerator enumerator = null;
        DeviceCollection devices = null;
        try {
            enumerator = (DeviceEnumerator)new DeviceEnumeratorCom();
            if (enumerator.EnumAudioEndpoints(0, 1, out devices) != 0) throw new InvalidOperationException();
            uint count;
            if (devices.GetCount(out count) != 0 || count > 64) throw new InvalidOperationException();
            for (uint i = 0; i < count; i++) {
                Device device = null; object managerObject = null; SessionEnumerator sessions = null;
                try {
                    if (devices.Item(i, out device) != 0) continue;
                    Guid iid = ManagerId;
                    if (device.Activate(ref iid, 23, IntPtr.Zero, out managerObject) != 0) continue;
                    var manager = (SessionManager)managerObject;
                    if (manager.GetSessionEnumerator(out sessions) != 0) continue;
                    int total;
                    if (sessions.GetCount(out total) != 0 || total > 256) continue;
                    for (int j = 0; j < total; j++) {
                        AudioControl control = null;
                        try {
                            if (sessions.GetSession(j, out control) != 0) continue;
                            var control2 = (AudioControl2)control;
                            int state; uint pid; string instance;
                            // Reject system sounds, expired sessions and sessions spanning unrelated processes.
                            if (control2.GetState(out state) != 0 || state == 2 || control2.IsSystemSoundsSession() == 0
                                || control2.GetProcessId(out pid) != 0 || pid == 0
                                || control2.GetSessionInstanceIdentifier(out instance) != 0) continue;
                            string identity = Identity(app, pid);
                            if (identity == null) continue;
                            var volume = (SimpleVolume)control;
                            float level; bool muted;
                            if (volume.GetMasterVolume(out level) != 0 || volume.GetMute(out muted) != 0) continue;
                            found.Add(new Target { Control = control2, Volume = volume, Pid = pid,
                                Instance = instance, AppIdentity = identity });
                            control = null; // Ownership moves to found; interfaces share one RCW.
                        } catch { }
                        finally { Release(control); }
                    }
                } finally { Release(sessions); Release(managerObject); Release(device); }
            }
            // A desktop basename can refer to different installations. Never guess which one owns GSMTC.
            var identities = new System.Collections.Generic.HashSet<string>(StringComparer.OrdinalIgnoreCase);
            var parts = new System.Collections.Generic.List<string>();
            foreach (Target target in found) {
                identities.Add(target.AppIdentity);
                parts.Add(target.Pid + ":" + target.Instance + ":" + target.AppIdentity);
            }
            if (identities.Count > 1) { foreach (Target target in found) Release(target.Control); found.Clear(); }
            parts.Sort(StringComparer.Ordinal);
            string nextSignature = String.Join("\n", parts.ToArray());
            if (app != source || currentTrack != track || signature != nextSignature || found.Count == 0)
                token = found.Count == 0 ? "" : Guid.NewGuid().ToString("N");
            foreach (Target previous in targets) {
                Target same = null;
                if (app == source) {
                    foreach (Target candidate in found) {
                        if (candidate.Pid == previous.Pid && candidate.Instance == previous.Instance
                            && candidate.AppIdentity == previous.AppIdentity) { same = candidate; break; }
                    }
                }
                // A new song can share one audio session: carry the original baseline through
                // polling/track changes. A replaced/other app receives only its own baseline.
                if (same != null) same.Duck = previous.Duck;
                else RestoreTarget(previous, source);
                Release(previous.Control);
            }
            targets.Clear(); targets.AddRange(found); found.Clear();
            source = app; track = currentTrack; signature = nextSignature;
            NativeReadSucceeded = true;
        } catch {
            foreach (Target target in found) Release(target.Control);
            Reset();
        } finally { Release(devices); Release(enumerator); }
    }

    public static bool MatchesDesktop(string app, string filename) {
        if (String.IsNullOrEmpty(app) || String.IsNullOrEmpty(filename) || app.IndexOfAny(new [] {'\\', '/', '!'}) >= 0)
            return false;
        string stem = System.IO.Path.GetFileNameWithoutExtension(filename);
        // Minecraft/Java and this bridge are never selected, even if a malformed AUMID asks for them.
        if (stem.Equals("java", StringComparison.OrdinalIgnoreCase) || stem.Equals("javaw", StringComparison.OrdinalIgnoreCase)
            || stem.Equals("minecraft", StringComparison.OrdinalIgnoreCase) || stem.Equals("powershell", StringComparison.OrdinalIgnoreCase)
            || stem.Equals("pwsh", StringComparison.OrdinalIgnoreCase)) return false;
        // Yandex publishes an explicit desktop AUMID instead of its executable basename.
        // These exact executable identities were verified against the desktop app's own session.
        if (app.Equals("ru.yandex.desktop.music", StringComparison.Ordinal))
            return filename.Equals("\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430.exe", StringComparison.OrdinalIgnoreCase)
                || filename.Equals("Yandex Music.exe", StringComparison.OrdinalIgnoreCase);
        return app.Equals(filename, StringComparison.OrdinalIgnoreCase) || app.Equals(stem, StringComparison.OrdinalIgnoreCase);
    }

    private static string Identity(string app, uint pid) {
        if (String.IsNullOrEmpty(app)) return null;
        IntPtr handle = OpenProcess(0x1000, false, pid);
        if (handle == IntPtr.Zero) return null;
        try {
            uint appLength = 0;
            int appResult = GetApplicationUserModelId(handle, ref appLength, null);
            if (appResult == 122 && appLength > 0 && appLength <= 2048) {
                var appId = new System.Text.StringBuilder((int)appLength);
                if (GetApplicationUserModelId(handle, ref appLength, appId) == 0
                    && app.Equals(appId.ToString(), StringComparison.OrdinalIgnoreCase)) {
                    if (app.IndexOf("Minecraft", StringComparison.OrdinalIgnoreCase) >= 0) return null;
                    return "aumid:" + appId.ToString().ToLowerInvariant();
                }
                return null; // A packaged process must match the full app identity, not just package or filename.
            }
            if (appResult != 15703 && appResult != 15700) return null; // No application/package identity only.
            uint pathLength = 32768;
            var path = new System.Text.StringBuilder((int)pathLength);
            if (!QueryFullProcessImageName(handle, 0, path, ref pathLength)) return null;
            string fullPath = path.ToString();
            return MatchesDesktop(app, System.IO.Path.GetFileName(fullPath)) ? "exe:" + fullPath.ToLowerInvariant() : null;
        } finally { CloseHandle(handle); }
    }

    private void Reset() {
        foreach (Target target in targets) { RestoreTarget(target, source); Release(target.Control); }
        targets.Clear(); source = ""; track = ""; signature = ""; token = ""; refreshed = 0;
        NativeReadSucceeded = false; DuckTransitioning = false;
    }
    private static bool Valid(Target target, string app) {
        uint pid; string instance;
        return target.Control.GetProcessId(out pid) == 0 && pid == target.Pid
            && target.Control.GetSessionInstanceIdentifier(out instance) == 0 && instance == target.Instance
            && Identity(app, pid) == target.AppIdentity;
    }
    private static void RestoreTarget(Target target, string app) {
        try {
            float level; bool muted;
            if (!Valid(target, app) || target.Volume.GetMasterVolume(out level) != 0
                || target.Volume.GetMute(out muted) != 0) { target.Duck.Cancel(); return; }
            float? restore = target.Duck.Restore(level, muted);
            if (restore.HasValue) {
                Guid context = Context;
                target.Volume.SetMasterVolume(restore.Value, ref context);
            }
        } catch { target.Duck.Cancel(); }
    }
    public void Dispose() { Reset(); }
    private static void Release(object value) {
        if (value != null && System.Runtime.InteropServices.Marshal.IsComObject(value))
            System.Runtime.InteropServices.Marshal.ReleaseComObject(value);
    }

    [System.Runtime.InteropServices.DllImport("kernel32.dll", SetLastError = true)]
    private static extern IntPtr OpenProcess(uint access, bool inherit, uint processId);
    [System.Runtime.InteropServices.DllImport("kernel32.dll")]
    private static extern bool CloseHandle(IntPtr handle);
    [System.Runtime.InteropServices.DllImport("kernel32.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode)]
    private static extern int GetApplicationUserModelId(IntPtr process, ref uint length, System.Text.StringBuilder appId);
    [System.Runtime.InteropServices.DllImport("kernel32.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, SetLastError = true)]
    private static extern bool QueryFullProcessImageName(IntPtr process, uint flags, System.Text.StringBuilder path, ref uint length);

    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")]
    private class DeviceEnumeratorCom { }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface DeviceEnumerator {
        [System.Runtime.InteropServices.PreserveSig] int EnumAudioEndpoints(int flow, uint mask, out DeviceCollection collection);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("0BD7A1BE-7A1A-44DB-8397-CC5392387B5E"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface DeviceCollection {
        [System.Runtime.InteropServices.PreserveSig] int GetCount(out uint count);
        [System.Runtime.InteropServices.PreserveSig] int Item(uint index, out Device device);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("D666063F-1587-4E43-81F1-B948E807363F"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface Device {
        [System.Runtime.InteropServices.PreserveSig] int Activate(ref Guid id, uint context, IntPtr parameters, [System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.IUnknown)] out object result);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface SessionManager {
        [System.Runtime.InteropServices.PreserveSig] int GetAudioSessionControl(IntPtr guid, uint flags, out IntPtr control);
        [System.Runtime.InteropServices.PreserveSig] int GetSimpleAudioVolume(IntPtr guid, uint flags, out IntPtr volume);
        [System.Runtime.InteropServices.PreserveSig] int GetSessionEnumerator(out SessionEnumerator enumerator);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("E2F5BB11-0570-40CA-ACDD-3AA01277DEE8"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface SessionEnumerator {
        [System.Runtime.InteropServices.PreserveSig] int GetCount(out int count);
        [System.Runtime.InteropServices.PreserveSig] int GetSession(int index, out AudioControl control);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("F4B1A599-7266-4319-A8CA-E70ACB11E8CD"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface AudioControl { }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("BFB7FF88-7239-4FC9-8FA2-07C950BE9C6D"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface AudioControl2 {
        [System.Runtime.InteropServices.PreserveSig] int GetState(out int state);
        [System.Runtime.InteropServices.PreserveSig] int GetDisplayName([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] out string name);
        [System.Runtime.InteropServices.PreserveSig] int SetDisplayName([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] string name, ref Guid context);
        [System.Runtime.InteropServices.PreserveSig] int GetIconPath([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] out string path);
        [System.Runtime.InteropServices.PreserveSig] int SetIconPath([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] string path, ref Guid context);
        [System.Runtime.InteropServices.PreserveSig] int GetGroupingParam(out Guid group);
        [System.Runtime.InteropServices.PreserveSig] int SetGroupingParam(ref Guid group, ref Guid context);
        [System.Runtime.InteropServices.PreserveSig] int RegisterAudioSessionNotification(IntPtr callback);
        [System.Runtime.InteropServices.PreserveSig] int UnregisterAudioSessionNotification(IntPtr callback);
        [System.Runtime.InteropServices.PreserveSig] int GetSessionIdentifier([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] out string id);
        [System.Runtime.InteropServices.PreserveSig] int GetSessionInstanceIdentifier([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.LPWStr)] out string id);
        [System.Runtime.InteropServices.PreserveSig] int GetProcessId(out uint processId);
        [System.Runtime.InteropServices.PreserveSig] int IsSystemSoundsSession();
        [System.Runtime.InteropServices.PreserveSig] int SetDuckingPreference(bool optOut);
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("87CE5498-68D6-44E5-9215-6DA47EF883D8"), System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface SimpleVolume {
        [System.Runtime.InteropServices.PreserveSig] int SetMasterVolume(float level, ref Guid context);
        [System.Runtime.InteropServices.PreserveSig] int GetMasterVolume(out float level);
        [System.Runtime.InteropServices.PreserveSig] int SetMute([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.Bool)] bool mute, ref Guid context);
        [System.Runtime.InteropServices.PreserveSig] int GetMute([System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.Bool)] out bool mute);
    }
}

'@

    $manager = Await-WinRT ($ManagerType::RequestAsync()) $ManagerType 3000
    if ($ReadOnlySamples -eq 0) { [BloodHelperMediaInput]::Start() }
    $clock = New-Object BloodHelperMediaClock
    $volumeControl = New-Object BloodHelperVolume
    $volumeRequestId = [long]0
    $volumeAccepted = $false
    $lastKey = $null
    $lastArtwork = $null
    $lastArtworkRead = [DateTime]::MinValue
    $observations = 0
    $failures = 0
    $trackToken = ''
    $properties = $null
    $key = ''
    $source = ''
    $lastMetadataRead = [DateTime]::MinValue
    $seekRequestId = [long]0
    $seekAccepted = $false

    # UI Automation can hang inside a third-party provider. It runs only in one disposable
    # child process, never in this GSMTC/audio loop or an accumulating in-process thread.
    $script:favoriteWorkerPath = Join-Path $PSScriptRoot 'media-favorites.ps1'
    $script:favoriteProcess = $null
    $script:favoriteOutput = $null
    $script:favoriteJob = $null
    $script:favoritePending = $null # At most one explicit ADD waiting to start.
    $script:favoriteTrack = ''
    $script:favoriteSessionIdentity = ''
    $script:favoriteParentStarted = [long]0
    try { $script:favoriteParentStarted = [Diagnostics.Process]::GetCurrentProcess().StartTime.ToUniversalTime().Ticks } catch {}
    $script:favoriteNextProbe = [long]0
    $script:favoriteState = @{ favoriteProvider = ''; canFavorite = $false; favoriteSaved = $false;
        favoriteRequestId = [long]0; favoriteAccepted = $false; favoriteStatus = 'unavailable' }

    function Favorite-Now { return [long]([Diagnostics.Stopwatch]::GetTimestamp() * 1000.0 / [Diagnostics.Stopwatch]::Frequency) }
    function Test-FavoriteSource([string]$app) {
        # Pure admission only; the worker separately validates executable/window and the
        # exact PWA AppUserModelID + SoundCloud document URL before exposing any button.
        return $app -imatch '\A(?:spotify(?:\.exe)?|SpotifyAB\.SpotifyMusic_[a-z0-9]+!Spotify|yandexmusic(?:\.exe)?|yandex music(?:\.exe)?|\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430(?:\.exe)?|ru\.yandex\.desktop\.music|ru\.yandex\.music\.desktop|com\.squirrel\.yandexmusic\.yandexmusic|soundcloud(?:\.exe)?|SoundCloud\.SoundCloud_[a-z0-9]+!App|(?:Chrome|MSEdge)\._crx_[a-p]{32})\z'
    }
    function Stop-FavoriteWorker {
        if ($null -ne $script:favoriteProcess) {
            try { if (-not $script:favoriteProcess.HasExited) { $script:favoriteProcess.Kill() } } catch {}
            try { $script:favoriteProcess.Dispose() } catch {}
        }
        $script:favoriteProcess = $null; $script:favoriteOutput = $null; $script:favoriteJob = $null
    }
    function Reset-FavoriteTrack([string]$token) {
        if ($token -ceq $script:favoriteTrack) { return }
        Stop-FavoriteWorker
        $script:favoritePending = $null; $script:favoriteTrack = $token; $script:favoriteNextProbe = [long]0
        $script:favoriteState = @{ favoriteProvider = ''; canFavorite = $false; favoriteSaved = $false;
            favoriteRequestId = [long]0; favoriteAccepted = $false; favoriteStatus = 'unavailable' }
    }
    function Update-FavoriteSession($mediaSession) {
        $identity = ''
        $unknown = [IntPtr]::Zero
        try {
            if ($null -ne $mediaSession) {
                $unknown = [Runtime.InteropServices.Marshal]::GetIUnknownForObject($mediaSession)
                $identity = $unknown.ToInt64().ToString('x')
            }
        } catch { }
        finally { if ($unknown -ne [IntPtr]::Zero) { $null = [Runtime.InteropServices.Marshal]::Release($unknown) } }
        if ($script:favoriteSessionIdentity -cne $identity) { Reset-FavoriteTrack '' }
        $script:favoriteSessionIdentity = $identity
    }
    function Request-Favorite([string]$token, [long]$id, [string]$app, $mediaProperties) {
        if ($ReadOnlySamples -gt 0 -or $id -le 0 -or $token -cne $trackToken -or $refreshTrack) { return }
        Reset-FavoriteTrack $trackToken
        if ($null -ne $script:favoritePending -or ($null -ne $script:favoriteJob -and $script:favoriteJob.mode -ceq 'add')) { return }
        if ($id -le $script:favoriteState.favoriteRequestId) { return }
        Stop-FavoriteWorker # A read-only capability probe never delays an explicit request.
        $script:favoritePending = @{ mode = 'add'; trackToken = $token; requestId = $id; source = $app;
            title = [string]$mediaProperties.Title; artist = [string]$mediaProperties.Artist;
            album = [string]$mediaProperties.AlbumTitle; deadline = (Favorite-Now) + 8000 }
        $script:favoriteState.canFavorite = $false; $script:favoriteState.favoriteStatus = 'pending'
    }
    function Start-FavoriteWorker($job) {
        try {
            if (-not (Test-FavoriteSource $job.source)) {
                $script:favoriteState.favoriteProvider = ''; $script:favoriteState.canFavorite = $false
                $script:favoriteState.favoriteSaved = $false; $script:favoriteState.favoriteStatus = 'unsupported'
                if ($job.mode -ceq 'add') {
                    $script:favoriteState.favoriteRequestId = [long]$job.requestId; $script:favoriteState.favoriteAccepted = $false
                }
                $script:favoriteNextProbe = (Favorite-Now) + 30000
                return
            }
            if (-not [IO.File]::Exists($script:favoriteWorkerPath) -or $job.deadline -le (Favorite-Now)) { throw 'Favorite worker unavailable' }
            $job.preferredSource = $PreferredSource
            $job.parentProcessId = [int]$PID; $job.parentStartedTicks = $script:favoriteParentStarted
            $info = New-Object Diagnostics.ProcessStartInfo
            $info.FileName = Join-Path $PSHOME 'powershell.exe'
            $info.Arguments = '-NoLogo -NoProfile -NonInteractive -Mta -ExecutionPolicy Bypass -File "' + $script:favoriteWorkerPath + '"'
            $info.UseShellExecute = $false; $info.CreateNoWindow = $true
            $info.RedirectStandardInput = $true; $info.RedirectStandardOutput = $true
            # No raw UIA exception or account metadata is emitted on the worker's stderr.
            $process = New-Object Diagnostics.Process
            $process.StartInfo = $info
            $null = $process.Start()
            $script:favoriteProcess = $process; $script:favoriteJob = $job
            $script:favoriteOutput = $process.StandardOutput.ReadToEndAsync()
            $process.StandardInput.WriteLine(($job | ConvertTo-Json -Compress -Depth 3))
            $process.StandardInput.Close()
        } catch {
            Stop-FavoriteWorker
            $script:favoriteState.canFavorite = $false; $script:favoriteState.favoriteSaved = $false; $script:favoriteState.favoriteStatus = 'unavailable'
            if ($job.mode -ceq 'add') {
                $script:favoriteState.favoriteRequestId = [long]$job.requestId; $script:favoriteState.favoriteAccepted = $false
            }
            $script:favoriteNextProbe = (Favorite-Now) + 5000
        }
    }
    function Observe-Favorite([string]$token, [string]$app, $mediaProperties) {
        Reset-FavoriteTrack $token
        $now = Favorite-Now
        if ($null -ne $script:favoriteProcess) {
            $job = $script:favoriteJob
            if ($script:favoriteProcess.HasExited -and $script:favoriteOutput.IsCompleted) {
                try {
                    $raw = [string]$script:favoriteOutput.Result
                    if ($raw.Length -gt 4096 -or $now -ge $job.deadline -or $job.trackToken -cne $token) { throw 'Expired favorite response' }
                    $answer = $raw | ConvertFrom-Json
                    if ($answer.status -cnotmatch '\A(?:ready|saved|unavailable|unsupported|not_confirmed)\z' -or
                            $answer.provider -cnotmatch '\A(?:spotify|yandex|soundcloud)?\z') { throw 'Invalid favorite response' }
                    if ($answer.status -cmatch '\A(?:ready|saved)\z' -and [string]::IsNullOrEmpty([string]$answer.provider)) { throw 'Missing favorite provider' }
                    $script:favoriteState.favoriteProvider = [string]$answer.provider
                    $script:favoriteState.favoriteSaved = ($answer.saved -eq $true -and $answer.status -ceq 'saved')
                    $script:favoriteState.canFavorite = ($answer.canFavorite -eq $true -and $answer.status -cmatch '\A(?:ready|saved)\z')
                    $script:favoriteState.favoriteStatus = [string]$answer.status
                    if ($job.mode -ceq 'add') {
                        $script:favoriteState.favoriteRequestId = [long]$job.requestId
                        $script:favoriteState.favoriteAccepted = ($answer.requestId -eq $job.requestId -and $answer.accepted -eq $true -and $script:favoriteState.favoriteSaved)
                    }
                } catch {
                    $script:favoriteState.canFavorite = $false; $script:favoriteState.favoriteSaved = $false; $script:favoriteState.favoriteStatus = 'not_confirmed'
                    if ($job.mode -ceq 'add') {
                        $script:favoriteState.favoriteRequestId = [long]$job.requestId; $script:favoriteState.favoriteAccepted = $false
                    }
                }
                Stop-FavoriteWorker
                # Recheck a transient UI rebuild soon, without repeating the ADD operation.
                $script:favoriteNextProbe = $now + $(if ($script:favoriteState.favoriteStatus -ceq 'not_confirmed') { 3000 } else { 12000 })
            } elseif ($now -ge $job.deadline) {
                Stop-FavoriteWorker
                $script:favoriteState.canFavorite = $false; $script:favoriteState.favoriteSaved = $false; $script:favoriteState.favoriteStatus = 'not_confirmed'
                if ($job.mode -ceq 'add') {
                    $script:favoriteState.favoriteRequestId = [long]$job.requestId; $script:favoriteState.favoriteAccepted = $false
                }
                $script:favoriteNextProbe = $now + 5000
            }
        }
        if ($null -eq $script:favoriteProcess) {
            if ($null -ne $script:favoritePending) {
                $job = $script:favoritePending; $script:favoritePending = $null
                Start-FavoriteWorker $job
            } elseif ($now -ge $script:favoriteNextProbe -and $token -cmatch '\A[a-f0-9]{32}\z' -and
                    -not [string]::IsNullOrWhiteSpace([string]$mediaProperties.Title) -and
                    -not [string]::IsNullOrWhiteSpace([string]$mediaProperties.Artist)) {
                $job = @{ mode = 'observe'; trackToken = $token; requestId = [long]0; source = $app;
                    title = [string]$mediaProperties.Title; artist = [string]$mediaProperties.Artist;
                    album = [string]$mediaProperties.AlbumTitle; deadline = $now + 8000 }
                Start-FavoriteWorker $job
            }
        }
        return $script:favoriteState
    }

    function Get-MediaKey($Session, $Properties) {
        return ([string]$Session.SourceAppUserModelId) + "`n" + $Properties.Title + "`n" + $Properties.Artist + "`n" + $Properties.AlbumTitle
    }

    function Get-Artwork($Thumbnail) {
        if ($null -eq $Thumbnail) { return $null }
        $stream = $null
        $reader = $null
        $inputStream = $null
        try {
            $stream = Await-WinRT ($Thumbnail.OpenReadAsync()) $StreamType 120
            # WinRT interface results appear as System.__ComObject in PowerShell 5.1.
            # Invoke the declared interface explicitly; dynamic .Size would silently be null.
            $length = [long]$RandomAccessStreamType.GetProperty('Size').GetValue($stream, $null)
            if ($length -le 0 -or $length -gt $MaxArtworkBytes) { return $null }
            $inputStream = $RandomAccessStreamType.GetMethod('GetInputStreamAt').Invoke($stream, @([uint64]0))
            $reader = [Windows.Storage.Streams.DataReader]::new($inputStream)
            $loaded = Await-WinRT ($reader.LoadAsync([uint32]$length)) ([uint32]) 120
            if ([long]$loaded -ne $length) { return $null }
            $bytes = New-Object byte[] $length
            $reader.ReadBytes($bytes)
            return [Convert]::ToBase64String($bytes)
        } catch {
            # Some players publish a stale or inaccessible thumbnail. Text/progress still work.
            return $null
        } finally {
            if ($null -ne $reader) { try { $reader.Dispose() } catch {} }
            if ($null -ne $inputStream) { try { ([IDisposable]$inputStream).Dispose() } catch {} }
            if ($null -ne $stream) { try { ([IDisposable]$stream).Dispose() } catch {} }
        }
    }

    $lastSourcesPacket = ''
    while ($ReadOnlySamples -gt 0 -or -not [BloodHelperMediaInput]::Closed) {
        $cycle = [Diagnostics.Stopwatch]::StartNew()
        try {
            $session = (Select-MediaSession $manager $PreferredSource)
            $sourcesPacket = Get-MediaSourcesPacket $manager $session
            $sourcesJson = $sourcesPacket | ConvertTo-Json -Compress -Depth 4
            if ($sourcesJson -cne $lastSourcesPacket) { Write-Packet $sourcesPacket; $lastSourcesPacket = $sourcesJson }
            Update-FavoriteSession $session
            $refreshTrack = $false
            $metadataChanged = $false
            # Initialize a session identity once. Subsequent button presses use cached metadata,
            # so toggle/next/previous do not wait for artwork or a properties poll before dispatch.
            if ($null -ne $session -and ($null -eq $properties -or $source -cne [string]$session.SourceAppUserModelId)) {
                $properties = Await-WinRT ($session.TryGetMediaPropertiesAsync()) $PropertiesType 500
                $source = [string]$session.SourceAppUserModelId
                $key = Get-MediaKey $session $properties
                $lastMetadataRead = [DateTime]::UtcNow
                $trackToken = [Guid]::NewGuid().ToString('N')
                $seekRequestId = [long]0
                $seekAccepted = $false
                $metadataChanged = $true
            }
            if ($ReadOnlySamples -eq 0) {
                # Consume at most 16 explicit requests. The stdin reader wakes this loop immediately.
                for ($i = 0; $i -lt 16; $i++) {
                    $command = [BloodHelperMediaInput]::Take()
                    if ($null -eq $command) { break }
                    if ($command -match '\Aduck:([1-9][0-9]{0,18}):([01]):([0-9]{1,4})\z') {
                        $duckEpoch = [long]0
                        if ([long]::TryParse($Matches[1], [ref]$duckEpoch)) {
                            $volumeControl.RequestDucking($duckEpoch, $Matches[2] -eq '1', [int]$Matches[3])
                        }
                        continue
                    }
                    if ($null -eq $session) { continue }
                    try {
                        $controlPlayback = $session.GetPlaybackInfo()
                        $controls = $controlPlayback.Controls
                        switch ($command) {
                            'toggle' {
                                if ($controls.IsPlayPauseToggleEnabled) {
                                    $null = Await-WinRT ($session.TryTogglePlayPauseAsync()) ([bool])
                                } elseif ($controlPlayback.PlaybackStatus.ToString() -eq 'Playing' -and $controls.IsPauseEnabled) {
                                    $null = Await-WinRT ($session.TryPauseAsync()) ([bool])
                                } elseif ($controls.IsPlayEnabled) {
                                    $null = Await-WinRT ($session.TryPlayAsync()) ([bool])
                                }
                            }
                            'next' {
                                Reset-FavoriteTrack ''
                                $refreshTrack = $true
                                if ($controls.IsNextEnabled) {
                                    $null = Await-WinRT ($session.TrySkipNextAsync()) ([bool])
                                }
                            }
                            'previous' {
                                Reset-FavoriteTrack ''
                                $refreshTrack = $true
                                if ($controls.IsPreviousEnabled) {
                                    $null = Await-WinRT ($session.TrySkipPreviousAsync()) ([bool])
                                }
                            }
                            default {
                                if ($command -match '\Avolume:([a-f0-9]{32}):([1-9][0-9]{0,18}):([0-9]{1,4})\z') {
                                    $requestedVolumeToken = $Matches[1]
                                    $volumeRequestId = [long]$Matches[2]
                                    $volumeLevel = [int]$Matches[3]
                                    $volumeAccepted = $false
                                    $volumeSession = (Select-MediaSession $manager $PreferredSource)
                                    if (-not $refreshTrack -and $null -ne $volumeSession -and $volumeLevel -le 1000 -and
                                            [string]$volumeSession.SourceAppUserModelId -ceq $source) {
                                        # Track-bound gestures must not reach a new song in the same app.
                                        # Read metadata only; this path never opens or decodes the thumbnail.
                                        $volumeProperties = Await-WinRT ($volumeSession.TryGetMediaPropertiesAsync()) $PropertiesType 500
                                        if ((Get-MediaKey $volumeSession $volumeProperties) -ceq $key) {
                                            $volumeAccepted = $volumeControl.Apply($source, $trackToken, $requestedVolumeToken, $volumeLevel)
                                        } else {
                                            $refreshTrack = $true
                                        }
                                    }
                                } elseif ($command -match '\Afavorite:([a-f0-9]{32}):([1-9][0-9]{0,18})\z') {
                                    $favoriteId = [long]0
                                    if ([long]::TryParse($Matches[2], [ref]$favoriteId)) {
                                        Request-Favorite $Matches[1] $favoriteId $source $properties
                                    }
                                } elseif ($command -match '\Aseek:([a-f0-9]{32}):([1-9][0-9]{0,18}):([0-9]{1,11})\z') {
                                    $requestedTrack = $Matches[1]
                                    $seekRequestId = [long]$Matches[2]
                                    $relativeMs = [long]$Matches[3]
                                    $seekAccepted = $false
                                    if (-not $refreshTrack -and $requestedTrack -ceq $trackToken -and $controls.IsPlaybackPositionEnabled -and
                                            $relativeMs -le 31536000000) {
                                        # A preceding Next command or system session switch can invalidate the
                                        # pre-poll identity. Recheck immediately before this explicit seek.
                                        $currentSession = (Select-MediaSession $manager $PreferredSource)
                                        if ($null -ne $currentSession) {
                                            $currentProperties = Await-WinRT ($currentSession.TryGetMediaPropertiesAsync()) $PropertiesType 500
                                            if ((Get-MediaKey $currentSession $currentProperties) -ceq $key -and
                                                    $currentSession.GetPlaybackInfo().Controls.IsPlaybackPositionEnabled) {
                                                $seekTimeline = $currentSession.GetTimelineProperties()
                                                $startTicks = [long]$seekTimeline.StartTime.Ticks
                                                $endTicks = [long]$seekTimeline.EndTime.Ticks
                                                if ($endTicks -gt $startTicks) {
                                                    # WinRT expects 100 ns ticks, relative to its timeline start;
                                                    # decimal arithmetic prevents overflow before the final clamp.
                                                    $targetTicks = [BloodHelperMediaSeek]::TargetTicks(
                                                        [string]$currentSession.SourceAppUserModelId, $relativeMs, $startTicks, $endTicks)
                                                    $seekAccepted = [bool](Await-WinRT ($currentSession.TryChangePlaybackPositionAsync([long]$targetTicks)) ([bool]))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch {
                        Write-Diagnostic 'The selected media application did not accept a control request.'
                    }
                }
            }

            if ($refreshTrack -or ([DateTime]::UtcNow - $lastMetadataRead).TotalMilliseconds -ge 250) {
                # Commands such as Next may already have changed the track. Do not publish
                # the old title/cover alongside the new timeline for the rest of this poll.
                $session = (Select-MediaSession $manager $PreferredSource)
                if ($null -ne $session) {
                    $properties = Await-WinRT ($session.TryGetMediaPropertiesAsync()) $PropertiesType 500
                    $source = [string]$session.SourceAppUserModelId
                    $lastMetadataRead = [DateTime]::UtcNow
                    $refreshedKey = Get-MediaKey $session $properties
                    if ($refreshedKey -cne $key) {
                        $trackToken = [Guid]::NewGuid().ToString('N')
                        $seekRequestId = [long]0
                        $seekAccepted = $false
                    }
                    $key = $refreshedKey
                    $metadataChanged = $key -cne $lastKey
                }
            }

            if ($null -eq $session) {
                $lastKey = $null
                $properties = $null
                $lastArtwork = $null
                $trackToken = ''
                $seekRequestId = [long]0
                $seekAccepted = $false
                $volumeControl.Dispose()
                Reset-FavoriteTrack ''
                Write-Packet @{ kind = 'snapshot'; available = $false }
            } else {
                $title = [string]$properties.Title
                $artist = [string]$properties.Artist
                if ($title.Length -gt 1024) { $title = $title.Substring(0, 1024) }
                if ($artist.Length -gt 1024) { $artist = $artist.Substring(0, 1024) }
                $packet = @{
                    kind = 'snapshot'
                    available = -not ([string]::IsNullOrWhiteSpace($title) -and [string]::IsNullOrWhiteSpace($artist))
                    title = $title
                    artist = $artist
                    trackToken = $trackToken
                    seekRequestId = $seekRequestId
                    seekAccepted = $seekAccepted
                }
                if ($metadataChanged) { $lastArtworkRead = [DateTime]::MinValue; $lastArtwork = $null }
                if (($metadataChanged -or ([DateTime]::UtcNow - $lastArtworkRead).TotalMilliseconds -ge $(if ($null -eq $lastArtwork) { 250 } else { 2000 })) -and
                        ($ReadOnlySamples -gt 0 -or -not [BloodHelperMediaInput]::HasPending)) {
                    $artwork = Get-Artwork $properties.Thumbnail
                    if ($metadataChanged -or $artwork -cne $lastArtwork) {
                        $packet.artworkBase64 = $artwork
                        $lastArtwork = $artwork
                    }
                    $lastArtworkRead = [DateTime]::UtcNow
                }
                # Position is current as of LastUpdatedTime, not as of this read. Sample after
                # thumbnail IO and attach the observation time so Java can compensate pipe latency.
                $playback = $session.GetPlaybackInfo()
                $timeline = $session.GetTimelineProperties()
                $observedAt = [DateTimeOffset]::UtcNow
                $playing = $playback.PlaybackStatus.ToString() -eq 'Playing'
                $rate = 1.0
                if ($null -ne $playback.PlaybackRate -and -not [double]::IsNaN([double]$playback.PlaybackRate) -and
                        -not [double]::IsInfinity([double]$playback.PlaybackRate)) {
                    $rate = [Math]::Min(16.0, [Math]::Max(0, [double]$playback.PlaybackRate))
                }
                $start = $timeline.StartTime.TotalMilliseconds
                $duration = [Math]::Max(0, $timeline.EndTime.TotalMilliseconds - $start)
                $position = [Math]::Max(0, $timeline.Position.TotalMilliseconds - $start)
                $updatedEpoch = [long]0
                if ($timeline.LastUpdatedTime.Year -gt 2000) {
                    $updatedEpoch = $timeline.LastUpdatedTime.ToUnixTimeMilliseconds()
                }
                $position = $clock.Observe($trackToken, $position, $updatedEpoch, $playing, $rate, $duration,
                    $observedAt.ToUnixTimeMilliseconds(), [Diagnostics.Stopwatch]::GetTimestamp())
                $packet.playing = $playing
                $packet.playbackRate = $rate
                $packet.positionMs = [long]$position
                $packet.durationMs = [long]$duration
                $packet.observedAtEpochMs = $observedAt.ToUnixTimeMilliseconds()
                $packet.canSeek = [bool]($playback.Controls.IsPlaybackPositionEnabled -and $duration -gt 0)
                $volumeObservation = $volumeControl.Observe($source, $trackToken)
                $packet.canControlVolume = $volumeObservation.Available
                $packet.volume = $volumeObservation.Level
                $packet.volumeTargetToken = $volumeObservation.Token
                $packet.volumeRequestId = $volumeRequestId
                $packet.volumeAccepted = $volumeAccepted
                $favoriteObservation = Observe-Favorite $trackToken $source $properties
                foreach ($field in $favoriteObservation.Keys) {
                    if ($field -ceq 'favoriteSaved') { $packet.favorite = $favoriteObservation[$field] }
                    else { $packet[$field] = $favoriteObservation[$field] }
                }
                $lastKey = $key
                Write-Packet $packet
            }
            $failures = 0
        } catch {
            # No exception text includes the media property values or is copied into stdout.
            Write-Diagnostic ('Windows media read failed: ' + $_.Exception.GetType().Name)
            $lastKey = $null
            $properties = $null
            $lastArtwork = $null
            $volumeControl.Dispose()
            Reset-FavoriteTrack ''
            Write-Packet @{ kind = 'snapshot'; available = $false }
            $failures++
            if ($failures -ge 10) { throw 'Windows media session repeatedly failed' }
        }
        $observations++
        if ($ReadOnlySamples -gt 0 -and $observations -ge $ReadOnlySamples) { break }
        $pollMs = 100
        if ($volumeControl.DuckTransitioning) { $pollMs = 40 }
        $delay = [Math]::Max(1, $pollMs - $cycle.ElapsedMilliseconds)
        if ($ReadOnlySamples -gt 0) { Start-Sleep -Milliseconds ([int]$delay) }
        else { [BloodHelperMediaInput]::Wait([int]$delay) }
    }
} catch {
    Write-Diagnostic ('BLOODHELPER media bridge unavailable: ' + $_.Exception.GetType().Name)
    exit 1
} finally {
    if ($null -ne $script:favoriteProcess) { Stop-FavoriteWorker }
    if ($null -ne $volumeControl) { $volumeControl.Dispose() }
}
