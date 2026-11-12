/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.hud;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.AlertTracker;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.NotificationCardLayout;
import ru.bloodhelper.client.hud.NotificationPolicy;
import ru.bloodhelper.client.hud.Notifications;
import ru.bloodhelper.client.modules.CooldownReadyModule;

public final class NotificationWidget {
    private static final long TRANSITION_MS = 160L;
    private static final Map<String, VisualNotice> visuals = new LinkedHashMap<String, VisualNotice>();
    private static long lastFrameAt;
    private static Object world;
    private static Object configuration;
    private static String language;

    private NotificationWidget() {
    }

    public static void resetMotion() {
        visuals.clear();
        lastFrameAt = 0L;
        configuration = null;
        world = null;
        language = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig config, boolean preview) {
        List<AlertTracker.Notice> notices;
        ClientConfig.NotificationSettings settings = config.notifications;
        if (!(settings.enabled || FeatureAlerts.enabled(config) || preview)) {
            NotificationWidget.resetMotion();
            return;
        }
        long now = Notifications.now();
        ClientLevel currentWorld = Minecraft.getInstance().level;
        if (preview || world != currentWorld || configuration != config || !config.language.equals(language) || now < lastFrameAt || now - lastFrameAt > 500L) {
            NotificationWidget.resetMotion();
        }
        world = currentWorld;
        configuration = config;
        language = config.language;
        lastFrameAt = now;
        if (preview) {
            notices = NotificationWidget.examples(config);
        } else {
            ArrayList<AlertTracker.Notice> combined = new ArrayList<AlertTracker.Notice>();
            if (settings.enabled) {
                combined.addAll(Notifications.snapshot());
            }
            combined.addAll(FeatureAlerts.snapshot());
            combined.sort(Comparator.comparingLong(AlertTracker.Notice::createdAt));
            notices = combined;
            visuals.values().removeIf(v -> !FeatureAlerts.displayAllowed(v.notice.key(), config));
        }
        List<AlertTracker.Notice> selected = NotificationPolicy.select(notices, settings.maxVisible, config.language);
        ArrayList<DisplayNotice> displayed = new ArrayList<DisplayNotice>();
        if (preview || !config.animations) {
            visuals.clear();
            for (AlertTracker.Notice notice : selected) {
                displayed.add(new DisplayNotice(notice, 1.0));
            }
        } else {
            HashSet<CallSite> keys = new HashSet<CallSite>();
            for (AlertTracker.Notice notice : selected) {
                String key = notice.key() + ":" + notice.createdAt();
                keys.add((CallSite)((Object)key));
                VisualNotice visual2 = visuals.computeIfAbsent(key, ignored -> new VisualNotice(notice));
                visual2.notice = notice;
            }
            for (Map.Entry<String, VisualNotice> entry : visuals.entrySet()) {
                VisualNotice visual3 = entry.getValue();
                if (keys.contains(entry.getKey()) || visual3.removedAt >= 0L) continue;
                visual3.removedVisibility = visual3.visibility(now);
                visual3.removedAt = now;
            }
            visuals.values().removeIf(visual -> visual.removedAt >= 0L && now - visual.removedAt >= 160L);
            while (visuals.size() > 8) {
                visuals.remove(visuals.keySet().iterator().next());
            }
            for (VisualNotice visual4 : visuals.values()) {
                displayed.add(new DisplayNotice(visual4.notice, visual4.visibility(now)));
            }
            displayed.sort(Comparator.comparingInt(v -> NotificationPolicy.priority(v.notice)).reversed());
        }
        if (displayed.isEmpty()) {
            return;
        }
        ArrayList<NotificationCardLayout.Card> cards = new ArrayList<NotificationCardLayout.Card>(displayed.size());
        for (DisplayNotice display : displayed) {
            cards.add(NotificationCardLayout.card(display.notice, config.hudTitle("notifications"), HudStyle.strongWidth(NotificationCardLayout.line(display.notice)), display.visibility));
        }
        NotificationCardLayout.Stack stack = NotificationCardLayout.stack(cards, "CENTER", 214);
        if (stack.height() < 1) {
            return;
        }
        HudRenderer.begin(g, "notifications", settings.position, settings.alignment, settings.bottom, stack.width(), stack.height());
        try {
            for (int index = 0; index < displayed.size(); ++index) {
                DisplayNotice display = (DisplayNotice)displayed.get(index);
                NotificationCardLayout.Placement placement = stack.cards().get(index);
                AlertTracker.Notice notice = display.notice;
                double visibility = display.visibility;
                if (placement.clipHeight() <= 0) continue;
                int cardHeight = placement.height();
                int opacity = (int)(255.0 * visibility);
                int inset = placement.inset();
                g.pose().pushMatrix();
                g.pose().translate((float)placement.x() + (float)inset * 0.5f, (float)placement.y());
                g.enableScissor(0, 0, placement.width() - inset, placement.clipHeight());
                try {
                    int width = placement.width() - inset;
                    if (settings.background) {
                        HudStyle.surface(g, 0, 0, width, cardHeight, 4, config, visibility);
                    }
                    NotificationWidget.drawIcon(g, notice, preview, config, opacity);
                    HudStyle.strong(g, HudStyle.fitStrong(NotificationCardLayout.line(notice), width - 23 - 7), 23, 5, UiDraw.alpha(-921101, opacity), settings.shadow);
                    continue;
                }
                finally {
                    g.disableScissor();
                    g.pose().popMatrix();
                }
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    private static void drawIcon(GuiGraphicsExtractor g, AlertTracker.Notice notice, boolean preview, ClientConfig config, int opacity) {
        Optional effect;
        ItemStack item;
        ItemStack item2;
        if ((preview || notice.key().startsWith("test:")) && Minecraft.getInstance().level == null) {
            String icon = notice.icon() == null ? "info" : (notice.icon().startsWith("effect:") ? "effects" : (notice.icon().startsWith("item:") ? (notice.key().contains("armor") ? "armor" : "items") : (notice.icon().startsWith("cooldown:") ? "check" : notice.icon())));
            UiIcons.draw(g, icon, 5, 3, 14, UiDraw.alpha(config.accent, opacity));
            return;
        }
        String itemId = NotificationCardLayout.itemId(notice);
        if (!itemId.isEmpty() && !(item2 = Notifications.itemIcon(itemId, preview || notice.key().startsWith("test:"))).isEmpty()) {
            g.item(item2, 4, 2);
            return;
        }
        String group = NotificationCardLayout.payloadId(notice.icon(), "cooldown:");
        if (!group.isEmpty() && !(item = CooldownReadyModule.observedIcon(group)).isEmpty()) {
            g.item(item, 4, 2);
            return;
        }
        String effectId = NotificationCardLayout.payloadId(notice.icon(), "effect:");
        if (!effectId.isEmpty() && (effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse((String)effectId))).isPresent()) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite((Holder)((Holder)effect.get())), 5, 3, 14, 14);
            return;
        }
        String icon = !group.isEmpty() ? "check" : (!effectId.isEmpty() ? "effects" : (!itemId.isEmpty() ? "items" : notice.icon()));
        UiIcons.draw(g, icon == null ? "info" : icon, 5, 3, 14, UiDraw.alpha(config.accent, opacity));
    }

    private static double ease(double amount) {
        double t = Math.clamp(amount, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    private static List<AlertTracker.Notice> examples(ClientConfig config) {
        boolean ru = "ru".equals(config.language);
        return List.of(new AlertTracker.Notice("preview:armor", ru ? "\u0410\u043b\u043c\u0430\u0437\u043d\u044b\u0439 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a" : "Diamond Chestplate", ru ? "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c 18%" : "remaining 18%", "item:minecraft:diamond_chestplate", 0L, 4000L), NotificationCardLayout.itemExample(config.language, "preview:"), new AlertTracker.Notice("preview:effect", ru ? "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c" : "Speed", ru ? "\u0432\u044b\u0434\u0430\u043d\u043e \u043d\u0430 10\u0441" : "granted for 10s", "effect:minecraft:speed", 0L, 4000L));
    }

    private record DisplayNotice(AlertTracker.Notice notice, double visibility) {
    }

    private static final class VisualNotice {
        AlertTracker.Notice notice;
        long removedAt = -1L;
        double removedVisibility = 1.0;

        VisualNotice(AlertTracker.Notice notice) {
            this.notice = notice;
        }

        double visibility(long now) {
            return this.removedAt < 0L ? NotificationWidget.ease((double)(now - this.notice.createdAt()) / 160.0) : this.removedVisibility * (1.0 - NotificationWidget.ease((double)(now - this.removedAt) / 160.0));
        }
    }
}

