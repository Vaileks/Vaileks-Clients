/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.HudEdgeAnchor;
import ru.bloodhelper.client.config.HudEditorSettings;
import ru.bloodhelper.client.hud.HudLayout;

public final class AdaptiveHudLayout {
    private AdaptiveHudLayout() {
    }

    public static String key(String widget, boolean combat) {
        return (combat ? "combat:" : "normal:") + widget;
    }

    public static HudLayout.Placement place(HudEditorSettings settings, String widget, boolean combat, ClientConfig.HudPosition source, String alignment, boolean bottom, HudLayout.Placement natural, int screenWidth, int screenHeight) {
        if (!settings.adaptive) {
            return natural;
        }
        String key = AdaptiveHudLayout.key(widget, combat);
        HudEdgeAnchor anchor = settings.edgeAnchors.get(key);
        if (anchor == null || !AdaptiveHudLayout.matches(anchor, source, alignment, bottom)) {
            anchor = AdaptiveHudLayout.capture(source, alignment, bottom, natural, screenWidth, screenHeight);
            if (settings.edgeAnchors.size() < 576 || settings.edgeAnchors.containsKey(key)) {
                settings.edgeAnchors.put(key, anchor);
            }
        }
        double x = switch (anchor.horizontal) {
            case "RIGHT" -> (double)(screenWidth - natural.width()) - anchor.offsetX;
            case "CENTER" -> (double)(screenWidth - natural.width()) / 2.0 + anchor.offsetX;
            default -> anchor.offsetX;
        };
        double y = "BOTTOM".equals(anchor.vertical) ? (double)(screenHeight - natural.height()) - anchor.offsetY : anchor.offsetY;
        return new HudLayout.Placement(Math.clamp((long)((int)Math.round(x)), 0, Math.max(0, screenWidth - natural.width())), Math.clamp((long)((int)Math.round(y)), 0, Math.max(0, screenHeight - natural.height())), natural.width(), natural.height(), natural.scale());
    }

    private static boolean matches(HudEdgeAnchor a, ClientConfig.HudPosition p, String alignment, boolean bottom) {
        return a.valid() && Double.compare(a.sourceX, p.x) == 0 && Double.compare(a.sourceY, p.y) == 0 && Double.compare(a.sourceScale, p.scale) == 0 && a.sourceAlignment.equals(alignment) && a.sourceBottom == bottom;
    }

    private static HudEdgeAnchor capture(ClientConfig.HudPosition p, String alignment, boolean bottom, HudLayout.Placement box, int width, int height) {
        HudEdgeAnchor a = new HudEdgeAnchor();
        a.sourceX = p.x;
        a.sourceY = p.y;
        a.sourceScale = p.scale;
        a.sourceAlignment = alignment;
        a.sourceBottom = bottom;
        double center = (double)box.x() + (double)box.width() / 2.0;
        a.horizontal = "CENTER".equals(alignment) && Math.abs(center - (double)width / 2.0) <= (double)width * 0.15 ? "CENTER" : (center > (double)width / 2.0 ? "RIGHT" : "LEFT");
        a.vertical = bottom || (double)box.y() + (double)box.height() / 2.0 > (double)height / 2.0 ? "BOTTOM" : "TOP";
        a.offsetX = switch (a.horizontal) {
            case "RIGHT" -> width - box.x() - box.width();
            case "CENTER" -> center - (double)width / 2.0;
            default -> box.x();
        };
        a.offsetY = "BOTTOM".equals(a.vertical) ? (double)(height - box.y() - box.height()) : (double)box.y();
        return a;
    }
}

