/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.QuickCommandsModule;

@Mixin(value={class_309.class})
abstract class QuickCommandsKeyboardMixin {
    QuickCommandsKeyboardMixin() {
    }

    @Inject(method={"method_1466"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$wheelPress(long window, int action, class_11908 event, CallbackInfo ci) {
        if (QuickCommandsModule.keyPress(class_310.method_1551(), window, action, event)) {
            ci.cancel();
        }
    }
}

