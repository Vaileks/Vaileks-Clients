/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Locale;
import java.util.Set;

public final class LoginGuard {
    private static final Set<String> COMMANDS = Set.of("l", "login", "reg", "register", "changepassword", "changepass", "cp");

    private LoginGuard() {
    }

    public static int secretStart(String text) {
        int split;
        if (text == null || !text.startsWith("/")) {
            return -1;
        }
        for (split = 1; split < text.length() && !Character.isWhitespace(text.charAt(split)); ++split) {
        }
        if (split == text.length()) {
            return -1;
        }
        String command = text.substring(1, split).toLowerCase(Locale.ROOT);
        int colon = command.lastIndexOf(58);
        if (colon >= 0) {
            command = command.substring(colon + 1);
        }
        if (!COMMANDS.contains(command)) {
            return -1;
        }
        return split + 1;
    }
}

