/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import ru.bloodhelper.client.hud.FfaKillReceipt;
import ru.bloodhelper.client.hud.KillMessageTemplate;

final class SessionState {
    private String identity = "";
    private String localName = "";
    private String mode = "FFA";
    private int kills;
    private int rewardedKills;
    private int deaths;
    private int streak;
    private long elapsed;
    private long lastTick = -1L;
    private boolean connected;
    private boolean alive;
    private boolean active;
    private boolean killFeed;

    SessionState() {
    }

    void tick(String newIdentity, String name, String newMode, boolean aliveNow, boolean enabled, boolean paused, long now) {
        boolean nowActive;
        if (!newIdentity.equals(this.identity) || !newMode.equals(this.mode)) {
            this.clear();
            this.active = false;
            this.identity = newIdentity;
            this.mode = newMode;
        }
        boolean bl = nowActive = enabled && !paused;
        if (!this.connected) {
            this.connected = true;
            this.alive = aliveNow;
            this.lastTick = now;
            this.active = false;
        }
        if (this.active && nowActive && this.lastTick >= 0L && now >= this.lastTick) {
            this.elapsed += now - this.lastTick;
        }
        if (this.active && nowActive && this.alive && !aliveNow) {
            ++this.deaths;
            this.streak = 0;
        }
        this.localName = name;
        this.alive = aliveNow;
        this.active = nowActive;
        this.lastTick = now;
    }

    void disconnect(boolean clear) {
        this.active = false;
        this.connected = false;
        this.lastTick = -1L;
        if (clear) {
            this.clear();
            this.identity = "";
            this.localName = "";
        }
    }

    void reset() {
        this.clear();
    }

    private void clear() {
        this.streak = 0;
        this.deaths = 0;
        this.rewardedKills = 0;
        this.kills = 0;
        this.elapsed = 0L;
        this.killFeed = false;
        this.lastTick = -1L;
    }

    boolean confirm(KillMessageTemplate.Kill kill, boolean serverGameMessage, boolean overlay, long now) {
        if ("FFA".equals(this.mode) || !this.connected || !this.active || !serverGameMessage || overlay || kill == null || !KillMessageTemplate.isName(kill.killer()) || !KillMessageTemplate.isName(kill.victim()) || kill.killer().equals(kill.victim())) {
            return false;
        }
        this.killFeed = true;
        if (!kill.killer().equals(this.localName)) {
            return false;
        }
        ++this.kills;
        if (this.alive) {
            ++this.streak;
        }
        return true;
    }

    boolean confirmFfa(FfaKillReceipt receipt, boolean serverGameMessage, boolean overlay, long now) {
        if (!("FFA".equals(this.mode) && this.connected && this.active && serverGameMessage && !overlay && receipt != null)) {
            return false;
        }
        ++this.kills;
        if (receipt.rewarded()) {
            ++this.rewardedKills;
        }
        this.killFeed = true;
        return true;
    }

    Snapshot snapshot() {
        return new Snapshot(this.mode, this.kills, this.rewardedKills, this.deaths, this.streak, this.elapsed / 1000000000L, this.killFeed);
    }

    record Snapshot(String mode, int kills, int rewardedKills, int deaths, int streak, long playSeconds, boolean killFeedAvailable) {
    }
}

