/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.cosmetic;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import ru.bloodhelper.client.cosmetic.CosmeticCache;
import ru.bloodhelper.client.cosmetic.CosmeticRoster;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

public final class CosmeticNames {
    private static final CosmeticCache CACHE = new CosmeticCache(System::nanoTime);

    private CosmeticNames() {
    }

    public static void acceptRoster(CosmeticRoster roster) {
        CACHE.acceptRoster(roster);
    }

    public static void acceptSelf(String publicUid, String nicknameColor, String minecraftUuid, String minecraftName, boolean clientDisabled) {
        CACHE.acceptSelf(publicUid, nicknameColor, minecraftUuid, minecraftName, clientDisabled);
    }

    public static void acceptSelf(String uid, String color, NicknamePalette palette, String uuid, String name, boolean disabled) {
        CACHE.acceptSelf(uid, color, palette, uuid, name, disabled);
    }

    public static Optional<NicknamePalette> palette(UUID uuid, String name, boolean privacyHidden) {
        return CACHE.palette(uuid, name, privacyHidden);
    }

    public static OptionalInt color(UUID uuid, String name, boolean privacyHidden) {
        return CACHE.color(uuid, name, privacyHidden);
    }

    public static int displayColor(UUID uuid, String name, int fallbackArgb, boolean privacyHidden) {
        OptionalInt color = CosmeticNames.color(uuid, name, privacyHidden);
        return color.isPresent() ? 0xFF000000 | color.getAsInt() : fallbackArgb;
    }

    public static String uidLabel(boolean privacyHidden) {
        return CACHE.uidLabel(privacyHidden);
    }

    public static void clear() {
        CACHE.clear();
    }
}

