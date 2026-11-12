/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.gui.components.PlayerTabOverlay
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.CosmeticTabCache;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.HeadsModule;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.policy.ClientControlPolicy;
import ru.bloodhelper.client.tiers.TierDisplay;

@Mixin(value={PlayerTabOverlay.class})
public class TierTabMixin {
    @Unique
    private final CosmeticTabCache bloodhelper$painted = new CosmeticTabCache();

    @ModifyExpressionValue(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;onlineMode()Z")})
    private boolean bloodhelper$heads(boolean alreadyVisible) {
        return HeadsModule.tabHeads(alreadyVisible);
    }

    @Inject(method={"getNameForDisplay"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$tiers(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        GameProfile profile;
        Optional<NicknamePalette> color;
        Component display = (Component)cir.getReturnValue();
        if (ClientControlPolicy.enabled() && (color = CosmeticNames.palette((profile = info.getProfile()).id(), profile.name(), StreamerMode.hideNickname() || StreamerMode.hideNametags())).isPresent()) {
            display = this.bloodhelper$painted.color(info, display, profile.name(), color.get(), System.nanoTime() / 1000000L);
        }
        cir.setReturnValue((Object)TierDisplay.decorate(info.getProfile().name(), display, true));
    }
}

