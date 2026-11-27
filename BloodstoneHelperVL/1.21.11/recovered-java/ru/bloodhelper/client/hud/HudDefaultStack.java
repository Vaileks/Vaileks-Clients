/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.hud.HudLayout;

public final class HudDefaultStack {
    public static final int GAP = 4;

    private HudDefaultStack() {
    }

    public static HudLayout.Placement music(ClientConfig c, boolean combat, HudLayout.Placement target, int watermarkBottom, int viewportHeight) {
        if (combat || !c.music.enabled || !c.watermark.enabled || watermarkBottom < 0 || c.music.position.x != 0.5 || c.music.position.y != 0.035 || c.watermark.position.x != 0.5 || c.watermark.position.y != 0.02 || !"CENTER".equals(c.watermark.alignment) || c.watermark.bottom) {
            return target;
        }
        int y = Math.max(target.y(), watermarkBottom + 4);
        int available = viewportHeight - y;
        if (available <= 0) {
            return target;
        }
        if (target.height() <= available) {
            return new HudLayout.Placement(target.x(), y, target.width(), target.height(), target.scale());
        }
        double fit = (double)available / (double)target.height();
        int width = Math.max(1, (int)Math.ceil((double)target.width() * fit));
        return new HudLayout.Placement(target.x() + (target.width() - width) / 2, y, width, available, target.scale() * fit);
    }

    public static HudLayout.Placement watermark(ClientConfig c, boolean combat, HudLayout.Placement target, HudLayout.Placement music, int viewportHeight) {
        if (combat || !c.music.enabled || !c.watermark.enabled || music == null || c.watermark.position.x != 0.5 || c.watermark.position.y != 0.02 || !"CENTER".equals(c.watermark.alignment) || c.watermark.bottom || c.music.position.x == 0.5 && c.music.position.y == 0.035 || !HudDefaultStack.intersects(target, music)) {
            return target;
        }
        int y = music.y() + music.height() + 4;
        int available = viewportHeight - y;
        if (available <= 0) {
            return target;
        }
        if (target.height() <= available) {
            return new HudLayout.Placement(target.x(), y, target.width(), target.height(), target.scale());
        }
        double fit = (double)available / (double)target.height();
        int width = Math.max(1, (int)Math.ceil((double)target.width() * fit));
        return new HudLayout.Placement(target.x() + (target.width() - width) / 2, y, width, available, target.scale() * fit);
    }

    private static boolean intersects(HudLayout.Placement a, HudLayout.Placement b) {
        return a.width() > 0 && a.height() > 0 && b.width() > 0 && b.height() > 0 && a.x() < b.x() + b.width() && b.x() < a.x() + a.width() && a.y() < b.y() + b.height() && b.y() < a.y() + a.height();
    }
}

