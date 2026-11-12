/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package pyz1k.extensions.pvp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pyz1k.extensions.pvp.Core;
import pyz1k.extensions.pvp.Feature;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class Config {
 private transient Config layoutCache;
 Config layoutCopy(){
  if(layoutCache==null)layoutCache=new Config();Config c=layoutCache;
  c.scale=scale;c.shadow=shadow;c.background=background;c.icons=icons;c.color=color;c.accent=accent;c.x=x;c.y=y;return c;
 }

    public String searchQuery="";
 public boolean searchPersistent=true;
 public boolean enabled = false;
    public boolean shadow = true;
    public boolean icons = false;
    public boolean background = true;
    public boolean sound = false;
    public boolean manualPosition = false;
    public boolean hideScoreboard = true;
    public boolean hideBossbar = true;
    public boolean hideItemName = true;
    public boolean hideEffects = false;
    public double scale = 0.9;
    public double x = 0.03;
    public double y = 0.15;
    public int color = 0xFFFFFF;
    public int accent = 7920895;
    public int threshold = 3;
    public int mode = 0;
    public int actionKey = -1;
    public int holdKey = 72;
    public List<SavedItem> kit = new ArrayList<SavedItem>();
    public List<SavedItem> hotbar = new ArrayList<SavedItem>();
    public Map<String, Integer> notes = new LinkedHashMap<String, Integer>();
    public Map<String, Config> profiles = new LinkedHashMap<String, Config>();
    static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();

    static Path path(Feature f) {
        return FabricLoader.getInstance().getConfigDir().resolve("pyz1k-pvp").resolve(f.id + ".json");
    }

    static Config defaults(Feature f) {
        Config c = new Config();
        c.threshold = f.threshold;
        c.y = 0.06 + (double)f.ordinal() * 0.045;
        c.x = f.ordinal() > 9 ? 0.65 : 0.03;
        c.y = Math.min(c.y, 0.83);
        return c;
    }

    static Config load(Feature f) {
        Path path = Config.path(f);
        try {
            Config c;
            if (Files.exists(path, new LinkOption[0]) && (c = (Config)JSON.fromJson(Files.readString(path), Config.class)) != null) {
                c.sanitize(f);
                return c;
            }
        }
        catch (Exception e) {
            Core.LOG.warn("Cannot read {}. Preserving file; using defaults.", (Object)path, (Object)e);
            try {
                Files.copy(path, path.resolveSibling(String.valueOf(path.getFileName()) + ".invalid-" + System.currentTimeMillis()), new CopyOption[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return Config.defaults(f);
    }

    void sanitize(Feature f) {
  if(searchQuery==null)searchQuery="";if(searchQuery.length()>80)searchQuery=searchQuery.substring(0,80);

        this.scale = Double.isFinite(this.scale) ? Math.clamp(this.scale, 0.5, 2.5) : 0.9;
        this.x = Double.isFinite(this.x) ? Math.clamp(this.x, 0.0, 1.0) : 0.03;
        this.y = Double.isFinite(this.y) ? Math.clamp(this.y, 0.0, 1.0) : 0.15;
        this.threshold = Math.clamp((long)this.threshold, 1, 120);
        this.mode = Math.floorMod(this.mode, f==Feature.INVENTORY_SEARCH_FLASH?4:3);
        this.color &= 0xFFFFFF;
        this.accent &= 0xFFFFFF;
        if (this.kit == null) {
            this.kit = new ArrayList<SavedItem>();
        }
        if (this.hotbar == null) {
            this.hotbar = new ArrayList<SavedItem>();
        }
        if (this.notes == null) {
            this.notes = new LinkedHashMap<String, Integer>();
        }
        if (this.profiles == null) {
            this.profiles = new LinkedHashMap<String, Config>();
        }
        this.kit.removeIf(s -> s == null || s.key() == null || s.item() == null || s.name() == null || s.count() < 0);
        this.hotbar.removeIf(s -> s == null || s.key() == null || s.item() == null || s.name() == null);
    }

    Config copy() {
        return (Config)JSON.fromJson(JSON.toJson((Object)this), Config.class);
    }

    static void write(Feature f, Config c) {
        Path target = Config.path(f);
        Path tmp = target.resolveSibling(String.valueOf(target.getFileName()) + ".tmp");
        try {
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            Files.writeString(tmp, (CharSequence)JSON.toJson((Object)c), new OpenOption[0]);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e) {
            Core.LOG.error("Cannot save {}", (Object)target, (Object)e);
            Core.notice("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c " + f.title);
        }
    }

    public record SavedItem(String key, String item, String name, int count, int slot) {
    }
}

