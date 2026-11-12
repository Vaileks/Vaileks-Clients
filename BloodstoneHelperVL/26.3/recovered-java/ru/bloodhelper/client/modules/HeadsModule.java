/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription$PlayerSprite
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$ShowEntity
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.contents.objects.ObjectInfo
 *  net.minecraft.network.chat.contents.objects.PlayerSprite
 *  net.minecraft.world.item.component.ResolvableProfile
 */
package ru.bloodhelper.client.modules;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.component.ResolvableProfile;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.HeadsSettings;
import ru.bloodhelper.client.modules.StreamerMode;

public final class HeadsModule {
    private static final boolean EXTERNAL_CHAT_HEADS = FabricLoader.getInstance().isModLoaded("chat_heads") || FabricLoader.getInstance().isModLoaded("chatheads");

    private HeadsModule() {
    }

    public static boolean externalChatHeads() {
        return EXTERNAL_CHAT_HEADS;
    }

    public static boolean containsHead(Component message) {
        return message != null && message.visit((style, text) -> style.getFont() instanceof FontDescription.PlayerSprite ? Optional.of(Boolean.TRUE) : Optional.empty(), Style.EMPTY).orElse(false) != false;
    }

    public static Component withoutOwnHead(Component original) {
        List parts = original.getSiblings();
        return parts.size() == 3 && "bloodhelper:heads".equals(((Component)parts.getFirst()).getStyle().getInsertion()) ? (Component)parts.get(2) : original;
    }

    public static Component decorate(Component original) {
        return HeadsModule.decorate(original, original);
    }

    public static Component decorate(Component original, Component identitySource) {
        Component previousHead = identitySource.getSiblings().size() == 3 && "bloodhelper:heads".equals(((Component)identitySource.getSiblings().getFirst()).getStyle().getInsertion()) ? (Component)identitySource.getSiblings().getFirst() : null;
        original = HeadsModule.withoutOwnHead(original);
        identitySource = HeadsModule.withoutOwnHead(identitySource);
        HeadsSettings cfg = BloodHelperClient.config().heads;
        if (!cfg.enabled || !cfg.chat || EXTERNAL_CHAT_HEADS || StreamerMode.hideChat() || StreamerMode.hideNickname() || HeadsModule.containsHead(original)) {
            return original;
        }
        if (previousHead != null) {
            return Component.empty().append(previousHead).append(" ").append(original);
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return original;
        }
        PlayerInfo[] sender = new PlayerInfo[]{null};
        int[] offset = new int[]{0};
        identitySource.visit((style, text) -> {
            if (offset[0] > 160) {
                return Optional.of(Boolean.TRUE);
            }
            offset[0] = offset[0] + text.length();
            HoverEvent patt0$temp = style.getHoverEvent();
            if (patt0$temp instanceof HoverEvent.ShowEntity) {
                HoverEvent.ShowEntity entity = (HoverEvent.ShowEntity)patt0$temp;
                sender[0] = connection.getPlayerInfo(entity.entity().uuid);
            }
            return sender[0] == null ? Optional.empty() : Optional.of(Boolean.TRUE);
        }, Style.EMPTY);
        if (sender[0] == null) {
            String prefix = identitySource.getString(160);
            for (PlayerInfo info : connection.getOnlinePlayers()) {
                String name = info.getProfile().name();
                if (!prefix.matches("^\\s*(?:<|\\[)?" + Pattern.quote(name) + "(?:>|\\])?\\s*[:\u00bb>].*") && !prefix.startsWith("<" + name + "> ") && !prefix.startsWith("[" + name + "] ")) continue;
                sender[0] = info;
                break;
            }
        }
        if (sender[0] == null) {
            return original;
        }
        MutableComponent head = Component.object((ObjectInfo)new PlayerSprite(ResolvableProfile.createResolved((GameProfile)sender[0].getProfile()), true)).withStyle(style -> style.withInsertion("bloodhelper:heads"));
        return Component.empty().append((Component)head).append(" ").append(original);
    }

    public static boolean tabHeads(boolean alreadyVisible) {
        if (alreadyVisible) {
            return true;
        }
        HeadsSettings cfg = BloodHelperClient.config().heads;
        if (!cfg.enabled || !cfg.tab || StreamerMode.hideNickname() || StreamerMode.hideNametags()) {
            return false;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (!HeadsModule.containsHead(info.getTabListDisplayName())) continue;
            return false;
        }
        return true;
    }
}

