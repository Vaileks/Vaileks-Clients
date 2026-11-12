/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package ru.bloodhelper.client.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.SizeTimerSettings;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.SizeTimerModule;
import ru.bloodhelper.client.modules.SizeTimerState;

public final class SizeTimerWidget {
    private SizeTimerWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig config, boolean preview) {
        String time;
        SizeTimerState.Snapshot state;
        SizeTimerState.Snapshot snapshot = state = preview ? new SizeTimerState.Snapshot(SizeTimerState.Size.ENLARGED, 18.0, 0.6f, true, false) : SizeTimerModule.snapshot();
        if (state == null) {
            return;
        }
        SizeTimerSettings settings = config.sizeTimer;
        String name = state.size() == SizeTimerState.Size.ENLARGED ? HudStyle.tr(config, "\u0422\u044b \u0443\u0432\u0435\u043b\u0438\u0447\u0435\u043d", "You are enlarged") : HudStyle.tr(config, "\u0422\u044b \u0443\u043c\u0435\u043d\u044c\u0448\u0435\u043d", "You are reduced");
        String string = time = Double.isFinite(state.seconds()) ? "\u2248 " + (int)Math.ceil(state.seconds()) + HudStyle.tr(config, " \u0441", " s") : "\u2014";
        String detail = state.overdue() ? HudStyle.tr(config, "\u041e\u0436\u0438\u0434\u0430\u044e \u043e\u0431\u044b\u0447\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440", "Waiting for normal size") : (!state.estimated() ? HudStyle.tr(config, "\u041d\u0430\u0447\u0430\u043b\u043e \u044d\u0444\u0444\u0435\u043a\u0442\u0430 \u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e", "Effect start is unknown") : HudStyle.tr(config, "\u0414\u043e \u043e\u0431\u044b\u0447\u043d\u043e\u0433\u043e \u0440\u0430\u0437\u043c\u0435\u0440\u0430", "Until normal size"));
        int width = Math.max(180, 36 + HudStyle.strongWidth(name) + HudStyle.strongWidth(time));
        int height = config.hudTitle("sizeTimer") ? 42 : 25;
        HudRenderer.begin(g, "sizeTimer", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (config.background && settings.background) {
                HudStyle.panel(g, width, height, config);
            }
            UiIcons.draw(g, "clock", 6, 6, 12, config.accent);
            HudStyle.strong(g, name, 24, 5, -921101, settings.shadow);
            HudStyle.strong(g, time, width - 8 - HudStyle.strongWidth(time), 5, -4802625, settings.shadow);
            if (config.hudTitle("sizeTimer")) {
                HudStyle.label(g, detail, 24, 22, -4802625, settings.shadow);
            }
            if (settings.showProgress && state.estimated() && !state.overdue()) {
                HudStyle.progress(g, "sizeTimer", 8, height - 4, width - 16, state.remaining(), config.accent);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }
}

