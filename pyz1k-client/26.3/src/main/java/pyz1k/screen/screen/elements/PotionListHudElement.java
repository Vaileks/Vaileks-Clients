/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.ReloadableTexture
 *  net.minecraft.client.renderer.texture.SimpleTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.effect.MobEffectInstance
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.screen.screen.elements;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
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
    private static final Set<Identifier> LOADED_EFFECT_TEXTURES = new HashSet<Identifier>();
    private final PotionList module;

    public PotionListHudElement(PotionList module) {
        super("potion_list", "PotionList", 8.0f, 82.0f, 92.0f, 27.0f);
        this.module = module;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }
        boolean compact = this.module.isCompactStyle();
        ArrayList<Row> rows = new ArrayList<Row>();
        float maxWidth = 0.0f;
        if (mc.player != null) {
            for (MobEffectInstance effect : mc.player.getActiveEffects()) {
                String descriptionId = effect.getDescriptionId();
                String name = Component.translatable((String)descriptionId).getString();
                int level = Math.max(1, effect.getAmplifier() + 1);
                String title = name + " " + level;
                String time = this.formatDuration(effect);
                Identifier icon = this.iconTexture(descriptionId);
                this.ensureEffectTexture(mc, icon);
                float width = compact ? this.measureCompactWidth(mc, time) : this.measureClassicWidth(mc, title, time);
                rows.add(new Row(icon, title, time, level, width));
                maxWidth = Math.max(maxWidth, width);
            }
        }
        if (rows.isEmpty() && this.canInteractInEditor(mc)) {
            String title = "Скорость 2";
            String time = "01:30";
            int level = 2;
            float width = compact ? this.measureCompactWidth(mc, time) : this.measureClassicWidth(mc, title, time);
            Identifier icon = Identifier.fromNamespaceAndPath((String)"minecraft", (String)"textures/mob_effect/speed.png");
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
        Matrix3x2fStack matrices = context.pose();
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

    private void drawClassicRow(GuiGraphicsExtractor context, Minecraft mc, Row row, float rowY) {
        this.drawBackground(context, 0.0f, rowY, row.width(), 27.0f, this.module.getBackgroundColor());
        this.drawEffectIcon(context, mc, row.icon(), 5.0f, rowY + 4.5f);
        float textX = 28.0f;
        if (this.module.isClientStyle()) {
            RenderUtil.drawText(context, row.title(), textX, rowY + 5.0f, -1, GFonts.MEDIUM, 7.0f);
            RenderUtil.drawText(context, row.time(), textX, rowY + 15.2f, -1, GFonts.MEDIUM, 6.5f);
        } else {
            context.text(mc.font, row.title(), Math.round(textX), Math.round(rowY + 3.0f), -1, true);
            context.text(mc.font, row.time(), Math.round(textX), Math.round(rowY + 15.0f), -1, true);
        }
    }

    private void drawCompactRow(GuiGraphicsExtractor context, Minecraft mc, Row row, float rowY) {
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
            context.text(mc.font, levelText, Math.round(levelX), Math.round(levelY), -1, true);
        }
        float timeWidth = this.textWidth(mc, row.time(), GFonts.MEDIUM, 6.5f);
        float timeX = timerX + (timerWidth - timeWidth) * 0.5f;
        float timeY = timerY + 5.5f;
        if (this.module.isClientStyle()) {
            RenderUtil.drawText(context, row.time(), timeX + 0.5f, timeY, -1, GFonts.MEDIUM, 6.5f);
        } else {
            context.text(mc.font, row.time(), Math.round(timeX), Math.round(timerY + 4.5f), -1, true);
        }
    }

    private float measureClassicWidth(Minecraft mc, String title, String time) {
        float titleWidth = this.textWidth(mc, title, GFonts.MEDIUM, 7.0f);
        float timeWidth = this.textWidth(mc, time, GFonts.MEDIUM, 6.5f);
        return 33.0f + Math.max(titleWidth, timeWidth);
    }

    private float measureCompactWidth(Minecraft mc, String time) {
        float timerWidth = Math.max(29.0f, this.textWidth(mc, time, GFonts.MEDIUM, 6.5f) + 10.0f);
        return 29.0f + timerWidth;
    }

    private float textWidth(Minecraft mc, String text, GFonts font, float size) {
        return this.module.isClientStyle() ? RenderUtil.getTextWidth(text, font, size) : (float)mc.font.width(text);
    }

    private String formatDuration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return "∞";
        }
        int totalSeconds = Math.max(0, (effect.getDuration() + 19) / 20);
        if (totalSeconds >= 6000) {
            return "**:**";
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private Identifier iconTexture(String descriptionId) {
        String rest;
        int dot;
        String namespace = "minecraft";
        String path = "speed";
        if (descriptionId != null && descriptionId.startsWith("effect.") && (dot = (rest = descriptionId.substring("effect.".length())).indexOf(46)) > 0 && dot < rest.length() - 1) {
            namespace = rest.substring(0, dot);
            path = rest.substring(dot + 1).replace('.', '/');
        }
        return Identifier.fromNamespaceAndPath((String)namespace, (String)("textures/mob_effect/" + path + ".png"));
    }

    private void ensureEffectTexture(Minecraft mc, Identifier texture) {
        if (texture == null || LOADED_EFFECT_TEXTURES.contains(texture)) {
            return;
        }
        mc.getTextureManager().registerAndLoad(texture, (ReloadableTexture)new SimpleTexture(texture));
        LOADED_EFFECT_TEXTURES.add(texture);
    }

    private void drawEffectIcon(GuiGraphicsExtractor context, Minecraft mc, Identifier icon, float x, float y) {
        if (icon == null) {
            return;
        }
        AbstractTexture texture = mc.getTextureManager().getTexture(icon);
        if (texture == null || texture.getTextureView() == null) {
            return;
        }
        GpuTextureView view = texture.getTextureView();
        float textureWidth = Math.max(1, view.getWidth(0));
        float textureHeight = Math.max(1, view.getHeight(0));
        RenderUtil.drawHeadImageRegion(context, view, x, y, 18.0f, 18.0f, 0.0f, 0.0f, textureWidth, textureHeight, 0.0f, -1, 0.0f);
    }

    private void drawBackground(GuiGraphicsExtractor context, float x, float y, float width, float height, int color) {
        if (this.module.isBackgroundEnabled()) {
            if (this.module.isClientStyle()) {
                HudVisualStyle.drawGlassPanel(context, x, y, width, height, 1.0f, color);
            } else {
                context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
            }
        }
    }

    private record Row(Identifier icon, String title, String time, int level, float width) {
    }
}

