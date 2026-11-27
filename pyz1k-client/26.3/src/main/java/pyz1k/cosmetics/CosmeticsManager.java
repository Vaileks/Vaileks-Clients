/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package pyz1k.cosmetics;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import pyz1k.Pyz1k;
import pyz1k.config.ConfigManager;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticCatalog;
import pyz1k.cosmetics.CosmeticHat;
import pyz1k.cosmetics.KirbyMesh;

public final class CosmeticsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final CosmeticsManager INSTANCE = new CosmeticsManager();
    private static final long SAVE_DELAY_MS = 120L;
    private final Path file = ConfigManager.getRootDir().resolve("config").resolve("cosmetics.json");
    private final Map<String, ItemStack> stackCache = new HashMap<String, ItemStack>();
    private String selectedId = "";
    private boolean chickenSelected;
    private boolean kirbySelected;
    private boolean onlyWithHelmet = true;
    private boolean dirty;
    private long lastSaveMs;

    private CosmeticsManager() {
        this.load();
    }

    public static CosmeticsManager getInstance() {
        return INSTANCE;
    }

    public List<CosmeticHat> getHats() {
        return CosmeticCatalog.hats();
    }

    public CosmeticHat getSelected() {
        if (this.selectedId == null || this.selectedId.isBlank()) {
            return null;
        }
        for (CosmeticHat hat : CosmeticCatalog.hats()) {
            if (!hat.id().equals(this.selectedId)) continue;
            return hat;
        }
        return null;
    }

    public boolean isSelected(CosmeticHat hat) {
        return hat != null && hat.id().equals(this.selectedId);
    }

    public void select(CosmeticHat hat) {
        String next;
        String string = next = hat == null ? "" : hat.id();
        if (!next.equals(this.selectedId)) {
            this.selectedId = next;
            this.markDirty();
        }
    }

    public void clearSelection() {
        this.select(null);
    }

    public boolean isChickenSelected() {
        return this.chickenSelected;
    }

    public void toggleChicken() {
        boolean bl = this.chickenSelected = !this.chickenSelected;
        if (this.chickenSelected) {
            this.kirbySelected = false;
        }
        this.markDirty();
    }

    public boolean isKirbySelected() {
        return this.kirbySelected;
    }

    public void toggleKirby() {
        boolean bl = this.kirbySelected = !this.kirbySelected;
        if (this.kirbySelected) {
            this.chickenSelected = false;
        }
        this.markDirty();
    }

    public boolean isCustomModelAvailable() {
        return this.chickenSelected && ChickenMesh.isAvailable() || this.kirbySelected && KirbyMesh.isAvailable();
    }

    public boolean isOnlyWithHelmet() {
        return this.onlyWithHelmet;
    }

    public void setOnlyWithHelmet(boolean onlyWithHelmet) {
        if (this.onlyWithHelmet == onlyWithHelmet) {
            return;
        }
        this.onlyWithHelmet = onlyWithHelmet;
        this.markDirty();
    }

    public void toggleHelmetMode() {
        this.setOnlyWithHelmet(!this.onlyWithHelmet);
    }

    public ItemStack getStack(CosmeticHat hat) {
        if (net.minecraft.client.Minecraft.getInstance().level == null) return ItemStack.EMPTY;
        if (hat == null) {
            return ItemStack.EMPTY;
        }
        return this.stackCache.computeIfAbsent(hat.id(), id -> {
            ItemStack stack = pyz1k.compat.PreviewStacks.of((ItemLike)Items.CARVED_PUMPKIN);
            stack.set(DataComponents.ITEM_MODEL, hat.itemModel());
            return stack;
        });
    }

    public ItemStack getSelectedStack() {
        CosmeticHat selected = this.getSelected();
        return selected == null ? ItemStack.EMPTY : this.getStack(selected);
    }

    public void markDirty() {
        this.dirty = true;
        Pyz1k client = Pyz1k.getInstance();
        if (client != null && client.getConfigManager() != null) {
            client.markConfigDirty();
        }
    }

    public ConfigState captureConfigState() {
        ConfigState state = new ConfigState();
        state.selectedHatId = this.selectedId == null ? "" : this.selectedId;
        state.chickenSelected = this.chickenSelected;
        state.kirbySelected = this.kirbySelected;
        state.onlyWithHelmet = this.onlyWithHelmet;
        return state;
    }

    public void applyConfigState(ConfigState state) {
        if (state == null) {
            return;
        }
        this.selectedId = state.selectedHatId == null ? "" : state.selectedHatId;
        this.chickenSelected = state.chickenSelected;
        this.kirbySelected = state.kirbySelected;
        if (this.kirbySelected) {
            this.chickenSelected = false;
        }
        if (state.onlyWithHelmet != null) {
            this.onlyWithHelmet = state.onlyWithHelmet;
        }
        this.dirty = true;
        this.save();
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
            Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
            if (!Files.exists(this.file, new LinkOption[0])) {
                return false;
            }
            String loadedSelectedId = "";
            boolean loadedOnlyWithHelmet = true;
            boolean loadedChickenSelected = false;
            boolean loadedKirbySelected = false;
            try (BufferedReader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8);){
                SaveData data = (SaveData)GSON.fromJson((Reader)reader, SaveData.class);
                if (data == null) {
                    boolean bl = false;
                    return bl;
                }
                if (data.selectedId != null) {
                    loadedSelectedId = data.selectedId;
                }
                loadedChickenSelected = data.chickenSelected;
                loadedKirbySelected = data.kirbySelected;
                if (data.onlyWithHelmet != null) {
                    loadedOnlyWithHelmet = data.onlyWithHelmet;
                }
            }
            this.selectedId = loadedSelectedId;
            this.chickenSelected = loadedChickenSelected;
            this.kirbySelected = loadedKirbySelected;
            if (this.kirbySelected) {
                this.chickenSelected = false;
            }
            this.onlyWithHelmet = loadedOnlyWithHelmet;
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
            data.selectedId = this.selectedId == null ? "" : this.selectedId;
            data.chickenSelected = this.chickenSelected;
            data.kirbySelected = this.kirbySelected;
            data.onlyWithHelmet = this.onlyWithHelmet;
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

    public static final class ConfigState {
        public String selectedHatId = "";
        public boolean chickenSelected;
        public boolean kirbySelected;
        public Boolean onlyWithHelmet;
    }

    private static final class SaveData {
        String selectedId = "";
        boolean chickenSelected;
        boolean kirbySelected;
        Boolean onlyWithHelmet;

        private SaveData() {
        }
    }
}

