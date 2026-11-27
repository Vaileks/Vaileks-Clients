/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    public static void render(GuiGraphicsExtractor g, ClientConfig c, boolean preview) {
        ClientConfig.EffectsSettings s = c.effects;
        LocalPlayer player = Minecraft.getInstance().player;
        ArrayList<MobEffectInstance> effects = new ArrayList<MobEffectInstance>(preview ? List.of(new MobEffectInstance(MobEffects.SPEED, 1630, 1), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4380), new MobEffectInstance(MobEffects.WEAKNESS, 420), new MobEffectInstance(MobEffects.NIGHT_VISION, -1)) : (player == null ? List.of() : player.getActiveEffects()));
        if (effects.isEmpty()) {
            return;
        }
        if (s.sortByTime) {
            effects.sort(Comparator.comparingInt(e -> e.isInfiniteDuration() ? Integer.MAX_VALUE : e.getDuration()));
        } else {
            effects.sort(Comparator.comparing(e -> UiLanguage.text((Component)Component.translatable((String)e.getDescriptionId()))));
        }
        int visible = Math.min(s.maxVisible, effects.size());
        int width = s.showNames ? 180 : 88;
        long now = System.nanoTime();
        boolean animate = c.animations && !preview && !(Minecraft.getInstance().gui.screen() instanceof ChatScreen);
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
            g.enableScissor(0, Math.max(0, contentY - 1), width, height);
            for (int i = 0; i < visible; ++i) {
                MobEffectInstance effect = (MobEffectInstance)effects.get(i);
                int y = (int)Math.round(motion.value(motionPrefix + "row." + effect.getDescriptionId(), contentY + i * 22, now, animate, RenderQuality.animationMillis(140.0)));
                g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite((Holder)effect.getEffect()), 6, y + 2, 16, 16);
                String time = EffectsWidget.duration(effect.getDuration(), effect.isInfiniteDuration());
                int timeX = width - 7 - (int)Math.ceil(CustomFont.regular().width(time, 9.5f));
                int color = !effect.isInfiniteDuration() && effect.getDuration() <= 200 ? -23125 : -4802625;
                HudStyle.text(g, time, timeX, y + 4, 9.5f, color, s.shadow);
                String level = s.showLevel && effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
                String name = s.showNames ? UiLanguage.text((Component)Component.translatable((String)effect.getDescriptionId())) + level : level.trim();
                HudStyle.text(g, CustomFont.regular().ellipsize(name, 10.5f, Math.max(0, timeX - 33)), 28, y + 3, 10.5f, -921101, s.shadow);
            }
            if (effects.size() > visible) {
                HudStyle.label(g, "+" + (effects.size() - visible), 7, contentY + 2 + visible * 22, -7763050, s.shadow);
            }
            g.disableScissor();
        }
        finally {
            g.pose().popMatrix();
        }
    }

    static String duration(int ticks, boolean infinite) {
        return EffectTime.format(ticks, infinite);
    }
}

