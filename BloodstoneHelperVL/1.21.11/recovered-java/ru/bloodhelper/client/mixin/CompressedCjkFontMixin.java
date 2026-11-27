/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_2960
 *  net.minecraft.class_3300
 *  net.minecraft.class_8557
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_8557;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.gui.BundledCjkFont;

@Mixin(value={class_8557.class})
public abstract class CompressedCjkFontMixin {
    @WrapOperation(method={"method_51759"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_3300;open(Lnet/minecraft/class_2960;)Ljava/io/InputStream;")})
    private InputStream bloodhelper$cjk(class_3300 resources, class_2960 id, Operation<InputStream> original) throws IOException {
        if ("bloodhelper".equals(id.method_12836()) && "font/cjk.ttf".equals(id.method_12832()) && resources.method_14486(id).isEmpty()) {
            return BundledCjkFont.open();
        }
        return (InputStream)original.call(new Object[]{resources, id});
    }
}

