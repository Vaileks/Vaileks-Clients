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

public final class ExternalChatPatches {
    private static Boolean installed;
    private static boolean inspected;
    private static Field config;
    private static Field restoredTag;
    private static Method restoring;
    private static Method screen;
    private static Method save;

    private ExternalChatPatches() {
    }

    public static boolean loaded() {
        if (installed == null) {
            installed = FabricLoader.getInstance().isModLoaded("chatpatches");
        }
        return installed;
    }

    private static void initialize() {
        if (inspected || !ExternalChatPatches.loaded()) {
            return;
        }
        inspected = true;
        try {
            Class<?> log = Class.forName("obro1961.chatpatches.ChatLog");
            restoring = log.getMethod("isRestoring", new Class[0]);
            restoredTag = log.getField("RESTORED_INDICATOR");
            config = Class.forName("obro1961.chatpatches.ChatPatches").getField("config");
            Class<?> type = config.getType();
            screen = type.getMethod("getConfigScreen", Screen.class);
            save = type.getMethod("serialize", new Class[0]);
        }
        catch (LinkageError | ReflectiveOperationException error) {
            LoggerFactory.getLogger((String)"BloodHelper").warn("Chat Patches settings integration unavailable", error);
        }
    }

    public static boolean restoring(Object tag) {
        ExternalChatPatches.initialize();
        try {
            return restoredTag != null && tag != null && tag.equals(restoredTag.get(null)) || restoring != null && Boolean.TRUE.equals(restoring.invoke(null, new Object[0]));
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static boolean settingsAvailable() {
        ExternalChatPatches.initialize();
        return screen != null && save != null;
    }

    public static boolean option(String name, boolean fallback) {
        ExternalChatPatches.initialize();
        try {
            return config == null ? fallback : config.getType().getField(name).getBoolean(config.get(null));
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    public static void setOption(String name, boolean value) {
        ExternalChatPatches.initialize();
        try {
            if (config != null && save != null) {
                config.getType().getField(name).setBoolean(config.get(null), value);
                save.invoke(null, new Object[0]);
            }
        }
        catch (LinkageError | ReflectiveOperationException error) {
            LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot update Chat Patches option {}", (Object)name, (Object)error);
        }
    }

    public static void openSettings(Screen parent) {
        if (!ExternalChatPatches.settingsAvailable()) {
            return;
        }
        try {
            Object result = screen.invoke(config.get(null), parent);
            if (result instanceof Screen) {
                Screen settings = (Screen)result;
                Minecraft.getInstance().gui.setScreen(settings);
            }
        }
        catch (LinkageError | ReflectiveOperationException error) {
            LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot open Chat Patches settings", error);
        }
    }
}

