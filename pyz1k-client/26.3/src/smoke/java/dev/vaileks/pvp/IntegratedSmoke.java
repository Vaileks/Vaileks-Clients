package pyz1k.extensions.pvp;
import java.util.*;
import pyz1k.extensions.*;
public final class IntegratedSmoke {
 public static void idle(){
  if(Core.roots.size()!=17)throw new AssertionError("Expected 17 independent PvP features");
  for(Feature f:Core.roots.keySet()){PvpModules.enable(f,true);if(!Core.on(f)||!Core.any())throw new AssertionError("toggle on "+f);PvpModules.enable(f,false);}
  if(Core.any())throw new AssertionError("Disabled PvP modules still running");
  var bean=java.lang.management.ManagementFactory.getThreadMXBean();
  for(int i=0;i<10000;i++)Core.tick();
  if(bean instanceof com.sun.management.ThreadMXBean allocations&&allocations.isThreadAllocatedMemorySupported()){
   allocations.setThreadAllocatedMemoryEnabled(true);long id=Thread.currentThread().threadId();long bytes=Long.MAX_VALUE;
   for(int sample=0;sample<5;sample++){long before=allocations.getThreadAllocatedBytes(id);for(int i=0;i<100000;i++)Core.tick();bytes=Math.min(bytes,allocations.getThreadAllocatedBytes(id)-before);}
   if(bytes>4096)throw new AssertionError("Idle tick allocated "+bytes+" bytes");
   System.out.println("PYZ1K_SMOKE: 100000 disabled PvP ticks allocated "+bytes+" bytes");
  }
 }
 public static void open(int index,int page){
  var f=new ArrayList<>(Core.roots.keySet()).get(index);var screen=new SettingsScreen(null,f);
  Core.mc().gui.setScreen(screen);screen.page=page;screen.refresh();
 }
 public static void world(){
  var p=Core.mc().player;if(p==null)throw new AssertionError("No player for PvP behavior checks");
  for(Feature f:Core.roots.keySet())PvpModules.enable(f,false);
  p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL,4));
  PvpModules.enable(Feature.HOTBAR_MEMORY,true);Core.capture(Feature.HOTBAR_MEMORY,Core.config(Feature.HOTBAR_MEMORY));
  p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE,2));Core.updateRows();
  if(Core.list(Feature.HOTBAR_MEMORY).isEmpty())throw new AssertionError("Hotbar change not detected");
  PvpModules.enable(Feature.KIT_CHECK,true);Core.capture(Feature.KIT_CHECK,Core.config(Feature.KIT_CHECK));
  p.getInventory().setItem(0,net.minecraft.world.item.ItemStack.EMPTY);Core.updateRows();
  if(Core.list(Feature.KIT_CHECK).stream().noneMatch(Core.Row::critical))throw new AssertionError("Missing kit item not detected");
  PvpModules.enable(Feature.OFFHAND_ALERT,true);Core.updateRows();
  if(Core.list(Feature.OFFHAND_ALERT).isEmpty())throw new AssertionError("Offhand alert missing");
  PvpModules.enable(Feature.ARMOR_PANIC,true);var armor=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);armor.setDamageValue(armor.getMaxDamage()-1);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,armor);Core.updateRows();
  if(Core.list(Feature.ARMOR_PANIC).isEmpty())throw new AssertionError("Armor threshold missing");
  PvpModules.enable(Feature.EFFECT_ALARM,true);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.STRENGTH,40,0));Core.updateRows();
  if(Core.list(Feature.EFFECT_ALARM).isEmpty())throw new AssertionError("Effect expiration alert missing");
  PvpModules.enable(Feature.READY_AGAIN,true);var pearl=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL);p.getInventory().setItem(1,pearl);var manager=p.getCooldowns();manager.addCooldown(pearl,2);Core.updateRows();manager.tick();manager.tick();Core.updateRows();
  if(Core.list(Feature.READY_AGAIN).isEmpty())throw new AssertionError("Cooldown completion alert missing");
  PvpModules.enable(Feature.FIGHT_RECAP,true);PvpModules.enable(Feature.FIGHT_TIMELINE,true);
  Core.startFight("Fixture");Core.used(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE),true);Core.finishFight();
  if(Core.lastUsed.getOrDefault(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE).getHoverName().getString(),0)!=1||Core.lastTimeline.isEmpty())throw new AssertionError("Fight tracking failed");
  PvpModules.enable(Feature.INVENTORY_SEARCH_FLASH,true);var cfg=Core.config(Feature.INVENTORY_SEARCH_FLASH);cfg.mode=3;cfg.searchQuery="diamond sword";cfg.searchPersistent=true;
  var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);p.getInventory().setItem(4,sword);
  Core.mc().gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(p));Core.updateRows();
  if(!InventorySearch.matches(sword)||InventorySearch.slots!=1)throw new AssertionError("Text search snapshot failed");
  cfg.mode=1;p.getInventory().setItem(5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));Core.updateRows();
  if(InventorySearch.matches(p.getInventory().getItem(5)))throw new AssertionError("Wearable pumpkin considered armor");
  cfg.mode=3;cfg.searchPersistent=false;Core.searchUntil=0;Core.updateRows();if(InventorySearch.matches(sword))throw new AssertionError("Expired search still highlighted");
  cfg.searchPersistent=true;Core.updateRows();PvpModules.enable(Feature.INVENTORY_SEARCH_FLASH,false);if(InventorySearch.matches(sword)||InventorySearch.slots!=0)throw new AssertionError("Disabled search retains highlight");
  Core.capture(Feature.KIT_CHECK,Core.config(Feature.KIT_CHECK));if(!Core.missing(Core.config(Feature.KIT_CHECK),true).isEmpty())throw new AssertionError("Kit counts duplicate equipment");
  System.out.println("PYZ1K_SMOKE: inventory text/category/expiration/disable and kit counting regressions passed");
  for(Feature f:Core.roots.keySet())PvpModules.enable(f,false);
  System.out.println("PYZ1K_SMOKE: kit, hotbar, offhand, armor, effects, cooldown completion and fight recording passed");
 }
}
