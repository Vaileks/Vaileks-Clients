package pyz1k.smoke;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import pyz1k.Pyz1k;
import pyz1k.extensions.*;
import pyz1k.screen.menu.MenuScreen;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.lang.reflect.*;

/** Test source set is excluded from the deliverable JAR. No server connection is made. */
public final class SmokeClient implements ClientModInitializer {
    private int ticks;
    private boolean packReady;
    private boolean packRequested;
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
            mc.execute(()->{
                net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(mc,()->{});
                if (!(mc.gui.screen() instanceof net.minecraft.client.gui.screens.worldselection.CreateWorldScreen screen)) throw new AssertionError("No create-world screen");
                var ui=screen.getUiState();
                ui.setName("Pyz1k offline smoke");ui.setSeed("1");ui.setGenerateStructures(false);
                var preset=ui.getSettings().worldgenLoadContext().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT);
                ui.setWorldType(new net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.WorldTypeEntry(preset));
                ui.setGameMode(net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.SelectedGameMode.CREATIVE);
                try {var create=screen.getClass().getDeclaredMethod("onCreate");create.setAccessible(true);create.invoke(screen);}
                catch(Exception error){throw new RuntimeException(error);}
            });
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            if(mc.player!=null&&!packRequested&&mc.gui.overlay()==null){packRequested=true;loadPack(mc);}
            if(mc.gui.overlay()!=null||!packReady)return;
            ticks++;
            if(ticks==30)mc.gui.setScreen(new MenuScreen(null));
            if(ticks==60)shot(mc,"01-menu.png");
            if(ticks==70)mc.gui.setScreen(new ExtensionScreen(null));
            if(ticks==100)shot(mc,"02-catalog.png");
            if(ticks==110){try{var field=ExtensionScreen.class.getDeclaredField("target");field.setAccessible(true);field.set(mc.gui.screen(),net.minecraft.world.item.Items.TOTEM_OF_UNDYING);var method=ExtensionScreen.class.getDeclaredMethod("invalidate");method.setAccessible(true);method.invoke(mc.gui.screen());}catch(Exception e){throw new RuntimeException(e);}}
            if(ticks==140)shot(mc,"03-totem-catalog.png");
            if(ticks==150){
                try {
                    Class<?> page=Class.forName("pyz1k.extensions.ExtensionScreen$Page");
                    Field field=ExtensionScreen.class.getDeclaredField("page");field.setAccessible(true);
                    field.set(mc.gui.screen(),Enum.valueOf((Class)page,"LAYOUT"));
                }catch(Exception e){throw new RuntimeException(e);}
            }
            if(ticks==180)shot(mc,"04-layout.png");
            if(ticks==190)openPage(mc,"PRESETS");
            if(ticks==210)shot(mc,"05-presets.png");


            if(ticks==250)openPage(mc,"PROFILES");
            if(ticks==270)shot(mc,"07-profiles.png");
            if(ticks==280){
                var target=net.minecraft.world.item.Items.DIAMOND_SWORD;
                mc.gui.setScreen(new SkinPreviewScreen(new ExtensionScreen(null),target,SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).findFirst().orElseThrow()));
            }
            if(ticks==310)shot(mc,"08-preview.png");
            if(ticks==320){
                FeatureAlerts.notice("Проверка прочности",net.minecraft.network.chat.Component.literal("Тестовое уведомление"),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD),true);
                mc.gui.setScreen(new net.minecraft.client.gui.screens.Screen(net.minecraft.network.chat.Component.literal("Alert test")){
                    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor ctx,int mx,int my,float delta){
                        if(ticks==339)FeatureAlerts.notice("Проверка прочности",net.minecraft.network.chat.Component.literal("Тестовое уведомление"),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD),false);
                        FeatureAlerts.render(ctx);
                    }
                });
            }
            if(ticks==340)shot(mc,"09-alert.png");
            if(ticks==350){
                mc.gui.setScreen(new net.minecraft.client.gui.screens.Screen(net.minecraft.network.chat.Component.literal("BloodSword test")){
                    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor ctx,int mx,int my,float delta){
                        var sword=bloodStack();long now=System.currentTimeMillis();var pdc=new net.minecraft.nbt.CompoundTag();
                        pdc.putLong("runeslegendary:bloodsword_cooldown_until",now+247000);
                        pdc.putLong("runeslegendary:bloodsword_bow_pull_cooldown_until",now+13000);
                        var tag=new net.minecraft.nbt.CompoundTag();tag.put("PublicBukkitValues",pdc);
                        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
                        BloodSwordCooldownTracker.scan(java.util.List.of(sword),new net.minecraft.world.item.ItemCooldowns(),BloodCooldownProfiles.mode(FeatureModules.blood.mode.getValue()),now,true);
                        ExtensionClient.BLOOD_HUD.render(ctx,width,height,delta);
                    }
                });
            }
            if(ticks==370)shot(mc,"10-blood-cooldown.png");
            if(ticks==380)FeatureModules.blood.mode.setValue("СМП/ФФА СМП");
            if(ticks==400)shot(mc,"11-blood-smp.png");
            if(ticks==410){
                var target=net.minecraft.world.item.Items.DIAMOND_SWORD;
                mc.gui.setScreen(new SkinPreviewScreen(new ExtensionScreen(null),target,new SkinCatalog.Skin("old-mirror",net.minecraft.resources.Identifier.parse("pyz1k:legacy_mirror"),ModelVariants.Data.empty(),"Старый Зеркальный Меч","client","diamond_sword")));
            }
            if(ticks==440)shot(mc,"12-old-mirror.png");
            if(ticks>=450&&ticks<620&&(ticks-450)%10==0)pyz1k.extensions.pvp.IntegratedSmoke.open((ticks-450)/10,0);
            if(ticks==625)pyz1k.extensions.pvp.IntegratedSmoke.open(16,1);
            if(ticks==630)pyz1k.extensions.pvp.IntegratedSmoke.open(0,2);
            if(ticks==635)shot(mc,"13-pvp-hud-settings.png");
            if(ticks==636){mc.gui.setScreen(new MenuScreen(null));mc.gui.screen().keyPressed(new net.minecraft.client.input.KeyEvent(58,0,0));if(!(mc.gui.screen() instanceof FeatureHelpScreen help))throw new AssertionError("F1 help did not open");help.choose(31);}
            if(ticks==639)shot(mc,"14-function-help.png");
            if(ticks==640){pyz1k.extensions.pvp.IntegratedSmoke.world();
                System.out.println("PYZ1K_SMOKE: all 17 PvP settings screens rendered");

                System.out.println("PYZ1K_SMOKE: all screens including BloodSword HUD rendered without error");mc.stop();
            }
        });
    }
    private void loadPack(Minecraft mc){
            String fixture=System.getProperty("pyz1k.testPack");
            if(fixture!=null){
                var id=java.util.UUID.fromString("a0000000-0000-4000-8000-000000000001");
                var loader=mc.getDownloadedPackSource();loader.configureForLocalWorld();
                loader.pushLocalPack(id,java.nio.file.Path.of(fixture));loader.allowServerPacks();
                loader.waitForPackFeedback(id).thenRun(()->mc.execute(()->{try{
                    SkinCatalog.session("smoke.invalid");SkinCatalog.ensureLoaded();
                    if(SkinCatalog.entries().size()<12)throw new AssertionError("Test server pack was not indexed: "+SkinCatalog.entries().size());
                    for(var target:SkinCatalog.TARGETS){
                        var valid=SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).toList();
                        if(valid.size()!=switch(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(target).getPath()){case "mace","diamond_sword","bow"->4;case "netherite_sword"->5;case "totem_of_undying"->6;default->3;})throw new AssertionError("Wrong material filter: "+target+" / "+valid.size());
                        SkinCatalog.select(target,valid.getFirst());
                        var incompatible=SkinCatalog.entries().stream().filter(v->!SkinCatalog.compatible(v,target)).findFirst().orElseThrow();
                        SkinCatalog.select(target,incompatible);
                        if(!SkinCatalog.chosen(target).equals(valid.getFirst()))throw new AssertionError("Cross-material selection accepted");
                        SkinCatalog.select(target,null);
                    }
                    System.out.println("PYZ1K_SMOKE: six material filters isolated; cross-material selections rejected");
                    var skin=SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,net.minecraft.world.item.Items.DIAMOND_SWORD)).findFirst().orElseThrow();var original=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
                    var copy=original.copy();skin.apply(copy);
                    if(net.minecraft.world.item.ItemStack.matches(original,copy))throw new AssertionError("Skin was not applied to render copy");
                    if(!original.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).equals(net.minecraft.resources.Identifier.withDefaultNamespace("diamond_sword")))throw new AssertionError("Original item mutated");
                    System.out.println("PYZ1K_SMOKE: server pack indexed, model previews available, original item preserved");
                    SkinCatalog.toggleFavorite(skin);if(!SkinCatalog.favorite(skin))throw new AssertionError("Favorite did not persist");SkinCatalog.toggleFavorite(skin);
                    for(var target:SkinCatalog.TARGETS)SkinCatalog.select(target,SkinCatalog.entries().stream().filter(v->SkinCatalog.compatible(v,target)).findFirst().orElseThrow());
                    if(!SkinCatalog.savePreset("Smoke test"))throw new AssertionError("Preset save failed");
                    for(var target:SkinCatalog.TARGETS)SkinCatalog.select(target,null);
                    if(SkinCatalog.applyPreset("Smoke test")!=6)throw new AssertionError("Preset did not restore all six materials");
                    SkinCatalog.deletePreset("Smoke test");
                    boolean before=mc.gui.hud.isHidden();ScreenshotMode.toggle();if(!mc.gui.hud.isHidden()||!ScreenshotMode.active())throw new AssertionError("Screenshot mode failed");ScreenshotMode.toggle();if(mc.gui.hud.isHidden()!=before)throw new AssertionError("Screenshot mode restore failed");
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
                    var mirror=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
                    mirror.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("bloodstone:tools/mirror_blade/default/mirror_blade"));
                    var restored=LootGloow.oldMirror(mirror);
                    if(restored==mirror||!restored.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).toString().equals("pyz1k:legacy_mirror")||!mirror.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).toString().startsWith("bloodstone:"))throw new AssertionError("Old mirror render copy failed");
                    System.out.println("PYZ1K_SMOKE: old mirror render copy preserves original item");
                    testBloodData();
                    testWorldRendering(mc);
                    if(Boolean.getBoolean("pyz1k.policyOnly")){System.out.println("PYZ1K_SMOKE: final entity glow and render-state policy passed");mc.stop();}
                    packReady=true;
                }catch(Throwable error){error.printStackTrace();System.exit(1);}})).exceptionally(error->{error.printStackTrace();System.exit(1);return null;});
            }else packReady=true;
    }
    private void testWorldRendering(Minecraft mc) throws Exception {
        var actor=new net.minecraft.client.player.RemotePlayer(mc.level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("a0000000-0000-4000-8000-000000000002"),"Pyz1kPreview"));
        actor.setId(-100);actor.setPos(mc.player.getX(),mc.player.getY(),mc.player.getZ()+4);
        actor.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
        mc.level.addEntity(actor);mc.player.setYRot(0);mc.player.setXRot(0);
        FeatureModules.names.setEnable(true);
        net.minecraft.client.renderer.entity.EntityRenderer renderer=mc.getEntityRenderDispatcher().getRenderer(actor);
        var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)renderer.createRenderState(actor,0f);
        if(state.nameTag==null||!state.nameTag.getString().contains("Pyz1kPreview"))throw new AssertionError("Name extraction failed");
        Object[] mode={null};
        var queue=(net.minecraft.client.renderer.SubmitNodeCollector)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.client.renderer.SubmitNodeCollector.class},(proxy,method,args)->{
            if(method.getName().equals("submitText"))mode[0]=args[5];
            return method.getReturnType().isInstance(proxy)?proxy:null;
        });
        var draw=net.minecraft.client.renderer.entity.EntityRenderer.class.getDeclaredMethod("submitNameDisplay",net.minecraft.client.renderer.entity.state.EntityRenderState.class,com.mojang.blaze3d.vertex.PoseStack.class,net.minecraft.client.renderer.SubmitNodeCollector.class,net.minecraft.client.renderer.state.level.CameraRenderState.class,int.class);
        draw.setAccessible(true);
        var camera=new net.minecraft.client.renderer.state.level.CameraRenderState();
        FeatureModules.names.throughWalls.setValue(true);
        draw.invoke(renderer,state,new com.mojang.blaze3d.vertex.PoseStack(),queue,camera,0);
        if(mode[0]!=net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH)throw new AssertionError("Names through walls not submitted");
        mode[0]=null;FeatureModules.names.throughWalls.setValue(false);
        draw.invoke(renderer,state,new com.mojang.blaze3d.vertex.PoseStack(),queue,camera,0);
        if(mode[0]!=net.minecraft.client.gui.Font.DisplayMode.NORMAL)throw new AssertionError("Normal name depth failed");
        FeatureModules.names.throughWalls.setValue(true);
        FeatureModules.skins.setEnable(true);
        var held=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        var local=SkinCatalog.displayCopy(held,net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,mc.player);
        if(local==held||!held.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).equals(net.minecraft.resources.Identifier.withDefaultNamespace("diamond_sword")))throw new AssertionError("Local render copy failed");
        if(SkinCatalog.displayCopy(held,net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,actor)!=held)throw new AssertionError("Other player's item changed");
        FeatureModules.loot.setEnable(true);FeatureModules.loot.scale.setValue(1.5f);
        var regular=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),held.copy());
        if(LootGloow.kind(regular)!=LootPolicy.Kind.ORDINARY||LootGloow.scale(regular)!=1.5f)throw new AssertionError("Ordinary loot not eligible");
        var legendary=held.copy();legendary.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("bloodstone:tools/mirror_blade/default/mirror_blade"));
        var protectedEntity=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),legendary);
        if(LootGloow.glow(protectedEntity)||LootGloow.scale(protectedEntity)!=1)throw new AssertionError("Legendary highlighted or scaled");
        var epic=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD);epic.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("bloodstone:tools/netherite_shield/default/netherite_shield"));
        var epicEntity=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),epic);
        if(LootGloow.kind(epicEntity)!=LootPolicy.Kind.SHIELD||!LootGloow.glow(epicEntity))throw new AssertionError("Epic shield whitelist failed");
        var totem=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
        totem.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("minecraft:wither_totem"));
        var totemEntity=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),totem);
        if(LootGloow.kind(totemEntity)!=LootPolicy.Kind.TOTEM||!LootGloow.glow(totemEntity))throw new AssertionError("Custom totem whitelist failed");
        var ballista=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER);ballista.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("modelengine:blood_ballista_pack_v2/root"));
        var ballistaEntity=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),ballista);
        if(LootGloow.kind(ballistaEntity)!=LootPolicy.Kind.BALLISTA||!ballistaEntity.isCurrentlyGlowing())throw new AssertionError("ModelEngine ballista whitelist failed");
        if(!regular.isCurrentlyGlowing()||protectedEntity.isCurrentlyGlowing()||!epicEntity.isCurrentlyGlowing()||!totemEntity.isCurrentlyGlowing())throw new AssertionError("Entity glow mixin violated loot policy");
        regular.setId(-102);protectedEntity.setId(-103);
        var groundRenderer=(net.minecraft.client.renderer.entity.ItemEntityRenderer)(Object)mc.getEntityRenderDispatcher().getRenderer(protectedEntity);
        var groundState=groundRenderer.createRenderState();groundRenderer.extractRenderState(protectedEntity,groundState,0);
        if(((GroundLootState)(Object)groundState).lootGloow$getScale()!=1)throw new AssertionError("Legendary render scale changed");
        groundRenderer.extractRenderState(regular,groundState,0);
        if(((GroundLootState)(Object)groundState).lootGloow$getScale()!=1.5f)throw new AssertionError("Ordinary render scale missing");
        var tagged=epic.copy();var legendaryTag=new net.minecraft.nbt.CompoundTag();var legendaryPdc=new net.minecraft.nbt.CompoundTag();legendaryPdc.putString("runeslegendary:group","original");legendaryTag.put("PublicBukkitValues",legendaryPdc);
        tagged.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(legendaryTag));
        var taggedEntity=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX(),mc.player.getY(),mc.player.getZ(),tagged);
        if(taggedEntity.isCurrentlyGlowing()||LootGloow.scale(taggedEntity)!=1)throw new AssertionError("Legendary marker bypassed whitelist");
        System.out.println("PYZ1K_SMOKE: vanilla loot scales, legendary exclusion and epic shield whitelist passed");
        var dropped=new net.minecraft.world.entity.item.ItemEntity(mc.level,mc.player.getX()+1,mc.player.getY()+1,mc.player.getZ()+2,held.copy());
        dropped.setId(-101);mc.level.addEntity(dropped);
        System.out.println("PYZ1K_SMOKE: name extraction/depth modes, local skin ownership and dropped-item render fixture passed");
    }
    private net.minecraft.world.item.ItemStack bloodStack(){
        var stack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
        stack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse("bloodstone:tools/bloodsword/default/sword"));return stack;
    }
    private void testBloodData(){
        var stack=bloodStack();var manager=new net.minecraft.world.item.ItemCooldowns();manager.addCooldown(stack,100);
        var mode=BloodCooldownProfiles.mode("Митапы");
        var timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack,stack.copy(),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)),manager,mode,100000,false);
        if(timers.size()!=1||timers.getFirst().remaining()!=5)throw new AssertionError("Native cooldown or group dedup failed");
        for(int i=0;i<20;i++)manager.tick();
        timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack),manager,mode,100000,false);
        if(timers.getFirst().remaining()!=4)throw new AssertionError("Native cooldown tick countdown failed");
        var pdc=new net.minecraft.nbt.CompoundTag();pdc.putLong("plugin:bloodsword_cooldown_until",105000);pdc.putLong("plugin:bloodsword_bow_pull_cooldown_until",102000);
        var tag=new net.minecraft.nbt.CompoundTag();tag.put("PublicBukkitValues",pdc);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
        timers=BloodSwordCooldownTracker.scan(java.util.List.of(stack,stack.copy()),manager,BloodCooldownProfiles.mode("СМП/ФФА СМП"),100000,true);
        if(timers.size()!=2||timers.stream().noneMatch(t->t.remaining()==5)||timers.stream().noneMatch(t->t.remaining()==2))throw new AssertionError("Server item timers/dedup/SMP failed");
        if(!BloodSwordCooldownTracker.scan(java.util.List.of(stack),new net.minecraft.world.item.ItemCooldowns(),mode,110000,true).isEmpty())throw new AssertionError("Expired cooldown visible");
        BloodSwordCooldownTracker.clear();
        System.out.println("PYZ1K_SMOKE: BloodSword native cooldowns, tick countdown, PDC timers, dedup and server-only SMP passed");
    }
    private void openPage(Minecraft mc,String name){
        try{ExtensionScreen screen=new ExtensionScreen(null);mc.gui.setScreen(screen);Class<?> page=Class.forName("pyz1k.extensions.ExtensionScreen$Page");Field field=ExtensionScreen.class.getDeclaredField("page");field.setAccessible(true);field.set(screen,Enum.valueOf((Class)page,name));}
        catch(Exception e){throw new RuntimeException(e);}
    }
    private void shot(Minecraft mc,String name){System.out.println("PYZ1K_SMOKE: screenshot "+name);Screenshot.grab(mc,false);}
}
