/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice
 *  net.minecraft.client.gui.render.GuiRenderer
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.mixin.GlassDrawAccessor;

@Mixin(value={GuiRenderer.class})
public abstract class GlassGuiRendererMixin {
    @Shadow
    @Final
    private List<?> draws;
    @Unique
    private boolean bloodhelper$insideGlassRange;

    @Shadow
    private void executeDrawRange(Supplier<String> name, RenderTarget target, GpuBufferSlice transform, int start, int end) {
        throw new AssertionError();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"executeDrawRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$compositeGui(Supplier<String> name, RenderTarget target, GpuBufferSlice transform, int start, int end, CallbackInfo ci) {
        if (this.bloodhelper$insideGlassRange) {
            return;
        }
        boolean hasGlass = false;
        for (int i = start; i < end; ++i) {
            if (!this.bloodhelper$isGlass(i)) continue;
            hasGlass = true;
            break;
        }
        if (!hasGlass) {
            return;
        }
        this.bloodhelper$insideGlassRange = true;
        LiquidGlass.beginGuiRange();
        try {
            int from = start;
            while (from < end) {
                int to;
                boolean glass = this.bloodhelper$isGlass(from);
                for (to = from + 1; !(to >= end || this.bloodhelper$isGlass(to) != glass || glass && ((GlassDrawAccessor)this.draws.get(from)).bloodhelper$pipeline() != ((GlassDrawAccessor)this.draws.get(to)).bloodhelper$pipeline()); ++to) {
                }
                if (glass) {
                    LiquidGlass.captureGuiBackground(((GlassDrawAccessor)this.draws.get(from)).bloodhelper$pipeline());
                }
                this.executeDrawRange(name, target, transform, from, to);
                from = to;
            }
        }
        finally {
            this.bloodhelper$insideGlassRange = false;
        }
        ci.cancel();
    }

    @Unique
    private boolean bloodhelper$isGlass(int index) {
        return LiquidGlass.isGlassPipeline(((GlassDrawAccessor)this.draws.get(index)).bloodhelper$pipeline());
    }
}

