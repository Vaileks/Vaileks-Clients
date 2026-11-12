/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class StaffCheckSettings {
    public boolean enabled;
    public boolean showTimer;
    public int timerSeconds;
    public String callCommand = "";
    public String postCallMessage = "";
    public String refusalDuration = "";
    public String refusalReason = "";
    public String confessionDuration = "";
    public String confessionReason = "";

    public void sanitize() {
        this.timerSeconds = Math.clamp((long)this.timerSeconds, 0, 86400);
        this.callCommand = StaffCheckSettings.clean(this.callCommand, 256);
        this.postCallMessage = StaffCheckSettings.clean(this.postCallMessage, 256);
        this.refusalDuration = StaffCheckSettings.clean(this.refusalDuration, 64);
        this.refusalReason = StaffCheckSettings.clean(this.refusalReason, 160);
        this.confessionDuration = StaffCheckSettings.clean(this.confessionDuration, 64);
        this.confessionReason = StaffCheckSettings.clean(this.confessionReason, 160);
    }

    private static String clean(String s, int n) {
        if (s == null) {
            return "";
        }
        return s.substring(0, Math.min(n, s.length())).replace('\n', ' ').replace('\r', ' ').replace("\u0000", "").strip();
    }
}

