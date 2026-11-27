package pyz1k.smoke;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import pyz1k.Pyz1k;
import pyz1k.extensions.*;
import pyz1k.screen.menu.MenuScreen;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.lang.reflect.*;

/** Test source set is excluded from the deliverable JAR. No server connection is made. */
public final class SmokeClient implements ClientModInitializer {
    private int ticks;
    private boolean packReady;
    @Override public void onInitializeClient(){
        if(!Boolean.getBoolean("pyz1k.smoke"))return;
        ClientLifecycleEvents.CLIENT_STARTED.register(mc->{
            MixinEnvironment.getCurrentEnvironment().audit();
            pyz1k.extensions.pvp.IntegratedSmoke.idle();
            int count=Pyz1k.getInstance().getModuleManager().getModules().size();
            if(count!=32)throw new AssertionError("Expected 32 modules, got "+count);
            for(var module:Pyz1k.getInstance().getModuleManager().getModules()){
                if(module.getName().equals("LegList")||module.getName().equals("LegAlerts")||module.getName().equals("LootGlow"))throw new AssertionError("Removed module registered");
            }
            System.out.println("PYZ1K_SMOKE: mixin audit passed, 32 modules registered");
            String fixture=System.getProperty("pyz1k.testPack");
            if(fixture!=null){
                var id=java.util.UUID.fromString("a0000000-0000-4000-8000-000000000001");
                var loader=mc.getServerResourcePackProvider();loader.initWorldPack();
                loader.addResourcePack(id,java.nio.file.Path.of(fixture));loader.acceptAll();
                loader.getPackLoadFuture(id).thenRun(()->mc.execute(()->{
                    SkinCatalog.session("smoke.invalid");SkinCatalog.ensureLoaded();
                    if(SkinCatalog.entries().size()<12)throw new AssertionError("Test server pack was not indexed: "+SkinCatalog.entries().size());
                    for(var target:SkinCatalog.TARGETS){
                        var valid=SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).toList();
                        if(valid.size()!=switch(net.minecraft.registry.Registries.ITEM.getId(target).getPath()){case "mace","diamond_sword","bow"->4;case "netherite_sword"->5;case "totem_of_undying"->6;default->3;})throw new AssertionError("Wrong material filter: "+target+" / "+valid.size());
                        SkinCatalog.select(target,valid.getFirst());
                        var incompatible=SkinCatalog.entries().stream().filter(v->!SkinCatalog.compatible(v,target)).findFirst().orElseThrow();
                        SkinCatalog.select(target,incompatible);
                        if(!SkinCatalog.chosen(target).equals(valid.getFirst()))throw new AssertionError("Cross-material selection accepted");
                        SkinCatalog.select(target,null);
                    }
                    System.out.println("PYZ1K_SMOKE: six material filters isolated; cross-material selections rejected");
                    var skin=SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,net.minecraft.item.Items.DIAMOND_SWORD)).findFirst().orElseThrow();var original=new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SWORD);
                    var copy=original.copy();skin.apply(copy);
                    if(net.minecraft.item.ItemStack.areEqual(original,copy))throw new AssertionError("Skin was not applied to render copy");
                    if(!original.get(net.minecraft.component.DataComponentTypes.ITEM_MODEL).equals(net.minecraft.util.Identifier.ofVanilla("diamond_sword")))throw new AssertionError("Original item mutated");
                    System.out.println("PYZ1K_SMOKE: server pack indexed, model previews available, original item preserved");
                    SkinCatalog.toggleFavorite(skin);if(!SkinCatalog.favorite(skin))throw new AssertionError("Favorite did not persist");SkinCatalog.toggleFavorite(skin);
                    for(var target:SkinCatalog.TARGETS)SkinCatalog.select(target,SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).findFirst().orElseThrow());
                    if(!SkinCatalog.savePreset("Smoke test"))throw new AssertionError("Preset save failed");
                    for(var target:SkinCatalog.TARGETS)SkinCatalog.select(target,null);
                    if(SkinCatalog.applyPreset("Smoke test")!=6)throw new AssertionError("Preset did not restore all six materials");
                    SkinCatalog.deletePreset("Smoke test");
                    boolean before=mc.options.hudHidden;ScreenshotMode.toggle();if(!mc.options.hudHidden||!ScreenshotMode.active())throw new AssertionError("Screenshot mode failed");ScreenshotMode.toggle();if(mc.options.hudHidden!=before)throw new AssertionError("Screenshot mode restore failed");
                    ServerProfiles.automatic(false);ServerProfiles.switchServer("");ExtensionConfig.get().serverProfiles.removeAll(java.util.List.of("smoke-a.invalid","smoke-b.invalid","smoke-c.invalid"));
                    boolean oldLoot=FeatureModules.skins.isEnable(),oldNames=FeatureModules.names.isEnable();
                    ServerProfiles.automatic(true);ServerProfiles.switchServer("smoke-a.invalid");FeatureModules.skins.setEnable(!oldLoot);ServerProfiles.save();
                    ServerProfiles.switchServer("smoke-b.invalid");if(FeatureModules.skins.isEnable()!=oldLoot)throw new AssertionError("Global baseline not restored for new server");
                    FeatureModules.names.setEnable(!oldNames);ServerProfiles.switchServer("smoke-a.invalid");
                    if(FeatureModules.skins.isEnable()==oldLoot||FeatureModules.names.isEnable()!=oldNames)throw new AssertionError("Server profile isolation failed");
                    ServerProfiles.switchServer("");FeatureModules.names.setEnable(!oldNames);
                    ServerProfiles.switchServer("smoke-c.invalid");if(FeatureModules.names.isEnable()==oldNames)throw new AssertionError("Global edits outside server lost");
                    ServerProfiles.switchServer("");FeatureModules.names.setEnable(oldNames);
                    ServerProfiles.automatic(false);ServerProfiles.switchServer("");
                    System.out.println("PYZ1K_SMOKE: favorite/preset round trip, screenshot state and two-server profile isolation passed");
                    FeatureModules.oldMirror.setEnable(true);
                    var mirror=new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SWORD);
                    mirror.set(net.minecraft.component.DataComponentTypes.ITEM_MODEL,net.minecraft.util.Identifier.of("bloodstone:tools/mirror_blade/default/mirror_blade"));
                    var restored=LootGloow.oldMirror(mirror);
                    if(restored==mirror||!restored.get(net.minecraft.component.DataComponentTypes.ITEM_MODEL).toString().equals("pyz1k:legacy_mirror")||!mirror.get(net.minecraft.component.DataComponentTypes.ITEM_MODEL).toString().startsWith("bloodstone:"))throw new AssertionError("Old mirror render copy failed");
                    System.out.println("PYZ1K_SMOKE: old mirror render copy preserves original item");
                    testBloodData();
                    packReady=true;
                })).exceptionally(error->{error.printStackTrace();System.exit(1);return null;});
            }else packReady=true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            if(mc.getOverlay()!=null||!packReady)return;
            ticks++;
            if(ticks==30)mc.setScreen(new MenuScreen(null));
            if(ticks==60)shot(mc,"01-menu.png");
            if(ticks==70)mc.setScreen(new ExtensionScreen(null));
            if(ticks==100)shot(mc,"02-catalog.png");
            if(ticks==110){try{var field=ExtensionScreen.class.getDeclaredField("target");field.setAccessible(true);field.set(mc.currentScreen,net.minecraft.item.Items.TOTEM_OF_UNDYING);var method=ExtensionScreen.class.getDeclaredMethod("invalidate");method.setAccessible(true);method.invoke(mc.currentScreen);}catch(Exception e){throw new RuntimeException(e);}}
            if(ticks==140)shot(mc,"03-totem-catalog.png");
            if(ticks==150){
                try {
                    Class<?> page=Class.forName("pyz1k.extensions.ExtensionScreen$Page");
                    Field field=ExtensionScreen.class.getDeclaredField("page");field.setAccessible(true);
                    field.set(mc.currentScreen,Enum.valueOf((Class)page,"LAYOUT"));
                }catch(Exception e){throw new RuntimeException(e);}
            }
            if(ticks==180)shot(mc,"04-layout.png");
            if(ticks==190)openPage(mc,"PRESETS");
            if(ticks==210)shot(mc,"05-presets.png");


            if(ticks==250)openPage(mc,"PROFILES");
            if(ticks==270)shot(mc,"07-profiles.png");
            if(ticks==280){
                var target=net.minecraft.item.Items.DIAMOND_SWORD;
                mc.setScreen(new SkinPreviewScreen(new ExtensionScreen(null),target,SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).findFirst().orElseThrow()));
            }
            if(ticks==310)shot(mc,"08-preview.png");
            if(ticks==320){
                FeatureAlerts.notice("Проверка прочности",net.minecraft.text.Text.literal("Тестовое уведомление"),new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SWORD),true);
                mc.setScreen(new net.minecraft.client.gui.screen.Screen(net.minecraft.text.Text.literal("Alert test")){
                    @Override public void render(net.minecraft.client.gui.DrawContext ctx,int mx,int my,float delta){
                        if(ticks==339)FeatureAlerts.notice("Проверка прочности",net.minecraft.text.Text.literal("Тестовое уведомление"),new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SWORD),false);
                        FeatureAlerts.render(ctx);
                    }
                });
            }
            if(ticks==340)shot(mc,"09-alert.png");
            if(ticks==350){
                mc.setScreen(new net.minecraft.client.gui.screen.Screen(net.minecraft.text.Text.literal("BloodSword test")){
                    @Override public void render(net.minecraft.client.gui.DrawContext ctx,int mx,int my,float delta){
                        var sword=bloodStack();long now=System.currentTimeMillis();var pdc=new net.minecraft.nbt.NbtCompound();
                        pdc.putLong("runeslegendary:bloodsword_cooldown_until",now+247000);
                        pdc.putLong("runeslegendary:bloodsword_bow_pull_cooldown_until",now+13000);
                        var tag=new net.minecraft.nbt.NbtCompound();tag.put("PublicBukkitValues",pdc);
                        sword.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,net.minecraft.component.type.NbtComponent.of(tag));
                        BloodSwordCooldownTracker.scan(java.util.List.of(sword),new net.minecraft.entity.player.ItemCooldownManager(),BloodCooldownProfiles.mode(FeatureModules.blood.mode.getValue()),now,true);
                        ExtensionClient.BLOOD_HUD.render(ctx,width,height,delta);
                    }
                });
            }
            if(ticks==370)shot(mc,"10-blood-cooldown.png");
            if(ticks==380)FeatureModules.blood.mode.setValue("СМП/ФФА СМП");
            if(ticks==400)shot(mc,"11-blood-smp.png");
            if(ticks==410){
                var target=net.minecraft.item.Items.DIAMOND_SWORD;
                mc.setScreen(new SkinPreviewScreen(new ExtensionScreen(null),target,new SkinCatalog.Skin("old-mirror",net.minecraft.util.Identifier.of("pyz1k:legacy_mirror"),ModelVariants.Data.empty(),"Старый Зеркальный Меч","client","diamond_sword")));
            }
            if(ticks==440)shot(mc,"12-old-mirror.png");
            if(ticks>=450&&ticks<620&&(ticks-450)%10==0)pyz1k.extensions.pvp.IntegratedSmoke.open((ticks-450)/10,0);
            if(ticks==625)pyz1k.extensions.pvp.IntegratedSmoke.open(16,1);
            if(ticks==630)pyz1k.extensions.pvp.IntegratedSmoke.open(0,2);
            if(ticks==635)shot(mc,"13-pvp-hud-settings.png");
            if(ticks==636){mc.setScreen(new MenuScreen(null));mc.currentScreen.keyPressed(new net.minecraft.client.input.KeyInput(290,0,0));if(!(mc.currentScreen instanceof FeatureHelpScreen help))throw new AssertionError("F1 help did not open");help.choose(31);}
            if(ticks==639)shot(mc,"14-function-help.png");
            if(ticks==640){
                System.out.println("PYZ1K_SMOKE: all 17 PvP settings screens rendered");

                System.out.println("PYZ1K_SMOKE: all screens including BloodSword HUD rendered without error");mc.scheduleStop();
            }
        });
    }
    private net.minecraft.item.ItemStack bloodStack(){
        var stack=new net.minecraft.item.ItemStack(net.minecraft.item.Items.NETHERITE_SWORD);
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_MODEL,net.minecraft.util.Identifier.of("bloodstone:tools/bloodsword/default/sword"));return stack;
    }
    private void testBloodData(){
        var stack=bloodStack();var manager=new net.minecraft.entity.player.ItemCooldownManager();manager.set(stack,100);
        var mode=BloodCooldownProfiles.mode("Митапы");
        var timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack,stack.copy(),new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SWORD)),manager,mode,100000,false);
        if(timers.size()!=1||timers.getFirst().remaining()!=5)throw new AssertionError("Native cooldown or group dedup failed");
        for(int i=0;i<20;i++)manager.update();
        timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack),manager,mode,100000,false);
        if(timers.getFirst().remaining()!=4)throw new AssertionError("Native cooldown tick countdown failed");
        var pdc=new net.minecraft.nbt.NbtCompound();pdc.putLong("plugin:bloodsword_cooldown_until",105000);pdc.putLong("plugin:bloodsword_bow_pull_cooldown_until",102000);
        var tag=new net.minecraft.nbt.NbtCompound();tag.put("PublicBukkitValues",pdc);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,net.minecraft.component.type.NbtComponent.of(tag));
        timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack,stack.copy()),manager,BloodCooldownProfiles.mode("СМП/ФФА СМП"),100000,true);
        if(timers.size()!=2||timers.stream().noneMatch(t->t.remaining()==5)||timers.stream().noneMatch(t->t.remaining()==2))throw new AssertionError("Server item timers/dedup/SMP failed");
        if(!BloodSwordCooldownTracker.scan(java.util.List.of(stack),new net.minecraft.entity.player.ItemCooldownManager(),mode,110000,true).isEmpty())throw new AssertionError("Expired cooldown visible");
        BloodSwordCooldownTracker.clear();
        System.out.println("PYZ1K_SMOKE: BloodSword native cooldowns, tick countdown, PDC timers, dedup and server-only SMP passed");
    }
    private void openPage(MinecraftClient mc,String name){
        try{ExtensionScreen screen=new ExtensionScreen(null);mc.setScreen(screen);Class<?> page=Class.forName("pyz1k.extensions.ExtensionScreen$Page");Field field=ExtensionScreen.class.getDeclaredField("page");field.setAccessible(true);field.set(screen,Enum.valueOf((Class)page,name));}
        catch(Exception e){throw new RuntimeException(e);}
    }
    private void shot(MinecraftClient mc,String name){ScreenshotRecorder.saveScreenshot(mc.runDirectory,name,mc.getFramebuffer(),1,t->System.out.println("PYZ1K_SMOKE: "+t.getString()));}
}
