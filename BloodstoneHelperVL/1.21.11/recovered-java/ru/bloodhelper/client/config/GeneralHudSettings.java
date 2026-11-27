/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import ru.bloodhelper.client.config.ClientConfig;

public final class GeneralHudSettings {
    public boolean enabled;
    public String keysStyle = "KEYCAPS";
    public Map<String, ClientConfig.WidgetSettings> widgets = new LinkedHashMap<String, ClientConfig.WidgetSettings>();

    public GeneralHudSettings() {
        for (Metric m : Metric.values()) {
            this.widgets.put(m.key(), GeneralHudSettings.defaults(m));
        }
    }

    private static ClientConfig.WidgetSettings defaults(Metric m) {
        ClientConfig.WidgetSettings s = new ClientConfig.WidgetSettings();
        s.enabled = m == Metric.FPS;
        s.position = new ClientConfig.HudPosition(0.02 + (double)(m.ordinal() / 4) * 0.2, 0.16 + (double)(m.ordinal() % 4) * 0.12);
        return s;
    }

    public ClientConfig.WidgetSettings get(Metric m) {
        return this.widgets.get(m.key());
    }

    public void sanitize() {
        this.keysStyle = "LETTERS".equals(this.keysStyle) ? "LETTERS" : "KEYCAPS";
        LinkedHashMap<String, ClientConfig.WidgetSettings> clean = new LinkedHashMap<String, ClientConfig.WidgetSettings>();
        for (Metric m : Metric.values()) {
            ClientConfig.WidgetSettings s;
            ClientConfig.WidgetSettings widgetSettings = s = this.widgets == null ? null : this.widgets.get(m.key());
            if (s == null) {
                s = GeneralHudSettings.defaults(m);
            }
            ClientConfig.sanitizeWidget(s, GeneralHudSettings.defaults(m));
            clean.put(m.key(), s);
        }
        this.widgets = clean;
    }

    public static enum Metric {
        FPS("FPS", "FPS"),
        CPS("CPS", "CPS"),
        RAM("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u041e\u0417\u0423", "Memory usage"),
        COMPASS("\u041a\u043e\u043c\u043f\u0430\u0441", "Compass"),
        PLAYTIME("\u0412\u0440\u0435\u043c\u044f \u0438\u0433\u0440\u044b", "Play time"),
        DAYS("\u0414\u0435\u043d\u044c \u043c\u0438\u0440\u0430", "World day"),
        KEYS("\u041d\u0430\u0436\u0430\u0442\u044b\u0435 \u043a\u043b\u0430\u0432\u0438\u0448\u0438", "Keystrokes"),
        SPEED("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f", "Movement speed");

        public final String ru;
        public final String en;

        private Metric(String ru, String en) {
            this.ru = ru;
            this.en = en;
        }

        public String key() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}

