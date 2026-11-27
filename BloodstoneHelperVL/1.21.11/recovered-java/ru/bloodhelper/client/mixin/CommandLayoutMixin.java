/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.bloodhelper.client.modules.CommandLayoutModule;

@Mixin(value={class_408.class})
public abstract class CommandLayoutMixin {
    @ModifyVariable(method={"method_44056"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private String bloodhelper$correctCommandLayout(String message) {
        return CommandLayoutModule.correct(message);
    }
}

