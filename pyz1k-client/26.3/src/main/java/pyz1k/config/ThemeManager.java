/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import pyz1k.Pyz1k;
import pyz1k.config.ConfigManager;
import pyz1k.utilites.color.ClientTheme;

public final class ThemeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int NAME_MAX_LENGTH = 50;
    private static final int BUILTIN_VERSION = 3;
    private static final String FILE_SUFFIX = ".theme.json";
    private final Path themeDir = ConfigManager.getRootDir().resolve("themes");

    public ThemeManager() {
        try {
            Files.createDirectories(this.themeDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.ensureBuiltins();
    }

    public List<ThemeEntry> listThemes() {
        ArrayList<ThemeEntry> entries = new ArrayList<ThemeEntry>();
        try {
            Files.createDirectories(this.themeDir, new FileAttribute[0]);
            try (Stream<Path> paths = Files.list(this.themeDir);){
                paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(FILE_SUFFIX)).forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String name = fileName.substring(0, fileName.length() - FILE_SUFFIX.length());
                    entries.add(new ThemeEntry(name, this.resolveCreatedAt((Path)path), this.previewColor((Path)path)));
                });
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        entries.sort((a, b) -> {
            if ("Dark".equalsIgnoreCase(a.name())) {
                return -1;
            }
            if ("Dark".equalsIgnoreCase(b.name())) {
                return 1;
            }
            if ("Light".equalsIgnoreCase(a.name())) {
                return -1;
            }
            return "Light".equalsIgnoreCase(b.name()) ? 1 : a.name().compareToIgnoreCase(b.name());
        });
        return entries;
    }

    public String createTheme() {
        String base = "Новая тема";
        String candidate = base;
        int index = 2;
        List<String> names = this.listThemes().stream().map(ThemeEntry::name).toList();
        while (this.containsIgnoreCase(names, (String)candidate)) {
            candidate = base + " " + index++;
        }
        if (!this.saveTheme((String)candidate)) {
            return null;
        }
        ClientTheme.setActiveThemeName((String)candidate);
        Pyz1k.getInstance().markConfigDirty();
        return candidate;
    }

    public boolean saveTheme(String name) {
        String safe = this.sanitize(name);
        if (safe != null && !this.isBuiltin(safe)) {
            ThemeFile file = new ThemeFile();
            file.version = 2;
            file.createdAtMs = this.resolveCreatedAt(this.themePath(safe));
            file.light = ClientTheme.isLightMode();
            file.colors = ClientTheme.captureColors();
            return this.writeTheme(safe, file);
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean loadTheme(String name) {
        String safe = this.sanitize(name);
        if (safe == null) {
            return false;
        }
        if ("Dark".equalsIgnoreCase(safe)) {
            ClientTheme.setLightMode(false);
            Pyz1k.getInstance().markConfigDirty();
            return true;
        }
        if ("Light".equalsIgnoreCase(safe)) {
            ClientTheme.setLightMode(true);
            Pyz1k.getInstance().markConfigDirty();
            return true;
        }
        if ("Rainbow".equalsIgnoreCase(safe)) {
            ClientTheme.enableRainbowMode();
            Pyz1k.getInstance().markConfigDirty();
            return true;
        }
        Path path = this.themePath(safe);
        if (!Files.exists(path, new LinkOption[0])) {
            return false;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            ThemeFile file = (ThemeFile)GSON.fromJson((Reader)reader, ThemeFile.class);
            if (file == null) {
                boolean bl = false;
                return bl;
            }
            if (file.rainbow || "Rainbow".equalsIgnoreCase(safe)) {
                ClientTheme.enableRainbowMode();
                Pyz1k.getInstance().markConfigDirty();
                boolean bl = true;
                return bl;
            }
            if (file.colors != null && !file.colors.isEmpty()) {
                ClientTheme.setLightMode(file.light);
                ClientTheme.applyColors(file.colors);
                ClientTheme.setActiveThemeName(safe);
                Pyz1k.getInstance().markConfigDirty();
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public boolean deleteTheme(String name) {
        String safe = this.sanitize(name);
        if (safe != null && !this.isBuiltin(safe)) {
            try {
                boolean deleted = Files.deleteIfExists(this.themePath(safe));
                if (deleted && safe.equalsIgnoreCase(ClientTheme.getActiveThemeName())) {
                    this.loadTheme(ClientTheme.isLightMode() ? "Light" : "Dark");
                }
                return deleted;
            }
            catch (IOException ignored) {
                return false;
            }
        }
        return false;
    }

    public String renameTheme(String from, String to) {
        String safeFrom = this.sanitize(from);
        String safeTo = this.sanitize(to);
        if (safeFrom != null && safeTo != null && !this.isBuiltin(safeFrom) && !this.isBuiltin(safeTo)) {
            if (safeFrom.equals(safeTo)) {
                return safeFrom;
            }
            for (ThemeEntry entry : this.listThemes()) {
                if (entry.name().equalsIgnoreCase(safeFrom) || !entry.name().equalsIgnoreCase(safeTo)) continue;
                return null;
            }
            Path source = this.themePath(safeFrom);
            Path target = this.themePath(safeTo);
            if (Files.isRegularFile(source, new LinkOption[0]) && !Files.exists(target, new LinkOption[0])) {
                try {
                    Files.move(source, target, new CopyOption[0]);
                    if (safeFrom.equalsIgnoreCase(ClientTheme.getActiveThemeName())) {
                        ClientTheme.setActiveThemeName(safeTo);
                    }
                    Pyz1k.getInstance().markConfigDirty();
                    return safeTo;
                }
                catch (IOException ignored) {
                    return null;
                }
            }
            return null;
        }
        return null;
    }

    public void reload() {
        this.ensureBuiltins();
    }

    public void openThemeFolder() {
        try {
            Desktop desktop;
            Files.createDirectories(this.themeDir, new FileAttribute[0]);
            if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.OPEN)) {
                desktop.open(this.themeDir.toFile());
                return;
            }
            new ProcessBuilder("explorer.exe", this.themeDir.toAbsolutePath().toString()).start();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean isBuiltin(String name) {
        return "Dark".equalsIgnoreCase(name) || "Light".equalsIgnoreCase(name) || "Rainbow".equalsIgnoreCase(name);
    }

    private void ensureBuiltins() {
        Path rainbow;
        Path light;
        try {
            Files.createDirectories(this.themeDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Path dark = this.themePath("Dark");
        if (this.requiresBuiltinRefresh(dark)) {
            ThemeFile file = new ThemeFile();
            file.version = 3;
            file.createdAtMs = System.currentTimeMillis();
            file.light = false;
            file.colors = ClientTheme.darkPreset();
            this.writeTheme("Dark", file);
        }
        if (this.requiresBuiltinRefresh(light = this.themePath("Light"))) {
            ThemeFile file = new ThemeFile();
            file.version = 3;
            file.createdAtMs = System.currentTimeMillis();
            file.light = true;
            file.colors = ClientTheme.lightPreset();
            this.writeTheme("Light", file);
        }
        if (this.requiresBuiltinRefresh(rainbow = this.themePath("Rainbow"))) {
            ThemeFile file = new ThemeFile();
            file.version = 3;
            file.createdAtMs = System.currentTimeMillis();
            file.light = false;
            file.rainbow = true;
            file.colors = ClientTheme.darkPreset();
            this.writeTheme("Rainbow", file);
        }
    }

    private boolean writeTheme(String name, ThemeFile file) {
        try {
            Files.createDirectories(this.themeDir, new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(this.themePath(name), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
                GSON.toJson((Object)file, (Appendable)writer);
            }
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    private boolean requiresBuiltinRefresh(Path path) {
        if (!Files.exists(path)) return true;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ThemeFile file = GSON.fromJson(reader, ThemeFile.class);
            return file == null || file.version < 3;
        } catch (Exception ignored) { return true; }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private int previewColor(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            ThemeFile file = (ThemeFile)GSON.fromJson((Reader)reader, ThemeFile.class);
            if (file != null && file.rainbow) {
                int n2 = -45620;
                return n2;
            }
            Integer color = file != null && file.colors != null ? file.colors.get("accent") : null;
            int n = color == null ? ClientTheme.getAccentSetting().getColor() : color.intValue();
            return n;
        }
        catch (Exception ignored) {
            return ClientTheme.getAccentSetting().getColor();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private long resolveCreatedAt(Path path) {
        if (!Files.exists(path, new LinkOption[0])) {
            return System.currentTimeMillis();
        }
        try (BufferedReader reader2222 = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            ThemeFile file = (ThemeFile)GSON.fromJson((Reader)reader2222, ThemeFile.class);
            if (file != null && file.createdAtMs > 0L) {
                long l = file.createdAtMs;
                return l;
            }
        }
        catch (Exception reader2222) {
            // empty catch block
        }
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]).creationTime().toMillis();
        }
        catch (Exception ignored) {
            return System.currentTimeMillis();
        }
    }

    private Path themePath(String name) {
        return this.themeDir.resolve(name + FILE_SUFFIX);
    }

    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String safe = input.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safe.isEmpty()) {
            return null;
        }
        if (safe.length() > 50) {
            safe = safe.substring(0, 50);
        }
        return safe;
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        for (String value : values) {
            if (!value.equalsIgnoreCase(target)) continue;
            return true;
        }
        return false;
    }

    private static final class ThemeFile {
        int version;
        long createdAtMs;
        boolean light;
        boolean rainbow;
        Map<String, Integer> colors;

        private ThemeFile() {
        }
    }

    public record ThemeEntry(String name, long createdAtMs, int previewColor) {
    }
}

