/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public enum ModuleCategory {
    STAFF("\u0421\u0442\u0430\u0444\u0444", "Staff", "shield"),
    COMBAT("\u0411\u043e\u0439", "Combat", "swords"),
    VISUALS("\u0412\u0438\u0437\u0443\u0430\u043b", "Visuals", "palette"),
    UTILITIES("\u0423\u0442\u0438\u043b\u0438\u0442\u044b", "Utilities", "modules"),
    CHAT("\u0427\u0430\u0442", "Chat", "chat"),
    MUSIC("\u041c\u0443\u0437\u044b\u043a\u0430", "Music", "music");

    private final String russian;
    private final String english;
    private final String icon;

    private ModuleCategory(String russian, String english, String icon) {
        this.russian = russian;
        this.english = english;
        this.icon = icon;
    }

    public String title(boolean ru) {
        return ru ? this.russian : this.english;
    }

    public String icon() {
        return this.icon;
    }

    public static ModuleCategory forModule(String id) {
        return switch (id) {
            case "sanctions", "staffRulebook", "staffChat", "staffChecks", "staffJournal", "staffShift", "playerReminders", "playerReturns", "observationHistory", "staffStatus", "spectatorTimer", "staffStats", "chatViolations", "nicknameCopy" -> STAFF;
            case "sizeTimer", "combatHud", "pvp", "cooldowns", "armor", "itemCounter", "effects", "opponentCooldowns", "reachDisplay", "comboCounter", "saturation", "otherTiers", "cooldownReady", "deathRecap", "dropGuard", "abilityGuide" -> COMBAT;
            case "hotbarMarkers", "hotbarKeys", "totemAnimation", "scoreboardControl", "healthDisplay", "titlesControl", "actionbarControl", "timeChanger", "weatherChanger", "fullBright", "ambience", "crosshair", "skins", "hitboxes", "freeLook", "aspectRatio", "motionBlur", "betterNameVisibility", "legendaryVisuals", "watermark", "resourcePack", "zoom", "legendarySize" -> VISUALS;
            case "heads", "chatCalculator", "commandLayout", "chatTools", "chatMentions", "chatDrafts", "coordinateLinks", "quickCommands" -> CHAT;
            case "music", "musicDucking", "legendarySounds" -> MUSIC;
            default -> UTILITIES;
        };
    }
}

