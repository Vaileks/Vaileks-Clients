/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bloodhelper.client.BloodHelperClient;

@Mixin(value={class_757.class})
abstract class AspectRatioMixin {
    AspectRatioMixin() {
    }

    @ModifyArg(method={"method_22973"}, at=@At(value="INVOKE", target="Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"), index=1)
    private float bloodhelper$aspect(float original) {
        return class_310.method_1551().field_1687 == null ? original : BloodHelperClient.config().aspectRatio.ratio(original);
    }
}

