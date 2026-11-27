/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.class_1657
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.modules.HudItemControlsModule;

@Mixin(value={class_329.class})
abstract class HudItemControlsGuiMixin {
    @Shadow
    private class_2561 field_2016;
    @Shadow
    private class_2561 field_2039;
    @Shadow
    private class_2561 field_2018;

    HudItemControlsGuiMixin() {
    }

    @Inject(method={"method_1759"}, at={@At(value="TAIL")})
    private void bloodhelper$hotbar(class_332 g, class_9779 delta, CallbackInfo ci) {
        HudItemControlsModule.renderHotbar(g);
    }

    @Inject(method={"method_1760"}, at={@At(value="TAIL")})
    private void bloodhelper$health(class_332 g, CallbackInfo ci) {
        HudItemControlsModule.renderHealth(g);
    }

    @Inject(method={"method_37298"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$replaceHearts(class_332 g, class_1657 player, int x, int y, int spacing, int offset, float maxHealth, int health, int displayHealth, int absorption, boolean blink, CallbackInfo ci) {
        if (HudItemControlsModule.replacesHearts()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1757"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$scoreboard(class_332 g, class_266 objective, CallbackInfo ci) {
        if (HudItemControlsModule.settings().scoreboard.enabled) {
            HudItemControlsModule.renderScoreboard(g, objective);
            ci.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"method_55801"})
    private void bloodhelper$title(class_332 g, class_9779 delta, Operation<Void> original) {
        HudItemControlsSettings.Placement s = HudItemControlsModule.settings().titles;
        if (!s.enabled) {
            original.call(new Object[]{g, delta});
            return;
        }
        g.method_51448().pushMatrix();
        try {
            class_327 font = class_310.method_1551().field_1772;
            int width = Math.max(this.field_2016 == null ? 1 : font.method_27525((class_5348)this.field_2016) * 4, this.field_2039 == null ? 1 : font.method_27525((class_5348)this.field_2039) * 2);
            HudItemControlsModule.transform(g, s, g.method_51421() / 2, g.method_51443() / 2, (float)(-width) / 2.0f, -40.0f, width, this.field_2039 == null ? 36.0f : 68.0f);
            original.call(new Object[]{g, delta});
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"method_55800"})
    private void bloodhelper$actionbar(class_332 g, class_9779 delta, Operation<Void> original) {
        HudItemControlsSettings.Placement s = HudItemControlsModule.settings().actionbar;
        if (!s.enabled) {
            original.call(new Object[]{g, delta});
            return;
        }
        g.method_51448().pushMatrix();
        try {
            int width = this.field_2018 == null ? 1 : class_310.method_1551().field_1772.method_27525((class_5348)this.field_2018);
            HudItemControlsModule.transform(g, s, g.method_51421() / 2, g.method_51443() - 68, (float)(-width) / 2.0f, -4.0f, width, 9.0f);
            original.call(new Object[]{g, delta});
        }
        finally {
            g.method_51448().popMatrix();
        }
    }
}

