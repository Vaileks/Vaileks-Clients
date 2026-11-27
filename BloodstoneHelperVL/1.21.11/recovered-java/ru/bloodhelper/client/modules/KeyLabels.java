/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Locale;

public final class KeyLabels {
    private KeyLabels() {
    }

    public static String label(String saved, String localizedSpace) {
        if (saved == null) {
            return "?";
        }
        String key = saved.toLowerCase(Locale.ROOT);
        if (key.equals("key.keyboard.space")) {
            return localizedSpace;
        }
        if (key.startsWith("key.keyboard.")) {
            key = key.substring(13);
        } else if (key.startsWith("key.mouse.")) {
            return switch (key.substring(10)) {
                case "left" -> "LMB";
                case "right" -> "RMB";
                case "middle" -> "MMB";
                default -> "M" + key.substring(10);
            };
        }
        return switch (key) {
            case "apostrophe" -> "'";
            case "comma" -> ",";
            case "minus" -> "-";
            case "period" -> ".";
            case "slash" -> "/";
            case "semicolon" -> ";";
            case "equal" -> "=";
            case "left.bracket" -> "[";
            case "backslash" -> "\\";
            case "right.bracket" -> "]";
            case "grave.accent" -> "`";
            case "left.shift" -> "L SHIFT";
            case "right.shift" -> "R SHIFT";
            case "left.control" -> "L CTRL";
            case "right.control" -> "R CTRL";
            case "left.alt" -> "L ALT";
            case "right.alt" -> "R ALT";
            default -> key.replace("key.", "").replace("keyboard.", "").replace('.', ' ').toUpperCase(Locale.ROOT);
        };
    }
}

