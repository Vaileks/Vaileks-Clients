/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Hud.class})
abstract class HudMixin {
    HudMixin() {
    }

    @Inject(method={"extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvp$scoreboard(GuiGraphicsExtractor c, DeltaTracker t, CallbackInfo ci) {
        if (Core.hideVanilla("scoreboard")) {
            ci.cancel();
        }
    }

    @Inject(method={"extractBossOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvp$bossbar(GuiGraphicsExtractor c, DeltaTracker t, CallbackInfo ci) {
        if (Core.hideVanilla("bossbar")) {
            ci.cancel();
        }
    }

    @Inject(method={"extractEffects"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvp$effects(GuiGraphicsExtractor c, DeltaTracker t, CallbackInfo ci) {
        if (Core.hideVanilla("effects")) {
            ci.cancel();
        }
    }

    @Inject(method={"extractSelectedItemName"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvp$item(GuiGraphicsExtractor c, CallbackInfo ci) {
        if (Core.hideVanilla("item")) {
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void pvp$render(GuiGraphicsExtractor c, DeltaTracker t, CallbackInfo ci) {
        Core.render(c);
    }

    @Inject(method={"extractSlot"}, at={@At(value="TAIL")})
    private void pvp$slot(GuiGraphicsExtractor c, int x, int y, DeltaTracker t, Player p, ItemStack s, int seed, CallbackInfo ci) {
        Core.hotbarSlot(c, s, x, y);
    }
}

