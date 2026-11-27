/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.scores.Objective
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.modules.HudItemControlsModule;

@Mixin(value={Hud.class})
abstract class HudItemControlsGuiMixin {
    @Shadow
    private Component title;
    @Shadow
    private Component subtitle;
    @Shadow
    private Component overlayMessageString;

    HudItemControlsGuiMixin() {
    }

    @Inject(method={"extractItemHotbar"}, at={@At(value="TAIL")})
    private void bloodhelper$hotbar(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        HudItemControlsModule.renderHotbar(g);
    }

    @Inject(method={"extractPlayerHealth"}, at={@At(value="TAIL")})
    private void bloodhelper$health(GuiGraphicsExtractor g, CallbackInfo ci) {
        HudItemControlsModule.renderHealth(g);
    }

    @Inject(method={"extractHearts"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$replaceHearts(GuiGraphicsExtractor g, Player player, int x, int y, int spacing, int offset, float maxHealth, int health, int displayHealth, int absorption, boolean blink, CallbackInfo ci) {
        if (HudItemControlsModule.replacesHearts()) {
            ci.cancel();
        }
    }

    @Inject(method={"displayScoreboardSidebar"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$scoreboard(GuiGraphicsExtractor g, Objective objective, CallbackInfo ci) {
        if (HudItemControlsModule.settings().scoreboard.enabled) {
            HudItemControlsModule.renderScoreboard(g, objective);
            ci.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"extractTitle"})
    private void bloodhelper$title(GuiGraphicsExtractor g, DeltaTracker delta, Operation<Void> original) {
        HudItemControlsSettings.Placement s = HudItemControlsModule.settings().titles;
        if (!s.enabled) {
            original.call(new Object[]{g, delta});
            return;
        }
        g.pose().pushMatrix();
        try {
            Font font = Minecraft.getInstance().font;
            int width = Math.max(this.title == null ? 1 : font.width((FormattedText)this.title) * 4, this.subtitle == null ? 1 : font.width((FormattedText)this.subtitle) * 2);
            HudItemControlsModule.transform(g, s, g.guiWidth() / 2, g.guiHeight() / 2, (float)(-width) / 2.0f, -40.0f, width, this.subtitle == null ? 36.0f : 68.0f);
            original.call(new Object[]{g, delta});
        }
        finally {
            g.pose().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"extractOverlayMessage"})
    private void bloodhelper$actionbar(GuiGraphicsExtractor g, DeltaTracker delta, Operation<Void> original) {
        HudItemControlsSettings.Placement s = HudItemControlsModule.settings().actionbar;
        if (!s.enabled) {
            original.call(new Object[]{g, delta});
            return;
        }
        g.pose().pushMatrix();
        try {
            int width = this.overlayMessageString == null ? 1 : Minecraft.getInstance().font.width((FormattedText)this.overlayMessageString);
            HudItemControlsModule.transform(g, s, g.guiWidth() / 2, g.guiHeight() - 68, (float)(-width) / 2.0f, -4.0f, width, 9.0f);
            original.call(new Object[]{g, delta});
        }
        finally {
            g.pose().popMatrix();
        }
    }
}

