/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.mixin.LoginGuardChatAccessor;
import ru.bloodhelper.client.modules.LoginGuard;

@Mixin(value={class_342.class})
public abstract class LoginGuardInputMixin {
    @Inject(method={"method_48579"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hidePassword(class_332 g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BloodHelperClient.config().loginGuard) {
            return;
        }
        class_437 screen = class_310.method_1551().field_1755;
        if (!(screen instanceof class_408) || ((LoginGuardChatAccessor)screen).bloodhelper$loginInput() != this) {
            return;
        }
        class_342 input = (class_342)this;
        int start = LoginGuard.secretStart(input.method_1882());
        if (start < 0) {
            return;
        }
        int x = input.method_46426() + (input.method_1851() ? 4 : 0);
        int y = input.method_46427() + (input.method_1851() ? (input.method_25364() - 8) / 2 : 0);
        String prefix = input.method_1882().substring(0, start);
        class_327 font = class_310.method_1551().field_1772;
        if (input.method_1851()) {
            UiDraw.border(g, input.method_46426(), input.method_46427(), input.method_25368(), input.method_25364(), 3, -15461351, -9934731);
        }
        g.method_51433(font, prefix, x, y, -2039584, true);
        int left = Math.min(x + font.method_1727(prefix) + 2, input.method_46426() + input.method_25368() - 4);
        int width = Math.max(0, input.method_46426() + input.method_25368() - 4 - left);
        UiDraw.roundedSolid(g, left, y - 1, width, 11, 3, -11841703);
        UiDraw.roundedSolid(g, left + 2, y + 1, Math.max(0, width - 4), 7, 2, -8946554);
        UiDraw.roundedSolid(g, left + 4, y + 3, Math.max(0, width - 8), 3, 1, -6709851);
        ci.cancel();
    }
}

