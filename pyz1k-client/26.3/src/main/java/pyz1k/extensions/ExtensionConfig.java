/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 */
package pyz1k.extensions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pyz1k.config.ConfigManager;
import pyz1k.extensions.ExtensionClient;

public final class ExtensionConfig {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = ConfigManager.getRootDir().resolve("config/vaileks-skins.json");
    private static ExtensionConfig data = new ExtensionConfig();
    public Map<String, Map<String, String>> skins = new HashMap<String, Map<String, String>>();
    public Set<String> favorites = new LinkedHashSet<String>();
    public Map<String, Map<String, String>> skinPresets = new LinkedHashMap<String, Map<String, String>>();
    public boolean automaticProfiles;
    public boolean baselineSaved;
    public Set<String> serverProfiles = new HashSet<String>();

    public void normalize() {
        if (this.skins == null) {
            this.skins = new HashMap<String, Map<String, String>>();
        }
        this.skins.values().removeIf(Objects::isNull);
        if (this.favorites == null) {
            this.favorites = new LinkedHashSet<String>();
        }
        if (this.skinPresets == null) {
            this.skinPresets = new LinkedHashMap<String, Map<String, String>>();
        }
        this.skinPresets.values().removeIf(Objects::isNull);
        if (this.serverProfiles == null) {
            this.serverProfiles = new HashSet<String>();
        }
    }

    public static ExtensionConfig get() {
        return data;
    }

    public static void load() {
        if (!Files.exists(FILE, new LinkOption[0])) {
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8);){
            ExtensionConfig loaded = (ExtensionConfig)JSON.fromJson((Reader)r, ExtensionConfig.class);
            if (loaded != null) {
                loaded.normalize();
                data = loaded;
            }
        }
        catch (Exception e) {
            ExtensionClient.LOG.warn("Cannot load skin selections", (Throwable)e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Path temp = FILE.resolveSibling(String.valueOf(FILE.getFileName()) + ".tmp");
            Files.writeString(temp, (CharSequence)JSON.toJson((Object)data), StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            ExtensionClient.LOG.warn("Cannot save skin selections", (Throwable)e);
        }
    }
}

