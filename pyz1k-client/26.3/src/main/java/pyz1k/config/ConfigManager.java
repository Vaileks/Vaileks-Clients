/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  lombok.Generated
 *  net.fabricmc.loader.api.FabricLoader
 */
package pyz1k.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleManager;
import pyz1k.module.bind.BindData;
import pyz1k.module.bind.BindMode;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.module.setting.AbstractSetting;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.InputSetting;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.Setting;
import pyz1k.module.setting.SliderSetting;
import pyz1k.screen.screen.HudElement;
import pyz1k.utilites.color.ClientTheme;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String AUTO_CONFIG_NAME = "auto";
    private static final int CONFIG_NAME_MAX_LENGTH = 50;
    private static final long AUTO_SAVE_INTERVAL_MS = 150L;
    private final ModuleManager moduleManager;
    private final Path configDir;
    private final MenuState menuState = new MenuState();
    private boolean dirty = true;
    private boolean loading;
    private long lastAutoSaveMs;
    private String loadedConfigName = "auto";

    public ConfigManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        Path legacyFabricConfigDir = FabricLoader.getInstance().getConfigDir().resolve("divine");
        Path legacyRootConfigDir = ConfigManager.getLegacyRootDir().resolve("config");
        this.configDir = ConfigManager.getRootDir().resolve("config");
        try {
            Files.createDirectories(this.configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.migrateLegacyConfigs(legacyFabricConfigDir);
        this.migrateLegacyConfigs(legacyRootConfigDir);
        this.loadOrCreateAuto();
    }

    public static Path getRootDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return Path.of(System.getProperty("user.home", "."), "Pyz1k");
        }
        String drive = System.getenv("SystemDrive");
        return Path.of((String)(drive != null && !drive.isBlank() ? drive + "\\" : "C:\\"), "Pyz1k");
    }

    private static Path getLegacyRootDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return Path.of(System.getProperty("user.home", "."), "Divine");
        }
        String drive = System.getenv("SystemDrive");
        return Path.of((String)(drive != null && !drive.isBlank() ? drive + "\\" : "C:\\"), "Divine");
    }

    private void migrateLegacyConfigs(Path legacyConfigDir) {
        if (legacyConfigDir != null && !legacyConfigDir.equals(this.configDir) && Files.isDirectory(legacyConfigDir, new LinkOption[0])) {
            try (Stream<Path> paths = Files.list(legacyConfigDir);){
                paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).forEach(path -> {
                    Path target = this.configDir.resolve(path.getFileName().toString());
                    if (!Files.exists(target, new LinkOption[0])) {
                        try {
                            Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                });
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public void markDirty() {
        if (!this.loading) {
            this.dirty = true;
        }
    }

    public void flushAutoSave() {
        long now;
        if (!this.loading && this.dirty && (now = System.currentTimeMillis()) - this.lastAutoSaveMs >= 150L) {
            this.saveConfig(AUTO_CONFIG_NAME);
        }
    }

    public List<String> listConfigNames() {
        return this.listConfigEntries().stream().map(ConfigEntry::name).toList();
    }

    public List<ConfigEntry> listConfigEntries() {
        ArrayList<ConfigEntry> entries = new ArrayList<ConfigEntry>();
        try {
            if (!Files.exists(this.configDir, new LinkOption[0])) {
                Files.createDirectories(this.configDir, new FileAttribute[0]);
            }
            Files.list(this.configDir).filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).filter(path -> {
                String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                return !"item-render.json".equals(fileName) && !"hand-render.json".equals(fileName);
            }).forEach(path -> {
                String fileName = path.getFileName().toString();
                String name = fileName.substring(0, fileName.length() - 5);
                long createdAt = this.resolveCreatedAt((Path)path);
                entries.add(new ConfigEntry(name, createdAt));
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
        entries.sort((a, b) -> {
            if (AUTO_CONFIG_NAME.equalsIgnoreCase(a.name())) {
                return -1;
            }
            return AUTO_CONFIG_NAME.equalsIgnoreCase(b.name()) ? 1 : a.name().compareToIgnoreCase(b.name());
        });
        return entries;
    }

    public String createDefaultConfig() {
        String base = "New Config";
        String candidate = base;
        int index = 2;
        List<String> names = this.listConfigNames();
        while (this.containsNameIgnoreCase(names, (String)candidate)) {
            if (((String)(candidate = base + " " + index++)).length() <= 50) continue;
            candidate = ((String)candidate).substring(0, 50);
        }
        return this.createConfig((String)candidate) ? candidate : null;
    }

    private boolean containsNameIgnoreCase(List<String> names, String name) {
        for (String n : names) {
            if (!n.equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private long resolveCreatedAt(Path path) {
        try (BufferedReader reader2222 = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            ConfigFile file = (ConfigFile)GSON.fromJson((Reader)reader2222, ConfigFile.class);
            if (file != null && file.createdAtMs > 0L) {
                long l = file.createdAtMs;
                return l;
            }
        }
        catch (Exception reader2222) {
            // empty catch block
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
            long fromFs = attrs.creationTime().toMillis();
            if (fromFs > 0L) {
                return fromFs;
            }
        }
        catch (Exception attrs) {
            // empty catch block
        }
        try {
            return Files.getLastModifiedTime(path, new LinkOption[0]).toMillis();
        }
        catch (IOException ignored) {
            return System.currentTimeMillis();
        }
    }

    public boolean createConfig(String name) {
        String safe = this.sanitizeConfigName(name);
        return safe == null ? false : this.saveConfig(safe);
    }

    public boolean saveConfig(String name) {
        String safe = this.sanitizeConfigName(name);
        if (safe == null) {
            return false;
        }
        ConfigFile file = this.buildSnapshot();
        Path path = this.configPath(safe);
        file.createdAtMs = this.resolveCreatedAt(path);
        try {
            Files.createDirectories(this.configDir, new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
                GSON.toJson((Object)file, (Appendable)writer);
            }
            this.dirty = false;
            this.lastAutoSaveMs = System.currentTimeMillis();
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean loadConfig(String name) {
        String safe = this.sanitizeConfigName(name);
        if (safe == null) {
            return false;
        }
        Path path = this.configPath(safe);
        if (!Files.exists(path, new LinkOption[0])) {
            return false;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            ConfigFile file = (ConfigFile)GSON.fromJson((Reader)reader, ConfigFile.class);
            if (file == null) {
                boolean bl2 = false;
                return bl2;
            }
            this.applySnapshot(file);
            this.dirty = false;
            this.lastAutoSaveMs = System.currentTimeMillis();
            this.loadedConfigName = safe;
            boolean bl = true;
            return bl;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public boolean renameConfig(String from, String to) {
        String safeFrom = this.sanitizeConfigName(from);
        String safeTo = this.sanitizeConfigName(to);
        if (safeFrom != null && safeTo != null && !AUTO_CONFIG_NAME.equalsIgnoreCase(safeFrom)) {
            Path fromPath = this.configPath(safeFrom);
            Path toPath = this.configPath(safeTo);
            if (!Files.exists(fromPath, new LinkOption[0])) {
                return false;
            }
            try {
                Files.move(fromPath, toPath, new CopyOption[0]);
                if (this.loadedConfigName != null && this.loadedConfigName.equalsIgnoreCase(safeFrom)) {
                    this.loadedConfigName = safeTo;
                }
                return true;
            }
            catch (IOException ignored) {
                return false;
            }
        }
        return false;
    }

    public boolean deleteConfig(String name) {
        String safe = this.sanitizeConfigName(name);
        if (safe != null && !AUTO_CONFIG_NAME.equalsIgnoreCase(safe)) {
            Path path = this.configPath(safe);
            try {
                boolean deleted = Files.deleteIfExists(path);
                if (deleted && this.loadedConfigName != null && this.loadedConfigName.equalsIgnoreCase(safe)) {
                    this.loadedConfigName = AUTO_CONFIG_NAME;
                }
                return deleted;
            }
            catch (IOException ignored) {
                return false;
            }
        }
        return false;
    }

    public void reload() {
        String current = this.loadedConfigName;
        if (current != null && !current.isBlank()) {
            this.loadConfig(current);
        }
        this.listConfigNames();
    }

    public void openConfigFolder() {
        try {
            Desktop desktop;
            Files.createDirectories(this.configDir, new FileAttribute[0]);
            if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.OPEN)) {
                desktop.open(this.configDir.toFile());
                return;
            }
            new ProcessBuilder("explorer.exe", this.configDir.toAbsolutePath().toString()).start();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public MenuState getMenuState() {
        MenuState copy = new MenuState();
        copy.selectedCategory = this.menuState.selectedCategory;
        copy.selectedExtension = this.menuState.selectedExtension;
        copy.settingsModule = this.menuState.settingsModule;
        copy.searchQuery = this.menuState.searchQuery;
        copy.selectedConfig = this.menuState.selectedConfig;
        copy.configInput = this.menuState.configInput;
        copy.activeTopTab = this.menuState.activeTopTab;
        copy.configsSelectedConfig = this.menuState.configsSelectedConfig;
        copy.configsScroll = this.menuState.configsScroll;
        copy.guiScalePercent = this.menuState.guiScalePercent;
        copy.lightTheme = this.menuState.lightTheme;
        copy.moduleTabScroll = new HashMap<String, Float>(this.menuState.moduleTabScroll);
        copy.selectExpanded = new HashMap<String, Boolean>(this.menuState.selectExpanded);
        copy.multiSelectExpanded = new HashMap<String, Boolean>(this.menuState.multiSelectExpanded);
        return copy;
    }

    public void updateMenuState(MenuState next) {
        if (next != null) {
            boolean changed = !Objects.equals(this.menuState.selectedCategory, next.selectedCategory) || !Objects.equals(this.menuState.selectedExtension, next.selectedExtension) || !Objects.equals(this.menuState.settingsModule, next.settingsModule) || !Objects.equals(this.menuState.searchQuery, next.searchQuery) || !Objects.equals(this.menuState.selectedConfig, next.selectedConfig) || !Objects.equals(this.menuState.configInput, next.configInput) || !Objects.equals(this.menuState.activeTopTab, next.activeTopTab) || !Objects.equals(this.menuState.configsSelectedConfig, next.configsSelectedConfig) || this.menuState.configsScroll != next.configsScroll || this.menuState.guiScalePercent != next.guiScalePercent || this.menuState.lightTheme != next.lightTheme || !Objects.equals(this.menuState.moduleTabScroll, next.moduleTabScroll) || !Objects.equals(this.menuState.selectExpanded, next.selectExpanded) || !Objects.equals(this.menuState.multiSelectExpanded, next.multiSelectExpanded);
            this.menuState.selectedCategory = next.selectedCategory;
            this.menuState.selectedExtension = next.selectedExtension;
            this.menuState.settingsModule = next.settingsModule;
            this.menuState.searchQuery = next.searchQuery;
            this.menuState.selectedConfig = next.selectedConfig;
            this.menuState.configInput = next.configInput;
            this.menuState.activeTopTab = next.activeTopTab;
            this.menuState.configsSelectedConfig = next.configsSelectedConfig;
            this.menuState.configsScroll = next.configsScroll;
            this.menuState.guiScalePercent = next.guiScalePercent <= 0 ? 100 : Math.clamp((long)next.guiScalePercent, 75, 125);
            this.menuState.lightTheme = next.lightTheme;
            this.menuState.moduleTabScroll = next.moduleTabScroll == null ? new HashMap<String, Float>() : new HashMap<String, Float>(next.moduleTabScroll);
            this.menuState.selectExpanded = next.selectExpanded == null ? new HashMap<String, Boolean>() : new HashMap<String, Boolean>(next.selectExpanded);
            Map<String, Boolean> map = this.menuState.multiSelectExpanded = next.multiSelectExpanded == null ? new HashMap<String, Boolean>() : new HashMap<String, Boolean>(next.multiSelectExpanded);
            if (changed) {
                this.markDirty();
            }
        }
    }

    public void updateMenuState(String selectedCategory, String selectedExtension, String settingsModule, String searchQuery, String selectedConfig, String configInput) {
        MenuState next = this.getMenuState();
        next.selectedCategory = selectedCategory;
        next.selectedExtension = selectedExtension;
        next.settingsModule = settingsModule;
        next.searchQuery = searchQuery;
        next.selectedConfig = selectedConfig;
        next.configInput = configInput;
        this.updateMenuState(next);
    }

    private void loadOrCreateAuto() {
        if (!this.loadConfig(AUTO_CONFIG_NAME)) {
            this.saveConfig(AUTO_CONFIG_NAME);
        }
    }

    private ConfigFile buildSnapshot() {
        ConfigFile file = new ConfigFile();
        file.version = 4;
        file.menuState = this.getMenuState();
        file.menuState.lightTheme = ClientTheme.isLightMode();
        file.themeAccentColor = ClientTheme.getAccentSetting().getColor();
        file.themePresetName = ClientTheme.getActiveThemeName();
        file.themeColors = ClientTheme.captureColors();
        file.modules = new ArrayList<ModuleState>();
        for (Module module : this.moduleManager.getModules()) {
            ModuleState moduleState = new ModuleState();
            moduleState.name = module.getName();
            moduleState.enabled = module.isEnable();
            moduleState.key = module.getKey();
            moduleState.bind = this.captureBind(module.getBind());
            moduleState.settings = new ArrayList<SettingState>();
            for (Setting setting : module.getSettings()) {
                SettingState settingState = this.captureSetting(setting);
                if (settingState == null) continue;
                moduleState.settings.add(settingState);
            }
            file.modules.add(moduleState);
        }
        Interface interfaceModule = this.moduleManager.getModule(Interface.class);
        if (interfaceModule != null) {
            file.hudElements = new ArrayList<HudElementState>();
            for (HudElement hudElement : interfaceModule.getHudElements()) {
                HudElementState hudState = new HudElementState();
                hudState.id = hudElement.getId();
                hudState.x = Float.valueOf(hudElement.getX());
                hudState.y = Float.valueOf(hudElement.getY());
                hudState.settings = new ArrayList<SettingState>();
                for (Setting setting : hudElement.getSettings()) {
                    SettingState settingState = this.captureSetting(setting);
                    if (settingState == null) continue;
                    hudState.settings.add(settingState);
                }
                file.hudElements.add(hudState);
            }
        }
        return file;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applySnapshot(ConfigFile file) {
        this.loading = true;
        try {
            boolean builtinTheme;
            if (file.menuState != null) {
                this.menuState.selectedCategory = file.menuState.selectedCategory;
                this.menuState.selectedExtension = file.menuState.selectedExtension;
                this.menuState.settingsModule = file.menuState.settingsModule;
                this.menuState.searchQuery = file.menuState.searchQuery;
                this.menuState.selectedConfig = file.menuState.selectedConfig;
                this.menuState.configInput = file.menuState.configInput;
                this.menuState.guiScalePercent = file.menuState.guiScalePercent <= 0 ? 100 : Math.clamp((long)file.menuState.guiScalePercent, 75, 125);
                this.menuState.lightTheme = file.menuState.lightTheme;
                ClientTheme.setLightMode(this.menuState.lightTheme);
            }
            if (file.themeAccentColor != null) {
                ClientTheme.setAccentColor(file.themeAccentColor);
            }
            boolean bl = builtinTheme = "Dark".equalsIgnoreCase(file.themePresetName) || "Light".equalsIgnoreCase(file.themePresetName) || "Rainbow".equalsIgnoreCase(file.themePresetName);
            if (!builtinTheme && file.themeColors != null && !file.themeColors.isEmpty()) {
                ClientTheme.applyColors(file.themeColors);
            }
            if (file.themePresetName != null && !file.themePresetName.isBlank()) {
                if ("Rainbow".equalsIgnoreCase(file.themePresetName)) {
                    ClientTheme.enableRainbowMode();
                } else if ("Light".equalsIgnoreCase(file.themePresetName)) {
                    ClientTheme.setLightMode(true);
                } else if ("Dark".equalsIgnoreCase(file.themePresetName)) {
                    ClientTheme.setLightMode(false);
                } else {
                    ClientTheme.setActiveThemeName(file.themePresetName);
                }
            }
            this.menuState.lightTheme = ClientTheme.isLightMode();
            if (file.modules == null) {
                return;
            }
            for (ModuleState moduleState : file.modules) {
                Module module = this.findModule(moduleState.name);
                if (module == null) continue;
                module.setKey(moduleState.key);
                if (moduleState.bind != null) {
                    this.applyBind(module.getBind(), moduleState.bind);
                }
                module.setEnable(moduleState.enabled);
                if (moduleState.settings == null) continue;
                for (SettingState settingState : moduleState.settings) {
                    Setting setting = this.findSetting(module, settingState.name);
                    if (!(setting instanceof AbstractSetting)) continue;
                    this.applySetting(setting, settingState);
                }
            }
            if (file.hudElements != null && !file.hudElements.isEmpty()) {
                this.applyHudElements(file.hudElements);
            }
        }
        finally {
            this.loading = false;
        }
    }

    private void applyHudElements(List<HudElementState> hudStates) {
        Interface interfaceModule = this.moduleManager.getModule(Interface.class);
        if (interfaceModule != null) {
            for (HudElementState hudState : hudStates) {
                HudElement element = this.findHudElement(interfaceModule, hudState.id);
                if (element == null) continue;
                if (hudState.x != null && hudState.y != null && Float.isFinite(hudState.x.floatValue()) && Float.isFinite(hudState.y.floatValue())) {
                    element.setPosition(hudState.x.floatValue(), hudState.y.floatValue());
                }
                if (hudState.settings == null) continue;
                for (SettingState settingState : hudState.settings) {
                    Setting setting = this.findSetting(element.getSettings(), settingState.name);
                    if (!(setting instanceof AbstractSetting)) continue;
                    this.applySetting(setting, settingState);
                }
            }
        }
    }

    private HudElement findHudElement(Interface interfaceModule, String id) {
        if (interfaceModule != null && id != null) {
            for (HudElement element : interfaceModule.getHudElements()) {
                if (!id.equals(element.getId())) continue;
                return element;
            }
            return null;
        }
        return null;
    }

    private Module findModule(String name) {
        if (name == null) {
            return null;
        }
        for (Module module : this.moduleManager.getModules()) {
            if (!name.equals(module.getName())) continue;
            return module;
        }
        return null;
    }

    private Setting findSetting(Module module, String name) {
        if (module != null && name != null) {
            for (Setting setting : module.getSettings()) {
                if (!name.equals(setting.getName())) continue;
                return setting;
            }
            return null;
        }
        return null;
    }

    private Setting findSetting(List<Setting> settings, String name) {
        if (settings != null && name != null) {
            for (Setting setting : settings) {
                if (!name.equals(setting.getName())) continue;
                return setting;
            }
            return null;
        }
        return null;
    }

    private BindState captureBind(BindData bind) {
        BindState state = new BindState();
        state.mode = bind.getMode() == null ? BindMode.TOGGLE.name() : bind.getMode().name();
        state.keys = new ArrayList<Integer>(bind.getKeys());
        state.active = bind.isActive();
        state.onBindEnable = bind.isOnBindEnable();
        state.showInBinds = bind.isShowInBinds();
        return state;
    }

    private void applyBind(BindData bind, BindState state) {
        if (state.mode != null) {
            try {
                bind.setMode(BindMode.valueOf(state.mode));
            }
            catch (IllegalArgumentException ignored) {
                bind.setMode(BindMode.TOGGLE);
            }
        }
        bind.setKeys(state.keys == null ? List.of() : state.keys);
        bind.setActive(state.active);
        bind.setOnBindEnable(state.onBindEnable);
        bind.setShowInBinds(state.showInBinds);
    }

    private SettingState captureSetting(Setting setting) {
        SettingState state = new SettingState();
        state.name = setting.getName();
        state.type = setting.getType().name();
        state.bind = this.captureBind(setting.getBind());
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            state.boolValue = s.isValue();
            state.boolBindOn = (Boolean)s.getBindOnValue();
            state.boolBindOff = (Boolean)s.getBindOffValue();
            return state;
        }
        if (setting instanceof SliderSetting) {
            SliderSetting s = (SliderSetting)setting;
            state.floatValue = (Float)s.getValue();
            state.floatBindOn = (Float)s.getBindOnValue();
            state.floatBindOff = (Float)s.getBindOffValue();
            return state;
        }
        if (setting instanceof SelectSetting) {
            SelectSetting s = (SelectSetting)setting;
            state.stringValue = s.getValue();
            state.stringBindOn = (String)s.getBindOnValue();
            state.stringBindOff = (String)s.getBindOffValue();
            return state;
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting s = (MultiSelectSetting)setting;
            state.listValue = new ArrayList<String>(s.getSelectedValues());
            state.listBindOn = new ArrayList<String>((Collection)s.getBindOnValue());
            state.listBindOff = new ArrayList<String>((Collection)s.getBindOffValue());
            return state;
        }
        if (setting instanceof InputSetting) {
            InputSetting s = (InputSetting)setting;
            state.stringValue = s.getValue();
            state.stringBindOn = (String)s.getBindOnValue();
            state.stringBindOff = (String)s.getBindOffValue();
            return state;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            state.intValue = s.getColor();
            state.intBindOn = (Integer)s.getBindOnValue();
            state.intBindOff = (Integer)s.getBindOffValue();
            return state;
        }
        return null;
    }

    private void applySetting(Setting setting, SettingState state) {
        if (state.bind != null) {
            this.applyBind(setting.getBind(), state.bind);
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            if (state.boolValue != null) {
                s.setValue(state.boolValue);
            }
            if (state.boolBindOn != null) {
                s.setBindOnValue(state.boolBindOn);
            }
            if (state.boolBindOff != null) {
                s.setBindOffValue(state.boolBindOff);
            }
        } else if (setting instanceof SliderSetting) {
            SliderSetting s = (SliderSetting)setting;
            if (state.floatValue != null) {
                s.setValue(state.floatValue);
            }
            if (state.floatBindOn != null) {
                s.setBindOnValue(state.floatBindOn);
            }
            if (state.floatBindOff != null) {
                s.setBindOffValue(state.floatBindOff);
            }
        } else if (setting instanceof SelectSetting) {
            SelectSetting s = (SelectSetting)setting;
            if (state.stringValue != null) {
                s.setValue(state.stringValue);
            }
            if (state.stringBindOn != null) {
                s.setBindOnValue(state.stringBindOn);
            }
            if (state.stringBindOff != null) {
                s.setBindOffValue(state.stringBindOff);
            }
        } else if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting s = (MultiSelectSetting)setting;
            if (state.listValue != null) {
                s.setValue((Set<String>)new LinkedHashSet<String>(state.listValue));
            }
            if (state.listBindOn != null) {
                s.setBindOnValue(new LinkedHashSet<String>(state.listBindOn));
            }
            if (state.listBindOff != null) {
                s.setBindOffValue(new LinkedHashSet<String>(state.listBindOff));
            }
        } else if (setting instanceof InputSetting) {
            InputSetting s = (InputSetting)setting;
            if (state.stringValue != null) {
                s.setValue(state.stringValue);
            }
            if (state.stringBindOn != null) {
                s.setBindOnValue(state.stringBindOn);
            }
            if (state.stringBindOff != null) {
                s.setBindOffValue(state.stringBindOff);
            }
        } else if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            if (state.intValue != null) {
                s.setColor(state.intValue);
            }
            if (state.intBindOn != null) {
                s.setBindOnValue(state.intBindOn);
            }
            if (state.intBindOff != null) {
                s.setBindOffValue(state.intBindOff);
            }
        }
    }

    private Path configPath(String configName) {
        return this.configDir.resolve(configName + ".json");
    }

    private String sanitizeConfigName(String input) {
        if (input == null) {
            return null;
        }
        String safe = input.trim();
        if (safe.isEmpty()) {
            return null;
        }
        if ((safe = safe.replaceAll("[\\\\/:*?\"<>|]", "_")).length() > 50) {
            safe = safe.substring(0, 50);
        }
        return safe.isEmpty() ? null : safe;
    }

    private boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public String getLoadedConfigName() {
        return this.loadedConfigName;
    }

    public static final class MenuState {
        public String selectedCategory;
        public String selectedExtension;
        public String settingsModule;
        public String searchQuery;
        public String selectedConfig;
        public String configInput;
        public String activeTopTab;
        public String configsSelectedConfig;
        public float configsScroll;
        public int guiScalePercent = 100;
        public boolean lightTheme;
        public Map<String, Float> moduleTabScroll = new HashMap<String, Float>();
        public Map<String, Boolean> selectExpanded = new HashMap<String, Boolean>();
        public Map<String, Boolean> multiSelectExpanded = new HashMap<String, Boolean>();
    }

    private static final class ConfigFile {
        int version;
        long createdAtMs;
        MenuState menuState;
        Integer themeAccentColor;
        String themePresetName;
        Map<String, Integer> themeColors;
        List<ModuleState> modules;
        List<HudElementState> hudElements;

        private ConfigFile() {
        }
    }

    private static final class ModuleState {
        String name;
        int key;
        boolean enabled;
        BindState bind;
        List<SettingState> settings;

        private ModuleState() {
        }
    }

    private static final class BindState {
        String mode;
        List<Integer> keys;
        boolean active;
        boolean onBindEnable;
        boolean showInBinds;

        private BindState() {
        }
    }

    private static final class SettingState {
        String name;
        String type;
        BindState bind;
        Boolean boolValue;
        Boolean boolBindOn;
        Boolean boolBindOff;
        Float floatValue;
        Float floatBindOn;
        Float floatBindOff;
        Integer intValue;
        Integer intBindOn;
        Integer intBindOff;
        String stringValue;
        String stringBindOn;
        String stringBindOff;
        List<String> listValue;
        List<String> listBindOn;
        List<String> listBindOff;

        private SettingState() {
        }
    }

    private static final class HudElementState {
        String id;
        Float x;
        Float y;
        List<SettingState> settings;

        private HudElementState() {
        }
    }

    public record ConfigEntry(String name, long createdAtMs) {
    }
}

