/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.protocol.game.ClientboundDamageEventPacket
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.PotionItem
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.LevelResource
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  pyz1k.extensions.ScreenshotMode
 */
package pyz1k.extensions.pvp;

import pyz1k.extensions.pvp.Config;
import pyz1k.extensions.pvp.Feature;
import pyz1k.extensions.pvp.HubScreen;
import pyz1k.extensions.pvp.NotesScreen;
import pyz1k.extensions.pvp.PvpModules;
import pyz1k.extensions.pvp.ReportScreen;
import pyz1k.extensions.pvp.SettingsScreen;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import pyz1k.compat.LegacyInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyz1k.extensions.ScreenshotMode;

public final class Core
implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger((String)"vaileks-pvp-tools");
    public static final EnumMap<Feature, Config> roots = new EnumMap(Feature.class);
    static final EnumMap<Feature, List<Row>> rows = new EnumMap(Feature.class);
    static final Map<Integer, Boolean> keys = new HashMap<Integer, Boolean>();
    static final Map<Identifier, ItemStack> cooldowns = new HashMap<Identifier, ItemStack>();
    static final Set<String> effectWarned = new HashSet<String>();
    static final Set<String> armorWarned = new HashSet<String>();
    static final Map<Feature, Long> soundTimes = new EnumMap<Feature, Long>(Feature.class);
    static final List<Event> timeline = new ArrayList<Event>();
    static final List<Event> lastTimeline = new ArrayList<Event>();
    static final Map<String, Integer> used = new LinkedHashMap<String, Integer>();
    static final Map<String, Integer> lastUsed = new LinkedHashMap<String, Integer>();
    static final List<String> pearlResults = new ArrayList<String>();
    static final Map<UUID, Long> pearlStarts = new HashMap<UUID, Long>();
    static final Map<EquipmentSlot, ItemStack> armorBefore = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
    static long fightStart;
    static long lastCombat;
    static long lastDuration;
    static long totemUntil;
    static long searchUntil;
    static long notificationUntil;
    static int fightTotems;
    static int lastTotems;
    static int ticks;
    static int smokeStep;
    static String context;
    static String notification;
    static String opponent;
    static String lastOpponent;
    static boolean initialized;
    static boolean wasDead;
    static boolean wasInventory;
    static volatile UUID playerId;
    static Screen previousScreen;
    static final List<TimedRow> readyMessages;
    static final Set<String> refillTracked;
    static boolean offhandBad;
    static boolean resupplyPending;

    public void onInitializeClient() {
        for (Feature f : Feature.values()) {
            if (f == Feature.SERVER_PROFILES) continue;
            roots.put(f, Config.load(f));
            rows.put(f, new ArrayList());
        }
        initialized = true;
        LOG.info("PvP Tools loaded: {}", roots.keySet());
    }

    static Minecraft mc() {
        return Minecraft.getInstance();
    }

    static long now() {
        return System.nanoTime() / 1000000L;
    }

    public static boolean has(Feature f) {
        return roots.containsKey((Object)f);
    }

    public static boolean on(Feature f) {
        return PvpModules.enabled(f);
    }

    public static boolean recording() {
        return fightStart > 0L && (Core.on(Feature.FIGHT_RECAP) || Core.on(Feature.FIGHT_TIMELINE));
    }

    public static boolean any() {
        return PvpModules.any();
    }

    static String profile() {
        return Core.onProfiles() ? context : "";
    }

    static boolean onProfiles() {
        return pyz1k.extensions.ExtensionConfig.get().automaticProfiles;
    }

    public static Config config(Feature f) {
        Config root = roots.get((Object)f);
        if (root == null) {
            return Config.defaults(f);
        }
        root.enabled = Core.on(f);
        if (f == Feature.SERVER_PROFILES || !Core.onProfiles() || context.isEmpty()) {
            return root;
        }
        Config active=root.profiles.computeIfAbsent(context, k -> {
            Config copy = root.copy();
            copy.profiles.clear();
            return copy;
        });
        active.enabled=on(f);return active;
    }

    public static void save(Feature f, Config c) {
        c.sanitize(f);
        PvpModules.enable(f, c.enabled);
        if (f != Feature.SERVER_PROFILES && Core.onProfiles() && !context.isEmpty()) {
            c.profiles.clear();
            Core.roots.get((Object)((Object)f)).profiles.put(context, c);
        } else {
            roots.put(f, c);
        }
        Config.write(f, roots.get((Object)f));
    }

    public static void notice(String s) {
        notification = s;
        notificationUntil = Core.now() + 3500L;
    }

    static boolean pressed(int key) {
        return key >= 32 && key <= 348 && LegacyInput.glfwGetKey((long)Core.mc().getWindow().handle(), (int)key) == 1;
    }

    static boolean edge(int key) {
        boolean down = Core.pressed(key);
        boolean old = keys.getOrDefault(key, false);
        keys.put(key, down);
        return down && !old;
    }

    public static void tick() {
        int timeout;
        String next;
        if (!initialized || !Core.any()) {
            return;
        }
        Minecraft c = Core.mc();
        ++ticks;
        next = c.getCurrentServer()!=null ? "server:"+c.getCurrentServer().ip : c.getSingleplayerServer()!=null ? "world:"+c.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize() : "";
        if (!((String)next).equals(context)) {
            Core.flush();
            context = next;
            Core.resetSession();
        }
        if (c.player == null || c.level == null) {
            playerId = null;
            return;
        }
        playerId = c.player.getUUID();
        boolean inv = c.gui.screen() instanceof AbstractContainerScreen;
        if (Core.on(Feature.INVENTORY_SEARCH_FLASH) && inv && !wasInventory) {
            searchUntil = Core.now() + (long)Core.config((Feature)Feature.INVENTORY_SEARCH_FLASH).threshold * 1000L;
        }
        wasInventory = inv;
        for (Feature f : roots.keySet()) {
            boolean hit;
            Config cfg = Core.config(f);
            if (cfg.actionKey <= 0 || !(hit = Core.edge(cfg.actionKey)) || !Core.on(f) || c.gui.screen() instanceof SettingsScreen || c.gui.screen() != null && f != Feature.INVENTORY_SEARCH_FLASH) continue;
            Core.action(f);
        }
        boolean dead = c.player.isDeadOrDying();
        if (dead && !wasDead) {
            Core.event("\u0421\u043c\u0435\u0440\u0442\u044c");
            resupplyPending = true;
            Core.finishFight();
        }
        wasDead = dead;
        timeout = Core.on(Feature.FIGHT_RECAP)?Core.config(Feature.FIGHT_RECAP).threshold:Core.on(Feature.FIGHT_TIMELINE)?Core.config(Feature.FIGHT_TIMELINE).threshold:Core.on(Feature.COMBAT_FOCUS)?Core.config(Feature.COMBAT_FOCUS).threshold:15;
        if (fightStart > 0L && Core.now() - lastCombat > (long)timeout * 1000L) {
            Core.finishFight();
        }
        if (Core.recording()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack current;
                ItemStack before;
                if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || (before = armorBefore.put(slot, (current = c.player.getItemBySlot(slot)).copy())) == null || before.isEmpty() || !current.isEmpty()) continue;
                Core.event("\u0421\u043d\u044f\u0442\u0430 / \u0441\u043b\u043e\u043c\u0430\u043d\u0430 \u0431\u0440\u043e\u043d\u044f: " + before.getHoverName().getString());
            }
        }
        if (ticks % 4 == 0) {
            Core.updateRows();
        }
    }

 public static void disabled(Feature f){
  var list=rows.get(f);if(list!=null)list.clear();
  if((f==Feature.FIGHT_RECAP||f==Feature.FIGHT_TIMELINE||f==Feature.COMBAT_FOCUS)&&!on(Feature.FIGHT_RECAP)&&!on(Feature.FIGHT_TIMELINE)&&!on(Feature.COMBAT_FOCUS)){finishFight();armorBefore.clear();}
  switch(f){
   case READY_AGAIN -> {cooldowns.clear();readyMessages.clear();}
   case EFFECT_ALARM -> effectWarned.clear();
   case ARMOR_PANIC -> armorWarned.clear();
   case OFFHAND_ALERT -> offhandBad=false;
   case REFILL_REMINDER -> refillTracked.clear();
   case INVENTORY_SEARCH_FLASH -> {InventorySearch.clear();wasInventory=false;searchUntil=0;}
   default -> {}
  }
 }
    static void flush() {
        for (Feature f : roots.keySet()) {
            Config.write(f, roots.get((Object)f));
        }
    }

    static void resetSession() {
        if (fightStart > 0L) {
            Core.finishFight();
        }
        cooldowns.clear();
        effectWarned.clear();
        armorWarned.clear();
        pearlStarts.clear();
        pearlResults.clear();
        InventorySearch.clear();
        searchUntil = 0L;
        totemUntil = 0L;
        lastCombat = 0L;
        fightStart = 0L;
        offhandBad = false;
        resupplyPending = false;
        wasInventory = false;
        wasDead = false;
        refillTracked.clear();
        readyMessages.clear();
        armorBefore.clear();
        for (List<Row> r : rows.values()) {
            r.clear();
        }
    }

    static ItemStack icon(Feature f) {
        return new ItemStack((ItemLike)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)f.icon)));
    }

    static Row row(ItemStack s, String label, String value, boolean critical) {
        return new Row(s.copy(), label, value, critical);
    }

    static List<Row> list(Feature f) {
        return rows.get((Object)f);
    }

    static void add(Feature f, Row r) {
        if (Core.has(f)) {
            Core.list(f).add(r);
        }
    }

    public static String key(ItemStack s) {
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder b = new StringBuilder(BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).append('|').append(s.getHoverName().getString());
        PotionContents p = (PotionContents)s.get(DataComponents.POTION_CONTENTS);
        if (p != null) {
            for (MobEffectInstance e : p.getAllEffects()) {
                b.append('|').append(e.getEffect().getRegisteredName()).append(':').append(e.getAmplifier()).append(':').append(e.getDuration());
            }
        }
        return b.toString();
    }

 public static List<ItemStack> inventory(){
  List<ItemStack> out=new ArrayList<>(37);var p=Core.mc().player;if(p==null)return out;
  for(int i=0;i<36;i++)out.add(p.getInventory().getItem(i));out.add(p.getOffhandItem());return out;
 }
    static int count(Item item) {
        int total = 0;
        for (ItemStack s : Core.inventory()) {
            if (!s.is(item)) continue;
            total += s.getCount();
        }
        return total;
    }

    static Map<String, Integer> counts() {
        LinkedHashMap<String, Integer> out = new LinkedHashMap<String, Integer>();
        for (ItemStack s : Core.inventory()) {
            if (s.isEmpty()) continue;
            out.merge(Core.key(s), s.getCount(), Integer::sum);
        }
        return out;
    }

    public static void capture(Feature f, Config cfg) {
        if (Core.mc().player == null) {
            Core.notice("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442, \u043d\u0430\u0445\u043e\u0434\u044f\u0441\u044c \u0432 \u043c\u0438\u0440\u0435");
            return;
        }
        ArrayList<Config.SavedItem> out = new ArrayList<Config.SavedItem>();
        Inventory inv = Core.mc().player.getInventory();
        if (f == Feature.HOTBAR_MEMORY) {
            for (int i = 0; i < 9; ++i) {
                ItemStack s = inv.getItem(i);
                out.add(new Config.SavedItem(Core.key(s), BuiltInRegistries.ITEM.getKey(s.getItem()).toString(), s.isEmpty() ? "\u041f\u0443\u0441\u0442\u043e" : s.getHoverName().getString(), s.getCount(), i));
            }
            cfg.hotbar = out;
        } else {
            LinkedHashMap<String, Config.SavedItem> map = new LinkedHashMap<String, Config.SavedItem>();
            for (ItemStack s : Core.inventory()) {
                if (s.isEmpty()) continue;
                String k = Core.key(s);
                Config.SavedItem old = (Config.SavedItem)map.get(k);
                map.put(k, new Config.SavedItem(k, BuiltInRegistries.ITEM.getKey(s.getItem()).toString(), s.getHoverName().getString(), s.getCount() + (old == null ? 0 : old.count()), -1));
            }
            out.addAll(map.values());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack s;
                if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || (s = Core.mc().player.getItemBySlot(slot)).isEmpty()) continue;
                out.add(new Config.SavedItem(Core.key(s), BuiltInRegistries.ITEM.getKey(s.getItem()).toString(), s.getHoverName().getString(), 1, slot.getId()));
            }
            cfg.kit = out;
        }
        Core.notice("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e: " + f.title);
    }

    static List<Row> missing(Config cfg, boolean equipment) {
        ArrayList<Row> out = new ArrayList<Row>();
        Map<String, Integer> current = Core.counts();
        for (Config.SavedItem saved : cfg.kit) {
            int lack;
            if (saved.slot() >= 0) {
                if (!equipment) continue;
                ItemStack actual = ItemStack.EMPTY;
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || slot.getId() != saved.slot()) continue;
                    actual = Core.mc().player.getItemBySlot(slot);
                }
                lack = Core.key(actual).equals(saved.key()) ? 0 : 1;
            } else {
                lack = Math.max(0, saved.count() - current.getOrDefault(saved.key(), 0));
            }
            if (lack <= 0) continue;
            out.add(Core.row(new ItemStack((ItemLike)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)saved.item()))), saved.name(), (String)(saved.slot() >= 0 ? "\u041d\u0435 \u043d\u0430\u0434\u0435\u0442\u043e" : "\u2212" + lack), true));
        }
        return out;
    }

    static void signal(Feature f) {
        if (!Core.on(f) || !Core.config((Feature)f).sound || Core.now() - soundTimes.getOrDefault((Object)f, 0L) < 1500L) {
            return;
        }
        soundTimes.put(f, Core.now());
        Core.mc().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.2f));
    }

    static void updateRows() {
        int total;
        for (List<Row> r2 : rows.values()) {
            r2.clear();
        }
        InventorySearch.tick();
        LocalPlayer p = Core.mc().player;
        Inventory inv = p.getInventory();
        long t = Core.now();
        if (Core.on(Feature.READY_AGAIN)) {
            ItemCooldowns manager = p.getCooldowns();
            for (ItemStack s : Core.inventory()) {
                if (s.isEmpty() || !manager.isOnCooldown(s)) continue;
                cooldowns.put(manager.getCooldownGroup(s), s.copy());
            }
            var it = cooldowns.entrySet().iterator();
            while (it.hasNext()) {
                var e2 = it.next();
                if (manager.isOnCooldown((ItemStack)e2.getValue())) continue;
                readyMessages.add(new TimedRow(t + (long)Core.config((Feature)Feature.READY_AGAIN).threshold * 1000L, Core.row((ItemStack)e2.getValue(), ((ItemStack)e2.getValue()).getHoverName().getString(), "\u0413\u043e\u0442\u043e\u0432\u043e", true)));
                Core.signal(Feature.READY_AGAIN);
                it.remove();
            }
            readyMessages.removeIf(r -> r.until < t);
            for (TimedRow r3 : readyMessages) {
                Core.add(Feature.READY_AGAIN, r3.row);
            }
        }
        if (Core.on(Feature.EFFECT_ALARM)) {
            HashSet<String> active = new HashSet<String>();
            for (MobEffectInstance e2 : p.getActiveEffects()) {
                boolean low;
                String id = e2.getEffect().getRegisteredName();
                active.add(id);
                int sec = (int)Math.ceil((double)e2.getDuration() / 20.0);
                boolean bl = low = !e2.isInfiniteDuration() && sec <= Core.config((Feature)Feature.EFFECT_ALARM).threshold;
                if (low) {
                    Core.add(Feature.EFFECT_ALARM, Core.row(Core.icon(Feature.EFFECT_ALARM), ((MobEffect)e2.getEffect().value()).getDisplayName().getString(), sec + " \u0441", true));
                    if (!effectWarned.add(id)) continue;
                    Core.signal(Feature.EFFECT_ALARM);
                    continue;
                }
                effectWarned.remove(id);
            }
            effectWarned.retainAll(active);
        }
        if (Core.on(Feature.REFILL_REMINDER)) {
            Map<String,Integer> hot = new LinkedHashMap<>();
            Map<String,Integer> reserve = new LinkedHashMap<>();
            HashMap<String, ItemStack> examples = new HashMap<String, ItemStack>();
            for (int i = 0; i < 36; ++i) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty() || !s.isStackable() && !(s.getItem() instanceof PotionItem) || !(s.getItem() instanceof PotionItem) && !s.is(Items.ENDER_PEARL) && !s.is(Items.GOLDEN_APPLE) && !s.is(Items.ENCHANTED_GOLDEN_APPLE) && !s.is(Items.END_CRYSTAL) && !s.is(Items.OBSIDIAN) && !s.is(Items.EXPERIENCE_BOTTLE)) continue;
                String k = Core.key(s);
                (i < 9 ? hot : reserve).merge(k, s.getCount(), Integer::sum);
                examples.put(k, s.copy());
            }
            for (Map.Entry e3 : hot.entrySet()) {
                refillTracked.add((String)e3.getKey());
            }
            for (String k : refillTracked) {
                ItemStack s;
                if (hot.getOrDefault(k, 0) > Core.config((Feature)Feature.REFILL_REMINDER).threshold || reserve.getOrDefault(k, 0) <= 0 || (s = (ItemStack)examples.get(k)) == null) continue;
                Core.add(Feature.REFILL_REMINDER, Core.row(s, s.getHoverName().getString(), String.valueOf(hot.getOrDefault(k, 0)) + " / \u0437\u0430\u043f\u0430\u0441 " + String.valueOf(reserve.get(k)), true));
            }
        }
        if (Core.on(Feature.ARMOR_PANIC)) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                ItemStack s = p.getItemBySlot(slot);
                String k = slot.getName();
                if (!s.isEmpty() && s.isDamageableItem()) {
                    int left = s.getMaxDamage() - s.getDamageValue();
                    int pc = (int)Math.ceil(100.0 * (double)left / (double)s.getMaxDamage());
                    if (pc <= Core.config((Feature)Feature.ARMOR_PANIC).threshold) {
                        Core.add(Feature.ARMOR_PANIC, Core.row(s, s.getHoverName().getString(), left + " \u00b7 " + pc + "%", true));
                        if (!armorWarned.add(k)) continue;
                        Core.signal(Feature.ARMOR_PANIC);
                        continue;
                    }
                    armorWarned.remove(k);
                    continue;
                }
                armorWarned.remove(k);
            }
        }
        if (Core.on(Feature.KIT_CHECK)) {
            Config cfg = Core.config(Feature.KIT_CHECK);
            if (cfg.kit.isEmpty()) {
                Core.add(Feature.KIT_CHECK, new Row("\u041a\u043e\u043c\u043f\u043b\u0435\u043a\u0442", "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445", false));
            } else {
                List<Row> diff = Core.missing(cfg, true);
                Core.list(Feature.KIT_CHECK).addAll(diff);
                if (diff.isEmpty()) {
                    Core.add(Feature.KIT_CHECK, new Row("\u041a\u043e\u043c\u043f\u043b\u0435\u043a\u0442", "\u0413\u043e\u0442\u043e\u0432", false));
                }
            }
        }
        if (Core.on(Feature.RESUPPLY_LIST) && wasDead && !Core.config((Feature)Feature.RESUPPLY_LIST).kit.isEmpty()) {
            Core.list(Feature.RESUPPLY_LIST).addAll(Core.missing(Core.config(Feature.RESUPPLY_LIST), false));
        }
        if (Core.on(Feature.RESUPPLY_LIST) && !wasDead && resupplyPending) {
            List<Row> diff = Core.missing(Core.config(Feature.RESUPPLY_LIST), false);
            Core.list(Feature.RESUPPLY_LIST).addAll(diff);
            if (diff.isEmpty()) {
                resupplyPending = false;
            }
        }
        if (Core.on(Feature.TOTEM_PULSE) && t < totemUntil) {
            int n = Core.count(Items.TOTEM_OF_UNDYING);
            Core.add(Feature.TOTEM_PULSE, Core.row(Core.icon(Feature.TOTEM_PULSE), "\u0422\u043e\u0442\u0435\u043c\u043e\u0432 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c", String.valueOf(n), n <= Core.config((Feature)Feature.TOTEM_PULSE).threshold));
        }
        if (Core.on(Feature.OFFHAND_ALERT)) {
            Config cfg = Core.config(Feature.OFFHAND_ALERT);
            Item target = switch (cfg.mode) {
                case 1 -> Items.SHIELD;
                case 2 -> Items.GOLDEN_APPLE;
                default -> Items.TOTEM_OF_UNDYING;
            };
            ItemStack hand = p.getOffhandItem();
            if (!hand.is(target)) {
                Core.add(Feature.OFFHAND_ALERT, Core.row(new ItemStack((ItemLike)target), "\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430", hand.isEmpty() ? "\u041f\u0443\u0441\u0442\u043e" : "\u0414\u0440\u0443\u0433\u043e\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", true));
                if (!offhandBad) {
                    Core.signal(Feature.OFFHAND_ALERT);
                }
                offhandBad = true;
            } else {
                offhandBad = false;
            }
        }
        if (Core.on(Feature.HOTBAR_MEMORY)) {
            Config cfg = Core.config(Feature.HOTBAR_MEMORY);
            int wrong = 0;
            for (Config.SavedItem saved : cfg.hotbar) {
                if (saved.slot() < 0 || saved.slot() >= 9 || saved.key().equals(Core.key(inv.getItem(saved.slot())))) continue;
                ++wrong;
            }
            if (cfg.hotbar.isEmpty()) {
                Core.add(Feature.HOTBAR_MEMORY, new Row("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430", "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445", false));
            } else if (wrong > 0) {
                Core.add(Feature.HOTBAR_MEMORY, new Row("\u041d\u0435 \u043d\u0430 \u0441\u0432\u043e\u0438\u0445 \u043c\u0435\u0441\u0442\u0430\u0445", wrong + " \u0441\u043b.", true));
            }
        }
        if (Core.on(Feature.PEARL_LANDING_TIMER)) {
            if (Core.mc().getSingleplayerServer() == null) {
                Core.add(Feature.PEARL_LANDING_TIMER, new Row("\u0422\u0440\u0435\u043d\u0438\u0440\u043e\u0432\u043a\u0430", "\u0422\u043e\u043b\u044c\u043a\u043e \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u044b\u0439 \u043c\u0438\u0440", false));
            } else {
                if (!pearlResults.isEmpty()) {
                    Core.add(Feature.PEARL_LANDING_TIMER, new Row("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u0431\u0440\u043e\u0441\u043e\u043a", pearlResults.get(pearlResults.size() - 1), false));
                }
                if (!pearlStarts.isEmpty()) {
                    Core.add(Feature.PEARL_LANDING_TIMER, new Row("\u0416\u0435\u043c\u0447\u0443\u0433", "\u0412 \u043f\u043e\u043b\u0451\u0442\u0435", false));
                }
            }
        }
        if (Core.on(Feature.FIGHT_RECAP)) {
            if (fightStart > 0L) {
                Core.add(Feature.FIGHT_RECAP, new Row("\u0411\u043e\u0439: " + opponent, Core.seconds(t - fightStart), false));
            } else if (lastDuration > 0L) {
                Core.add(Feature.FIGHT_RECAP, new Row("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u0431\u043e\u0439", Core.seconds(lastDuration), false));
                Core.add(Feature.FIGHT_RECAP, new Row("\u0421\u0440\u0430\u0431\u043e\u0442\u0430\u043b\u043e \u0442\u043e\u0442\u0435\u043c\u043e\u0432", String.valueOf(lastTotems), false));
                for (Map.Entry<String, Integer> e4 : lastUsed.entrySet()) {
                    Core.add(Feature.FIGHT_RECAP, new Row(e4.getKey(), "\u00d7" + String.valueOf(e4.getValue()), false));
                }
            }
        }
        if (Core.on(Feature.FIGHT_TIMELINE)) {
            List<Event> source = fightStart > 0L ? timeline : lastTimeline;
            for (int i = Math.max(0, source.size() - 5); i < source.size(); ++i) {
                Event e5 = source.get(i);
                Core.add(Feature.FIGHT_TIMELINE, new Row(Core.seconds(e5.ms), e5.text, false));
            }
        }
        if (Core.on(Feature.DUEL_NOTES) && (total = Core.config((Feature)Feature.DUEL_NOTES).notes.values().stream().mapToInt(Integer::intValue).sum()) > 0) {
            Core.add(Feature.DUEL_NOTES, new Row("\u0417\u0430\u043c\u0435\u0442\u043a\u0438", total + " \u00b7 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445", false));
        }
        if (Core.on(Feature.COMBAT_FOCUS) && fightStart > 0L) {
            Core.add(Feature.COMBAT_FOCUS, new Row("Combat Focus", "\u0411\u043e\u0439", false));
        }
        if (Core.on(Feature.SERVER_PROFILES)) {
            Core.add(Feature.SERVER_PROFILES, new Row("\u041f\u0440\u043e\u0444\u0438\u043b\u044c", context.startsWith("server:") ? context.substring(7) : "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u044b\u0439 \u043c\u0438\u0440", false));
        }
        pearlStarts.entrySet().removeIf(e -> t - (Long)e.getValue() > 120000L);
    }

    static String seconds(long ms) {
        return String.format(Locale.ROOT, "%.1f \u0441", (double)ms / 1000.0);
    }

    static void startFight(String name) {
        if (fightStart == 0L) {
            fightStart = Core.now();
            timeline.clear();
            used.clear();
            fightTotems = 0;
            opponent = name;
            Core.event("\u041d\u0430\u0447\u0430\u043b\u043e: " + name);
        }
        lastCombat = Core.now();
    }

    static void event(String text) {
        if (fightStart > 0L) {
            timeline.add(new Event(Core.now() - fightStart, text));
            if (timeline.size() > 500) {
                timeline.remove(0);
            }
        }
    }

    static void finishFight() {
        if (fightStart == 0L) {
            return;
        }
        Core.event("\u041a\u043e\u043d\u0435\u0446 \u0437\u0430\u043f\u0438\u0441\u0438");
        lastDuration = Core.now() - fightStart;
        lastTotems = fightTotems;
        lastOpponent = opponent;
        lastUsed.clear();
        lastUsed.putAll(used);
        lastTimeline.clear();
        lastTimeline.addAll(timeline);
        fightStart = 0L;
        if (Core.on(Feature.FIGHT_RECAP) || Core.on(Feature.FIGHT_TIMELINE)) {
            Core.exportFight(false);
        }
    }

    public static void status(ClientboundEntityEventPacket packet) {
        if (!initialized || !Core.on(Feature.TOTEM_PULSE) && !Core.recording() || Core.mc().level == null || Core.mc().player == null) {
            return;
        }
        if (packet.getEventId() == 35 && packet.getEntity((Level)Core.mc().level) == Core.mc().player) {
            totemUntil = Core.now() + 5000L;
            ++fightTotems;
            Core.event("\u0421\u0440\u0430\u0431\u043e\u0442\u0430\u043b \u0442\u043e\u0442\u0435\u043c");
            Core.signal(Feature.TOTEM_PULSE);
        }
    }

    public static void damage(ClientboundDamageEventPacket packet) {
        if (!initialized || Core.mc().level == null || Core.mc().player == null || !Core.on(Feature.FIGHT_RECAP) && !Core.on(Feature.FIGHT_TIMELINE) && !Core.on(Feature.COMBAT_FOCUS)) {
            return;
        }
        Entity target = Core.mc().level.getEntity(packet.entityId());
        Entity source = packet.getSource((Level)Core.mc().level).getEntity();
        if (target instanceof Player && source instanceof Player && (target == Core.mc().player || source == Core.mc().player)) {
            Core.startFight((target == Core.mc().player ? source : target).getName().getString());
        }
    }

    public static void used(ItemStack s, boolean consumed) {
        if (!initialized || !Core.recording() || Core.mc().player == null || s.isEmpty()) {
            return;
        }
        if (!(consumed || s.is(Items.ENDER_PEARL) || s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION) || s.is(Items.EXPERIENCE_BOTTLE))) {
            return;
        }
        if (fightStart > 0L) {
            String name = s.getHoverName().getString();
            used.merge(name, 1, Integer::sum);
            Core.event((consumed ? "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u043e: " : "\u0411\u0440\u043e\u0441\u043e\u043a: ") + name);
        }
    }

    public static void pearlTick(ThrownEnderpearl pearl) {
        UUID local = playerId;
        if (!initialized || !Core.on(Feature.PEARL_LANDING_TIMER) || pearl.tickCount > 1 || pearl.level().isClientSide() || local == null || pearl.getOwner() == null || !pearl.getOwner().getUUID().equals(local)) {
            return;
        }
        UUID id = pearl.getUUID();
        Core.mc().execute(() -> {
            if (Core.on(Feature.PEARL_LANDING_TIMER) && Core.mc().getSingleplayerServer() != null) {
                pearlStarts.putIfAbsent(id, Core.now());
            }
        });
    }

    public static void pearlLand(ThrownEnderpearl pearl) {
        if (!initialized || !Core.on(Feature.PEARL_LANDING_TIMER) || pearl.level().isClientSide() || playerId == null || pearl.getOwner() == null || !pearl.getOwner().getUUID().equals(playerId)) {
            return;
        }
        int age = pearl.tickCount;
        UUID id = pearl.getUUID();
        Core.mc().execute(() -> {
            if (!Core.on(Feature.PEARL_LANDING_TIMER) || Core.mc().getSingleplayerServer() == null) {
                return;
            }
            pearlStarts.remove(id);
            String result = String.format(Locale.ROOT, "%.2f \u0441 \u00b7 %d \u0442\u0438\u043a\u043e\u0432", (double)age / 20.0, age);
            pearlResults.add(result);
            if (pearlResults.size() > 50) {
                pearlResults.remove(0);
            }
            Core.notice("\u0416\u0435\u043c\u0447\u0443\u0433: " + result);
        });
    }

    public static void action(Feature f) {
        if (Core.mc().player == null) {
            Core.notice("\u042d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0432 \u043c\u0438\u0440\u0435");
            return;
        }
        switch (f) {
            case KIT_CHECK: {
                Core.updateRows();
                Core.mc().gui.setScreen((Screen)new ReportScreen(Core.mc().gui.screen(), "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442\u0430", Core.reportRows(Core.list(f))));
                break;
            }
            case RESUPPLY_LIST: {
                resupplyPending = true;
                Core.updateRows();
                Core.mc().gui.setScreen((Screen)new ReportScreen(Core.mc().gui.screen(), "\u041f\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442\u0430", Core.reportRows(Core.missing(Core.config(f), false))));
                break;
            }
            case FIGHT_RECAP: 
            case FIGHT_TIMELINE: {
                if (fightStart == 0L) {
                    Core.startFight("\u0420\u0443\u0447\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u044c");
                    break;
                }
                Core.finishFight();
                Core.notice("\u0417\u0430\u043f\u0438\u0441\u044c \u0431\u043e\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430");
                break;
            }
            case DUEL_NOTES: {
                Core.mc().gui.setScreen((Screen)new NotesScreen(Core.mc().gui.screen()));
                break;
            }
            case INVENTORY_SEARCH_FLASH: {
                Config cfg = Core.config(f);
                cfg.mode = (cfg.mode + 1) % 4;
                Core.save(f, cfg);
                searchUntil = Core.now() + (long)cfg.threshold * 1000L;
                Core.notice("\u041f\u043e\u0438\u0441\u043a: " + Core.searchCategory(cfg.mode));
                break;
            }
            case PEARL_LANDING_TIMER: {
                Core.mc().gui.setScreen((Screen)new ReportScreen(Core.mc().gui.screen(), "\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0431\u0440\u043e\u0441\u043a\u043e\u0432", new ArrayList<String>(pearlResults)));
                break;
            }
            default: {
                Config cfg = Core.config(f);
                cfg.enabled = !cfg.enabled;
                Core.save(f, cfg);
                Core.notice(f.title + ": " + (cfg.enabled ? "\u0432\u043a\u043b." : "\u0432\u044b\u043a\u043b."));
            }
        }
    }

    static List<String> reportRows(List<Row> rows) {
        ArrayList<String> lines = new ArrayList<String>();
        for (Row r : rows) {
            lines.add(r.label + "  " + r.value);
        }
        if (lines.isEmpty()) {
            lines.add("\u0412\u0441\u0451 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435");
        }
        return lines;
    }

    public static void exportFight(boolean feedback) {
        if (lastDuration == 0L) {
            if (feedback) {
                Core.notice("\u0417\u0430\u0432\u0435\u0440\u0448\u0451\u043d\u043d\u044b\u0445 \u0437\u0430\u043f\u0438\u0441\u0435\u0439 \u043f\u043e\u043a\u0430 \u043d\u0435\u0442");
            }
            return;
        }
        try {
            Path dir = FabricLoader.getInstance().getGameDir().resolve("pvp-tools/reports");
            Files.createDirectories(dir, new FileAttribute[0]);
            LinkedHashMap<String, Object> report = new LinkedHashMap<String, Object>();
            report.put("created", Instant.now().toString());
            report.put("opponent", lastOpponent);
            report.put("durationMs", lastDuration);
            report.put("totems", lastTotems);
            report.put("clientUses", lastUsed);
            report.put("events", lastTimeline);
            String filename = "fight-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now()) + ".json";
            Files.writeString(dir.resolve(filename), (CharSequence)Config.JSON.toJson(report), new OpenOption[0]);
            if (feedback) {
                Core.notice("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e \u0432 pvp-tools/reports/");
            }
        }
        catch (Exception e) {
            LOG.error("Report export failed", (Throwable)e);
            Core.notice("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043e\u0442\u0447\u0451\u0442");
        }
    }

    public static void render(GuiGraphicsExtractor ctx) {
        if (!initialized || !Core.any() || ScreenshotMode.active() || Core.mc().player == null || Core.mc().gui.hud.isHidden() || Core.mc().gui.screen() instanceof SettingsScreen || Core.mc().gui.screen() instanceof HubScreen) {
            return;
        }
        int autoY = 8;
        int autoColumn = 0;
        for (Feature f : roots.keySet()) {
            List<Row> source;
            if (!Core.on(f) || (source = rows.get((Object)f)) == null || source.isEmpty()) continue;
            ArrayList<Row> visible = new ArrayList<Row>();
            for (Row r : source) {
                if (!Core.visible(f, r)) continue;
                visible.add(r);
            }
            if (visible.isEmpty()) continue;
            Config layout = Core.config(f);
            if (!layout.manualPosition) {
                layout = layout.layoutCopy();
                int h = (int)Math.ceil((double)(18 + Math.min(visible.size(), 8) * 19 + (visible.size() > 8 ? 12 : 0)) * layout.scale);
                if (autoY + h > ctx.guiHeight() - 45 && autoY > 8) {
                    ++autoColumn;
                    autoY = 8;
                }
                layout.x = autoColumn % 2 == 0 ? 0.02 : 0.98;
                layout.y = Math.clamp((double)autoY / (double)Math.max(1, ctx.guiHeight() - h), 0.0, 1.0);
                autoY += h + 6;
            }
            Core.panel(ctx, f, layout, visible, false);
        }
        if (Core.now() < notificationUntil) {
            ctx.centeredText(Core.mc().font, notification, ctx.guiWidth() / 2, ctx.guiHeight() - 65, -1);
        }
    }

    public static boolean hideVanilla(String part) {
        if (!initialized || !Core.on(Feature.COMBAT_FOCUS) && !Core.on(Feature.HUD_ON_DEMAND) || Core.mc().player == null) {
            return false;
        }
        block10: for (Feature f : new Feature[]{Feature.COMBAT_FOCUS, Feature.HUD_ON_DEMAND}) {
            boolean active;
            if (!Core.on(f)) continue;
            Config cfg = Core.config(f);
            active = f == Feature.COMBAT_FOCUS ? fightStart > 0L : !Core.pressed(cfg.holdKey);
            if (!active) continue;
            switch (part) {
                case "scoreboard": {
                    if (!cfg.hideScoreboard) continue block10;
                    break;
                }
                case "bossbar": {
                    if (!cfg.hideBossbar) continue block10;
                    break;
                }
                case "effects": {
                    if (!cfg.hideEffects) continue block10;
                    break;
                }
                default: {
                    if (!cfg.hideItemName) continue block10;
                    break;
                }
            }
            return true;
        }
        return false;
    }

    static boolean visible(Feature f, Row r) {
        if (r.critical) {
            return true;
        }
        if (Core.on(Feature.HUD_ON_DEMAND) && !Core.pressed(Core.config((Feature)Feature.HUD_ON_DEMAND).holdKey)) {
            return false;
        }
        return !Core.on(Feature.COMBAT_FOCUS) || fightStart <= 0L || f == Feature.COMBAT_FOCUS || f == Feature.FIGHT_RECAP || f == Feature.FIGHT_TIMELINE;
    }

    public static int[] panel(GuiGraphicsExtractor ctx, Feature f, Config cfg, List<Row> source, boolean preview) {
        int max = Core.mc().font.width(f.title);
        int lines = Math.min(source.size(), 8);
        for (int i = 0; i < lines; ++i) {
            Row r = source.get(i);
            max = Math.max(max, Core.mc().font.width((String)(cfg.icons ? "" : r.label + "  ") + r.value) + (cfg.icons ? 22 : 0));
        }
        int w = max + 14;
        int h = 18 + lines * 19 + (source.size() > 8 ? 12 : 0);
        double scale = cfg.scale;
        int sw = ctx.guiWidth();
        int sh = ctx.guiHeight();
        int x = (int)(cfg.x * Math.max(0.0, (double)sw - (double)w * scale));
        int y = (int)(cfg.y * Math.max(0.0, (double)sh - (double)h * scale));
        ctx.pose().pushMatrix();
        ctx.pose().translate((float)x, (float)y);
        ctx.pose().scale((float)scale, (float)scale);
        if (cfg.background || preview) {
            ctx.fill(0, 0, w, h, -870900958);
            ctx.outline(0, 0, w, h, -9405552);
        }
        ctx.text(Core.mc().font, f.title, 7, 5, 0xFF000000 | cfg.accent, cfg.shadow);
        int cy = 18;
        for (int i = 0; i < lines; ++i) {
            Row row = source.get(i);
            int color = 0xFF000000 | (row.critical ? cfg.accent : cfg.color);
            if (cfg.icons && !row.icon.isEmpty()) {
                ctx.item(row.icon, 6, cy);
                ctx.text(Core.mc().font, row.value, 28, cy + 4, color, cfg.shadow);
            } else {
                ctx.text(Core.mc().font, row.label + "  " + row.value, 7, cy + 4, color, cfg.shadow);
            }
            cy += 19;
        }
        if (source.size() > 8) {
            ctx.text(Core.mc().font, "\u0415\u0449\u0451 " + (source.size() - 8) + " \u00b7 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a", 7, cy, -5592406, cfg.shadow);
        }
        ctx.pose().popMatrix();
        return preview?new int[]{x, y, (int)Math.ceil((double)w * scale), (int)Math.ceil((double)h * scale)}:null;
    }

    public static List<Row> preview(Feature f) {
        if (rows.containsKey((Object)f) && !rows.get((Object)f).isEmpty()) {
            return rows.get((Object)f);
        }
        ItemStack itemStack = Core.icon(f);
        return List.of(Core.row(itemStack, switch (f) {
            case Feature.ARMOR_PANIC -> "\u0410\u043b\u043c\u0430\u0437\u043d\u044b\u0439 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a";
            case Feature.EFFECT_ALARM -> "\u0421\u0438\u043b\u0430 II";
            case Feature.TOTEM_PULSE -> "\u0422\u043e\u0442\u0435\u043c\u043e\u0432 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c";
            case Feature.OFFHAND_ALERT -> "\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430";
            default -> "\u041f\u0440\u0438\u043c\u0435\u0440";
        }, switch (f) {
            case Feature.READY_AGAIN -> "\u0413\u043e\u0442\u043e\u0432\u043e";
            case Feature.ARMOR_PANIC -> "31 \u00b7 6%";
            case Feature.EFFECT_ALARM -> "8 \u0441";
            case Feature.TOTEM_PULSE -> "2";
            default -> "12";
        }, true));
    }

    public static void hotbarSlot(GuiGraphicsExtractor ctx, ItemStack stack, int x, int y) {
        int slot;
        if (!initialized || !Core.on(Feature.HOTBAR_MEMORY) && !Core.on(Feature.POTION_IDENTITY) || Core.mc().player == null) {
            return;
        }
        Core.potionLabel(ctx, stack, x, y);
        if (Core.on(Feature.HOTBAR_MEMORY) && (slot = (x - (ctx.guiWidth() / 2 - 90)) / 20) >= 0 && slot < 9) {
            for (Config.SavedItem saved : Core.config((Feature)Feature.HOTBAR_MEMORY).hotbar) {
                if (saved.slot() != slot || saved.key().equals(Core.key(stack))) continue;
                ctx.outline(x - 1, y - 1, 18, 18, 0xFF000000 | Core.config((Feature)Feature.HOTBAR_MEMORY).accent);
            }
        }
    }

    public static void inventorySlot(GuiGraphicsExtractor ctx, Slot slot) {
        if (!initialized || !Core.on(Feature.HOTBAR_MEMORY) && !Core.on(Feature.POTION_IDENTITY) && !Core.on(Feature.INVENTORY_SEARCH_FLASH) || Core.mc().player == null || slot.container != Core.mc().player.getInventory()) {
            return;
        }
        Core.potionLabel(ctx, slot.getItem(), slot.x, slot.y);
        if (Core.on(Feature.HOTBAR_MEMORY) && slot.getContainerSlot() < 9) {
            for (Config.SavedItem saved : Core.config((Feature)Feature.HOTBAR_MEMORY).hotbar) {
                if (saved.slot() != slot.getContainerSlot() || saved.key().equals(Core.key(slot.getItem()))) continue;
                ctx.outline(slot.x - 1, slot.y - 1, 18, 18, 0xFF000000 | Core.config((Feature)Feature.HOTBAR_MEMORY).accent);
            }
        }
        if (InventorySearch.matches(slot.getItem())) {
            ctx.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x44000000 | Core.config((Feature)Feature.INVENTORY_SEARCH_FLASH).accent);
            ctx.outline(slot.x - 1, slot.y - 1, 18, 18, 0xFF000000 | Core.config((Feature)Feature.INVENTORY_SEARCH_FLASH).accent);
        }
    }

    static String searchCategory(int mode) {
        return switch (mode) {
            case 1 -> "\u0411\u0440\u043e\u043d\u044f";
            case 2 -> "Жемчуг";
            case 3 -> "По названию";
            default -> "\u041b\u0435\u0447\u0435\u043d\u0438\u0435";
        };
    }

    static boolean matchesCategory(ItemStack s, int mode) {
        if (s.isEmpty()) {
            return false;
        }
        if (mode == 2) {
            return s.is(Items.ENDER_PEARL);
        }
        if (mode == 1) {
            return pyz1k.extensions.InventorySearchQuery.armor(BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
        }
        if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            return true;
        }
        PotionContents p = (PotionContents)s.get(DataComponents.POTION_CONTENTS);
        if (p != null) {
            for (MobEffectInstance e : p.getAllEffects()) {
                if (!e.getEffect().equals((Object)MobEffects.INSTANT_HEALTH) && !e.getEffect().equals((Object)MobEffects.REGENERATION)) continue;
                return true;
            }
        }
        return false;
    }

    static void potionLabel(GuiGraphicsExtractor ctx, ItemStack s, int x, int y) {
        if (!Core.on(Feature.POTION_IDENTITY)) {
            return;
        }
        PotionContents p = (PotionContents)s.get(DataComponents.POTION_CONTENTS);
        if (p == null) {
            return;
        }
        Object label = "";
        Iterator iterator = p.getAllEffects().iterator();
        if (iterator.hasNext()) {
            MobEffectInstance e = (MobEffectInstance)iterator.next();
            label=e.getEffect().equals(MobEffects.STRENGTH)?"С":e.getEffect().equals(MobEffects.SPEED)?"Б":e.getEffect().equals(MobEffects.FIRE_RESISTANCE)?"О":e.getEffect().equals(MobEffects.INSTANT_HEALTH)?"Л":e.getEffect().equals(MobEffects.REGENERATION)?"Р":e.getEffect().equals(MobEffects.SLOWNESS)?"З":e.getEffect().equals(MobEffects.WEAKNESS)?"Сл":e.getEffect().equals(MobEffects.POISON)?"Я":"?";
            if (e.getAmplifier() > 0) {
                label = (String)label + (e.getAmplifier() + 1);
            }
        }
        if (!((String)label).isEmpty()) {
            Config cfg = Core.config(Feature.POTION_IDENTITY);
            ctx.fill(x, y, x + Core.mc().font.width((String)label) + 2, y + 9, -1072227550);
            ctx.text(Core.mc().font, (String)label, x + 1, y, 0xFF000000 | cfg.accent, cfg.shadow);
        }
    }

    static {
        context = "";
        notification = "";
        opponent = "";
        lastOpponent = "";
        readyMessages = new ArrayList<TimedRow>();
        refillTracked = new HashSet<String>();
    }

    public record Row(ItemStack icon, String label, String value, boolean critical) {
        public Row(String label, String value, boolean critical) {
            this(ItemStack.EMPTY, label, value, critical);
        }
    }

    record TimedRow(long until, Row row) {
    }

    public record Event(long ms, String text) {
    }
}

