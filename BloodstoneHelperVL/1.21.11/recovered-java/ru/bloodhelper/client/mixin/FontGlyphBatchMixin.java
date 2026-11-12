/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_11241
 *  net.minecraft.class_11244
 *  net.minecraft.class_12137
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.gen.Invoker
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11241;
import net.minecraft.class_11244;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bloodhelper.client.gui.FontBatchAccess;
import ru.bloodhelper.client.gui.FontGlyphBatchState;
import ru.bloodhelper.client.gui.FontGlyphRun;

@Mixin(value={class_332.class})
public abstract class FontGlyphBatchMixin
implements FontBatchAccess {
    @Unique
    private FontGlyphRun bloodhelper$pendingGlyphs;
    @Unique
    private float bloodhelper$fontX;
    @Unique
    private float bloodhelper$fontY;
    @Unique
    private float bloodhelper$fontSize;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void bloodhelper$submitGlyphRun(FontGlyphRun run, class_2960 texture, float x, float y, float size, int color) {
        if (this.bloodhelper$pendingGlyphs != null) {
            throw new IllegalStateException("Nested custom font submission");
        }
        int left = run.left(x, size);
        int top = run.top(y, size);
        int width = run.right(x, size) - left;
        int height = run.bottom(y, size) - top;
        if (width <= 0 || height <= 0) {
            return;
        }
        this.bloodhelper$pendingGlyphs = run;
        this.bloodhelper$fontX = x;
        this.bloodhelper$fontY = y;
        this.bloodhelper$fontSize = size;
        try {
            class_1044 atlas = class_310.method_1551().method_1531().method_4619(texture);
            this.bloodhelper$invokeSubmitBlit(class_10799.field_56883, atlas.method_71659(), atlas.method_75484(), left, top, left + width, top + height, 0.0f, 1.0f, 0.0f, 1.0f, color);
        }
        finally {
            this.bloodhelper$pendingGlyphs = null;
        }
    }

    @Invoker(value="method_70847")
    public abstract void bloodhelper$invokeSubmitBlit(RenderPipeline var1, GpuTextureView var2, class_12137 var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10, float var11, int var12);

    @ModifyArg(method={"method_70847"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11246;method_70919(Lnet/minecraft/class_11244;)V"), index=0)
    private class_11244 bloodhelper$batchOwnGlyphs(class_11244 original) {
        if (this.bloodhelper$pendingGlyphs == null) {
            return original;
        }
        if (!(original instanceof class_11241)) {
            throw new IllegalStateException("Unexpected custom-font blit context");
        }
        class_11241 blit = (class_11241)original;
        return new FontGlyphBatchState(blit, this.bloodhelper$pendingGlyphs, this.bloodhelper$fontX, this.bloodhelper$fontY, this.bloodhelper$fontSize);
    }
}

