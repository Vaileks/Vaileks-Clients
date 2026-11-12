package vl.smoke;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.lang.reflect.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import net.minecraft.item.*;import net.minecraft.component.DataComponentTypes;import net.minecraft.util.Identifier;import net.minecraft.text.Text;
public final class BloodSmoke implements ClientModInitializer {
 int ticks,worldWaitTicks;boolean created;long blurBefore;
 static Class<?> cls(String n)throws Exception{return Class.forName("ru.bloodhelper.client."+n);}
 static Object field(Object o,String n)throws Exception{return o.getClass().getField(n).get(o);}
 static void set(Object o,String n,Object v)throws Exception{o.getClass().getField(n).set(o,v);}
 static void check(boolean good,String message){if(!good)throw new AssertionError(message);}
 static void tests()throws Exception{
  Object cfg=cls("BloodHelperClient").getMethod("config").invoke(null);
  Object settings=field(cfg,"legendarySize");set(settings,"enabled",true);set(settings,"scaleAllDropped",true);set(settings,"droppedScale",2.0);set(settings,"enlargeDropped",true);
  Object visual=field(cfg,"legendaryVisuals");set(visual,"enabled",true);set(visual,"loot",true);
  Class<?> size=cls("modules.LegendarySizeModule");
  ItemStack ordinary=new ItemStack(Items.DIAMOND_SWORD);
  var ground=size.getMethod("identifyForGround",ItemStack.class);var hand=size.getMethod("identifyForHand",ItemStack.class);
  check(ground.invoke(null,ordinary)==null&&hand.invoke(null,ordinary)==null,"ordinary scaling still active");check(!(boolean)size.getMethod("addSelected",ItemStack.class).invoke(null,ordinary),"manual selection still active");
  var scale=settings.getClass().getMethod("scale",String.class,boolean.class,boolean.class);
  check(((Number)scale.invoke(settings,"ground:any",false,true)).floatValue()==1f,"ordinary ground scaling still active");
  check(((Number)scale.invoke(settings,"dragon_katana",true,true)).floatValue()==1f,"legendary settings scale not neutral");
  for(String id:java.util.Arrays.asList(null,"ground:any","selected:ordinary","dragon_katana","unknown_legendary"))for(boolean first:new boolean[]{false,true})for(boolean dropped:new boolean[]{false,true})check(((Number)scale.invoke(settings,id,first,dropped)).floatValue()==1f,"settings resize bypass: "+id);
  check(((Number)settings.getClass().getMethod("handScale",String.class).invoke(settings,"dragon_katana")).doubleValue()==1.0,"hand scale helper active");
  check(((Number)settings.getClass().getMethod("groundScale").invoke(settings)).doubleValue()==1.0,"ground scale helper active");
  for(Method method:size.getDeclaredMethods())if(method.getName().equals("scale")){for(Object context:method.getParameterTypes()[1].getEnumConstants())check(((Number)method.invoke(null,"selected:ordinary",context)).floatValue()==1f,"render context resize bypass: "+context);}
  for(String model:new String[]{"bloodstone:tools/mirror_blade/default/mirror_blade","bloodstone:clownsword","altarlegendary:kristalniymech","unknown:custom_weapon","minecraft:clownsword"}){
   ItemStack custom=new ItemStack(Items.DIAMOND_SWORD);custom.set(DataComponentTypes.ITEM_MODEL,Identifier.of(model));
   check(ground.invoke(null,custom)==null,"custom ground scale bypass: "+model);check(hand.invoke(null,custom)==null,"custom hand scale bypass: "+model);
   size.getMethod("addSelected",ItemStack.class).invoke(null,custom);
   check(ground.invoke(null,custom)==null&&hand.invoke(null,custom)==null,"selected custom scale bypass: "+model);
  }
  ItemStack named=new ItemStack(Items.DIAMOND_SWORD);named.set(DataComponentTypes.CUSTOM_NAME,Text.literal("Легендарка"));
  check(ground.invoke(null,named)==null,"named item bypass");
  Class<?> loot=cls("visuals.LootHighlight");
  for(Method m:loot.getDeclaredMethods()){m.setAccessible(true);if(m.getName().equals("tick"))m.invoke(null,new Object[]{null});if(m.getName().equals("glowColor"))check(((Number)m.invoke(null,new Object[]{null})).intValue()==0,"loot glow active");}
  check(((Number)loot.getMethod("selectedCount").invoke(null)).intValue()==0,"loot markers active");
  var enumValues=cls("gui.BloodHelperScreen$Module").getDeclaredMethod("values");enumValues.setAccessible(true);check(java.lang.reflect.Array.getLength(enumValues.invoke(null))==67,"modules removed");
  cfg=cls("config.ClientConfig").getConstructor().newInstance();visual=field(cfg,"legendaryVisuals");settings=field(cfg,"legendarySize");set(visual,"enabled",true);set(visual,"loot",true);set(settings,"enabled",true);set(settings,"enlargeDropped",true);Class<?> helper=cls("vl.VLRestrictions");helper.getMethod("enforce",Object.class).invoke(null,cfg);
  check(!(boolean)field(visual,"loot")&&!(boolean)field(settings,"enlargeDropped"),"old config not restricted");
  check((boolean)field(visual,"enabled")&&!(boolean)field(settings,"enabled")&&!(boolean)field(settings,"scaleAllDropped"),"size module not disabled or unrelated visuals disabled");Object[] visible=(Object[])helper.getMethod("visibleModules",Object[].class).invoke(null,(Object)enumValues.invoke(null));check(visible.length==66,"size module still visible");
  Class<?> guard=cls("vl.PrivacyGuard");String localName=net.minecraft.client.MinecraftClient.getInstance().getSession().getUsername();try{guard.getMethod("checkUri",java.net.URI.class).invoke(null,java.net.URI.create("https://example.invalid/profile/"+localName));throw new AssertionError("Local identity request allowed");}catch(java.lang.reflect.InvocationTargetException denied){check(denied.getCause() instanceof SecurityException,"Unexpected privacy failure");}guard.getMethod("checkUri",java.net.URI.class).invoke(null,java.net.URI.create("https://lrclib.net/api/search?q=test_track"));System.out.println("VL_SMOKE: local identity blocked; ordinary music endpoint allowed without connection");
  System.out.println("VL_SMOKE: all item scaling disabled; legendary/custom/ordinary/manual bypasses blocked; no loot outline or markers; size module hidden");
 }
 public void onInitializeClient(){
  ClientLifecycleEvents.CLIENT_STARTED.register(mc->{try{MixinEnvironment.getCurrentEnvironment().audit();tests();System.out.println("VL_SMOKE: mixin audit passed");}catch(Throwable e){e.printStackTrace();System.exit(1);}});
  ClientTickEvents.END_CLIENT_TICK.register(mc->{if(mc.getOverlay()!=null)return;if(Boolean.getBoolean("vl.personalTest")&&mc.player==null){worldWaitTicks++;if(worldWaitTicks==20)net.minecraft.client.gui.screen.world.CreateWorldScreen.show(mc,()->{});if(worldWaitTicks>1200)throw new AssertionError("World creation timed out");if(!created&&mc.currentScreen instanceof net.minecraft.client.gui.screen.world.CreateWorldScreen screen){created=true;var ui=screen.getWorldCreator();ui.setWorldName("VL personal performance smoke");ui.setSeed("1");ui.setGenerateStructures(false);ui.setGameMode(net.minecraft.client.gui.screen.world.WorldCreator.Mode.CREATIVE);ui.setWorldType(ui.getNormalWorldTypes().stream().filter(t->t.toString().contains("flat")).findFirst().orElseThrow());try{var create=screen.getClass().getDeclaredMethod("method_2736");create.setAccessible(true);create.invoke(screen);}catch(Exception e){throw new RuntimeException(e);}}return;}ticks++;try{
   if(Boolean.getBoolean("vl.personalTest")){
    if(ticks==1){Object root=cls("config.ConfigStore").getMethod("get").invoke(null);set(field(root,"motionBlur"),"enabled",true);cls("BloodHelperClient").getMethod("saveConfig").invoke(null);}
    if(ticks==55){Class<?> blur=cls("visuals.MotionBlurRenderer");check(((Number)blur.getMethod("processedFrames").invoke(null)).longValue()>2,"Motion Blur did not render");check((boolean)blur.getMethod("hasHistory").invoke(null),"No GPU history");blurBefore=((Number)blur.getMethod("processedFrames").invoke(null)).longValue();Object root=cls("config.ConfigStore").getMethod("get").invoke(null);set(field(root,"motionBlur"),"enabled",false);cls("BloodHelperClient").getMethod("saveConfig").invoke(null);}
    if(ticks==60){check(!(boolean)cls("visuals.MotionBlurRenderer").getMethod("hasHistory").invoke(null),"Disabled blur kept buffers");Object root=cls("config.ConfigStore").getMethod("get").invoke(null);set(field(root,"motionBlur"),"enabled",true);cls("BloodHelperClient").getMethod("saveConfig").invoke(null);}
    if(ticks==85){Class<?> blur=cls("visuals.MotionBlurRenderer");check(((Number)blur.getMethod("processedFrames").invoke(null)).longValue()>blurBefore+2,"Blur resume failed");var failure=blur.getDeclaredField("reportedFailure");failure.setAccessible(true);check(!(boolean)failure.get(null),"GPU blur failure");Object root=cls("BloodHelperClient").getMethod("config").invoke(null);check((boolean)field(field(root,"optimizer"),"enabled"),"Optimizer not enabled");check(field(field(root,"renderQuality"),"glassQuality").equals("LOW"),"Low quality profile not active");System.out.println("VL_SMOKE: private GPU Motion Blur blend, disable/re-enable, low-glass profile and optimizer passed");}
   }
   int menuTick=ticks-(Boolean.getBoolean("vl.personalTest")?90:0);
   if(menuTick==10){mc.setScreen((net.minecraft.client.gui.screen.Screen)cls("gui.BloodHelperScreen").getConstructor().newInstance());}
   if(menuTick==20){Object screen=mc.currentScreen;Class<?> enumClass=cls("gui.BloodHelperScreen$Module");Method values=enumClass.getDeclaredMethod("values");values.setAccessible(true);Method rows=screen.getClass().getDeclaredMethod("rows",enumClass);rows.setAccessible(true);for(Object module:(Object[])values.invoke(null))rows.invoke(screen,module);System.out.println("VL_SMOKE: all 67 module settings built");}
   if(menuTick==30){Object screen=mc.currentScreen;var page=screen.getClass().getDeclaredField("page");page.setAccessible(true);page.set(screen,Enum.valueOf((Class)page.getType(),"ABOUT"));System.out.println("VL_SMOKE: about page opened");}
   if(menuTick==50){System.out.println("VL_SMOKE: PASS startup and menu");mc.scheduleStop();}
  }catch(Throwable e){e.printStackTrace();System.exit(1);}});
 }
}
