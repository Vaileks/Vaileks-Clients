/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_332;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.SessionTracker;

public final class SessionWidget {
    private static SessionTracker.Snapshot cachedData;
    private static String cachedLanguage;
    private static String cachedMode;
    private static int cachedAccent;
    private static int cachedFlags;
    private static List<Row> cachedRows;

    private SessionWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig c, boolean preview) {
        ClientConfig.SessionSettings s = c.session;
        SessionTracker.Snapshot data = preview ? new SessionTracker.Snapshot(s.mode, 12, 8, 3, 4, 1452L, true) : SessionTracker.snapshot();
        boolean ffa = "FFA".equals(s.mode);
        List<Row> rows = SessionWidget.rows(data, c);
        boolean waiting = !ffa && !data.killFeedAvailable();
        int contentY = c.hudTitle("session") ? 28 : 3;
        int width = 154;
        int height = contentY - 1 + rows.size() * 16 + (waiting ? 14 : 0);
        HudRenderer.begin(g, "session", s.position, s.alignment, s.bottom, width, height);
        try {
            if (c.background && s.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("session")) {
                UiIcons.draw(g, "stats", 6, 5, 13, -4802625);
                HudStyle.strong(g, HudStyle.tr(c, "\u0421\u0435\u0441\u0441\u0438\u044f", "Session"), 24, 6, -921101, s.shadow);
                String mode = "MEETUPS".equals(s.mode) ? HudStyle.tr(c, "\u041c\u0438\u0442\u0430\u043f\u044b", "Meetups") : s.mode;
                HudStyle.label(g, mode, width - 8 - HudStyle.labelWidth(mode), 7, -7763050, s.shadow);
            }
            int y = contentY;
            for (Row row : rows) {
                HudStyle.text(g, row.label(), 9, y, -4802625, s.shadow);
                HudStyle.strong(g, row.value(), width - 9 - HudStyle.strongWidth(row.value()), y, row.color(), s.shadow);
                y += 16;
            }
            if (waiting) {
                HudStyle.label(g, HudStyle.fitLabel(HudStyle.tr(c, "\u041d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445 \u043e\u0431 \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u0430\u0445", "Kill feed unavailable"), width - 18), 9, y, -7763050, s.shadow);
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static List<Row> rows(SessionTracker.Snapshot data, ClientConfig c) {
        ClientConfig.SessionSettings s = c.session;
        int flags = (s.showTime ? 1 : 0) | (s.showKd ? 2 : 0) | (s.showStreak ? 4 : 0);
        if (data.equals(cachedData) && Objects.equals(cachedLanguage, c.language) && Objects.equals(cachedMode, s.mode) && cachedAccent == c.accent && cachedFlags == flags) {
            return cachedRows;
        }
        boolean ffa = "FFA".equals(s.mode);
        ArrayList<Row> rows = new ArrayList<Row>();
        if (s.showTime) {
            rows.add(new Row(HudStyle.tr(c, "\u0412\u0440\u0435\u043c\u044f", "Time"), SessionWidget.duration(data.playSeconds()), -921101));
        }
        if (ffa) {
            rows.add(new Row(HudStyle.tr(c, "\u0412\u0441\u0435\u0433\u043e \u0443\u0431\u0438\u0439\u0441\u0442\u0432", "Total kills"), Integer.toString(data.kills()), -921101));
            rows.add(new Row(HudStyle.tr(c, "\u0421 \u043d\u0430\u0433\u0440\u0430\u0434\u043e\u0439", "Rewarded kills"), Integer.toString(data.rewardedKills()), c.accent));
        } else {
            rows.add(new Row(HudStyle.tr(c, "\u0423\u0431\u0438\u0439\u0441\u0442\u0432\u0430", "Kills"), data.killFeedAvailable() ? Integer.toString(data.kills()) : "\u2014", -921101));
            rows.add(new Row(HudStyle.tr(c, "\u0421\u043c\u0435\u0440\u0442\u0438", "Deaths"), Integer.toString(data.deaths()), -921101));
            if (s.showKd) {
                rows.add(new Row(HudStyle.tr(c, "\u0423/\u0421", "K/D"), data.killFeedAvailable() ? (data.deaths() == 0 ? (data.kills() == 0 ? "0.00" : "\u221e") : String.format(Locale.ROOT, "%.2f", (double)data.kills() / (double)data.deaths())) : "\u2014", -4802625));
            }
            if (s.showStreak) {
                rows.add(new Row(HudStyle.tr(c, "\u0421\u0435\u0440\u0438\u044f", "Streak"), data.killFeedAvailable() ? Integer.toString(data.streak()) : "\u2014", c.accent));
            }
        }
        cachedData = data;
        cachedLanguage = c.language;
        cachedMode = s.mode;
        cachedAccent = c.accent;
        cachedFlags = flags;
        cachedRows = List.copyOf(rows);
        return cachedRows;
    }

    private static String duration(long seconds) {
        seconds = Math.max(0L, seconds);
        long hours = seconds / 3600L;
        long minutes = seconds / 60L % 60L;
        String tail = (minutes < 10L ? "0" : "") + minutes + ":" + (seconds % 60L < 10L ? "0" : "") + seconds % 60L;
        return hours == 0L ? tail : hours + ":" + tail;
    }

    static {
        cachedRows = List.of();
    }

    private record Row(String label, String value, int color) {
    }
}

