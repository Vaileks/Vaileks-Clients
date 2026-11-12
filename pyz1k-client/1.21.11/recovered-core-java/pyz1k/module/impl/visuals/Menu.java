/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_437
 */
package pyz1k.module.impl.visuals;

import java.util.List;
import net.minecraft.class_437;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.setting.SelectSetting;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.menu.sections.HandRenderSection;

@ModuleInfo(name="Menu", desc="Opens the client menu", category=Category.VISUALS)
public class Menu
extends Module {
    private final SelectSetting interfaceScale = this.registerSetting(new SelectSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430", "100", List.of("50", "75", "100", "125")));

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
        class_437 class_4372 = Menu.mc.field_1755;
        if (class_4372 instanceof MenuScreen) {
            MenuScreen menuScreen = (MenuScreen)class_4372;
            if (false || HandRenderSection.isAnyTextInputActive()) {
                return;
            }
            menuScreen.requestClose();
            return;
        }
        if (Menu.mc.field_1755 != null) {
            return;
        }
        mc.method_1507((class_437)new MenuScreen(null));
        this.setEnable(true);
    }
}

