/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2535
 *  net.minecraft.class_2720
 *  net.minecraft.class_310
 *  net.minecraft.class_642
 *  net.minecraft.class_8673
 *  net.minecraft.class_9053
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2535;
import net.minecraft.class_2720;
import net.minecraft.class_310;
import net.minecraft.class_642;
import net.minecraft.class_8673;
import net.minecraft.class_9053;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.resourcepack.BloodstoneResourcePack;

@Mixin(value={class_8673.class})
public abstract class BloodstonePackPacketMixin {
    @Shadow
    @Final
    protected class_310 field_45588;
    @Shadow
    @Final
    protected class_2535 field_45589;
    @Shadow
    @Final
    protected class_642 field_45590;

    @Inject(method={"method_52784"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void bloodhelper$serverPack(class_2720 packet, CallbackInfo ci) {
        if (BloodstoneResourcePack.offer(this.field_45588, this.field_45589, this.field_45590, packet)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_55512"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V", shift=At.Shift.AFTER)})
    private void bloodhelper$popPack(class_9053 packet, CallbackInfo ci) {
        BloodstoneResourcePack.pop(this.field_45589, packet.comp_2157());
    }
}

