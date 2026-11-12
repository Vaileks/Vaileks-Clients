/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package pyz1k.render.renderers;

import net.minecraft.class_332;

public interface Renderer {
    public void render(double var1, double var3, class_332 var5);

    public static int toAbgr(int argb) {
        int a = argb >>> 24 & 0xFF;
        int r = argb >>> 16 & 0xFF;
        int g = argb >>> 8 & 0xFF;
        int b = argb & 0xFF;
        return a << 24 | b << 16 | g << 8 | r;
    }
}

