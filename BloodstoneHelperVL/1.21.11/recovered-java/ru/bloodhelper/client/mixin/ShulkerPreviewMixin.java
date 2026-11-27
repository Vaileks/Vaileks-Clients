/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.bloodhelper.client.mixin;

import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.modules.ShulkerPreview;

@Mixin(value={class_332.class})
public abstract class ShulkerPreviewMixin {
    @ModifyArgs(method={"method_51446(Lnet/minecraft/class_327;Lnet/minecraft/class_1799;II)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_332;method_51437(Lnet/minecraft/class_327;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/class_2960;)V"))
    private void bloodhelper$shulkerContents(Args args, class_327 font, class_1799 stack, int mouseX, int mouseY) {
        class_332 graphics = (class_332)this;
        args.set(2, ShulkerPreview.forTooltip(stack, (Optional)args.get(2), graphics.method_51421(), graphics.method_51443()));
    }
}

