/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class StaffChatSettings {
    public boolean enabled;
    public boolean highlight;
    public String reportKeywords = "";
    public String keywords = "";
    public int color = -15755;
    public int retainedMessages = 250;

    public void sanitize() {
        this.reportKeywords = StaffChatSettings.clean(this.reportKeywords);
        this.keywords = StaffChatSettings.clean(this.keywords);
        this.retainedMessages = Math.clamp((long)this.retainedMessages, 25, 500);
        this.color |= 0xFF000000;
    }

    private static String clean(String s) {
        return s == null ? "" : s.substring(0, Math.min(4096, s.length())).replace("\u0000", "");
    }
}

