package ru.bloodhelper.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.QuickCommandsModule;

@Mixin(value={KeyboardHandler.class})
abstract class QuickCommandsKeyboardMixin {
    QuickCommandsKeyboardMixin() {
    }

    @Inject(method={"keyPress"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$wheelPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (QuickCommandsModule.keyPress(Minecraft.getInstance(), window, action, event)) {
            ci.cancel();
        }
    }
}

