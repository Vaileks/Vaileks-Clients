package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={Minecraft.class})
abstract class SlotLockKeybindMixin {
    SlotLockKeybindMixin() {
    }

    @Inject(method={"handleKeybinds"}, at={@At(value="HEAD")})
    private void bloodhelper$lockHandSwap(CallbackInfo ci) {
        SlotLockModule.guardSwapKey();
    }
}

