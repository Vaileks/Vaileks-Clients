/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_332
 *  net.minecraft.class_465
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1735;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={class_465.class})
public abstract class ReGlassContainerMixin {
    @Inject(method={"method_25420"}, at={@At(value="HEAD")})
    private void bloodhelper$containerFrame(class_332 g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReGlassGui.beginContainer();
    }

    @Inject(method={"method_2385"}, at={@At(value="HEAD")})
    private void bloodhelper$slot(class_332 g, class_1735 slot, int offsetX, int offsetY, CallbackInfo ci) {
        ReGlassGui.slot(g, slot);
    }

    @ModifyArg(method={"method_2388"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V"), index=4)
    private int bloodhelper$label(int color) {
        return ReGlassGui.labelColor(color);
    }
}

