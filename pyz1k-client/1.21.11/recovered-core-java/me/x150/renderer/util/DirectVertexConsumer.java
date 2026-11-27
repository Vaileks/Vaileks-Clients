/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  net.minecraft.class_287
 *  net.minecraft.class_3532
 *  net.minecraft.class_4588
 *  org.lwjgl.system.MemoryUtil
 */
package me.x150.renderer.util;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import me.x150.renderer.mixin.BufferBuilderAccessor;
import net.minecraft.class_287;
import net.minecraft.class_3532;
import net.minecraft.class_4588;
import org.lwjgl.system.MemoryUtil;

public final class DirectVertexConsumer
implements class_4588 {
    private final BufferBuilderAccessor buffer;
    private final VertexFormat format;
    private ByteBuffer target;

    public DirectVertexConsumer(class_287 original, boolean reuseCurrentVertex) {
        this.buffer = (BufferBuilderAccessor)original;
        this.format = this.buffer.getVertexFormat();
        long pointer = reuseCurrentVertex ? this.buffer.getVertexPointer() : this.buffer.beginNewVertex();
        this.target = this.view(pointer);
    }

    private ByteBuffer view(long pointer) {
        ByteBuffer result = MemoryUtil.memByteBuffer((long)pointer, (int)this.format.getVertexSize());
        result.order(ByteOrder.nativeOrder());
        return result;
    }

    private void ensureSpace() {
        if (!this.target.hasRemaining()) {
            this.target = this.view(this.buffer.beginNewVertex());
        }
    }

    public class_4588 method_22912(float x, float y, float z) {
        this.ensureSpace();
        this.target.putFloat(x).putFloat(y).putFloat(z);
        return this;
    }

    public class_4588 method_1336(int red, int green, int blue, int alpha) {
        return this.method_39415(alpha << 24 | red << 16 | green << 8 | blue);
    }

    public class_4588 method_39415(int argb) {
        this.ensureSpace();
        this.target.putInt(argb);
        return this;
    }

    public class_4588 method_22913(float u, float v) {
        this.ensureSpace();
        this.target.putFloat(u).putFloat(v);
        return this;
    }

    public class_4588 method_60796(int u, int v) {
        this.ensureSpace();
        this.target.putShort((short)u).putShort((short)v);
        return this;
    }

    public class_4588 method_22921(int u, int v) {
        return this.method_60796(u, v);
    }

    public class_4588 method_22914(float x, float y, float z) {
        this.ensureSpace();
        this.target.put(DirectVertexConsumer.toNormalByte(x)).put(DirectVertexConsumer.toNormalByte(y)).put(DirectVertexConsumer.toNormalByte(z));
        return this;
    }

    public class_4588 method_75298(float width) {
        this.ensureSpace();
        this.target.putFloat(width);
        return this;
    }

    private static byte toNormalByte(float value) {
        return (byte)((int)(class_3532.method_15363((float)value, (float)-1.0f, (float)1.0f) * 127.0f) & 0xFF);
    }
}

