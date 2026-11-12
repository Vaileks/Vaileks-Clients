/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.tiers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.PlayerDisplaySettings;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierIcons;
import ru.bloodhelper.client.tiers.TierProfile;
import ru.bloodhelper.client.tiers.TierService;

public final class TierDisplay {
    private static final FontDescription FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"tiers"));

    private TierDisplay() {
    }

    public static Component decorate(String name, Component original, boolean tab) {
        String icon;
        PlayerDisplaySettings.OtherTiers s = BloodHelperClient.config().otherTiers;
        if (original == null || !s.enabled || !(tab ? s.tab : s.nametags) || StreamerMode.hideNametags()) {
            return original;
        }
        TierProfile.Badge badge = TierService.badge(name, s.gamemode);
        if (badge == null) {
            return original;
        }
        MutableComponent prefix = Component.empty();
        if (s.icons && !(icon = TierIcons.glyph(s.provider, badge.mode())).isEmpty()) {
            prefix.append((Component)Component.literal((String)icon).withStyle(style -> style.withFont(FONT).withColor(0xFFFFFF))).append(" ");
        }
        int color = switch (badge.tier().charAt(2)) {
            case '1' -> 16766298;
            case '2' -> 14015461;
            case '3' -> 14391652;
            case '4' -> 9231272;
            default -> 8894696;
        };
        prefix.append((Component)Component.literal((String)badge.tier()).withStyle(style -> style.withColor(color)));
        return TierDisplay.arrange(original, (Component)prefix, s.position);
    }

    static Component arrange(Component original, Component badge, String position) {
        MutableComponent separator = Component.literal((String)" \u00b7 ").withStyle(style -> style.withColor(0x888888));
        return "RIGHT".equals(position) ? Component.empty().append((Component)original.copy()).append((Component)separator).append((Component)badge.copy()) : Component.empty().append((Component)badge.copy()).append((Component)separator).append((Component)original.copy());
    }
}

