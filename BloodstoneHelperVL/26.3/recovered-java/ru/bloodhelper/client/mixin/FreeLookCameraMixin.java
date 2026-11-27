package ru.bloodhelper.client.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.modules.FreeLookModule;

@Mixin(value={Camera.class})
public class FreeLookCameraMixin {
    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setRotation(FF)V")}, require=1)
    private void bloodhelper$camera(Args args) {
        if (FreeLookModule.active()) {
            args.set(0, (Object)Float.valueOf(FreeLookModule.yaw()));
            args.set(1, (Object)Float.valueOf(FreeLookModule.pitch()));
        }
    }
}

