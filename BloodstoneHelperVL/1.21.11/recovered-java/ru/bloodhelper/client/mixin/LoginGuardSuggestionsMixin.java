/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  net.minecraft.class_4717
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_4717;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.mixin.LoginGuardChatAccessor;
import ru.bloodhelper.client.modules.LoginGuard;

@Mixin(value={class_4717.class})
public abstract class LoginGuardSuggestionsMixin {
    @Inject(method={"method_23923"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hidePasswordSuggestions(class_332 g, int mouseX, int mouseY, CallbackInfo ci) {
        if (!BloodHelperClient.config().loginGuard) {
            return;
        }
        class_437 screen = class_310.method_1551().field_1755;
        if (screen instanceof class_408 && LoginGuard.secretStart(((LoginGuardChatAccessor)screen).bloodhelper$loginInput().method_1882()) >= 0) {
            ci.cancel();
        }
    }
}

