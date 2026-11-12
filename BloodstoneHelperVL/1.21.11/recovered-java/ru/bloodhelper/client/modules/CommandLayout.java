/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Set;

public final class CommandLayout {
    private static final String RU = "\u0451\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0445\u044a\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u044d\u044f\u0447\u0441\u043c\u0438\u0442\u044c\u0431\u044e";
    private static final String EN = "`qwertyuiop[]asdfghjkl;'zxcvbnm,.";

    private CommandLayout() {
    }

    public static String correct(String message, Set<String> knownCommands) {
        int end;
        if (message == null || message.length() < 2) {
            return message;
        }
        char prefix = message.charAt(0);
        if (prefix != '/' && prefix != '.') {
            return message;
        }
        for (end = 1; end < message.length() && !Character.isWhitespace(message.charAt(end)); ++end) {
        }
        String command = message.substring(1, end);
        if (knownCommands.contains(command)) {
            return message;
        }
        StringBuilder converted = new StringBuilder(command.length());
        boolean changed = false;
        for (int i = 0; i < command.length(); ++i) {
            char c = command.charAt(i);
            int index = RU.indexOf(Character.toLowerCase(c));
            if (index >= 0) {
                char mapped = EN.charAt(index);
                converted.append(Character.isUpperCase(c) ? Character.toUpperCase(mapped) : mapped);
                changed = true;
                continue;
            }
            converted.append(c);
        }
        String result = converted.toString();
        if (!changed || !result.matches("[A-Za-z0-9_:-]+")) {
            return message;
        }
        if (prefix == '.' && !knownCommands.contains(result)) {
            return message;
        }
        return "/" + result + message.substring(end);
    }
}

