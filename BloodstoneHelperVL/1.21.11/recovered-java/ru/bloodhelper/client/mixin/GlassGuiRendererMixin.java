/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  net.minecraft.class_11228
 *  net.minecraft.class_276
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_11228;
import net.minecraft.class_276;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.mixin.GlassDrawAccessor;

@Mixin(value={class_11228.class})
public abstract class GlassGuiRendererMixin {
    @Shadow
    @Final
    private List<?> field_59915;
    @Unique
    private boolean bloodhelper$insideGlassRange;

    @Shadow
    private void method_71289(Supplier<String> name, class_276 target, GpuBufferSlice fog, GpuBufferSlice transform, GpuBuffer indices, VertexFormat.class_5595 type, int start, int end) {
        throw new AssertionError();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_71289"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$compositeGui(Supplier<String> name, class_276 target, GpuBufferSlice fog, GpuBufferSlice transform, GpuBuffer indices, VertexFormat.class_5595 type, int start, int end, CallbackInfo ci) {
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
                for (to = from + 1; !(to >= end || this.bloodhelper$isGlass(to) != glass || glass && ((GlassDrawAccessor)this.field_59915.get(from)).bloodhelper$pipeline() != ((GlassDrawAccessor)this.field_59915.get(to)).bloodhelper$pipeline()); ++to) {
                }
                if (glass) {
                    LiquidGlass.captureGuiBackground(((GlassDrawAccessor)this.field_59915.get(from)).bloodhelper$pipeline());
                }
                this.method_71289(name, target, fog, transform, indices, type, from, to);
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
        return LiquidGlass.isGlassPipeline(((GlassDrawAccessor)this.field_59915.get(index)).bloodhelper$pipeline());
    }
}

