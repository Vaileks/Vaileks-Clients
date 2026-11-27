/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.Pyz1k;
import pyz1k.event.impl.EventKeyPress;
import pyz1k.module.impl.visuals.Interface;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin {
    @Inject(method={"keyPress"}, at={@At(value="HEAD")})
    private void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().handle()) {
            Pyz1k.getInstance().getEventBus().post((Object)new EventKeyPress(pyz1k.compat.LegacyInput.fromNativeKey(input.key()), action));
        }
    }

    @Inject(method={"keyPress"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHudEditorKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        Interface interfaceModule;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui.screen() instanceof ChatScreen && window == mc.getWindow().handle() && action == 1 && (interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class)) != null && interfaceModule.handleHudEditorKeyPressed(input)) {
            ci.cancel();
        }
    }

    @Inject(method={"charTyped"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHudEditorChar(long window, CharacterEvent input, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui.screen() instanceof ChatScreen && window == mc.getWindow().handle()) {
            int codePoint;
            Interface interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
            int n = codePoint = input == null ? 0 : input.codepoint();
            if (interfaceModule != null && interfaceModule.handleHudEditorCharTyped(codePoint)) {
                ci.cancel();
            }
        }
    }
}

