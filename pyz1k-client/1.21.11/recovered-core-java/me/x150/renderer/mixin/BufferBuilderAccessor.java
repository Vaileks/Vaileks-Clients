/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  net.minecraft.class_287
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package me.x150.renderer.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_287;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_287.class})
public interface BufferBuilderAccessor {
    @Accessor(value="field_1565")
    public VertexFormat getVertexFormat();

    @Accessor(value="field_52072")
    public long getVertexPointer();

    @Invoker(value="method_60805")
    public long beginNewVertex();
}

