/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4184
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 *  ru.bloodhelper.client.modules.FreeLookModule
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.modules.FreeLookModule;

@Mixin(value={class_4184.class})
public class FreeLookCameraMixin {
    @ModifyArgs(method={"method_19321"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_4184;method_19325(FF)V"), require=0)
    private void bloodhelper$camera(Args args) {
        if (FreeLookModule.active()) {
            args.set(0, (Object)Float.valueOf(FreeLookModule.yaw()));
            args.set(1, (Object)Float.valueOf(FreeLookModule.pitch()));
        }
    }
}

