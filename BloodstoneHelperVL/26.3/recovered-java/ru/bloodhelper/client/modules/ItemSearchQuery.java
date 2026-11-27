/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Locale;

public final class ItemSearchQuery {
    private ItemSearchQuery() {
    }

    public static String normalize(String text) {
        return text == null ? "" : text.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').strip();
    }

    public static boolean matches(String text, String query) {
        String haystack = ItemSearchQuery.normalize(text);
        for (String token : ItemSearchQuery.normalize(query).split("\\s+")) {
            if (haystack.contains(token)) continue;
            return false;
        }
        return true;
    }
}

