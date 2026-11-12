/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.abilitybind;

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
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pyz1k.compat.LegacyInput;
import pyz1k.abilitybind.AbilityBindRule;
import pyz1k.config.ConfigManager;

public final class AbilityBindManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final AbilityBindManager INSTANCE = new AbilityBindManager();
    private static final long SAVE_DELAY_MS = 120L;
    private final Path file = ConfigManager.getRootDir().resolve("config").resolve("ability-bind.json");
    private final List<AbilityBindRule> rules = new ArrayList<AbilityBindRule>();
    private int bindKey = 340;
    private boolean dirty;
    private long lastSaveMs;

    private AbilityBindManager() {
        this.load();
    }

    public static AbilityBindManager getInstance() {
        return INSTANCE;
    }

    public List<AbilityBindRule> getRules() {
        return Collections.unmodifiableList(this.rules);
    }

    public AbilityBindRule addRule() {
        AbilityBindRule rule = new AbilityBindRule();
        this.rules.add(rule);
        this.markDirty();
        return rule;
    }

    public void removeRule(AbilityBindRule rule) {
        if (rule != null && this.rules.remove(rule)) {
            this.markDirty();
        }
    }

    public void clearRules() {
        if (!this.rules.isEmpty()) {
            this.rules.clear();
            this.markDirty();
        }
    }

    public int getBindKey() {
        return this.bindKey;
    }

    public void setBindKey(int keyCode) {
        if (keyCode < 0) {
            return;
        }
        if (this.bindKey != keyCode) {
            this.bindKey = keyCode;
            this.markDirty();
        }
    }

    public String getBindLabel() {
        return AbilityBindManager.keyLabel(this.bindKey);
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isValidItemId(String raw) {
        Identifier id = AbilityBindManager.parseId(raw);
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    public AbilityBindRule findMatching(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Identifier actualId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String displayName = stack.getDisplayName().getString();
        String loweredName = displayName == null ? "" : displayName.toLowerCase(Locale.ROOT);
        for (AbilityBindRule rule : this.rules) {
            String needle;
            if (rule == null || !rule.checkId && !rule.checkName) continue;
            if (rule.checkId) {
                Identifier configured = AbilityBindManager.parseId(rule.itemId);
                if (actualId == null || configured == null || !configured.equals((Object)actualId)) continue;
            }
            if (rule.checkName && ((needle = rule.nameContains == null ? "" : rule.nameContains.trim().toLowerCase(Locale.ROOT)).isEmpty() || !loweredName.contains(needle))) continue;
            return rule;
        }
        return null;
    }

    public boolean shouldBlockUse(Player player, InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || player == null || minecraft.player != player || hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (this.findMatching(player.getItemInHand(InteractionHand.MAIN_HAND)) == null) {
            return false;
        }
        return !this.isBindDown(minecraft);
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

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean reloadFromDisk() {
        try {
            int loadedBindKey;
            ArrayList<AbilityBindRule> loadedRules;
            block26: {
                Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
                if (!Files.exists(this.file, new LinkOption[0])) {
                    return false;
                }
                loadedRules = new ArrayList<AbilityBindRule>();
                loadedBindKey = 340;
                try (BufferedReader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8);){
                    SaveData data = (SaveData)GSON.fromJson((Reader)reader, SaveData.class);
                    if (data == null) {
                        boolean bl = false;
                        return bl;
                    }
                    if (data.rules != null) {
                        for (AbilityBindRule rule : data.rules) {
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
                            loadedRules.add(rule);
                        }
                    }
                    if (data.bindKey != null && data.bindKey >= 0) {
                        loadedBindKey = data.bindKey;
                        break block26;
                    }
                    if (data.modifier != null) {
                        loadedBindKey = switch (data.modifier) {
                            case "LEFT_CONTROL" -> 341;
                            case "LEFT_ALT" -> 342;
                            default -> 340;
                        };
                    }
                }
            }
            this.rules.clear();
            this.rules.addAll(loadedRules);
            this.bindKey = loadedBindKey;
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
            data.rules = new ArrayList<AbilityBindRule>(this.rules);
            data.bindKey = this.bindKey;
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

    private boolean isBindDown(Minecraft minecraft) {
        if (minecraft == null || minecraft.getWindow() == null || this.bindKey < 0) {
            return false;
        }
        return LegacyInput.glfwGetKey((long)minecraft.getWindow().handle(), (int)this.bindKey) == 1;
    }

    private static String keyLabel(int keyCode) {
        return switch (keyCode) {
            case 340 -> "LShift";
            case 344 -> "RShift";
            case 341 -> "LCtrl";
            case 345 -> "RCtrl";
            case 342 -> "LAlt";
            case 346 -> "RAlt";
            case 32 -> "Space";
            case 257 -> "Enter";
            case 258 -> "Tab";
            case 280 -> "Caps";
            case 256 -> "Esc";
            case 259 -> "Backspace";
            case 261 -> "Delete";
            case 260 -> "Insert";
            case 268 -> "Home";
            case 269 -> "End";
            case 266 -> "PageUp";
            case 267 -> "PageDown";
            case 265 -> "Up";
            case 264 -> "Down";
            case 263 -> "Left";
            case 262 -> "Right";
            default -> {
                if (keyCode >= 290 && keyCode <= 314) {
                    yield "F" + (keyCode - 290 + 1);
                }
                String name = LegacyInput.glfwGetKeyName((int)keyCode, (int)0);
                if (name == null || name.isBlank()) {
                    yield "Key " + keyCode;
                }
                yield name.toUpperCase(Locale.ROOT);
            }
        };
    }

    private static final class SaveData {
        List<AbilityBindRule> rules = new ArrayList<AbilityBindRule>();
        Integer bindKey;
        String modifier;

        private SaveData() {
        }
    }
}

