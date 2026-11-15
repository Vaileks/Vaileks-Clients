package pyz1k.extensions.pvp;
import pyz1k.extensions.*;
import pyz1k.extensions.mixin.UnifiedMenuAccess;
import pyz1k.Pyz1k;
import pyz1k.screen.menu.MenuScreen;
import net.minecraft.client.input.CharacterEvent;
import java.lang.reflect.*;
public final class UnifiedMenuSmoke {
 static MenuScreen original;static String before;
 static UnifiedFeaturePanel panel(){return IntegratedSmoke.panel();}
 static double pos(String name){try{Field f=UnifiedFeaturePanel.class.getDeclaredField(name);f.setAccessible(true);return ((Number)f.get(panel())).doubleValue();}catch(Exception e){throw new AssertionError(e);}}
 static void row(int index){double x=pos("x")+pos("w")-20,y=pos("y")+47+(index-pos("scroll"))*21+8;if(!panel().click(x,y,0))throw new AssertionError("Row did not handle click");panel().release();}
 public static void openAll(){for(Feature f:Core.roots.keySet())if(PvpModules.module(f)==null)throw new AssertionError("Missing main-menu feature "+f);if(Pyz1k.getInstance().getModuleManager().getModules().stream().anyMatch(m->m.getName().equals("PvPTools")))throw new AssertionError("Legacy hub still registered");IntegratedSmoke.idle();}
 public static void openSearch(){UnifiedMenu.open(PvpModules.module(Feature.INVENTORY_SEARCH_FLASH));original=(MenuScreen)Core.mc().gui.screen();before=Core.config(Feature.INVENTORY_SEARCH_FLASH).searchQuery;Core.config(Feature.INVENTORY_SEARCH_FLASH).searchQuery="";}
 public static void scrollSearch(){panel().scroll(pos("x")+10,pos("y")+60,-1);}
 public static void editSearch(){row(3);original.charTyped(new CharacterEvent('t'));original.charTyped(new CharacterEvent('o'));original.charTyped(new CharacterEvent('t'));original.charTyped(new CharacterEvent('e'));original.charTyped(new CharacterEvent('m'));if(!Core.config(Feature.INVENTORY_SEARCH_FLASH).searchQuery.equals("totem"))throw new AssertionError("Menu char input did not update InventorySearchFlash");panel().key(257,false,"");Core.save(Feature.INVENTORY_SEARCH_FLASH,Core.config(Feature.INVENTORY_SEARCH_FLASH));if(!Config.load(Feature.INVENTORY_SEARCH_FLASH).profiles.isEmpty()&&Core.context.isEmpty())throw new AssertionError("Unexpected profile context");if(Core.mc().gui.screen()!=original)throw new AssertionError("Opened another menu while editing");}
 public static void openKit(){UnifiedMenu.actions(Feature.KIT_CHECK);if(Core.mc().gui.screen()!=original||panel().page()!=3)throw new AssertionError("Kit action left main menu");}
 public static void captureKit(){row(0);if(Core.config(Feature.KIT_CHECK).kit.isEmpty())throw new AssertionError("Kit not captured in menu");}
 public static void verifySavedKit(){var stored=Config.load(Feature.KIT_CHECK);if(!Core.context.isEmpty()&&stored.profiles.containsKey(Core.context))stored=stored.profiles.get(Core.context);if(stored.kit.isEmpty())throw new AssertionError("Kit not persisted by menu");row(1);if(Core.mc().gui.screen()!=original)throw new AssertionError("Report left main menu");System.out.println("UNIFIED_MENU_SMOKE: PASS 17 modules, search char input, kit capture and report in same MenuScreen");}
}
