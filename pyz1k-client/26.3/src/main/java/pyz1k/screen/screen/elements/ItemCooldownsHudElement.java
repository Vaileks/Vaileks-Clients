/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.screen.screen.elements;

import java.util.ArrayList;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;
import pyz1k.module.impl.visuals.ItemCooldownsModule;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.cooldown.CooldownHudUtil;

public final class ItemCooldownsHudElement
extends AbstractHudElement {
    private static final float PANEL_HEIGHT = 18.0f;
    private static final float ROW_GAP = 2.0f;
    private static final float PADDING_X = 4.0f;
    private static final float ITEM_SIZE = 16.0f;
    private static final float ITEM_TEXT_GAP = 4.0f;
    private static final float CLIENT_FONT_SIZE = 7.0f;
    private final ItemCooldownsModule module;

    public ItemCooldownsHudElement(ItemCooldownsModule module) {
        super("item_cooldowns", "Item Cooldowns", 8.0f, 34.0f, 58.0f, 18.0f);
        this.module = module;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }
        boolean clientStyle = this.module.isClientStyle();
        ArrayList<Row> rows = new ArrayList<Row>();
        float maxPanelWidth = 0.0f;
        for (Map.Entry<Identifier, Object> entry : CooldownHudUtil.getCooldowns(mc).entrySet()) {
            ItemStack stack;
            if (CooldownHudUtil.PVP_COOLDOWN.equals((Object)entry.getKey()) || (stack = CooldownHudUtil.findStackForCooldown(mc, entry.getKey())).isEmpty()) continue;
            String text = String.format("%.1fs", Float.valueOf(CooldownHudUtil.getRemainingSeconds(mc, entry.getValue())));
            float panelWidth = this.measurePanelWidth(mc, text, clientStyle);
            rows.add(new Row(stack, text, panelWidth));
            maxPanelWidth = Math.max(maxPanelWidth, panelWidth);
        }
        boolean editorPreview = this.canInteractInEditor(mc);
        if (rows.isEmpty() && editorPreview) {
            String preview = "1.0s";
            float panelWidth = this.measurePanelWidth(mc, preview, clientStyle);
            rows.add(new Row(pyz1k.compat.PreviewStacks.of((ItemLike)Items.ENDER_PEARL), preview, panelWidth));
            maxPanelWidth = panelWidth;
        }
        if (rows.isEmpty()) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        float scale = this.module.getScale();
        float unscaledHeight = (float)rows.size() * 18.0f + (float)Math.max(0, rows.size() - 1) * 2.0f;
        this.setBounds(maxPanelWidth * scale, unscaledHeight * scale);
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(this.getX(), this.getY());
        matrices.scale(scale, scale);
        float rowY = 0.0f;
        for (Row row : rows) {
            this.drawBackground(context, 0.0f, rowY, row.panelWidth(), 18.0f, clientStyle);
            context.item(row.stack(), Math.round(4.0f), Math.round(rowY + 0.6f));
            float textX = 24.0f;
            if (clientStyle) {
                RenderUtil.drawText(context, row.text(), textX, rowY + 6.45f, HudVisualStyle.PRIMARY_TEXT, GFonts.MEDIUM, 7.0f);
            } else {
                context.text(mc.font, row.text(), Math.round(textX), Math.round(rowY + 5.0f), -1, true);
            }
            rowY += 20.0f;
        }
        matrices.popMatrix();
    }

    private float measurePanelWidth(Minecraft mc, String text, boolean clientStyle) {
        float textWidth = clientStyle ? RenderUtil.getTextWidth(text, GFonts.MEDIUM, 7.0f) : (float)mc.font.width(text);
        return 28.0f + textWidth;
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

    private record Row(ItemStack stack, String text, float panelWidth) {
    }
}

