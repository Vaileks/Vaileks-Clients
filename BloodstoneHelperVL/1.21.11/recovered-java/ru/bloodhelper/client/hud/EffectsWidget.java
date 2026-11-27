/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.EffectTime;
import ru.bloodhelper.client.hud.HudMotion;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;

public final class EffectsWidget {
    private static final float NAME_SIZE = 10.5f;
    private static final float TIME_SIZE = 9.5f;
    private static final HudMotion motion = new HudMotion();

    public static void resetMotion() {
        motion.reset();
    }

    private EffectsWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig c, boolean preview) {
        ClientConfig.EffectsSettings s = c.effects;
        class_746 player = class_310.method_1551().field_1724;
        ArrayList<class_1293> effects = new ArrayList<class_1293>(preview ? List.of(new class_1293(class_1294.field_5904, 1630, 1), new class_1293(class_1294.field_5918, 4380), new class_1293(class_1294.field_5911, 420), new class_1293(class_1294.field_5925, -1)) : (player == null ? List.of() : player.method_6026()));
        if (effects.isEmpty()) {
            return;
        }
        if (s.sortByTime) {
            effects.sort(Comparator.comparingInt(e -> e.method_48559() ? Integer.MAX_VALUE : e.method_5584()));
        } else {
            effects.sort(Comparator.comparing(e -> UiLanguage.text((class_2561)class_2561.method_43471((String)e.method_5586()))));
        }
        int visible = Math.min(s.maxVisible, effects.size());
        int width = s.showNames ? 180 : 88;
        long now = System.nanoTime();
        boolean animate = c.animations && !preview && !(class_310.method_1551().field_1755 instanceof class_408);
        int contentY = c.hudTitle("effects") ? 22 : 0;
        String motionPrefix = c.hudTitle("effects") ? "details." : "minimal.";
        int height = (int)Math.round(motion.value(motionPrefix + "height", contentY + visible * 22 + (effects.size() > visible ? 16 : 0), now, animate, RenderQuality.animationMillis(140.0)));
        HudRenderer.begin(g, "effects", s.position, s.alignment, s.bottom, width, height);
        try {
            if (s.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("effects")) {
                UiIcons.draw(g, "effects", 5, 4, 12, -4802625);
                HudStyle.strong(g, HudStyle.tr(c, "\u042d\u0444\u0444\u0435\u043a\u0442\u044b", "Effects"), 23, 3, -921101, s.shadow);
                HudStyle.divider(g, 6, 19, width - 12);
            }
            g.method_44379(0, Math.max(0, contentY - 1), width, height);
            for (int i = 0; i < visible; ++i) {
                class_1293 effect = (class_1293)effects.get(i);
                int y = (int)Math.round(motion.value(motionPrefix + "row." + effect.method_5586(), contentY + i * 22, now, animate, RenderQuality.animationMillis(140.0)));
                g.method_52706(class_10799.field_56883, class_329.method_71644((class_6880)effect.method_5579()), 6, y + 2, 16, 16);
                String time = EffectsWidget.duration(effect.method_5584(), effect.method_48559());
                int timeX = width - 7 - (int)Math.ceil(CustomFont.regular().width(time, 9.5f));
                int color = !effect.method_48559() && effect.method_5584() <= 200 ? -23125 : -4802625;
                HudStyle.text(g, time, timeX, y + 4, 9.5f, color, s.shadow);
                String level = s.showLevel && effect.method_5578() > 0 ? " " + (effect.method_5578() + 1) : "";
                String name = s.showNames ? UiLanguage.text((class_2561)class_2561.method_43471((String)effect.method_5586())) + level : level.trim();
                HudStyle.text(g, CustomFont.regular().ellipsize(name, 10.5f, Math.max(0, timeX - 33)), 28, y + 3, 10.5f, -921101, s.shadow);
            }
            if (effects.size() > visible) {
                HudStyle.label(g, "+" + (effects.size() - visible), 7, contentY + 2 + visible * 22, -7763050, s.shadow);
            }
            g.method_44380();
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    static String duration(int ticks, boolean infinite) {
        return EffectTime.format(ticks, infinite);
    }
}

