/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.List;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={ServerSelectionList.OnlineServerEntry.class})
public abstract class StreamerServerListMixin {
    @ModifyExpressionValue(method={"extractContent"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/client/multiplayer/ServerData;name:Ljava/lang/String;")})
    private String bloodhelper$maskName(String original) {
        return StreamerMode.hideServerAddress() ? HudStyle.tr("\u0421\u043a\u0440\u044b\u0442\u044b\u0439 \u0441\u0435\u0440\u0432\u0435\u0440", "Hidden server") : original;
    }

    @ModifyExpressionValue(method={"extractContent"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/client/multiplayer/ServerData;motd:Lnet/minecraft/network/chat/Component;")})
    private Component bloodhelper$maskDescription(Component original) {
        return StreamerMode.hideServerAddress() ? Component.literal((String)"Streamer Mode") : original;
    }

    @ModifyExpressionValue(method={"extractContent"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList$OnlineServerEntry;onlinePlayersTooltip:Ljava/util/List;")})
    private List<Component> bloodhelper$maskOnlinePlayers(List<Component> original) {
        return StreamerMode.hideNickname() ? List.of() : original;
    }
}

