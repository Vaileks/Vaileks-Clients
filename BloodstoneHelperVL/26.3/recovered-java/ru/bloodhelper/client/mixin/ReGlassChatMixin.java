/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.gui.components.ChatComponent$DisplayMode
 *  org.spongepowered.asm.mixin.Mixin
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import ru.bloodhelper.client.gui.ReGlassChatContext;

@Mixin(value={ChatComponent.class})
public abstract class ReGlassChatMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"extractRenderState"})
    private void bloodhelper$chat(GuiGraphicsExtractor graphics, Font font, int tick, int mouseX, int mouseY, ChatComponent.DisplayMode focused, boolean hide, Operation<Void> original) {
        ReGlassChatContext.enter();
        try {
            original.call(new Object[]{graphics, font, tick, mouseX, mouseY, focused, hide});
        }
        finally {
            ReGlassChatContext.exit();
        }
    }
}

