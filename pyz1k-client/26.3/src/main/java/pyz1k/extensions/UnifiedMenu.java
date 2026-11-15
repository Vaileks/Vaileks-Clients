package pyz1k.extensions;
import net.minecraft.client.Minecraft;
import pyz1k.module.base.Module;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.extensions.mixin.UnifiedMenuAccess;
import pyz1k.extensions.pvp.*;
public final class UnifiedMenu {
 public interface Section {UnifiedFeaturePanel pyz1k$panel();}
 public static void open(Module module){var mc=Minecraft.getInstance();var menu=mc.gui.screen() instanceof MenuScreen existing?existing:new MenuScreen(mc.gui.screen());if(mc.gui.screen()!=menu)mc.gui.setScreen(menu);((UnifiedMenuAccess)(Object)menu).pyz1k$focus(module);}
 public static void actions(Feature f){Module module=PvpModules.module(f);if(module==null)return;open(module);var menu=(UnifiedMenuAccess)Minecraft.getInstance().gui.screen();((Section)(Object)menu.pyz1k$grid()).pyz1k$panel().actions();}
}
