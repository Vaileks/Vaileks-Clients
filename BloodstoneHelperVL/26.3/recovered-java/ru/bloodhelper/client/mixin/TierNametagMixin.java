/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import java.util.Optional;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.circles.BloodstoneCircles;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierDisplay;

@Mixin(value={EntityRenderer.class})
public class TierNametagMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void bloodhelper$tiers(Entity entity, EntityRenderState state, float partial, CallbackInfo ci) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (state.nameTag != null) {
                String name = player.getGameProfile().name();
                state.nameTag = TierDisplay.decorate(name, state.nameTag, false);
                Optional<NicknamePalette> palette = CosmeticNames.palette(player.getUUID(), name, StreamerMode.hideNickname() || StreamerMode.hideNametags());
                state.nameTag = BloodstoneCircles.rewrite(state.nameTag, false);
                if (palette.isPresent()) {
                    state.nameTag = CosmeticNameText.color(state.nameTag, name, palette.get(), System.currentTimeMillis());
                }
            }
        }
    }
}

