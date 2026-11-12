/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.screen.screen.elements;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import pyz1k.module.impl.visuals.PvPCooldown;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.cooldown.CooldownHudUtil;

public final class PvPCooldownHudElement
extends AbstractHudElement {
    private static final float PANEL_HEIGHT = 18.0f;
    private static final float PADDING_X = 4.0f;
    private static final float CLIENT_FONT_SIZE = 7.0f;
    private final PvPCooldown module;

    public PvPCooldownHudElement(PvPCooldown module) {
        super("pvp_cooldown", "PvP Cooldown", 8.0f, 58.0f, 58.0f, 18.0f);
        this.module = module;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }
        Object cooldown = null;
        for (Map.Entry<Identifier, Object> entry : CooldownHudUtil.getCooldowns(mc).entrySet()) {
            if (!CooldownHudUtil.PVP_COOLDOWN.equals((Object)entry.getKey())) continue;
            cooldown = entry.getValue();
            break;
        }
        boolean editorPreview = this.canInteractInEditor(mc);
        if (cooldown == null && !editorPreview) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        String text = cooldown == null ? "PvP 5.0s" : String.format("PvP %.1fs", Float.valueOf(CooldownHudUtil.getRemainingSeconds(mc, cooldown)));
        boolean clientStyle = this.module.isClientStyle();
        float textWidth = clientStyle ? RenderUtil.getTextWidth(text, GFonts.MEDIUM, 7.0f) : (float)mc.font.width(text);
        float panelWidth = 8.0f + textWidth;
        float scale = this.module.getScale();
        this.setBounds(panelWidth * scale, 18.0f * scale);
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(this.getX(), this.getY());
        matrices.scale(scale, scale);
        this.drawBackground(context, 0.0f, 0.0f, panelWidth, 18.0f, clientStyle);
        if (clientStyle) {
            RenderUtil.drawText(context, text, 4.0f, 6.35f, HudVisualStyle.PRIMARY_TEXT, GFonts.MEDIUM, 7.0f);
        } else {
            context.text(mc.font, text, Math.round(4.0f), 5, -1, true);
        }
        matrices.popMatrix();
    }

    private void drawBackground(GuiGraphicsExtractor context, float x, float y, float width, float height, boolean clientStyle) {
        if (!this.module.isBackgroundEnabled()) {
            return;
        }
        int color = this.module.getBackgroundColor();
        if (clientStyle) {
            HudVisualStyle.drawGlassPanel(context, x, y, width, height, 1.0f, color);
        } else {
            context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
        }
    }
}

