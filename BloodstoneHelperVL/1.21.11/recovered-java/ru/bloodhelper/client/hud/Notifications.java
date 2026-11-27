/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.AlertTracker;
import ru.bloodhelper.client.hud.EffectNoticeTracker;
import ru.bloodhelper.client.hud.InventorySnapshot;
import ru.bloodhelper.client.hud.NotificationCardLayout;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.hud.NotificationSounds;

public final class Notifications {
    private static final AlertTracker tracker = new AlertTracker();
    private static final AlertTracker tests = new AlertTracker();
    private static final EffectNoticeTracker effectChanges = new EffectNoticeTracker();
    private static final Map<String, class_1799> itemIcons = new LinkedHashMap<String, class_1799>(16, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, class_1799> entry) {
            return this.size() > 32;
        }
    };
    private static Object worldIdentity;
    private static Object playerIdentity;
    private static Object connectionIdentity;
    private static Object configIdentity;
    private static Object settingsIdentity;
    private static long lastSoundAt;
    private static int warmupTicks;
    private static boolean watchingEffects;
    private static NotificationSoundSettings.Event pendingSound;

    private Notifications() {
    }

    static long now() {
        return System.nanoTime() / 1000000L;
    }

    public static void tick(class_310 mc, ClientConfig config) {
        InventorySnapshot.Snapshot inventory;
        boolean warmingUp;
        if (!config.notifications.enabled) {
            Notifications.clear();
            return;
        }
        if (mc.field_1724 == null || mc.field_1687 == null) {
            Notifications.clearObserved();
            return;
        }
        if (!mc.field_1724.method_5805()) {
            Notifications.clear();
            return;
        }
        if (worldIdentity != mc.field_1687 || playerIdentity != mc.field_1724 || connectionIdentity != mc.method_1562() || configIdentity != config || settingsIdentity != config.notifications) {
            Notifications.clearObserved();
            worldIdentity = mc.field_1687;
            playerIdentity = mc.field_1724;
            connectionIdentity = mc.method_1562();
            configIdentity = config;
            settingsIdentity = config.notifications;
            warmupTicks = 20;
        }
        boolean bl = warmingUp = warmupTicks > 0;
        if (warmingUp) {
            --warmupTicks;
        }
        long now = Notifications.now();
        ClientConfig.NotificationSettings settings = config.notifications;
        tracker.expire(now, 16);
        HashSet<String> present = new HashSet<String>();
        boolean newNotice = false;
        InventorySnapshot.Snapshot snapshot = inventory = settings.armorWarnings || settings.itemWarnings ? InventorySnapshot.capture() : null;
        if (settings.armorWarnings) {
            for (InventorySnapshot.ArmorPiece piece : inventory.armor()) {
                InventorySnapshot.Durability durability = piece.durability();
                if (!durability.damageable()) continue;
                String key = "armor:" + String.valueOf(piece.slot()) + ":" + piece.id() + ":" + durability.maximum();
                present.add(key);
                double percent = (double)durability.remaining() * 100.0 / (double)durability.maximum();
                boolean crossed = tracker.observe(key, percent, config.armor.warnPercent);
                if (!crossed || warmingUp) continue;
                itemIcons.put(piece.id(), piece.item());
                String body = Notifications.tr(config, "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c ", "remaining ") + durability.percent() + "%";
                newNotice |= Notifications.push(config, key, piece.displayName(), body, "item:" + piece.id(), now);
            }
        }
        if (settings.itemWarnings) {
            for (String id : config.itemCounter.items) {
                int count;
                boolean crossed;
                String key = "item:" + id;
                present.add(key);
                class_1799 observedIcon = inventory.icons().get(id);
                if (observedIcon != null && !observedIcon.method_7960()) {
                    itemIcons.put(id, observedIcon);
                }
                if (!(crossed = tracker.observe(key, count = inventory.count(id), config.itemCounter.lowCount)) || warmingUp) continue;
                String body = NotificationCardLayout.itemBody(count, config.language);
                newNotice |= Notifications.push(config, key, "", body, "item:" + id, now);
            }
        }
        if (settings.effectWarnings) {
            HashSet<String> activeEffects = new HashSet<String>();
            for (class_1293 effect : mc.field_1724.method_6026()) {
                if (!effect.method_48559() && effect.method_5584() <= 0 || !((class_1291)effect.method_5579().comp_349()).method_5573()) continue;
                String id = class_7923.field_41174.method_10221((Object)((class_1291)effect.method_5579().comp_349())).toString();
                String key = "effect:" + id;
                activeEffects.add(id);
                String name = UiLanguage.text(((class_1291)effect.method_5579().comp_349()).method_5560()) + (String)(effect.method_5578() > 0 ? " " + (effect.method_5578() + 1) : "");
                EffectNoticeTracker.Change change = effectChanges.observe(id, effect.method_5584(), effect.method_5578(), effect.method_48559(), warmingUp || !watchingEffects);
                if (effect.method_48559() || effect.method_5584() > settings.effectSeconds * 20) {
                    tracker.dismiss(key + ":expires");
                }
                if (settings.effectUpdates && change != EffectNoticeTracker.Change.NONE) {
                    newNotice |= Notifications.push(config, key + ":granted", name, EffectNoticeTracker.body(change, effect.method_5584(), effect.method_48559(), config.language), "effect:" + id, now);
                }
                if (effect.method_48559()) continue;
                present.add(key);
                boolean crossed = tracker.observe(key, effect.method_5584(), (double)settings.effectSeconds * 20.0);
                if (!crossed || warmingUp) continue;
                String body = Notifications.tr(config, "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c ", "remaining ") + Math.max(1L, ((long)effect.method_5584() + 19L) / 20L) + Notifications.tr(config, " \u0441", " s");
                newNotice |= Notifications.push(config, key + ":expires", name, body, "effect:" + id, now);
            }
            effectChanges.retain(activeEffects);
            watchingEffects = true;
        } else {
            effectChanges.clear();
            watchingEffects = false;
        }
        tracker.retainObservations(present);
        if (newNotice) {
            Notifications.playSound(mc, config, now);
        }
    }

    private static boolean push(ClientConfig config, String key, String title, String body, String icon, long now) {
        boolean added = tracker.push(key, title, body, icon, now, (long)config.notifications.durationSeconds * 1000L, 16);
        if (added) {
            NotificationInbox.record("BloodstoneHelperVL", title, body);
        }
        if (added) {
            pendingSound = key.startsWith("armor:") ? NotificationSoundSettings.Event.ARMOR : (key.startsWith("item:") ? NotificationSoundSettings.Event.ITEM : (key.startsWith("effect:") ? NotificationSoundSettings.Event.EFFECT : NotificationSoundSettings.Event.INVENTORY));
        }
        return added;
    }

    private static void playSound(class_310 mc, ClientConfig config, long now) {
        if (!config.notifications.sound || lastSoundAt != Long.MIN_VALUE && now - lastSoundAt < 1500L) {
            return;
        }
        lastSoundAt = now;
        NotificationSounds.play(mc, pendingSound, 0.85f, 0.18f);
    }

    public static void testNotification(ClientConfig config) {
        if (!config.notifications.enabled) {
            return;
        }
        long now = Notifications.now();
        AlertTracker.Notice example = config.notifications.itemWarnings ? NotificationCardLayout.itemExample(config.language, "test:") : new AlertTracker.Notice("test", Notifications.tr(config, "\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f", "Notifications"), Notifications.tr(config, "\u0422\u0430\u043a \u0431\u0443\u0434\u0443\u0442 \u0432\u044b\u0433\u043b\u044f\u0434\u0435\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f", "This is how notifications will appear"), "info", 0L, 0L);
        pendingSound = NotificationSoundSettings.Event.INVENTORY;
        if (tests.push(example.key(), example.title(), example.body(), example.icon(), now, (long)config.notifications.durationSeconds * 1000L, 1)) {
            Notifications.playSound(class_310.method_1551(), config, now);
        }
    }

    static class_1799 itemIcon(String id, boolean example) {
        class_1799 observed;
        if (!example && (observed = itemIcons.get(id)) != null && !observed.method_7960()) {
            return observed;
        }
        class_2960 identifier = class_2960.method_12829((String)id);
        return identifier == null ? class_1799.field_8037 : class_7923.field_41178.method_17966(identifier).map(class_1799::new).orElse(class_1799.field_8037);
    }

    public static List<AlertTracker.Notice> snapshot() {
        if (NotificationInbox.snoozed()) {
            return List.of();
        }
        long now = Notifications.now();
        ArrayList<AlertTracker.Notice> result = new ArrayList<AlertTracker.Notice>(tracker.snapshot(now));
        result.addAll(tests.snapshot(now));
        return List.copyOf(result);
    }

    public static void clear() {
        Notifications.clearObserved();
        tests.clear();
    }

    private static void clearObserved() {
        tracker.clear();
        effectChanges.clear();
        watchingEffects = false;
        itemIcons.clear();
        settingsIdentity = null;
        configIdentity = null;
        connectionIdentity = null;
        playerIdentity = null;
        worldIdentity = null;
        warmupTicks = 0;
        lastSoundAt = Long.MIN_VALUE;
    }

    private static String tr(ClientConfig c, String ru, String en) {
        return "ru".equals(c.language) ? ru : en;
    }

    static {
        lastSoundAt = Long.MIN_VALUE;
        pendingSound = NotificationSoundSettings.Event.INVENTORY;
    }
}

