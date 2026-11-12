/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 *  org.joml.Vector2f
 *  org.lwjgl.opengl.GL11
 */
package pyz1k.render;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public final class ScissorUtil {
    private static final Deque<ScissorRect> SCISSOR_STACK = new ArrayDeque<ScissorRect>();

    private ScissorUtil() {
    }

    public static void push(class_332 ctx, float x, float y, float width, float height) {
        ScissorUtil.push(ctx.method_51448(), x, y, width, height);
    }

    public static void push(Matrix3x2fStack matrices, float x, float y, float width, float height) {
        ScissorUtil.push(ScissorUtil.transformRect(matrices, x, y, width, height));
    }

    public static void push(float x, float y, float width, float height) {
        ScissorUtil.push(new ScissorRect(x, y, width, height));
    }

    public static void pop() {
        if (!SCISSOR_STACK.isEmpty()) {
            SCISSOR_STACK.pop();
        }
        if (SCISSOR_STACK.isEmpty()) {
            GL11.glDisable((int)3089);
        } else {
            ScissorUtil.applyScissor(SCISSOR_STACK.peek());
        }
    }

    public static void clear() {
        SCISSOR_STACK.clear();
        GL11.glDisable((int)3089);
    }

    public static boolean isEnabled() {
        return !SCISSOR_STACK.isEmpty();
    }

    private static void push(ScissorRect rect) {
        ScissorRect clipped = rect;
        if (!SCISSOR_STACK.isEmpty()) {
            clipped = ScissorUtil.intersect(SCISSOR_STACK.peek(), rect);
        }
        SCISSOR_STACK.push(clipped);
        ScissorUtil.applyScissor(clipped);
    }

    private static ScissorRect transformRect(Matrix3x2fStack matrices, float x, float y, float width, float height) {
        Vector2f topLeft = new Vector2f(x, y);
        Vector2f bottomRight = new Vector2f(x + width, y + height);
        matrices.transformPosition(topLeft);
        matrices.transformPosition(bottomRight);
        float left = Math.min(topLeft.x, bottomRight.x);
        float top = Math.min(topLeft.y, bottomRight.y);
        float right = Math.max(topLeft.x, bottomRight.x);
        float bottom = Math.max(topLeft.y, bottomRight.y);
        return new ScissorRect(left, top, right - left, bottom - top);
    }

    private static ScissorRect intersect(ScissorRect a, ScissorRect b) {
        float left = Math.max(a.left(), b.left());
        float top = Math.max(a.top(), b.top());
        float right = Math.min(a.right(), b.right());
        float bottom = Math.min(a.bottom(), b.bottom());
        return new ScissorRect(left, top, Math.max(0.0f, right - left), Math.max(0.0f, bottom - top));
    }

    private static void applyScissor(ScissorRect rect) {
        class_310 client = class_310.method_1551();
        int framebufferHeight = client.method_22683().method_4506();
        double scale = client.method_22683().method_4495();
        float left = (float)((double)rect.left() * scale);
        float top = (float)((double)rect.top() * scale);
        float right = (float)((double)rect.right() * scale);
        float bottom = (float)((double)rect.bottom() * scale);
        int scissorX = (int)Math.floor(left);
        int scissorY = (int)Math.floor((float)framebufferHeight - bottom);
        int scissorWidth = Math.max(0, (int)Math.ceil(right - left));
        int scissorHeight = Math.max(0, (int)Math.ceil(bottom - top));
        GL11.glEnable((int)3089);
        GL11.glScissor((int)scissorX, (int)scissorY, (int)scissorWidth, (int)scissorHeight);
    }

    private record ScissorRect(float x, float y, float width, float height) {
        private float left() {
            return this.x;
        }

        private float top() {
            return this.y;
        }

        private float right() {
            return this.x + this.width;
        }

        private float bottom() {
            return this.y + this.height;
        }
    }
}

