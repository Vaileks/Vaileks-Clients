package pyz1k.smoke;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.MixinEnvironment;
import pyz1k.extensions.*;
import pyz1k.Pyz1k;
public final class ProgressSmoke {
 static int ticks;static boolean created;static int waiting;
 public static void start(){
  ClientLifecycleEvents.CLIENT_STARTED.register(mc->{try{if(!Boolean.getBoolean("pyz1k.hudOnly"))MixinEnvironment.getCurrentEnvironment().audit();else {mc.options.renderDistance().set(3);mc.options.simulationDistance().set(3);}if(Pyz1k.getInstance().getModuleManager().getModules().size()!=34)throw new AssertionError("module registry");}catch(Throwable t){fail(t);}});
  ClientTickEvents.END_CLIENT_TICK.register(mc->{try{
   if(mc.gui.overlay()!=null)return;
   if(mc.player==null){if(++waiting==20)CreateWorldScreen.openFresh(mc,()->{});if(!created&&mc.gui.screen() instanceof CreateWorldScreen screen){created=true;var ui=screen.getUiState();ui.setName("Pyz1k progress smoke");ui.setSeed("1");ui.setGenerateStructures(false);var preset=ui.getSettings().worldgenLoadContext().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT);ui.setWorldType(new WorldCreationUiState.WorldTypeEntry(preset));ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);var create=screen.getClass().getDeclaredMethod("onCreate");create.setAccessible(true);create.invoke(screen);}if(waiting>1200)throw new AssertionError("World creation timeout");return;}
   ticks++;
   if(Boolean.getBoolean("pyz1k.hudOnly")){if(ticks>=20)HudRegression.step(mc,ticks-20);return;}
   if(ticks==1)pyz1k.extensions.ProgressRuntimeChecks.prepare(mc);
   if(ticks==35){pyz1k.extensions.ProgressRuntimeChecks.verifyAdvancements();UnifiedMenu.open(Pyz1k.getInstance().getModuleManager().getModule(FeatureModules.Advancements.class));}
   if(ticks==45){shot(mc,"progress-achievements.png");mc.getSingleplayerServer().execute(()->{var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var entry=mc.getSingleplayerServer().getAdvancements().get(net.minecraft.resources.Identifier.parse("minecraft:husbandry/balanced_diet"));player.getAdvancements().revoke(entry,"bread");});}
   if(ticks==60)pyz1k.extensions.ProgressRuntimeChecks.verifyRevocation();
   if(ticks==65){UnifiedMenu.open(Pyz1k.getInstance().getModuleManager().getModule(FeatureModules.Playtime.class));}
   if(ticks==75){shot(mc,"progress-playtime.png");mc.gui.setScreen(null);}
   if(ticks==85){if(BlastProtectionHud.level()!=4||BlastProtectionHud.count()!=1)throw new AssertionError("Blast Protection equipped result");FeatureModules.blast.leggingsOnly.setValue(true);}
   if(ticks==100){if(BlastProtectionHud.count()!=0)throw new AssertionError("Leggings-only filter ignored");FeatureModules.blast.leggingsOnly.setValue(false);}
   if(ticks==115){shot(mc,"progress-blast.png");mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.item.ItemStack.EMPTY);}
   if(ticks==130){if(BlastProtectionHud.count()!=0)throw new AssertionError("Stale blast indicator after removal");FeatureModules.blast.setEnable(false);pyz1k.extensions.pvp.IntegratedSmoke.world();pyz1k.extensions.pvp.UnifiedMenuSmoke.openAll();}
   if(ticks>=140&&ticks<310&&(ticks-140)%10==0)pyz1k.extensions.pvp.IntegratedSmoke.open((ticks-140)/10,0);
   if(ticks==315)pyz1k.extensions.pvp.UnifiedMenuSmoke.openSearch();
   if(ticks==320)pyz1k.extensions.pvp.UnifiedMenuSmoke.scrollSearch();
   if(ticks==325){pyz1k.extensions.pvp.UnifiedMenuSmoke.editSearch();shot(mc,"unified-search.png");}
   if(ticks==335)pyz1k.extensions.pvp.UnifiedMenuSmoke.openKit();
   if(ticks==345){pyz1k.extensions.pvp.UnifiedMenuSmoke.captureKit();shot(mc,"unified-kit.png");}
   if(ticks==355)pyz1k.extensions.pvp.UnifiedMenuSmoke.verifySavedKit();
   if(ticks==365){System.out.println("PROGRESS_SMOKE: PASS advancement data, armor, PvP behavior, unified navigation, search editing and persistent kit capture");}if(ticks>=370){HudRegression.step(mc,ticks-370);}
  }catch(Throwable t){fail(t);}});
 }
 static void shot(Minecraft mc,String name){System.out.println("PROGRESS_SMOKE: screenshot "+name);Screenshot.grab(mc,false); }
 static void fail(Throwable t){t.printStackTrace();System.exit(1);}
}
