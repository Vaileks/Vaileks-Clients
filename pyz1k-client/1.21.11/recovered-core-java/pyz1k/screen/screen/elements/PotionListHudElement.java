/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1044
 *  net.minecraft.class_1049
 *  net.minecraft.class_10537
 *  net.minecraft.class_1293
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.screen.screen.elements;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_1044;
import net.minecraft.class_1049;
import net.minecraft.class_10537;
import net.minecraft.class_1293;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import pyz1k.module.impl.visuals.PotionList;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ColorUtil;

public final class PotionListHudElement
extends AbstractHudElement {
    private static final float ROW_HEIGHT = 27.0f;
    private static final float ROW_GAP = 2.0f;
    private static final float PADDING_X = 5.0f;
    private static final float ICON_SIZE = 18.0f;
    private static final float ICON_TEXT_GAP = 5.0f;
    private static final float NAME_FONT_SIZE = 7.0f;
    private static final float TIME_FONT_SIZE = 6.5f;
    private static final float LEVEL_FONT_SIZE = 5.5f;
    private static final float COMPACT_ICON_BOX = 27.0f;
    private static final float COMPACT_TIMER_HEIGHT = 16.0f;
    private static final float COMPACT_TIMER_PAD_X = 5.0f;
    private static final float COMPACT_GAP_X = 2.0f;
    private static final int TEXT_COLOR = -1;
    private static final int LEVEL_SHADOW_COLOR = -1342177280;
    private static final Set<class_2960> LOADED_EFFECT_TEXTURES = new HashSet<class_2960>();
    private final PotionList module;

    public PotionListHudElement(PotionList module) {
        super("potion_list", "PotionList", 8.0f, 82.0f, 92.0f, 27.0f);
        this.module = module;
    }

    @Override
    public void render(class_332 context, int screenWidth, int screenHeight, float tickDelta) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1772 == null) {
            return;
        }
        boolean compact = this.module.isCompactStyle();
        ArrayList<Row> rows = new ArrayList<Row>();
        float maxWidth = 0.0f;
        if (mc.field_1724 != null) {
            for (class_1293 effect : mc.field_1724.method_6026()) {
                String descriptionId = effect.method_5586();
                String name = class_2561.method_43471((String)descriptionId).getString();
                int level = Math.max(1, effect.method_5578() + 1);
                String title = name + " " + level;
                String time = this.formatDuration(effect);
                class_2960 icon = this.iconTexture(descriptionId);
                this.ensureEffectTexture(mc, icon);
                float width = compact ? this.measureCompactWidth(mc, time) : this.measureClassicWidth(mc, title, time);
                rows.add(new Row(icon, title, time, level, width));
                maxWidth = Math.max(maxWidth, width);
            }
        }
        if (rows.isEmpty() && this.canInteractInEditor(mc)) {
            String title = "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c 2";
            String time = "01:30";
            int level = 2;
            float width = compact ? this.measureCompactWidth(mc, time) : this.measureClassicWidth(mc, title, time);
            class_2960 icon = class_2960.method_60655((String)"minecraft", (String)"textures/mob_effect/speed.png");
            this.ensureEffectTexture(mc, icon);
            rows.add(new Row(icon, title, time, level, width));
            maxWidth = width;
        }
        if (rows.isEmpty()) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        float scale = this.module.getScale();
        float unscaledHeight = (float)rows.size() * 27.0f + (float)Math.max(0, rows.size() - 1) * 2.0f;
        this.setBounds(maxWidth * scale, unscaledHeight * scale);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(this.getVisualX(), this.getVisualY());
        matrices.scale(scale, scale);
        float rowY = 0.0f;
        for (Row row : rows) {
            if (compact) {
                this.drawCompactRow(context, mc, row, rowY);
            } else {
                this.drawClassicRow(context, mc, row, rowY);
            }
            rowY += 29.0f;
        }
        matrices.popMatrix();
    }

    @Override
    public float getVisualY() {
        float oneRowHeight = 27.0f * this.module.getScale();
        return this.getY() + oneRowHeight * 0.5f - this.getHeight() * 0.5f;
    }

    @Override
    public void setVisualPosition(float x, float y) {
        float oneRowHeight = 27.0f * this.module.getScale();
        super.setPosition(x, y - oneRowHeight * 0.5f + this.getHeight() * 0.5f);
    }

    private void drawClassicRow(class_332 context, class_310 mc, Row row, float rowY) {
        this.drawBackground(context, 0.0f, rowY, row.width(), 27.0f, this.module.getBackgroundColor());
        this.drawEffectIcon(context, mc, row.icon(), 5.0f, rowY + 4.5f);
        float textX = 28.0f;
        if (this.module.isClientStyle()) {
            RenderUtil.drawText(context, row.title(), textX, rowY + 5.0f, -1, GFonts.MEDIUM, 7.0f);
            RenderUtil.drawText(context, row.time(), textX, rowY + 15.2f, -1, GFonts.MEDIUM, 6.5f);
        } else {
            context.method_51433(mc.field_1772, row.title(), Math.round(textX), Math.round(rowY + 3.0f), -1, true);
            context.method_51433(mc.field_1772, row.time(), Math.round(textX), Math.round(rowY + 15.0f), -1, true);
        }
    }

    private void drawCompactRow(class_332 context, class_310 mc, Row row, float rowY) {
        float timerWidth = Math.max(31.0f, this.textWidth(mc, row.time(), GFonts.MEDIUM, 6.5f) + 10.0f);
        float timerX = 29.0f;
        float timerY = rowY + 5.5f;
        int baseColor = this.module.getBackgroundColor();
        int secondaryColor = ColorUtil.interpolate(baseColor, -1, 0.1f);
        secondaryColor = ColorUtil.withAlpha(secondaryColor, ColorUtil.alpha(baseColor));
        this.drawBackground(context, 0.0f, rowY, 27.0f, 27.0f, baseColor);
        this.drawBackground(context, timerX, timerY, timerWidth, 16.0f, baseColor);
        float iconX = 4.5f;
        float iconY = rowY + 4.5f;
        this.drawEffectIcon(context, mc, row.icon(), iconX, iconY);
        String levelText = Integer.toString(row.level());
        float levelWidth = this.textWidth(mc, levelText, GFonts.BOLD, 5.5f);
        float levelX = 24.5f - levelWidth;
        float levelY = rowY + 3.0f;
        if (this.module.isClientStyle()) {
            RenderUtil.drawText(context, levelText, levelX + 0.6f, levelY + 0.6f, -1342177280, GFonts.BOLD, 5.5f);
            RenderUtil.drawText(context, levelText, levelX, levelY, -1, GFonts.BOLD, 5.5f);
        } else {
            context.method_51433(mc.field_1772, levelText, Math.round(levelX), Math.round(levelY), -1, true);
        }
        float timeWidth = this.textWidth(mc, row.time(), GFonts.MEDIUM, 6.5f);
        float timeX = timerX + (timerWidth - timeWidth) * 0.5f;
        float timeY = timerY + 5.5f;
        if (this.module.isClientStyle()) {
            RenderUtil.drawText(context, row.time(), timeX + 0.5f, timeY, -1, GFonts.MEDIUM, 6.5f);
        } else {
            context.method_51433(mc.field_1772, row.time(), Math.round(timeX), Math.round(timerY + 4.5f), -1, true);
        }
    }

    private float measureClassicWidth(class_310 mc, String title, String time) {
        float titleWidth = this.textWidth(mc, title, GFonts.MEDIUM, 7.0f);
        float timeWidth = this.textWidth(mc, time, GFonts.MEDIUM, 6.5f);
        return 33.0f + Math.max(titleWidth, timeWidth);
    }

    private float measureCompactWidth(class_310 mc, String time) {
        float timerWidth = Math.max(29.0f, this.textWidth(mc, time, GFonts.MEDIUM, 6.5f) + 10.0f);
        return 29.0f + timerWidth;
    }

    private float textWidth(class_310 mc, String text, GFonts font, float size) {
        return this.module.isClientStyle() ? RenderUtil.getTextWidth(text, font, size) : (float)mc.field_1772.method_1727(text);
    }

    private String formatDuration(class_1293 effect) {
        if (effect.method_48559()) {
            return "\u221e";
        }
        int totalSeconds = Math.max(0, (effect.method_5584() + 19) / 20);
        if (totalSeconds >= 6000) {
            return "**:**";
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private class_2960 iconTexture(String descriptionId) {
        String rest;
        int dot;
        String namespace = "minecraft";
        String path = "speed";
        if (descriptionId != null && descriptionId.startsWith("effect.") && (dot = (rest = descriptionId.substring("effect.".length())).indexOf(46)) > 0 && dot < rest.length() - 1) {
            namespace = rest.substring(0, dot);
            path = rest.substring(dot + 1).replace('.', '/');
        }
        return class_2960.method_60655((String)namespace, (String)("textures/mob_effect/" + path + ".png"));
    }

    private void ensureEffectTexture(class_310 mc, class_2960 texture) {
        if (texture == null || LOADED_EFFECT_TEXTURES.contains(texture)) {
            return;
        }
        mc.method_1531().method_65876(texture, (class_10537)new class_1049(texture));
        LOADED_EFFECT_TEXTURES.add(texture);
    }

    private void drawEffectIcon(class_332 context, class_310 mc, class_2960 icon, float x, float y) {
        if (icon == null) {
            return;
        }
        class_1044 texture = mc.method_1531().method_4619(icon);
        if (texture == null || texture.method_71659() == null) {
            return;
        }
        GpuTextureView view = texture.method_71659();
        float textureWidth = Math.max(1, view.getWidth(0));
        float textureHeight = Math.max(1, view.getHeight(0));
        RenderUtil.drawHeadImageRegion(context, view, x, y, 18.0f, 18.0f, 0.0f, 0.0f, textureWidth, textureHeight, 0.0f, -1, 0.0f);
    }

    private void drawBackground(class_332 context, float x, float y, float width, float height, int color) {
        if (this.module.isBackgroundEnabled()) {
            if (this.module.isClientStyle()) {
                HudVisualStyle.drawGlassPanel(context, x, y, width, height, 1.0f, color);
            } else {
                context.method_25294(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
            }
        }
    }

    private record Row(class_2960 icon, String title, String time, int level, float width) {
    }
}

