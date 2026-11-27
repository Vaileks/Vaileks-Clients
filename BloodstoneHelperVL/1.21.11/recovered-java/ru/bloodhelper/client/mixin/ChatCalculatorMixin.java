/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.ChatCalculator;
import ru.bloodhelper.client.modules.ChatCalculatorPanel;

@Mixin(value={class_408.class})
public abstract class ChatCalculatorMixin {
    @Shadow
    protected class_342 field_2382;
    @Unique
    private final ChatCalculatorPanel bloodhelper$calculator = new ChatCalculatorPanel();

    @Inject(method={"method_25426()V"}, at={@At(value="TAIL")})
    private void bloodhelper$calculatorInit(CallbackInfo callback) {
        this.bloodhelper$calculator.resized();
    }

    @Inject(method={"method_25394"}, at={@At(value="HEAD")})
    private void bloodhelper$calculatorLayout(class_332 graphics, int x, int y, float delta, CallbackInfo callback) {
        class_408 screen = (class_408)this;
        this.bloodhelper$calculator.prepare(this.field_2382, screen.field_22789, screen.field_22790, graphics);
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void bloodhelper$calculatorResult(class_332 graphics, int x, int y, float delta, CallbackInfo callback) {
        this.bloodhelper$calculator.render(this.field_2382, graphics);
    }

    @Inject(method={"method_25404"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$calculatorKey(class_11908 event, CallbackInfoReturnable<Boolean> callback) {
        if (this.bloodhelper$calculator.key(this.field_2382, event)) {
            callback.setReturnValue((Object)true);
        }
    }

    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$calculatorClick(class_11909 event, boolean twice, CallbackInfoReturnable<Boolean> callback) {
        if (this.bloodhelper$calculator.click(this.field_2382, event)) {
            callback.setReturnValue((Object)true);
        }
    }

    @Inject(method={"method_44056"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$neverSendExpression(String message, boolean history, CallbackInfo callback) {
        if (BloodHelperClient.config().chatCalculator.enabled && ChatCalculator.isExpression(message)) {
            callback.cancel();
        }
    }
}

