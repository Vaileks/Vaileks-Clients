/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.extensions;

import pyz1k.Pyz1k;
import pyz1k.config.ConfigManager;
import pyz1k.extensions.ClientFeatureLogic;
import pyz1k.extensions.ExtensionConfig;
import pyz1k.extensions.ScreenshotMode;

public final class ServerProfiles {
    private static final String BASE = "vaileks_global";
    private static String current = "";

    public static void initialize() {
        ExtensionConfig cfg = ExtensionConfig.get();
        if (cfg.automaticProfiles && cfg.baselineSaved) {
            Pyz1k.getInstance().getConfigManager().loadConfig(BASE);
        }
    }

    public static void switchServer(String next) {
        ExtensionConfig cfg = ExtensionConfig.get();
        ConfigManager manager = Pyz1k.getInstance().getConfigManager();
        ScreenshotMode.reset();
        if (cfg.automaticProfiles) {
            if (!current.isEmpty()) {
                if (manager.saveConfig(ClientFeatureLogic.profileName(current))) {
                    cfg.serverProfiles.add(current);
                }
            } else {
                cfg.baselineSaved = manager.saveConfig(BASE);
            }
            if (cfg.baselineSaved) {
                manager.loadConfig(BASE);
            } else {
                cfg.baselineSaved = manager.saveConfig(BASE);
            }
            if (!next.isEmpty() && cfg.serverProfiles.contains(next)) {
                manager.loadConfig(ClientFeatureLogic.profileName(next));
            }
            manager.saveConfig("auto");
            ExtensionConfig.save();
        }
        current = next;
    }

    public static void automatic(boolean enabled) {
        ExtensionConfig cfg = ExtensionConfig.get();
        if (cfg.automaticProfiles == enabled) {
            return;
        }
        ConfigManager manager = Pyz1k.getInstance().getConfigManager();
        if (enabled) {
            cfg.baselineSaved = manager.saveConfig(BASE);
            cfg.automaticProfiles = true;
            if (!current.isEmpty() && cfg.serverProfiles.contains(current)) {
                manager.loadConfig(ClientFeatureLogic.profileName(current));
            }
        } else {
            if (current.isEmpty()) {
                cfg.baselineSaved = manager.saveConfig(BASE);
            } else {
                ServerProfiles.save();
            }
            cfg.automaticProfiles = false;
            if (cfg.baselineSaved) {
                manager.loadConfig(BASE);
            }
            manager.saveConfig("auto");
        }
        ExtensionConfig.save();
    }

    public static boolean save() {
        if (current.isEmpty()) {
            return false;
        }
        boolean ok = Pyz1k.getInstance().getConfigManager().saveConfig(ClientFeatureLogic.profileName(current));
        if (ok) {
            ExtensionConfig.get().serverProfiles.add(current);
            ExtensionConfig.save();
        }
        return ok;
    }

    public static boolean restore() {
        return !current.isEmpty() && ExtensionConfig.get().serverProfiles.contains(current) && Pyz1k.getInstance().getConfigManager().loadConfig(ClientFeatureLogic.profileName(current));
    }
}

