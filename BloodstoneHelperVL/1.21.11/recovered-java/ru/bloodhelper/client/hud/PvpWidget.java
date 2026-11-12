/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import net.minecraft.class_332;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.CooldownTracker;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;

public final class PvpWidget {
    private PvpWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, CooldownTracker.Entry entry, ClientConfig c) {
        ClientConfig.PvpSettings settings = c.pvp;
        String timer = Double.isFinite(entry.seconds()) ? String.format(Locale.ROOT, "%.1f", Math.max(0.0, entry.seconds())) + HudStyle.tr(c, "\u0441", "s") : "\u2014";
        int timerWidth = HudStyle.strongWidth(timer);
        int width = c.hudTitle("pvp") ? Math.max(86, 25 + HudStyle.strongWidth("PvP") + 12 + timerWidth + 8) : Math.max(46, timerWidth + 14);
        int textY = c.hudTitle("pvp") ? 6 : 2;
        int height = (c.hudTitle("pvp") ? 21 : 16) + (settings.showProgress ? 4 : 0);
        HudRenderer.begin(g, "pvp", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (c.background && settings.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("pvp")) {
                UiIcons.draw(g, "swords", 4, 4, 13, -4802625);
                HudStyle.separator(g, 19, 0, 21);
                HudStyle.strong(g, "PvP", 25, 6, -921101, settings.shadow);
            }
            HudStyle.strong(g, timer, c.hudTitle("pvp") ? width - 8 - timerWidth : (width - timerWidth) / 2, textY, -4802625, settings.shadow);
            if (settings.showProgress) {
                HudStyle.progress(g, "pvp", 7, height - 5, width - 14, entry.remaining(), c.accent);
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }
}

