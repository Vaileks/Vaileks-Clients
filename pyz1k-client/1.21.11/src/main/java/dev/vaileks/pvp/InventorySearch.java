package pyz1k.extensions.pvp;
import java.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import pyz1k.extensions.InventorySearchQuery;
/** Only updates on the shared 4-tick cadence. Rendering performs an identity lookup. */
public final class InventorySearch {
 private static final Set<ItemStack> highlighted=Collections.newSetFromMap(new IdentityHashMap<>());
 static int slots,units;
 public static void clear(){highlighted.clear();slots=units=0;}
 public static boolean matches(ItemStack stack){return Core.on(Feature.INVENTORY_SEARCH_FLASH)&&highlighted.contains(stack);}
 static void tick(){
  if(!Core.on(Feature.INVENTORY_SEARCH_FLASH)||Core.mc().player==null||!(Core.mc().currentScreen instanceof HandledScreen)){clear();return;}
  Config cfg=Core.config(Feature.INVENTORY_SEARCH_FLASH);
  if(!cfg.searchPersistent&&Core.now()>=Core.searchUntil){clear();return;}
  clear();var query=new InventorySearchQuery(cfg.searchQuery);
  for(ItemStack stack:Core.inventory()){
   boolean match=cfg.mode==3?!stack.isEmpty()&&query.matches(stack.getName().getString(),Registries.ITEM.getId(stack.getItem()).toString()):Core.matchesCategory(stack,cfg.mode);
   if(match){highlighted.add(stack);slots++;units+=stack.getCount();}
  }
  String title=cfg.mode==3?(cfg.searchQuery.isBlank()?"Введи запрос в PvPTools":"Поиск: "+cfg.searchQuery):Core.searchCategory(cfg.mode);
  Core.add(Feature.INVENTORY_SEARCH_FLASH,new Core.Row(title,slots+" сл. · "+units+" шт.",false));
 }
}
