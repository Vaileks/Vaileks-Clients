/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.notifications;

public record DesktopNotification(String title, String body, boolean sound) {
    public DesktopNotification {
        title = DesktopNotification.plain(title, 63);
        body = DesktopNotification.plain(body, 255);
    }

    static String plain(String text, int limit) {
        int code;
        if (text == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(Math.min(limit, text.length()));
        for (int i = 0; i < text.length(); i += Character.charCount(code)) {
            code = text.codePointAt(i);
            if (Character.isISOControl(code) || Character.getType(code) == 16 || code >= 55296 && code <= 57343) continue;
            if (result.length() + Character.charCount(code) > limit) break;
            result.appendCodePoint(code);
        }
        return result.toString();
    }
}

