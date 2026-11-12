/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_318
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package ru.bloodhelper.client.mixin;

import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_318;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.bloodhelper.client.modules.ScreenshotActions;

@Mixin(value={class_318.class})
public abstract class ScreenshotActionsMixin {
    @ModifyVariable(method={"method_22690(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/class_276;ILjava/util/function/Consumer;)V"}, at=@At(value="HEAD"), argsOnly=true)
    private static Consumer<class_2561> bloodhelper$screenshotActions(Consumer<class_2561> callback) {
        return ScreenshotActions.decorate(callback);
    }
}

