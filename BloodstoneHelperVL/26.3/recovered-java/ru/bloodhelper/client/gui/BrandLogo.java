/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BrandLogo {
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"textures/logo.png");

    private BrandLogo() {
    }

    public static void draw(GuiGraphicsExtractor g, int x, int y, int w) {
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, x, y, 0.0f, 0.0f, w, Math.round((float)w * 722.0f / 1105.0f), 1105, 722, 1105, 722);
    }
}

