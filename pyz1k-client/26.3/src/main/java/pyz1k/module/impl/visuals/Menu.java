/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 */
package pyz1k.module.impl.visuals;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.setting.SelectSetting;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.menu.sections.HandRenderSection;
import pyz1k.screen.menu.sections.ItemRenderSection;

@ModuleInfo(name="Menu", desc="Opens the client menu", category=Category.VISUALS)
public class Menu
extends Module {
    private final SelectSetting interfaceScale = this.registerSetting(new SelectSetting("Размер интерфейса", "100", List.of("50", "75", "100", "125")));

    public Menu() {
        this.setKey(344);
    }

    public float getInterfaceScale() {
        try {
            return (float)Integer.parseInt(this.interfaceScale.getValue()) / 100.0f;
        }
        catch (NumberFormatException ignored) {
            return 1.0f;
        }
    }

    @Override
    public void toggle() {
        Screen screen = Menu.mc.gui.screen();
        if (screen instanceof MenuScreen) {
            MenuScreen menuScreen = (MenuScreen)screen;
            if (ItemRenderSection.isAnyTextInputActive() || HandRenderSection.isAnyTextInputActive()) {
                return;
            }
            menuScreen.requestClose();
            return;
        }
        if (Menu.mc.gui.screen() != null) {
            return;
        }
        mc.gui.setScreen((Screen)new MenuScreen(null));
        this.setEnable(true);
    }
}

