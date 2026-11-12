/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 */
package ru.bloodhelper.client.modules;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.compat.OptionalMixinHooks;
import ru.bloodhelper.client.config.CrosshairSettings;
import ru.bloodhelper.client.modules.CrosshairPattern;

public final class CrosshairRenderer {
    private static final Identifier PIXEL = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"crosshair_pixel");
    private static ShapeKey key;
    private static CrosshairPattern.Mask shape;
    private static CrosshairPattern.Mask border;

    private CrosshairRenderer() {
    }

    public static boolean enabled() {
        return OptionalMixinHooks.crosshair() && BloodHelperClient.config().crosshair.enabled;
    }

    public static boolean showInF5() {
        return CrosshairRenderer.enabled() && OptionalMixinHooks.crosshairPerspective() && BloodHelperClient.config().crosshair.showInF5;
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        CrosshairRenderer.draw(g, g.guiWidth() / 2, g.guiHeight() / 2, BloodHelperClient.config().crosshair, 0, false);
    }

    public static void draw(GuiGraphicsExtractor g, int cx, int cy, CrosshairSettings c, int previewTarget, boolean preview) {
        int color;
        if (key == null || !key.matches(c)) {
            key = new ShapeKey(c.mode, c.preset, c.size, c.thickness, c.gap, c.dotSize, c.dot, c.outline, c.gridSize, (int[])c.pixels.clone());
            shape = CrosshairPattern.create(c);
            border = CrosshairPattern.outline(shape, c.outline);
        }
        int target = preview ? previewTarget : CrosshairRenderer.target();
        switch (c.colorMode) {
            case "DYNAMIC": {
                int n;
                if (target == 1) {
                    n = c.enemyColor;
                    break;
                }
                if (target == 2) {
                    n = c.friendlyColor;
                    break;
                }
                n = c.color;
                break;
            }
            case "CHROMA": {
                int n = Color.HSBtoRGB((float)(System.nanoTime() % 6000000000L) / 6.0E9f, 0.8f, 1.0f);
                break;
            }
            default: {
                int n = color = c.color;
            }
        }
        if (c.outline > 0) {
            CrosshairRenderer.paint(g, border, cx, cy, c.scale, CrosshairRenderer.alpha(c.outlineColor, c.opacity), false);
        }
        boolean invert = c.colorMode.equals("INVERT");
        CrosshairRenderer.paint(g, shape, cx, cy, c.scale, CrosshairRenderer.alpha(invert ? -1 : color, c.opacity), invert);
    }

    private static int target() {
        EntityHitResult hit;
        HitResult hitResult;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !((hitResult = mc.hitResult) instanceof EntityHitResult) || !((hitResult = (hit = (EntityHitResult)hitResult).getEntity()) instanceof LivingEntity)) {
            return 0;
        }
        LivingEntity entity = (LivingEntity)hitResult;
        if (mc.player.isAlliedTo((Entity)entity)) {
            return 2;
        }
        return entity instanceof Player || entity instanceof Enemy ? 1 : 2;
    }

    private static int alpha(int color, double opacity) {
        return color & 0xFFFFFF | (int)Math.round((double)(color >>> 24) * opacity) << 24;
    }

    public static void paint(GuiGraphicsExtractor g, CrosshairPattern.Mask mask, int cx, int cy, double scale, int color, boolean invert) {
        int n = mask.size();
        double center = (double)n / 2.0;
        for (int y = 0; y < n; ++y) {
            int x = 0;
            while (x < n) {
                if (!mask.at(x, y)) {
                    ++x;
                    continue;
                }
                int start = x;
                while (x < n && mask.at(x, y)) {
                    ++x;
                }
                int x0 = (int)Math.round((double)cx + ((double)start - center) * scale);
                int x1 = (int)Math.round((double)cx + ((double)x - center) * scale);
                int y0 = (int)Math.round((double)cy + ((double)y - center) * scale);
                int y1 = (int)Math.round((double)cy + ((double)(y + 1) - center) * scale);
                if (x1 <= x0 || y1 <= y0) continue;
                if (invert) {
                    g.blitSprite(RenderPipelines.CROSSHAIR, PIXEL, x0, y0, x1 - x0, y1 - y0, color);
                    continue;
                }
                g.fill(x0, y0, x1, y1, color);
            }
        }
    }

    private record ShapeKey(String mode, String preset, int size, int thickness, int gap, int dotSize, boolean dot, int outline, int grid, int[] pixels) {
        boolean matches(CrosshairSettings c) {
            return this.mode.equals(c.mode) && this.preset.equals(c.preset) && this.size == c.size && this.thickness == c.thickness && this.gap == c.gap && this.dotSize == c.dotSize && this.dot == c.dot && this.outline == c.outline && this.grid == c.gridSize && Arrays.equals(this.pixels, c.pixels);
        }
    }
}

