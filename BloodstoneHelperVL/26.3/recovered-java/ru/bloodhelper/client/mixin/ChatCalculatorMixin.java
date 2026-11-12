/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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

@Mixin(value={ChatScreen.class})
public abstract class ChatCalculatorMixin {
    @Shadow
    protected EditBox input;
    @Unique
    private final ChatCalculatorPanel bloodhelper$calculator = new ChatCalculatorPanel();

    @Inject(method={"init()V"}, at={@At(value="TAIL")})
    private void bloodhelper$calculatorInit(CallbackInfo callback) {
        this.bloodhelper$calculator.resized();
    }

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")})
    private void bloodhelper$calculatorLayout(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo callback) {
        ChatScreen screen = (ChatScreen)this;
        this.bloodhelper$calculator.prepare(this.input, screen.width, screen.height, graphics);
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void bloodhelper$calculatorResult(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo callback) {
        this.bloodhelper$calculator.render(this.input, graphics);
    }

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$calculatorKey(KeyEvent event, CallbackInfoReturnable<Boolean> callback) {
        if (this.bloodhelper$calculator.key(this.input, event)) {
            callback.setReturnValue((Object)true);
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$calculatorClick(MouseButtonEvent event, boolean twice, CallbackInfoReturnable<Boolean> callback) {
        if (this.bloodhelper$calculator.click(this.input, event)) {
            callback.setReturnValue((Object)true);
        }
    }

    @Inject(method={"handleChatInput"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$neverSendExpression(String message, boolean history, CallbackInfo callback) {
        if (BloodHelperClient.config().chatCalculator.enabled && ChatCalculator.isExpression(message)) {
            callback.cancel();
        }
    }
}

