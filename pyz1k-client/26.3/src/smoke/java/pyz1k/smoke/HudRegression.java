package pyz1k.smoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import pyz1k.Pyz1k;
import pyz1k.compat.LegacyInput;
import pyz1k.extensions.*;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.screen.screen.HudElement;
/** Regressions through MouseHandler's real GUI input path, not direct HUD position setters. */
public final class HudRegression {
 static int frames,seen,step;
 public static final class FrameCounter {@com.google.common.eventbus.Subscribe public void frame(pyz1k.event.impl.EventScreenRender e){if(e.getStage()==pyz1k.event.impl.EventScreenRender.Stage.HUD)frames++;}}
 static HudElement target;static float startX,startY,endX,endY;static int phase;
 static final String[] IDS={"logo","pvp_cooldown","item_cooldowns","blood_sword_cooldown","blast_protection_indicator","potion_list"};
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static HudElement element(String id){return Pyz1k.getInstance().getModuleManager().getModule(Interface.class).getHudElements().stream().filter(e->e.getId().equals(id)).findFirst().orElseThrow(()->new AssertionError("Missing HUD "+id));}
 static void move(Minecraft mc,double x,double y){var window=mc.getWindow();double px=x*window.getScreenWidth()/window.getGuiScaledWidth(),py=y*window.getScreenHeight()/window.getGuiScaledHeight();mc.mouseHandler.onMove(window.handle(),px,py,0,0);mc.mouseHandler.onMove(window.handle(),px,py,0,0);}
 static void button(Minecraft mc,int action){mc.mouseHandler.onButton(mc.getWindow().handle(),new MouseButtonInfo(1,0),action);}
 public static void step(Minecraft mc,int elapsedTicks){
  if(elapsedTicks!=0){if(frames==seen)return;seen=frames;step++;}
  int tick=step;
  if(tick==0){Pyz1k.getInstance().getEventBus().register(new FrameCounter());
   var modules=Pyz1k.getInstance().getModuleManager();modules.getModule(Interface.class).setEnable(true);modules.getModule(pyz1k.module.impl.visuals.PvPCooldown.class).setEnable(true);modules.getModule(pyz1k.module.impl.visuals.ItemCooldownsModule.class).setEnable(true);modules.getModule(pyz1k.module.impl.visuals.PotionList.class).setEnable(true);
   FeatureModules.blood.setEnable(true);FeatureModules.blast.setEnable(true);FeatureModules.blast.leggingsOnly.setValue(false);
   var registry=mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var armor=new ItemStack(Items.DIAMOND_LEGGINGS);armor.enchant(registry.getOrThrow(Enchantments.BLAST_PROTECTION),4);
   for(int i=0;i<12;i++){mc.player.setItemSlot(EquipmentSlot.LEGS,armor);BlastProtectionHud.tick();check(BlastProtectionHud.level()==4&&BlastProtectionHud.count()==1,"Blast Protection delayed after equip "+i);mc.player.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);BlastProtectionHud.tick();check(BlastProtectionHud.count()==0,"Blast Protection delayed after removal "+i);}
   System.out.println("HUD_REGRESSION: PASS 12 immediate equip/remove cycles");
   mc.gui.setScreen(new ChatScreen("",false));
   for(int i=0;i<IDS.length;i++)element(IDS[i]).setPosition(12,12+i*37);
  }
  if(tick>=10&&tick<70){int index=(tick-10)/10,stage=(tick-10)%10;
   if(stage==0){target=element(IDS[index]);for(var other:Pyz1k.getInstance().getModuleManager().getModule(Interface.class).getHudElements())if(other!=target)other.setVisualPosition(mc.getWindow().getGuiScaledWidth()-other.getWidth(),mc.getWindow().getGuiScaledHeight()-other.getHeight()-25);target.setVisualPosition(12,20);check(target.getWidth()>1,"No editor preview "+target.getId());startX=target.getVisualX();startY=target.getVisualY();move(mc,startX+8,startY+6);button(mc,1);check(LegacyInput.glfwGetMouseButton(0,0)==1,"GUI press missing from state");}
   if(stage==2){move(mc,startX+96,startY+23);System.out.println("HUD_REGRESSION: dragging "+target.getId()+" from "+startX+","+startY+" size "+target.getWidth()+","+target.getHeight());}
   if(stage==4&&target.getVisualX()<=startX+30){for(var e:Pyz1k.getInstance().getModuleManager().getModule(Interface.class).getHudElements())System.out.println("HUD_BOUNDS: "+e.getId()+" "+e.getVisualX()+","+e.getVisualY()+" "+e.getWidth()+","+e.getHeight());ProgressSmoke.shot(mc,"hud-drag-failure.png");}
   if(stage==5){check(target.getVisualX()>startX+30,"HUD did not move in chat: "+target.getId());endX=target.getVisualX();endY=target.getVisualY();button(mc,0);check(LegacyInput.glfwGetMouseButton(0,0)==0,"GUI release missing from state");}
   if(stage==7){move(mc,startX+140,startY+10);check(Math.abs(target.getVisualX()-endX)<.1,"HUD continued dragging after release");System.out.println("HUD_REGRESSION: PASS drag/release "+target.getId());}
  }
  if(tick==75){var cfg=Pyz1k.getInstance().getConfigManager();check(cfg.saveConfig("hud-regression"),"Save failed");float x=target.getX(),y=target.getY();target.setPosition(2,2);check(cfg.loadConfig("hud-regression"),"Reload failed");check(Math.abs(target.getX()-x)<.1&&Math.abs(target.getY()-y)<.1,"HUD position not restored");System.out.println("HUD_REGRESSION: PASS persisted HUD positions");}
  if(tick==85){ProgressSmoke.shot(mc,"hud-drag-fixed.png");System.out.println("HUD_REGRESSION: PASS all input, drag, persistence and immediate armor checks");}if(tick==87){mc.stop();}
 }
}
