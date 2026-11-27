package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;

@Mixin(value={MouseHandler.class})
abstract class DropGuardMouseMixin {
    DropGuardMouseMixin() {
    }

    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    private void bloodhelper$dropPhysicalMouse(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        DropGuardModule.mouseInput(Minecraft.getInstance(), window, info, action);
    }
}

