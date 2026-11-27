/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11661$class_11672
 *  net.minecraft.class_2561
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11661;
import net.minecraft.class_2561;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.cosmetic.CosmeticNameplates;
import ru.bloodhelper.client.modules.BetterNameVisibilityModule;

@Mixin(value={class_11661.class_11672.class})
public abstract class BetterNameStyleMixin {
    @Shadow
    @Final
    private Matrix4f comp_4514;
    @Shadow
    @Final
    private class_2561 comp_4517;

    @Inject(method={"comp_4519"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$color(CallbackInfoReturnable<Integer> cir) {
        if (BetterNameVisibilityModule.active()) {
            cir.setReturnValue((Object)BetterNameVisibilityModule.settings().foreground());
        }
    }

    @Inject(method={"comp_4520"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$background(CallbackInfoReturnable<Integer> cir) {
        if (BetterNameVisibilityModule.active()) {
            cir.setReturnValue((Object)BetterNameVisibilityModule.settings().background());
        }
    }

    @Inject(method={"comp_4518"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$light(CallbackInfoReturnable<Integer> cir) {
        if (BetterNameVisibilityModule.active() && BetterNameVisibilityModule.settings().fullbright) {
            cir.setReturnValue((Object)0xF000F0);
        }
    }

    @Inject(method={"comp_4514"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$scale(CallbackInfoReturnable<Matrix4f> cir) {
        if (BetterNameVisibilityModule.active() && BetterNameVisibilityModule.settings().distantScale != 0.0) {
            cir.setReturnValue((Object)BetterNameVisibilityModule.scalePose((Matrix4fc)this.comp_4514, BetterNameVisibilityModule.settings().distantScale));
        }
    }

    @Inject(method={"comp_4517"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$text(CallbackInfoReturnable<class_2561> cir) {
        if (BetterNameVisibilityModule.active() && BetterNameVisibilityModule.settings().disableTeamColors) {
            cir.setReturnValue((Object)CosmeticNameplates.recolor(BetterNameVisibilityModule.withoutTeamColors(this.comp_4517)));
        }
    }
}

