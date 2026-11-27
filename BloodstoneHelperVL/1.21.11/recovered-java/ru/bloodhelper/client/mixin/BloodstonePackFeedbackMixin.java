/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1066
 *  net.minecraft.class_2535
 *  net.minecraft.class_9039
 *  net.minecraft.class_9039$class_9040
 *  net.minecraft.class_9039$class_9060
 *  net.minecraft.class_9044$class_9047
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import java.util.UUID;
import net.minecraft.class_1066;
import net.minecraft.class_2535;
import net.minecraft.class_9039;
import net.minecraft.class_9044;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.resourcepack.BloodstoneResourcePack;

@Mixin(value={class_1066.class})
public abstract class BloodstonePackFeedbackMixin {
    @Shadow
    class_9039 field_47601;

    @Inject(method={"method_55528"}, at={@At(value="TAIL")})
    private void bloodhelper$observe(final class_2535 connection, class_9044.class_9047 prompt, CallbackInfo ci) {
        final class_9039 vanilla = this.field_47601;
        this.field_47601 = new class_9039(){

            public void method_55620(UUID id, class_9039.class_9060 update) {
                vanilla.method_55620(id, update);
            }

            public void method_55619(UUID id, class_9039.class_9040 result) {
                vanilla.method_55619(id, result);
                BloodstoneResourcePack.feedback(connection, id, result);
            }
        };
    }

    @Inject(method={"method_55541"}, at={@At(value="HEAD")})
    private void bloodhelper$disconnected(CallbackInfo ci) {
        BloodstoneResourcePack.disconnected();
    }
}

