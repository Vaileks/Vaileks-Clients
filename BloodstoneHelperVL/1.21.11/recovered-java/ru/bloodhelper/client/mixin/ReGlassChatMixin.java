/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_338
 *  org.spongepowered.asm.mixin.Mixin
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import ru.bloodhelper.client.gui.ReGlassChatContext;

@Mixin(value={class_338.class})
public abstract class ReGlassChatMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"method_75804"})
    private void bloodhelper$chat(class_332 graphics, class_327 font, int tick, int mouseX, int mouseY, boolean focused, boolean hide, Operation<Void> original) {
        ReGlassChatContext.enter();
        try {
            original.call(new Object[]{graphics, font, tick, mouseX, mouseY, focused, hide});
        }
        finally {
            ReGlassChatContext.exit();
        }
    }
}

