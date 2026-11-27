package vl.smoke;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.lang.reflect.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import net.minecraft.world.item.*;import net.minecraft.core.component.DataComponents;import net.minecraft.resources.Identifier;import net.minecraft.network.chat.Component;
public final class BloodSmoke implements ClientModInitializer {
 int ticks;
 static Class<?> cls(String n)throws Exception{return Class.forName("ru.bloodhelper.client."+n);}
 static Object field(Object o,String n)throws Exception{return o.getClass().getField(n).get(o);}
 static void set(Object o,String n,Object v)throws Exception{o.getClass().getField(n).set(o,v);}
 static void check(boolean good,String message){if(!good)throw new AssertionError(message);}
 static void tests()throws Exception{
  Object cfg=cls("BloodHelperClient").getMethod("config").invoke(null);
  if(Boolean.getBoolean("vl.lunarTest")){
   Class<?> distribution=cls("compat.ClientDistribution");check((boolean)distribution.getMethod("lunarClient").invoke(null),"Not Lunar distribution");check(distribution.getMethod("defaultMenuKeyName").invoke(null).equals("Insert"),"Lunar menu binding conflict");
   Class<?> hooks=cls("compat.OptionalMixinHooks");check((boolean)hooks.getMethod("freeLook").invoke(null),"Vanilla camera hooks lost");check((boolean)hooks.getMethod("crosshair").invoke(null),"Vanilla crosshair hook lost");
   System.out.println("VL_SMOKE: Lunar distribution, Insert menu binding and camera/crosshair compatibility hooks passed under Fabric");
  }

  Object settings=field(cfg,"legendarySize");set(settings,"enabled",true);set(settings,"scaleAllDropped",true);set(settings,"droppedScale",2.0);set(settings,"enlargeDropped",true);
  Object visual=field(cfg,"legendaryVisuals");set(visual,"enabled",true);set(visual,"loot",true);
  for(String removed:new String[]{"modules.LegendarySizeModule","modules.HandItemScaleScreen"}){try{cls(removed);throw new AssertionError("Removed implementation still exists: "+removed);}catch(ClassNotFoundException expected){}}
  var scale=settings.getClass().getMethod("scale",String.class,boolean.class,boolean.class);
  for(String id:java.util.Arrays.asList(null,"ground:any","selected:ordinary","dragon_katana","unknown_legendary"))for(boolean first:new boolean[]{false,true})for(boolean dropped:new boolean[]{false,true})check(((Number)scale.invoke(settings,id,first,dropped)).floatValue()==1f,"old config resize bypass: "+id);
  Class<?> loot=cls("visuals.LootHighlight");
  for(Method m:loot.getDeclaredMethods()){m.setAccessible(true);if(m.getName().equals("tick"))m.invoke(null,new Object[]{null});if(m.getName().equals("glowColor"))check(((Number)m.invoke(null,new Object[]{null})).intValue()==0,"loot glow active");}
  check(((Number)loot.getMethod("selectedCount").invoke(null)).intValue()==0,"loot markers active");
  var enumValues=cls("gui.BloodHelperScreen$Module").getDeclaredMethod("values");enumValues.setAccessible(true);check(java.lang.reflect.Array.getLength(enumValues.invoke(null))==66,"resizing module still registered");
  cfg=cls("config.ClientConfig").getConstructor().newInstance();visual=field(cfg,"legendaryVisuals");settings=field(cfg,"legendarySize");set(visual,"enabled",true);set(visual,"loot",true);set(settings,"enabled",true);set(settings,"enlargeDropped",true);Class<?> helper=cls("vl.VLRestrictions");helper.getMethod("enforce",Object.class).invoke(null,cfg);
  check(!(boolean)field(visual,"loot")&&!(boolean)field(settings,"enlargeDropped"),"old config not restricted");
  check((boolean)field(visual,"enabled")&&!(boolean)field(settings,"enabled")&&!(boolean)field(settings,"scaleAllDropped"),"size module not disabled or unrelated visuals disabled");Object[] visible=(Object[])helper.getMethod("visibleModules",Object[].class).invoke(null,(Object)enumValues.invoke(null));check(visible.length==66,"size module still visible");
  Class<?> guard=cls("vl.PrivacyGuard");String localName=net.minecraft.client.Minecraft.getInstance().getUser().getName();try{guard.getMethod("checkUri",java.net.URI.class).invoke(null,java.net.URI.create("https://example.invalid/profile/"+localName));throw new AssertionError("Local identity request allowed");}catch(java.lang.reflect.InvocationTargetException denied){check(denied.getCause() instanceof SecurityException,"Unexpected privacy failure");}guard.getMethod("checkUri",java.net.URI.class).invoke(null,java.net.URI.create("https://lrclib.net/api/search?q=test_track"));System.out.println("VL_SMOKE: local identity blocked; ordinary music endpoint allowed without connection");
  System.out.println("VL_SMOKE: all item scaling disabled; legendary/custom/ordinary/manual bypasses blocked; no loot outline or markers; size module and its screen removed");
 }
 public void onInitializeClient(){
  ClientLifecycleEvents.CLIENT_STARTED.register(mc->{try{MixinEnvironment.getCurrentEnvironment().audit();System.out.println("VL_SMOKE: mixin audit passed");mc.execute(()->{
 net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(mc,()->{});
 var screen=(net.minecraft.client.gui.screens.worldselection.CreateWorldScreen)mc.gui.screen();var ui=screen.getUiState();ui.setName("VL offline smoke");ui.setSeed("1");ui.setGenerateStructures(false);
 var preset=ui.getSettings().worldgenLoadContext().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT);
 ui.setWorldType(new net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.WorldTypeEntry(preset));ui.setGameMode(net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.SelectedGameMode.CREATIVE);
 try{var create=screen.getClass().getDeclaredMethod("onCreate");create.setAccessible(true);create.invoke(screen);}catch(Exception error){throw new RuntimeException(error);}
 });}catch(Throwable e){e.printStackTrace();System.exit(1);}});
  ClientTickEvents.END_CLIENT_TICK.register(mc->{if(mc.gui.overlay()!=null||mc.player==null)return;ticks++;try{if(ticks==1)tests();
   if(ticks==10){mc.gui.setScreen((net.minecraft.client.gui.screens.Screen)cls("gui.BloodHelperScreen").getConstructor().newInstance());}
   if(ticks==20){Object screen=mc.gui.screen();Class<?> enumClass=cls("gui.BloodHelperScreen$Module");Method values=enumClass.getDeclaredMethod("values");values.setAccessible(true);Method rows=screen.getClass().getDeclaredMethod("rows",enumClass);rows.setAccessible(true);for(Object module:(Object[])values.invoke(null))rows.invoke(screen,module);System.out.println("VL_SMOKE: all 66 module settings built; resizing absent from registry");}
   if(ticks==30){Object screen=mc.gui.screen();var page=screen.getClass().getDeclaredField("page");page.setAccessible(true);page.set(screen,Enum.valueOf((Class)page.getType(),"ABOUT"));System.out.println("VL_SMOKE: about page opened");}
   if(ticks==50){System.out.println("VL_SMOKE: PASS startup and menu");mc.stop();}
  }catch(Throwable e){e.printStackTrace();System.exit(1);}});
 }
}
