package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import pyz1k.module.base.Module;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.extensions.mixin.UnifiedMenuAccess;
import pyz1k.extensions.pvp.*;
public final class UnifiedMenu {
 public interface Section {UnifiedFeaturePanel pyz1k$panel();}
 public static void open(Module module){var mc=MinecraftClient.getInstance();var menu=mc.currentScreen instanceof MenuScreen existing?existing:new MenuScreen(mc.currentScreen);if(mc.currentScreen!=menu)mc.setScreen(menu);((UnifiedMenuAccess)(Object)menu).pyz1k$focus(module);}
 public static void actions(Feature f){Module module=PvpModules.module(f);if(module==null)return;open(module);var menu=(UnifiedMenuAccess)MinecraftClient.getInstance().currentScreen;((Section)(Object)menu.pyz1k$grid()).pyz1k$panel().actions();}
}
