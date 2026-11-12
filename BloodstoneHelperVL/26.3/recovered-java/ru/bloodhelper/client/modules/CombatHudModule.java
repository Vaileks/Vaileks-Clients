/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.hud.HudWidgets;
import ru.bloodhelper.client.modules.CombatStateModule;

public final class CombatHudModule {
    private static ClientConfig preparedConfig;
    private static CombatHudSettings preparedSettings;
    private static boolean previousCombat;

    private CombatHudModule() {
    }

    public static boolean beginFrame(ClientConfig c) {
        boolean combat = CombatHudModule.combatLayout(c);
        if (combat && (!previousCombat || preparedConfig != c || preparedSettings != c.combatHud)) {
            int before = c.combatHud.layout.size();
            CombatHudModule.prepareLayout(c);
            if (before != c.combatHud.layout.size()) {
                BloodHelperClient.saveConfig();
            }
        }
        preparedConfig = c;
        preparedSettings = c.combatHud;
        previousCombat = combat;
        return combat;
    }

    public static boolean combatLayout(ClientConfig c) {
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen instanceof HudEditorScreen) {
            HudEditorScreen editor = (HudEditorScreen)screen;
            return editor.combatLayout();
        }
        return c.combatLayoutEnabled() && CombatStateModule.active();
    }

    public static boolean compactMusic(ClientConfig c) {
        return c.music.compact || CombatHudModule.combatLayout(c) && c.combatHud.compactMusic;
    }

    public static CombatHudSettings.Pose pose(String key, ClientConfig.HudPosition normal, String alignment, boolean bottom, ClientConfig c) {
        return c.combatHud.ensure(key, normal, alignment, bottom);
    }

    public static HudWidgets.Widget editableWidget(String key, ClientConfig c) {
        HudWidgets.Widget normal = HudWidgets.get(key, c);
        if (!CombatHudModule.combatLayout(c)) {
            return normal;
        }
        CombatHudSettings.Pose pose = CombatHudModule.pose(key, normal.position(), normal.alignment(), normal.bottom(), c);
        return new HudWidgets.Widget(key, normal.title(), pose.position, pose.alignment, pose.bottom);
    }

    public static void prepareLayout(ClientConfig c) {
        for (HudWidgets.Widget widget : HudWidgets.all(c)) {
            CombatHudModule.pose(widget.key(), widget.position(), widget.alignment(), widget.bottom(), c);
        }
    }

    public static void openCombatEditor(Screen parent) {
        CombatHudModule.prepareLayout(BloodHelperClient.config());
        Minecraft.getInstance().gui.setScreen((Screen)new HudEditorScreen(parent, true));
    }

    public static void resetCombatLayout() {
        ClientConfig c = BloodHelperClient.config();
        c.combatHud.layout.clear();
        CombatHudModule.prepareLayout(c);
        BloodHelperClient.saveConfig();
    }
}

