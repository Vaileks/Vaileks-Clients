/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.CustomModelData
 *  net.minecraft.world.level.ItemLike
 */
package pyz1k.extensions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ItemLike;
import pyz1k.extensions.ExtensionClient;
import pyz1k.extensions.ExtensionConfig;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ModelVariants;
import pyz1k.extensions.SkinIndex;
import pyz1k.extensions.SkinTargets;

public final class SkinCatalog {
    public static final List<Item> TARGETS = List.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.BOW, Items.CROSSBOW, Items.TOTEM_OF_UNDYING, Items.MACE);
    private static volatile List<Skin> entries = List.of();
    private static volatile int generation;
    private static final Map<String, Skin> selected;
    private static String server;
    private static final ThreadLocal<Boolean> PREVIEW;
    private static boolean dirty;
    private static int failures;

    public static void reload(ResourceManager resources) {
        LootGloow.clearCache();
        dirty = true;
    }

    public static void session(String key) {
        server = key;
        selected.clear();
        entries = List.of();
        dirty = true;
    }

    public static void ensureLoaded() {
        if (server.isEmpty() || !dirty) {
            return;
        }
        dirty = false;
        long started = System.nanoTime();
        ArrayList<Skin> found = new ArrayList<Skin>();
        failures = 0;
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        ArrayList<SkinIndex.Definition> definitions = new ArrayList<SkinIndex.Definition>();
        try {
            for (Map.Entry entry : resources.listResources("items", id -> id.getPath().endsWith(".json")).entrySet()) {
                boolean serverPack;
                if (definitions.size() >= 8192) break;
                Identifier file = (Identifier)entry.getKey();
                Resource resource = (Resource)entry.getValue();
                if (file.getNamespace().equals("pyz1k") || resource.sourcePackId().equals("vanilla")) continue;
                String path = file.getPath().substring(6, file.getPath().length() - 5);
                boolean bl = serverPack = resource.sourcePackId().startsWith("server/") || resource.sourcePackId().startsWith("server_");
                if (!serverPack) continue;
                try {
                    InputStream stream = resource.open();
                    try {
                        byte[] bytes = stream.readNBytes(0x100001);
                        if (bytes.length > 0x100000) {
                            ++failures;
                            continue;
                        }
                        JsonObject root = JsonParser.parseString((String)new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                        Identifier model = Identifier.fromNamespaceAndPath((String)file.getNamespace(), (String)path);
                        definitions.add(new SkinIndex.Definition(model.toString(), root, resource.sourcePackId()));
                    }
                    finally {
                        if (stream == null) continue;
                        stream.close();
                    }
                }
                catch (Exception e) {
                    ++failures;
                }
            }
        }
        catch (Exception e) {
            ExtensionClient.LOG.warn("Cannot index server item skins", (Throwable)e);
        }
        HashMap<String, String> fingerprints = new HashMap<String, String>();
        Gson json = new Gson();
        for (SkinIndex.Entry entry : SkinIndex.build(definitions)) {
            SkinIndex.Definition definition = entry.definition();
            ModelVariants.Variant variant = entry.variant();
            Identifier model = Identifier.parse((String)definition.id());
            String label = variant.label().isBlank() ? model.getPath() : variant.label();
            String key = String.valueOf(model) + "|" + json.toJson((Object)variant.data()) + "|" + fingerprints.computeIfAbsent(definition.id(), id -> SkinCatalog.digest(definition.json().toString()));
            found.add(new Skin(key, model, variant.data(), label, definition.pack(), entry.material()));
        }
        found.sort(Comparator.comparing(Skin::name));
        entries = List.copyOf(found);
        ++generation;
        selected.clear();
        Map<String, String> saved = ExtensionConfig.get().skins.getOrDefault(server, Map.of());
        for (Map.Entry<String, String> entry : saved.entrySet()) {
            found.stream().filter(s -> s.key.equals(entry.getValue())).findFirst().filter(s -> SkinTargets.matches(s.inferredTarget(), Identifier.parse((String)((String)entry.getKey())).getPath())).ifPresent(s -> selected.put((String)entry.getKey(), (Skin)s));
        }
        ExtensionClient.LOG.info("Skin catalog: {} variants, {} invalid definitions, {} ms", new Object[]{found.size(), failures, (System.nanoTime() - started) / 1000000L});
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)), 0, 8);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean compatible(Skin skin, Item target) {
        return skin != null && SkinTargets.matches(skin.inferredTarget(), BuiltInRegistries.ITEM.getKey(target).getPath());
    }

    public static List<Skin> entries() {
        return entries;
    }

    public static int generation() {
        return generation;
    }

    public static int failures() {
        return failures;
    }

    public static boolean onServer() {
        return !server.isEmpty();
    }

    public static Skin chosen(Item target) {
        return selected.get(BuiltInRegistries.ITEM.getKey(target).toString());
    }

    public static void select(Item target, Skin skin) {
        if (server.isEmpty() || skin != null && !SkinCatalog.compatible(skin, target)) {
            return;
        }
        String item = BuiltInRegistries.ITEM.getKey(target).toString();
        Map choices = ExtensionConfig.get().skins.computeIfAbsent(server, k -> new HashMap());
        if (skin == null) {
            selected.remove(item);
            choices.remove(item);
        } else {
            selected.put(item, skin);
            choices.put(item, skin.key);
        }
        ExtensionConfig.save();
    }

    public static String server() {
        return server;
    }

    public static boolean favorite(Skin skin) {
        return ExtensionConfig.get().favorites.contains(skin.key());
    }

    public static void toggleFavorite(Skin skin) {
        Set<String> favorites = ExtensionConfig.get().favorites;
        if (!favorites.remove(skin.key())) {
            favorites.add(skin.key());
        }
        ExtensionConfig.save();
    }

    public static List<String> presetNames() {
        return ExtensionConfig.get().skinPresets.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public static boolean savePreset(String name) {
        name = name.strip();
        if (server.isEmpty() || name.isEmpty() || name.length() > 40) {
            return false;
        }
        ExtensionConfig config = ExtensionConfig.get();
        if (config.skinPresets.size() >= 64 && !config.skinPresets.containsKey(name)) {
            return false;
        }
        config.skinPresets.put(name, new HashMap(config.skins.getOrDefault(server, Map.of())));
        ExtensionConfig.save();
        return true;
    }

    public static int applyPreset(String name) {
        SkinCatalog.ensureLoaded();
        if (server.isEmpty()) {
            return 0;
        }
        Map<String, String> preset = ExtensionConfig.get().skinPresets.get(name);
        if (preset == null) {
            return 0;
        }
        int applied = 0;
        for (Item target : TARGETS) {
            String key = preset.get(BuiltInRegistries.ITEM.getKey(target).toString());
            Skin skin = entries.stream().filter(s -> s.key().equals(key) && SkinCatalog.compatible(s, target)).findFirst().orElse(null);
            SkinCatalog.select(target, skin);
            if (skin == null) continue;
            ++applied;
        }
        return applied;
    }

    public static void deletePreset(String name) {
        ExtensionConfig.get().skinPresets.remove(name);
        ExtensionConfig.save();
    }

    public static ItemStack displayCopy(ItemStack stack, ItemDisplayContext context, ItemOwner holder) {
        boolean gui;
        FeatureModules.Skins module = FeatureModules.skins;
        if(module==null||!module.isEnable())return stack;
        Minecraft mc = Minecraft.getInstance();
        if (PREVIEW.get().booleanValue() || module == null || !module.isEnable() || mc.player == null || server.isEmpty() || stack.isEmpty()) {
            return stack;
        }
        boolean bl = gui = context == ItemDisplayContext.GUI;
        if (gui ? !module.inventory.isValue() || !SkinCatalog.isLocalStack(stack, mc) : holder == null || holder.asLivingEntity() != mc.player) {
            return stack;
        }
        Skin skin = SkinCatalog.chosen(stack.getItem());
        if (!SkinCatalog.compatible(skin, stack.getItem())) {
            return stack;
        }
        ItemStack copy = stack.copy();
        skin.apply(copy);
        return copy;
    }

    private static boolean isLocalStack(ItemStack stack, Minecraft mc) {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); ++i) {
            if (mc.player.getInventory().getItem(i) != stack) continue;
            return true;
        }
        return mc.player.containerMenu.getCarried() == stack;
    }

    public static void drawOriginal(Runnable action) {
        boolean prev = PREVIEW.get();
        PREVIEW.set(true);
        try {
            action.run();
        }
        finally {
            PREVIEW.set(prev);
        }
    }

    static {
        selected = new HashMap<String, Skin>();
        server = "";
        PREVIEW = ThreadLocal.withInitial(() -> false);
        dirty = true;
    }

    public record Skin(String key, Identifier model, ModelVariants.Data data, String name, String pack, String inferredTarget) {
        public ItemStack preview(Item target) {
            ItemStack stack = pyz1k.compat.PreviewStacks.of((ItemLike)target);
            if (!stack.isEmpty()) this.apply(stack);
            return stack;
        }

        public void apply(ItemStack stack) {
            stack.set(DataComponents.ITEM_MODEL, this.model);
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(this.data.floats(), this.data.flags(), this.data.strings(), this.data.colors()));
        }
    }
}

