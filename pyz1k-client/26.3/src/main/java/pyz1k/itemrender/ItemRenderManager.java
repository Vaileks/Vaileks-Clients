/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 */
package pyz1k.itemrender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import pyz1k.config.ConfigManager;
import pyz1k.itemrender.ItemRenderRule;

public final class ItemRenderManager {
    public static final float MIN_SCALE = 0.25f;
    public static final float MAX_SCALE = 5.0f;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ItemRenderManager INSTANCE = new ItemRenderManager();
    private static final long SAVE_DELAY_MS = 120L;
    private final Path file = ConfigManager.getRootDir().resolve("config").resolve("item-render.json");
    private final List<ItemRenderRule> rules = new ArrayList<ItemRenderRule>();
    private boolean dirty;
    private long lastSaveMs;

    private ItemRenderManager() {
        this.load();
    }

    public static ItemRenderManager getInstance() {
        return INSTANCE;
    }

    public List<ItemRenderRule> getRules() {
        return Collections.unmodifiableList(this.rules);
    }

    public ItemRenderRule addRule() {
        ItemRenderRule rule = new ItemRenderRule();
        this.rules.add(rule);
        this.markDirty();
        return rule;
    }

    public void removeRule(ItemRenderRule rule) {
        if (rule != null && this.rules.remove(rule)) {
            this.markDirty();
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void flushAutoSave() {
        if (!this.dirty) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastSaveMs >= 120L) {
            this.save();
        }
    }

    public void saveNow() {
        if (this.dirty) {
            this.save();
        }
    }

    public boolean isValidItemId(String raw) {
        Identifier id = ItemRenderManager.parseId(raw);
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    public ItemRenderRule findMatching(ItemEntity entity) {
        return entity == null ? null : this.findMatching(entity.getItem());
    }

    public ItemRenderRule findMatching(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Identifier actualId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String displayName = stack.getDisplayName().getString();
        String loweredName = displayName == null ? "" : displayName.toLowerCase(Locale.ROOT);
        for (ItemRenderRule rule : this.rules) {
            String needle;
            if (rule.checkId) {
                Identifier configured = ItemRenderManager.parseId(rule.itemId);
                if (actualId == null || configured == null || !configured.equals((Object)actualId)) continue;
            }
            if (rule.checkName && ((needle = rule.nameContains == null ? "" : rule.nameContains.trim().toLowerCase(Locale.ROOT)).isEmpty() || !loweredName.contains(needle))) continue;
            return rule;
        }
        return null;
    }

    public float getScale(ItemEntity entity) {
        ItemRenderRule rule = this.findMatching(entity);
        return rule == null ? 1.0f : ItemRenderManager.clampScale(rule.groundScale);
    }

    public ItemRenderRule getGlowRule(ItemEntity entity) {
        ItemRenderRule rule = this.findMatching(entity);
        return rule != null && rule.glow ? rule : null;
    }

    public static float clampScale(float value) {
        return Math.max(0.25f, Math.min(5.0f, value));
    }

    public static int normalizeGlowColor(int color) {
        return 0xFF000000 | color & 0xFFFFFF;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean reloadFromDisk() {
        try {
            Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
            if (!Files.exists(this.file, new LinkOption[0])) {
                return false;
            }
            ArrayList<ItemRenderRule> loaded = new ArrayList<ItemRenderRule>();
            try (BufferedReader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8);){
                SaveData data = (SaveData)GSON.fromJson((Reader)reader, SaveData.class);
                if (data == null) {
                    boolean bl = false;
                    return bl;
                }
                if (data.rules != null) {
                    for (ItemRenderRule rule : data.rules) {
                        if (rule == null) continue;
                        if (rule.id == null || rule.id.isBlank()) {
                            rule.id = UUID.randomUUID().toString();
                        }
                        if (rule.itemId == null || rule.itemId.isBlank()) {
                            rule.itemId = "minecraft:diamond_sword";
                        }
                        if (rule.nameContains == null) {
                            rule.nameContains = "";
                        }
                        rule.groundScale = ItemRenderManager.clampScale(rule.groundScale <= 0.0f ? 1.0f : rule.groundScale);
                        rule.glowColor = ItemRenderManager.normalizeGlowColor(rule.glowColor);
                        loaded.add(rule);
                    }
                }
            }
            this.rules.clear();
            this.rules.addAll(loaded);
            this.dirty = false;
            this.lastSaveMs = System.currentTimeMillis();
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private void load() {
        this.reloadFromDisk();
    }

    private void save() {
        try {
            Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
            SaveData data = new SaveData();
            data.rules = new ArrayList<ItemRenderRule>(this.rules);
            try (BufferedWriter writer = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
                GSON.toJson((Object)data, (Appendable)writer);
            }
            this.dirty = false;
            this.lastSaveMs = System.currentTimeMillis();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static Identifier parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Identifier.parse((String)raw.trim().toLowerCase(Locale.ROOT));
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static final class SaveData {
        List<ItemRenderRule> rules = new ArrayList<ItemRenderRule>();

        private SaveData() {
        }
    }
}

