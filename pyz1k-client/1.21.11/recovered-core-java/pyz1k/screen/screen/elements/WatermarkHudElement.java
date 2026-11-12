/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 *  net.minecraft.class_642
 */
package pyz1k.screen.screen.elements;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;
import net.minecraft.class_642;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.animation.AnimatedValue;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class WatermarkHudElement
extends AbstractHudElement {
    private static final float ROW_HEIGHT = 15.5f;
    private static final float TEXT_SIZE = 7.0f;
    private static final float ICON_SIZE = 6.5f;
    private static final float ICON_GAP = 2.2f;
    private static final float DIVIDER_GAP = 3.2f;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Interface interfaceModule;
    private final AnimatedValue rowWidthAnim = new AnimatedValue();
    private final AnimatedValue visibilityAnim = new AnimatedValue();
    private final AnimatedValue fpsAnim = new AnimatedValue();
    private final AnimatedValue pingAnim = new AnimatedValue();
    private final List<Segment> rowScratch = new ArrayList<Segment>(4);
    private boolean active = true;

    public WatermarkHudElement(Interface interfaceModule) {
        super("logo", "Watermark", 12.0f, 12.0f, 130.0f, 15.5f);
        this.interfaceModule = interfaceModule;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean shouldRenderWhenInactive() {
        return true;
    }

    @Override
    public void render(class_332 context, int screenWidth, int screenHeight, float tickDelta) {
        class_640 entry;
        long now;
        float visible = this.visibilityAnim.update(this.active ? 1.0f : 0.0f, 0.1f, now = System.nanoTime());
        if (visible <= 0.01f) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        class_310 mc = class_310.method_1551();
        int ping = 0;
        if (mc != null && mc.field_1724 != null && mc.method_1562() != null && (entry = mc.method_1562().method_2871(mc.field_1724.method_5667())) != null) {
            ping = Math.max(0, entry.method_2959());
        }
        float fps = mc != null ? (float)Math.max(0, mc.method_47599()) : 0.0f;
        String fpsText = Long.toString(Math.round(this.fpsAnim.update(fps, 0.05f, now)));
        String pingText = Long.toString(Math.round(this.pingAnim.update(ping, 0.05f, now)));
        String timeText = LocalTime.now().format(TIME_FORMATTER);
        String serverText = this.getServerAddress(mc);
        Set<String> selected = this.interfaceModule.getWatermarkContent();
        this.rowScratch.clear();
        if (selected.contains("FPS")) {
            this.rowScratch.add(this.unitSegment("textures/gui/icons/computer.png", fpsText, "fps"));
        }
        if (selected.contains("Ping")) {
            this.rowScratch.add(this.unitSegment("textures/gui/icons/wifi.png", pingText, "ms"));
        }
        if (selected.contains("Time")) {
            this.rowScratch.add(this.plainSegment("textures/gui/icons/time.png", timeText));
        }
        if (selected.contains("Server IP") && serverText != null && !serverText.isBlank()) {
            this.rowScratch.add(this.plainSegment("textures/gui/icons/cloud.png", serverText));
        }
        boolean clientStyle = this.interfaceModule.isClientStyle() || mc == null || mc.field_1772 == null;
        float rowTargetWidth = this.rowScratch.isEmpty() ? 0.0f : this.measureRow(this.rowScratch, mc, clientStyle);
        float rowWidth = this.rowWidthAnim.update(rowTargetWidth, 0.13f, now);
        if (rowWidth <= 0.5f) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        this.setBounds(rowWidth, 15.5f);
        float panelAlpha = Math.min(1.0f, visible / 0.34f);
        this.drawRow(context, this.rowScratch, this.getX(), this.getY(), rowWidth, visible, panelAlpha, mc, clientStyle);
    }

    private String getServerAddress(class_310 mc) {
        if (mc == null) {
            return null;
        }
        class_642 server = mc.method_1558();
        if (server != null && server.field_3761 != null && !server.field_3761.isBlank()) {
            return server.field_3761;
        }
        if (mc.method_1542()) {
            return "Singleplayer";
        }
        return null;
    }

    private float measureRow(List<Segment> segments, class_310 mc, boolean clientStyle) {
        float width = 10.0f;
        for (int i = 0; i < segments.size(); ++i) {
            Segment segment = segments.get(i);
            if (i > 0) {
                width += 7.05f;
            }
            if (segment.iconPath() != null) {
                width += 8.7f;
            }
            for (TextPart part : segment.parts()) {
                width += clientStyle ? RenderUtil.getTextWidth(part.text(), GFonts.MEDIUM, 7.0f) : (float)mc.field_1772.method_1727(part.text());
            }
        }
        return width;
    }

    private void drawRow(class_332 context, List<Segment> segments, float x, float y, float width, float alpha, float panelAlpha, class_310 mc, boolean clientStyle) {
        if (this.interfaceModule.isBackgroundEnabled()) {
            if (clientStyle) {
                HudVisualStyle.drawGlassPanel(context, x, y, width, 15.5f, panelAlpha, this.interfaceModule.getBackgroundColor());
            } else {
                context.method_25294(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + 15.5f), ColorUtil.multiplyAlpha(this.interfaceModule.getBackgroundColor(), panelAlpha));
            }
        }
        float cursor = x + 5.0f;
        float textY = y + 4.9f;
        float iconY = y + 4.5f;
        RenderUtil.pushScissor(context, x, y, width, 15.5f);
        for (int i = 0; i < segments.size(); ++i) {
            Segment segment = segments.get(i);
            if (i > 0) {
                RenderUtil.drawRoundRect(context, cursor += 3.2f, y + 3.75f, 0.65f, 8.0f, 0.33f, ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 34), alpha));
                cursor += 3.85f;
            }
            if (segment.iconPath() != null) {
                RenderUtil.drawImage(context, segment.iconPath(), cursor, iconY, 6.5f, 6.5f, 0.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha), 1.0f);
                cursor += 8.7f;
            }
            for (TextPart part : segment.parts()) {
                int textColor = ColorUtil.multiplyAlpha(part.secondary() ? HudVisualStyle.SECONDARY_TEXT : HudVisualStyle.PRIMARY_TEXT, alpha);
                if (clientStyle) {
                    RenderUtil.drawText(context, part.text(), cursor, textY, textColor, GFonts.MEDIUM, 7.0f);
                } else {
                    context.method_51433(mc.field_1772, part.text(), Math.round(cursor), Math.round(y + 4.0f), textColor, true);
                }
                cursor += clientStyle ? RenderUtil.getTextWidth(part.text(), GFonts.MEDIUM, 7.0f) : (float)mc.field_1772.method_1727(part.text());
            }
        }
        RenderUtil.popScissor();
    }

    private Segment plainSegment(String iconPath, String text) {
        return new Segment(iconPath, List.of(new TextPart(text, false)));
    }

    private Segment unitSegment(String iconPath, String value, String unit) {
        return new Segment(iconPath, List.of(new TextPart(value, false), new TextPart(unit, true)));
    }

    private record Segment(String iconPath, List<TextPart> parts) {
    }

    private record TextPart(String text, boolean secondary) {
    }
}

