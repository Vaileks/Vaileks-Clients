/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Screenshot
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package ru.bloodhelper.client.mixin;

import java.util.function.Consumer;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.bloodhelper.client.modules.ScreenshotActions;

@Mixin(value={Screenshot.class})
public abstract class ScreenshotActionsMixin {
    @ModifyVariable(method={"grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"}, at={@At(value="HEAD")}, argsOnly=true)
    private static Consumer<Component> bloodhelper$screenshotActions(Consumer<Component> callback) {
        return ScreenshotActions.decorate(callback);
    }
}

