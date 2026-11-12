/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2663
 *  net.minecraft.class_2664
 *  net.minecraft.class_2765
 *  net.minecraft.class_2767
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_634
 *  net.minecraft.class_8143
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2663;
import net.minecraft.class_2664;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_634;
import net.minecraft.class_8143;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.killeffects.ServerKillSoundFilter;

@Mixin(value={class_634.class})
public abstract class ServerKillSoundMixin {
    @Inject(method={"method_49034"}, at={@At(value="HEAD")})
    private void pyz1k$recordDamage(class_8143 packet, CallbackInfo ci) {
        if (class_310.method_1551().method_18854()) {
            KillEffectEngine.recordDamage(packet.comp_1267(), packet.comp_1269(), packet.comp_1270());
        }
    }

    @Inject(method={"method_11124"}, at={@At(value="HEAD")})
    private void pyz1k$recordExplosion(class_2664 packet, CallbackInfo ci) {
        if (class_310.method_1551().method_18854()) {
            KillEffectEngine.recordExplosion(packet.comp_2883(), packet.comp_4594());
        }
    }

    @Inject(method={"method_11148"}, at={@At(value="HEAD")})
    private void pyz1k$recordDeath(class_2663 packet, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc.method_18854() && mc.field_1687 != null && packet.method_11470() == 3) {
            KillEffectEngine.recordDeath(packet.method_11469((class_1937)mc.field_1687));
        }
    }

    @Inject(method={"method_11146"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$muteKillSound(class_2767 packet, CallbackInfo ci) {
        if (!class_310.method_1551().method_18854()) {
            return;
        }
        if (ServerKillSoundFilter.shouldMute(((class_3414)packet.method_11894().comp_349()).comp_3319(), packet.method_11890(), packet.method_11889(), packet.method_11893())) {
            ci.cancel();
        }
    }

    @Inject(method={"method_11125"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$muteKillEntitySound(class_2765 packet, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (!mc.method_18854() || mc.field_1687 == null) {
            return;
        }
        class_1297 entity = mc.field_1687.method_8469(packet.method_11883());
        if (entity != null && ServerKillSoundFilter.shouldMute(((class_3414)packet.method_11882().comp_349()).comp_3319(), entity.method_23317(), entity.method_23318(), entity.method_23321())) {
            ci.cancel();
        }
    }
}

