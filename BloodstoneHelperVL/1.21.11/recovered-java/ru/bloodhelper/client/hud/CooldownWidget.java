/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.hud;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.CooldownTracker;
import ru.bloodhelper.client.hud.HudLayout;
import ru.bloodhelper.client.hud.HudMotion;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CombatHudModule;

public final class CooldownWidget {
    private static final int HEADER = 21;
    private static final int ROW = 23;
    private static final int BOTTOM = 0;
    private static final int MORE = 15;
    private static final int MAX_ROWS = 12;
    private static final int TEXT_X = 25;
    private static final int RIGHT = 7;
    private static final int COMPACT_HEIGHT = 22;
    private static final int COMPACT_STEP = 25;
    private static final int COMPACT_TEXT_X = 24;
    private static final HudMotion motion = new HudMotion();

    public static void resetMotion() {
        motion.reset();
    }

    private CooldownWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, List<CooldownTracker.Entry> entries, ClientConfig c) {
        boolean overflow;
        if (entries.isEmpty()) {
            return;
        }
        if (!"DETAILED".equals(c.cooldowns.layout)) {
            CooldownWidget.renderCompact(g, entries, c);
            return;
        }
        ClientConfig.CooldownSettings settings = c.cooldowns;
        String title = HudStyle.tr(c, "\u041f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438", "Cooldowns");
        String total = Integer.toString(entries.size());
        int width = 86;
        for (CooldownTracker.Entry entry : entries) {
            int nameWidth = settings.showNames ? Math.min(112, HudStyle.width(CooldownWidget.label(entry))) + 8 : 0;
            width = Math.max(width, 25 + nameWidth + Math.max(HudStyle.strongWidth(CooldownWidget.time(entry.seconds())), HudStyle.strongWidth(CooldownWidget.time(99.9))) + 7);
        }
        if (c.hudTitle("cooldowns")) {
            width = Math.max(width, HudStyle.width(title) + HudStyle.labelWidth(total) + 38);
        }
        int headerHeight = c.hudTitle("cooldowns") ? 21 : 0;
        String motionPrefix = c.hudTitle("cooldowns") ? "details." : "minimal.";
        ClientConfig.HudPosition position = CombatHudModule.combatLayout(c) ? CombatHudModule.pose((String)"cooldowns", (ClientConfig.HudPosition)settings.position, (String)settings.alignment, (boolean)settings.bottom, (ClientConfig)c).position : settings.position;
        double scale = HudLayout.scale(g.method_51421(), g.method_51443(), width, 1, position.scale);
        int available = (int)Math.floor((double)Math.max(1, g.method_51443() - 16) / scale);
        int capacity = Math.max(1, Math.min(12, (available - headerHeight - 0) / 23));
        boolean bl = overflow = entries.size() > capacity;
        if (overflow) {
            capacity = Math.max(1, Math.min(12, (available - headerHeight - 0 - 15) / 23));
        }
        int count = Math.min(entries.size(), capacity);
        int hidden = entries.size() - count;
        long now = System.nanoTime();
        class_437 screen = class_310.method_1551().field_1755;
        boolean animate = c.animations && !(screen instanceof HudEditorScreen) && !(screen instanceof class_408);
        int height = (int)Math.round(motion.value(motionPrefix + "height", headerHeight + count * 23 + 0 + (hidden > 0 ? 15 : 0), now, animate, RenderQuality.animationMillis(140.0)));
        HudRenderer.begin(g, "cooldowns", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (c.hudTitle("cooldowns")) {
                if (settings.background) {
                    HudStyle.surface(g, 0, 0, width, 18, 6, c);
                }
                CooldownWidget.clock(g, 5, 4, -4802625);
                HudStyle.separator(g, 18, 0, 18);
                HudStyle.strong(g, title, 25, 5, -921101, settings.shadow);
                HudStyle.label(g, total, width - 7 - HudStyle.labelWidth(total), 5, -7763050, settings.shadow);
            }
            g.method_44379(0, headerHeight, width, height);
            for (int i = 0; i < count; ++i) {
                CooldownTracker.Entry entry = entries.get(i);
                int y = (int)Math.round(motion.value(motionPrefix + "row." + String.valueOf(entry.group()), headerHeight + i * 23, now, animate, RenderQuality.animationMillis(140.0)));
                if (settings.background) {
                    HudStyle.surface(g, 0, y, width, 20, 5, c);
                }
                if (entry.known() && !entry.item().method_7960()) {
                    g.method_51448().pushMatrix();
                    g.method_51448().translate(4.0f, (float)(y + 3));
                    g.method_51448().scale(0.75f, 0.75f);
                    g.method_51427(entry.item(), 0, 0);
                    g.method_51448().popMatrix();
                } else {
                    CooldownWidget.clock(g, 5, y + 4, -4802625);
                }
                if (c.hudTitle("cooldowns")) {
                    HudStyle.separator(g, 19, y, 20);
                }
                String remaining = CooldownWidget.time(entry.seconds());
                int timerX = width - 7 - HudStyle.strongWidth(remaining);
                int textY = y + 3;
                if (settings.showNames) {
                    HudStyle.text(g, HudStyle.fit(CooldownWidget.label(entry), Math.max(0, timerX - 25 - 6)), 25, textY, -921101, settings.shadow);
                }
                HudStyle.strong(g, remaining, timerX, textY, -4802625, settings.shadow);
                if (!settings.showProgress) continue;
                HudStyle.progress(g, "cooldown." + String.valueOf(entry.group()), 25, y + 17, width - 25 - 7, entry.remaining(), c.accent);
            }
            if (hidden > 0) {
                if (settings.background) {
                    HudStyle.surface(g, 0, headerHeight + count * 23, width, 15, 5, c);
                }
                String more = HudStyle.tr("\u0415\u0449\u0451 ", "+ ") + hidden;
                if (!"ru".equals(c.language)) {
                    more = more + " more";
                }
                HudStyle.label(g, HudStyle.fitLabel(more, width - 18), 9, headerHeight + count * 23 + 1, -4802625, settings.shadow);
            }
            g.method_44380();
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderCompact(class_332 g, List<CooldownTracker.Entry> entries, ClientConfig c) {
        ClientConfig.CooldownSettings settings = c.cooldowns;
        int width = 0;
        for (CooldownTracker.Entry entry : entries) {
            width = Math.max(width, CooldownWidget.compactWidth(entry));
        }
        ClientConfig.HudPosition position = settings.position;
        String alignment = settings.alignment;
        if (CombatHudModule.combatLayout(c)) {
            CombatHudSettings.Pose pose = CombatHudModule.pose("cooldowns", position, alignment, settings.bottom, c);
            position = pose.position;
            alignment = pose.alignment;
        }
        double scale = HudLayout.scale(g.method_51421(), g.method_51443(), width, 1, position.scale);
        int available = (int)Math.floor((double)Math.max(1, g.method_51443() - 16) / scale);
        int capacity = Math.max(1, Math.min(12, (available + 25 - 22) / 25));
        if (entries.size() > capacity) {
            capacity = Math.max(1, Math.min(12, (available - 15) / 25));
        }
        int count = Math.min(entries.size(), capacity);
        int hidden = entries.size() - count;
        String more = HudStyle.tr(c, "\u0415\u0449\u0451 ", "+ ") + hidden + ("ru".equals(c.language) ? "" : " more");
        int moreWidth = HudStyle.labelWidth(more) + 12;
        if (hidden > 0) {
            width = Math.max(width, moreWidth);
        }
        long now = System.nanoTime();
        class_437 screen = class_310.method_1551().field_1755;
        boolean animate = c.animations && !(screen instanceof HudEditorScreen) && !(screen instanceof class_408);
        int targetHeight = count * 25 - 3 + (hidden > 0 ? 18 : 0);
        int height = (int)Math.round(motion.value("compact.height", targetHeight, now, animate, RenderQuality.animationMillis(140.0)));
        HudRenderer.begin(g, "cooldowns", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            g.method_44379(0, 0, width, height);
            for (int i = 0; i < count; ++i) {
                CooldownTracker.Entry entry = entries.get(i);
                int pillWidth = CooldownWidget.compactWidth(entry);
                int x = CooldownWidget.alignedX(width, pillWidth, alignment);
                int y = (int)Math.round(motion.value("compact.row." + String.valueOf(entry.group()), i * 25, now, animate, RenderQuality.animationMillis(140.0)));
                if (settings.background) {
                    HudStyle.surface(g, x, y, pillWidth, 22, 6, c);
                }
                if (entry.known() && !entry.item().method_7960()) {
                    g.method_51427(entry.item(), x + 3, y + 3);
                } else {
                    UiIcons.draw(g, "clock", x + 4, y + 4, 14, -4802625);
                }
                HudStyle.strong(g, CooldownWidget.time(entry.seconds()), x + 24, y + 6, -921101, settings.shadow);
            }
            if (hidden > 0) {
                int x = CooldownWidget.alignedX(width, moreWidth, alignment);
                int y = count * 25;
                if (settings.background) {
                    HudStyle.surface(g, x, y, moreWidth, 15, 5, c);
                }
                HudStyle.label(g, more, x + 6, y + 3, -4802625, settings.shadow);
            }
        }
        finally {
            g.method_44380();
            g.method_51448().popMatrix();
        }
    }

    private static int compactWidth(CooldownTracker.Entry entry) {
        return 24 + HudStyle.strongWidth(CooldownWidget.time(entry.seconds())) + 6;
    }

    private static int alignedX(int width, int rowWidth, String alignment) {
        return "RIGHT".equals(alignment) ? width - rowWidth : ("CENTER".equals(alignment) ? (width - rowWidth) / 2 : 0);
    }

    private static String label(CooldownTracker.Entry entry) {
        if (entry.label() != null && !entry.label().isBlank()) {
            return entry.label();
        }
        return entry.group() == null ? HudStyle.tr("\u041f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0430", "Cooldown") : entry.group().method_12832().replace('_', ' ');
    }

    private static void clock(class_332 g, int x, int y, int color) {
        UiIcons.draw(g, "clock", x - 1, y - 1, 12, color);
    }

    private static String time(double seconds) {
        return Double.isFinite(seconds) ? String.format(Locale.ROOT, "%.1f", Math.max(0.0, seconds)) + HudStyle.tr("\u0441", "s") : "\u2014";
    }
}

