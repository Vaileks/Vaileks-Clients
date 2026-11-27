/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.PlayerDisplaySettings;

public final class ExternalHudMods {
    private static final boolean ADVANCED = true;
    private static boolean initialized;
    private static boolean appleReady;
    private static boolean reachReady;
    private static Field appleInstance;
    private static Field reachEnabled;
    private static Field[] appleOptions;
    private static Object reachInstance;
    private static Method reachScreen;
    private static String reachError;

    private ExternalHudMods() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            if (FabricLoader.getInstance().isModLoaded("appleskin")) {
                Class<?> config = Class.forName("squeek.appleskin.ModConfig");
                appleInstance = config.getField("INSTANCE");
                String[] names = new String[]{"showSaturationHudOverlay", "showFoodExhaustionHudUnderlay", "showFoodValuesHudOverlay", "showFoodHealthHudOverlay", "showFoodValuesInTooltip", "showFoodValuesInTooltipAlways", "showFoodValuesHudOverlayWhenOffhand", "showVanillaAnimationsOverlay"};
                appleOptions = new Field[names.length];
                for (int i = 0; i < names.length; ++i) {
                    ExternalHudMods.appleOptions[i] = config.getField(names[i]);
                }
                appleReady = true;
            }
        }
        catch (LinkageError | ReflectiveOperationException e) {
            LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot connect to AppleSkin configuration", e);
        }
        try {
            if (FabricLoader.getInstance().isModLoaded("advanced_reach_display")) {
                String prefix = "net.blueskiez77.reach_display.config.";
                Class<?> config = Class.forName(prefix + "DisplayConfig");
                reachEnabled = config.getField("enabled");
                reachInstance = config.getField("INSTANCE").get(null);
                reachScreen = Class.forName(prefix + "ConfigGUI").getMethod("create", Screen.class);
                reachReady = true;
            }
        }
        catch (LinkageError | ReflectiveOperationException e) {
            reachError = e.getClass().getSimpleName();
            LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot connect to {}", (Object)ExternalHudMods.reachName(), (Object)e);
        }
        ExternalHudMods.tick();
    }

    public static String reachName() {
        return "Advanced Reach Display";
    }

    public static boolean reachAvailable() {
        return reachReady;
    }

    public static String reachStatus(boolean ru) {
        if (!reachError.isEmpty()) {
            return ru ? "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f: " + reachError : "Integration error: " + reachError;
        }
        return reachReady ? (ru ? "\u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043c\u043e\u0434 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0451\u043d" : "Original mod connected") : (ru ? "\u041d\u0443\u0436\u043d\u044b \u043c\u043e\u0434 \u0438 \u0435\u0433\u043e \u0431\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0438 \u0438\u0437 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442\u0430 \u0434\u043b\u044f \u044d\u0442\u043e\u0439 \u0432\u0435\u0440\u0441\u0438\u0438" : "Install the mod and libraries from this version's bundle");
    }

    public static String appleStatus(boolean ru) {
        return appleReady ? (ru ? "AppleSkin \u0432\u0441\u0442\u0440\u043e\u0435\u043d \u0438 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0451\u043d" : "Bundled AppleSkin connected") : (ru ? "AppleSkin \u043d\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0451\u043d \u2014 \u043f\u0440\u043e\u0432\u0435\u0440\u044c \u0436\u0443\u0440\u043d\u0430\u043b \u0437\u0430\u043f\u0443\u0441\u043a\u0430" : "AppleSkin unavailable \u2014 check the startup log");
    }

    public static void tick() {
        if (!initialized) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        if (appleReady) {
            try {
                Object target = appleInstance.get(null);
                if (target != null) {
                    PlayerDisplaySettings.Saturation a = config.saturation;
                    boolean[] values = new boolean[]{a.enabled, a.enabled && a.exhaustion, a.enabled && a.foodPreview, a.enabled && a.healthPreview, a.enabled && a.tooltips, a.tooltipsAlways, a.offhand, a.enabled};
                    for (int i = 0; i < values.length; ++i) {
                        if (appleOptions[i].getBoolean(target) == values[i]) continue;
                        appleOptions[i].setBoolean(target, values[i]);
                    }
                }
            }
            catch (LinkageError | ReflectiveOperationException e) {
                appleReady = false;
                LoggerFactory.getLogger((String)"BloodHelper").warn("AppleSkin configuration changed unexpectedly", e);
            }
        }
        if (reachReady) {
            try {
                boolean enabled = config.reachDisplay.enabled;
                if (reachEnabled.getBoolean(reachInstance) != enabled) {
                    reachEnabled.setBoolean(reachInstance, enabled);
                }
            }
            catch (LinkageError | ReflectiveOperationException e) {
                reachReady = false;
                reachError = e.getClass().getSimpleName();
                LoggerFactory.getLogger((String)"BloodHelper").warn("Reach Display configuration changed unexpectedly", e);
            }
        }
    }

    public static void openReachSettings(Screen parent) {
        if (!reachReady) {
            return;
        }
        try {
            Object screen = reachScreen.invoke(null, parent);
            if (screen instanceof Screen) {
                Screen settings = (Screen)screen;
                Minecraft.getInstance().gui.setScreen(settings);
            }
        }
        catch (LinkageError | ReflectiveOperationException e) {
            reachError = e.getClass().getSimpleName();
            LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot open {} settings", (Object)ExternalHudMods.reachName(), (Object)e);
        }
    }

    static {
        reachError = "";
    }
}

