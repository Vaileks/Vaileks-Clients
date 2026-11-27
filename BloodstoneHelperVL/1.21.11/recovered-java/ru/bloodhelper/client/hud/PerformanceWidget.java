/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import net.minecraft.class_332;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.PerformanceTracker;

public final class PerformanceWidget {
    private static final PerformanceTracker.Snapshot PREVIEW = PerformanceWidget.createPreview();

    private PerformanceWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig c, boolean preview) {
        ClientConfig.PerformanceSettings s = c.performance;
        PerformanceTracker.Snapshot sample = preview ? PREVIEW : PerformanceTracker.snapshot(s.showGraph);
        ArrayList<Metric> metrics = new ArrayList<Metric>(3);
        if (s.showFps) {
            metrics.add(new Metric("fps", "FPS", sample.fps() < 0 ? "\u2014" : Integer.toString(sample.fps())));
        }
        String milliseconds = HudStyle.tr(c, " \u043c\u0441", " ms");
        if (s.showPing) {
            metrics.add(new Metric("ping", HudStyle.tr(c, "\u041f\u0438\u043d\u0433", "Ping"), (String)(sample.pingMs() < 0 ? "\u2014" : sample.pingMs() + milliseconds)));
        }
        if (s.showFrameTime) {
            metrics.add(new Metric("frame", HudStyle.tr(c, "\u041a\u0430\u0434\u0440", "Frame"), (String)(Double.isFinite(sample.frameTimeMs()) ? PerformanceWidget.frameTime(sample.frameTimeMs()) + milliseconds : "\u2014")));
        }
        if (c.simpleHud) {
            if (metrics.isEmpty()) {
                return;
            }
            if ("SEPARATE".equals(s.layout)) {
                for (Metric metric : metrics) {
                    String text = metric.label() + " " + metric.value();
                    HudRenderer.begin(g, "performance." + metric.key(), s.partPosition(metric.key()), "LEFT", false, HudStyle.width(text) + 4, 14);
                    try {
                        HudStyle.text(g, text, 2, 2, -921101, s.shadow);
                    }
                    finally {
                        g.method_51448().popMatrix();
                    }
                }
            } else {
                String text = String.join((CharSequence)"  \u00b7  ", metrics.stream().map(m -> m.label() + " " + m.value()).toList());
                HudRenderer.begin(g, "performance", s.position, s.alignment, s.bottom, HudStyle.width(text) + 4, 14);
                try {
                    HudStyle.text(g, text, 2, 2, -921101, s.shadow);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            return;
        }
        if ("SEPARATE".equals(s.layout)) {
            for (Metric metric : metrics) {
                String key = "performance." + metric.key();
                boolean title = c.hudTitle(key) && c.hudTitle("performance");
                int w = Math.max(72, Math.max(HudStyle.strongWidth(metric.value()), title ? HudStyle.strongWidth(metric.label()) : 0) + 16);
                int h = title ? 38 : 24;
                HudRenderer.begin(g, key, s.partPosition(metric.key()), "LEFT", false, w, h);
                try {
                    if (c.background && s.background) {
                        HudStyle.panel(g, w, h, c);
                    }
                    if (title) {
                        HudStyle.label(g, metric.label(), 8, 6, -4802625, s.shadow);
                    }
                    HudStyle.strong(g, metric.value(), 8, title ? 21 : 7, -921101, s.shadow);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            if (s.showGraph) {
                HudRenderer.begin(g, "performance.graph", s.partPosition("graph"), "LEFT", false, 152, 40);
                try {
                    if (c.background && s.background) {
                        HudStyle.panel(g, 152, 40, c);
                    }
                    PerformanceWidget.drawGraph(g, sample.frames(), 6, 7, 140, 26, -6907228, false);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            return;
        }
        boolean compact = g.method_51421() < 550;
        int width = compact ? 118 : Math.max(136, metrics.size() * 57 + 14);
        String title = HudStyle.tr(c, "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "Performance");
        if (c.hudTitle("performance")) {
            width = Math.max(width, HudStyle.strongWidth(title) + 32);
        }
        if (!c.hudTitle("performance") && metrics.isEmpty() && !s.showGraph) {
            return;
        }
        int metricsHeight = compact ? metrics.size() * 15 : (metrics.isEmpty() ? 0 : 31);
        int contentY = c.hudTitle("performance") ? 26 : 3;
        int height = contentY - 1 + metricsHeight + (s.showGraph ? 31 : 0);
        HudRenderer.begin(g, "performance", s.position, s.alignment, s.bottom, width, height);
        try {
            if (c.background && s.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("performance")) {
                UiIcons.draw(g, "performance", 6, 5, 13, -4802625);
                HudStyle.strong(g, title, 24, 6, -921101, s.shadow);
            }
            int y = contentY;
            for (int i = 0; i < metrics.size(); ++i) {
                int x = compact ? 8 : 8 + i * ((width - 16) / metrics.size());
                Metric m2 = (Metric)metrics.get(i);
                HudStyle.label(g, m2.label(), x, compact ? y + i * 15 : y, -7763050, s.shadow);
                String value = HudStyle.fitStrong(m2.value(), compact ? 67 : (width - 16) / metrics.size() - 4);
                HudStyle.strong(g, value, compact ? width - 8 - HudStyle.strongWidth(value) : x, compact ? y + i * 15 : y + 12, -921101, s.shadow);
            }
            if (s.showGraph) {
                PerformanceWidget.drawGraph(g, sample.frames(), 8, contentY + 2 + metricsHeight, width - 16, 22, -6907228, c.hudTitle("performance"));
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static void drawGraph(class_332 g, double[] values, int x, int y, int width, int height, int color, boolean background) {
        if (background) {
            UiDraw.rounded(g, x, y, width, height, 3, 1143153194);
        }
        if (values.length == 0) {
            HudStyle.label(g, "\u2014", x + width / 2 - 3, y + 6, -7763050, false);
            return;
        }
        double max = 33.333;
        for (double v : values) {
            max = Math.max(max, v);
        }
        int baseline = y + height - 2;
        int reference = baseline - (int)Math.round((double)(height - 4) * 16.667 / max);
        g.method_25294(x + 2, reference, x + width - 2, reference + 1, 1146967648);
        int runStart = 0;
        int runHeight = -1;
        int fill = UiDraw.alpha(color, 160);
        for (int column = 0; column < width - 4; ++column) {
            int index = (int)((long)column * (long)values.length / (long)(width - 4));
            int next = Math.max(index + 1, (int)((long)(column + 1) * (long)values.length / (long)(width - 4)));
            double peak = values[index];
            for (int i = index + 1; i < Math.min(next, values.length); ++i) {
                peak = Math.max(peak, values[i]);
            }
            int h = Math.max(1, (int)Math.round((double)(height - 4) * peak / max));
            if (h == runHeight) continue;
            if (runHeight >= 0) {
                PerformanceWidget.graphRun(g, x + 2 + runStart, x + 2 + column, baseline, runHeight, fill, color);
            }
            runStart = column;
            runHeight = h;
        }
        if (runHeight >= 0) {
            PerformanceWidget.graphRun(g, x + 2 + runStart, x + width - 2, baseline, runHeight, fill, color);
        }
    }

    private static void graphRun(class_332 g, int left, int right, int baseline, int height, int fill, int color) {
        g.method_25294(left, baseline - height, right, baseline, fill);
        g.method_25294(left, baseline - height, right, baseline - height + 1, color);
    }

    private static String frameTime(double milliseconds) {
        long tenths = Math.round(milliseconds * 10.0);
        return tenths / 10L + "." + tenths % 10L;
    }

    private static PerformanceTracker.Snapshot createPreview() {
        double[] values = new double[90];
        for (int i = 0; i < values.length; ++i) {
            values[i] = 6.2 + Math.sin((double)i * 0.35) * 0.7 + (double)(i == 62 ? 6 : 0);
        }
        return new PerformanceTracker.Snapshot(144, 42, 6.9, values);
    }

    private record Metric(String key, String label, String value) {
    }
}

