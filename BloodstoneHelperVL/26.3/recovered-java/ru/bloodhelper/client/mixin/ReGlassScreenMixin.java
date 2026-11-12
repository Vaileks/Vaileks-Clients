/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;
import ru.bloodhelper.client.gui.ReGlassResources;

@Mixin(value={Screen.class})
public abstract class ReGlassScreenMixin {
    @Inject(method={"extractRenderStateWithTooltipAndSubtitles"}, at={@At(value="HEAD")})
    private void bloodhelper$containerFrame(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassGui.beginContainer();
    }

    @Inject(method={"extractMenuBackgroundTexture"}, at={@At(value="HEAD")}, cancellable=true)
    private static void bloodhelper$background(GuiGraphicsExtractor g, Identifier id, int x, int y, float u, float v, int w, int h, CallbackInfo ci) {
        if (ReGlassGui.active() && ReGlassResources.vanillaTexture(id) && !ReGlassResources.customContainer() && ReGlassGui.panel(g, x, y, w, h, 0, -1071634636)) {
            ci.cancel();
        }
    }
}

