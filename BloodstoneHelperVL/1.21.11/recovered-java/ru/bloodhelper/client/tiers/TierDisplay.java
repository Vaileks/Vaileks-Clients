/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_5250
 */
package ru.bloodhelper.client.tiers;

import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_5250;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.PlayerDisplaySettings;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierIcons;
import ru.bloodhelper.client.tiers.TierProfile;
import ru.bloodhelper.client.tiers.TierService;

public final class TierDisplay {
    private static final class_11719 FONT = new class_11719.class_11721(class_2960.method_60655((String)"bloodhelper", (String)"tiers"));

    private TierDisplay() {
    }

    public static class_2561 decorate(String name, class_2561 original, boolean tab) {
        String icon;
        PlayerDisplaySettings.OtherTiers s = BloodHelperClient.config().otherTiers;
        if (original == null || !s.enabled || !(tab ? s.tab : s.nametags) || StreamerMode.hideNametags()) {
            return original;
        }
        TierProfile.Badge badge = TierService.badge(name, s.gamemode);
        if (badge == null) {
            return original;
        }
        class_5250 prefix = class_2561.method_43473();
        if (s.icons && !(icon = TierIcons.glyph(s.provider, badge.mode())).isEmpty()) {
            prefix.method_10852((class_2561)class_2561.method_43470((String)icon).method_27694(style -> style.method_27704(FONT).method_36139(0xFFFFFF))).method_27693(" ");
        }
        int color = switch (badge.tier().charAt(2)) {
            case '1' -> 16766298;
            case '2' -> 14015461;
            case '3' -> 14391652;
            case '4' -> 9231272;
            default -> 8894696;
        };
        prefix.method_10852((class_2561)class_2561.method_43470((String)badge.tier()).method_27694(style -> style.method_36139(color)));
        return TierDisplay.arrange(original, (class_2561)prefix, s.position);
    }

    static class_2561 arrange(class_2561 original, class_2561 badge, String position) {
        class_5250 separator = class_2561.method_43470((String)" \u00b7 ").method_27694(style -> style.method_36139(0x888888));
        return "RIGHT".equals(position) ? class_2561.method_43473().method_10852((class_2561)original.method_27661()).method_10852((class_2561)separator).method_10852((class_2561)badge.method_27661()) : class_2561.method_43473().method_10852((class_2561)badge.method_27661()).method_10852((class_2561)separator).method_10852((class_2561)original.method_27661());
    }
}

