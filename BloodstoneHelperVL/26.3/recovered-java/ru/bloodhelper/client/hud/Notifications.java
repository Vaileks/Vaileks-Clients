/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
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
    private static final Map<String, ItemStack> itemIcons = new LinkedHashMap<String, ItemStack>(16, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ItemStack> entry) {
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

    public static void tick(Minecraft mc, ClientConfig config) {
        InventorySnapshot.Snapshot inventory;
        boolean warmingUp;
        if (!config.notifications.enabled) {
            Notifications.clear();
            return;
        }
        if (mc.player == null || mc.level == null) {
            Notifications.clearObserved();
            return;
        }
        if (!mc.player.isAlive()) {
            Notifications.clear();
            return;
        }
        if (worldIdentity != mc.level || playerIdentity != mc.player || connectionIdentity != mc.getConnection() || configIdentity != config || settingsIdentity != config.notifications) {
            Notifications.clearObserved();
            worldIdentity = mc.level;
            playerIdentity = mc.player;
            connectionIdentity = mc.getConnection();
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
                ItemStack observedIcon = inventory.icons().get(id);
                if (observedIcon != null && !observedIcon.isEmpty()) {
                    itemIcons.put(id, observedIcon);
                }
                if (!(crossed = tracker.observe(key, count = inventory.count(id), config.itemCounter.lowCount)) || warmingUp) continue;
                String body = NotificationCardLayout.itemBody(count, config.language);
                newNotice |= Notifications.push(config, key, "", body, "item:" + id, now);
            }
        }
        if (settings.effectWarnings) {
            HashSet<String> activeEffects = new HashSet<String>();
            for (MobEffectInstance effect : mc.player.getActiveEffects()) {
                if (!effect.isInfiniteDuration() && effect.getDuration() <= 0 || !((MobEffect)effect.getEffect().value()).isBeneficial()) continue;
                String id = BuiltInRegistries.MOB_EFFECT.getKey((Object)((MobEffect)effect.getEffect().value())).toString();
                String key = "effect:" + id;
                activeEffects.add(id);
                String name = UiLanguage.text(((MobEffect)effect.getEffect().value()).getDisplayName()) + (String)(effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "");
                EffectNoticeTracker.Change change = effectChanges.observe(id, effect.getDuration(), effect.getAmplifier(), effect.isInfiniteDuration(), warmingUp || !watchingEffects);
                if (effect.isInfiniteDuration() || effect.getDuration() > settings.effectSeconds * 20) {
                    tracker.dismiss(key + ":expires");
                }
                if (settings.effectUpdates && change != EffectNoticeTracker.Change.NONE) {
                    newNotice |= Notifications.push(config, key + ":granted", name, EffectNoticeTracker.body(change, effect.getDuration(), effect.isInfiniteDuration(), config.language), "effect:" + id, now);
                }
                if (effect.isInfiniteDuration()) continue;
                present.add(key);
                boolean crossed = tracker.observe(key, effect.getDuration(), (double)settings.effectSeconds * 20.0);
                if (!crossed || warmingUp) continue;
                String body = Notifications.tr(config, "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c ", "remaining ") + Math.max(1L, ((long)effect.getDuration() + 19L) / 20L) + Notifications.tr(config, " \u0441", " s");
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

    private static void playSound(Minecraft mc, ClientConfig config, long now) {
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
            Notifications.playSound(Minecraft.getInstance(), config, now);
        }
    }

    static ItemStack itemIcon(String id, boolean example) {
        ItemStack observed;
        if (!example && (observed = itemIcons.get(id)) != null && !observed.isEmpty()) {
            return observed;
        }
        Identifier identifier = Identifier.tryParse((String)id);
        return identifier == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.getOptional(identifier).map(ItemStack::new).orElse(ItemStack.EMPTY);
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

