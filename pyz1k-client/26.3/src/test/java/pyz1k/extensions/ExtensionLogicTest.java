package pyz1k.extensions;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public final class ExtensionLogicTest {
    private static int checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static JsonObject json(String s){return JsonParser.parseString(s).getAsJsonObject();}
    public static void main(String[] args)throws Exception{
        check(LootPolicy.classify("minecraft:diamond_sword","minecraft:diamond_sword",false,List.of(),false,false)==LootPolicy.Kind.ORDINARY,"vanilla loot allowed");
        for(String item:List.of("minecraft:diamond_sword","minecraft:mace")){
            check(LootPolicy.classify(item,"other:custom",true,List.of(),false,false)==LootPolicy.Kind.NONE,"unknown custom models denied");
            check(LootPolicy.classify(item,"minecraft:diamond_sword",false,List.of(99f),false,false)==LootPolicy.Kind.NONE,"unknown CMD denied");
        }
        for(String mirror:List.of("bloodstone:tools/mirror_blade/default/mirror_blade","legendary:tools/mirror_blade/default/mirror_blade"))check(LootPolicy.classify("minecraft:diamond_sword",mirror,true,List.of(),false,false)==LootPolicy.Kind.NONE,"mirror always protected");
        check(LootPolicy.classify("minecraft:crossbow","astrum:battle_royale/ballista",true,List.of(),false,false)==LootPolicy.Kind.BALLISTA,"ballista exact whitelist");
        check(LootPolicy.classify("minecraft:crossbow","minecraft:crossbow",false,List.of(90f),false,false)==LootPolicy.Kind.BALLISTA,"ballista CMD whitelist");
        for(float cmd:List.of(3f,6000f))check(LootPolicy.classify("minecraft:crossbow","minecraft:crossbow",false,List.of(cmd),false,false)==LootPolicy.Kind.FLAMING,"flaming CMD whitelist");
        check(LootPolicy.classify("minecraft:shield","bloodstone:tools/netherite_shield/default/netherite_shield",true,List.of(),false,false)==LootPolicy.Kind.SHIELD,"shield whitelist");
        check(LootPolicy.classify("minecraft:crossbow","astrum:battle_royale/ballista",true,List.of(),false,true)==LootPolicy.Kind.NONE,"legendary marker overrides whitelist");
        check(LootPolicy.classify("minecraft:diamond_sword","bloodstone:tools/netherite_shield/default/netherite_shield",true,List.of(),false,false)==LootPolicy.Kind.NONE,"whitelist material enforced");
        check(LootPolicy.classify("minecraft:crossbow","evil:legendary",true,List.of(90f),false,false)==LootPolicy.Kind.NONE,"CMD whitelist cannot admit arbitrary models");
        check(LootPolicy.classify("minecraft:totem_of_undying","minecraft:wither_totem",true,List.of(),false,false)==LootPolicy.Kind.TOTEM,"custom totem allowed");
        check(LootPolicy.classify("minecraft:totem_of_undying","astrum:smp/totem_of_undying/creeper",true,List.of(5f),false,false)==LootPolicy.Kind.TOTEM,"CMD custom totem allowed");
        check(LootPolicy.classify("minecraft:totem_of_undying","bloodstone:tools/bloodsword/default/sword",true,List.of(),false,false)==LootPolicy.Kind.NONE,"legendary model cannot use totem exception");
        check(LootPolicy.classify("minecraft:totem_of_undying","minecraft:wither_totem",true,List.of(),false,true)==LootPolicy.Kind.NONE,"legendary tag cannot use totem exception");
        var range=ModelVariants.parse(json("""
          {"model":{"type":"minecraft:range_dispatch","property":"minecraft:custom_model_data","index":1,"scale":2,
          "entries":[{"threshold":6,"model":{"type":"minecraft:model","model":"test:sword_a"}},
          {"threshold":12,"model":{"type":"minecraft:model","model":"test:sword_b"}}],
          "fallback":{"type":"minecraft:model","model":"minecraft:item/diamond_sword"}}}
          """));
        check(range.size()==3,"two CMD variants and fallback");
        check(range.stream().anyMatch(v->v.data().floats().equals(List.of(0f,3f))),"range scale and index preserved");
        var select=ModelVariants.parse(json("""
          {"model":{"type":"minecraft:select","property":"minecraft:custom_model_data","cases":[
          {"when":["red","ruby"],"model":{"type":"minecraft:condition","property":"minecraft:custom_model_data","index":2,
          "on_true":{"type":"minecraft:model","model":"test:glowing"},"on_false":{"type":"minecraft:model","model":"test:normal"}}}]}}
          """));
        check(select.size()==4,"string aliases and flag branches");
        check(select.stream().anyMatch(v->v.data().strings().equals(List.of("ruby"))&&v.data().flags().equals(List.of(false,false,true))),"nested CMD fields compose");
        var animation=ModelVariants.parse(json("""
          {"model":{"type":"minecraft:condition","property":"minecraft:using_item",
          "on_true":{"type":"minecraft:model","model":"test:pulling"},"on_false":{"type":"minecraft:model","model":"test:idle"}}}
          """));
        check(animation.size()==1&&animation.getFirst().label().equals("test:idle"),"bow animation is not a skin variant");
        check(ModelVariants.parse(json("{\"model\":{\"type\":\"minecraft:condition\",\"property\":\"minecraft:custom_model_data\",\"index\":999999999}}")).isEmpty(),"hostile CMD index bounded");
        JsonObject root=new JsonObject(),node=new JsonObject();root.add("model",node);
        for(int i=0;i<80;i++){node.addProperty("type","minecraft:condition");JsonObject child=new JsonObject();node.add("on_false",child);node=child;}
        node.addProperty("type","minecraft:model");node.addProperty("model","test:deep");
        check(ModelVariants.parse(root).isEmpty(),"untrusted definition recursion bounded");
        check(LootPolicy.classify("minecraft:paper","modelengine:blood_ballista_pack_v2/root",true,List.of(),false,false)==LootPolicy.Kind.BALLISTA,"complete ModelEngine ballista allowed");
        check(LootPolicy.classify("minecraft:paper","modelengine:blood_ballista_pack_v2/bow",true,List.of(),false,false)==LootPolicy.Kind.NONE,"ballista visual parts excluded");
        check(LootPolicy.classify("minecraft:diamond_sword","modelengine:blood_ballista_pack_v2/root",true,List.of(),false,false)==LootPolicy.Kind.NONE,"ballista model on wrong material excluded");
        check(LootPolicy.classify("minecraft:paper","modelengine:blood_ballista_pack_v2/root",true,List.of(),false,true)==LootPolicy.Kind.NONE,"legendary marker overrides ballista whitelist");
        check(new InventorySearchQuery("  diamond   sword ").matches("Алмазный меч","minecraft:diamond_sword"),"search words and whitespace");
        check(new InventorySearchQuery("МЕЧ").matches("Алмазный меч","minecraft:diamond_sword"),"localized case insensitive search");
        check(!new InventorySearchQuery("").matches("Sword","minecraft:diamond_sword"),"empty search selects nothing");
        check(!new InventorySearchQuery("diamond bow").matches("Sword","minecraft:diamond_sword"),"all search words required");
        check(InventorySearchQuery.armor("minecraft:diamond_chestplate")&&!InventorySearchQuery.armor("minecraft:carved_pumpkin")&&!InventorySearchQuery.armor("minecraft:elytra"),"armor excludes wearable nonarmor");
        check(FunctionDescriptions.all().size()==40&&FunctionDescriptions.all().stream().allMatch(e->e.text().length()>150),"complete detailed function explanations");
        for(String name:List.of("Tukisg","Trard","DarkCake","darkcake"))check(WelcomeOverlay.matches(name),"targeted greeting "+name);
        check(!WelcomeOverlay.matches("Vaileks")&&!WelcomeOverlay.matches("DarkCake2")&&!WelcomeOverlay.matches(null),"greeting exact allowlist only");
        for(String material:List.of("diamond_sword","netherite_sword","bow","crossbow","totem_of_undying","mace")){
            check(SkinTargets.fromDefinition(material).equals(material),"exact item definition "+material);
            for(String other:List.of("diamond_sword","netherite_sword","bow","crossbow","totem_of_undying","mace"))
                check(SkinTargets.matches(material,other)==material.equals(other),"material isolation "+material+" / "+other);
            check(!SkinTargets.matches("",material),"unknown model excluded from "+material);
            check(!SkinTargets.matches("sword",material),"generic sword excluded from "+material);
        }
        check(SkinTargets.fromDefinition("weapons/legendary_blade").isEmpty(),"model label cannot imply material");
        check(SkinTargets.fromDefinition("wooden_sword").isEmpty(),"unsupported material excluded");
        check(ClientFeatureLogic.lowDurability(100,85,15),"threshold inclusive");
        check(!ClientFeatureLogic.lowDurability(100,84,15),"above threshold");
        check(!ClientFeatureLogic.lowDurability(0,0,15),"non-damageable excluded");
        check(ClientFeatureLogic.lowDurability(2031,1800,15),"fractional durability");
        check(ClientFeatureLogic.profileName("one:25565").equals(ClientFeatureLogic.profileName("one:25565")),"profile name deterministic");
        check(!ClientFeatureLogic.profileName("one").equals(ClientFeatureLogic.profileName("two")),"server profiles isolated");
        check(BloodCooldownProfiles.ALL.size()==5,"five blood sword modes");
        for(var mode:BloodCooldownProfiles.ALL){
            if(mode.serverOnly()){check(mode.id().equals("smp")&&mode.values().isEmpty(),"SMP is server-only, no guessed values");continue;}
            check(mode.seconds("cooldown")==380,"blood mace cooldown from "+mode.id());
            check(mode.seconds("mace_duration")==120,"blood mace duration from "+mode.id());
            check(mode.seconds("transition_duration")==.4,"blood transition from "+mode.id());
            check(mode.seconds("bow_duration")==30,"blood harpoon duration from "+mode.id());
            check(mode.seconds("bow_cooldown")==4,"blood harpoon cooldown from "+mode.id());
            check(mode.seconds("bow_pull_cooldown")==20,"blood pull cooldown from "+mode.id());
        }
        check(BloodCooldownProfiles.time(380,false).equals("6:20"),"minutes formatting");
        check(BloodCooldownProfiles.time(.4,true).equals("0.4с"),"subsecond transition formatting");
        check(BloodCooldownProfiles.remaining(105000,100000)==5,"server timestamp countdown");
        check(BloodCooldownProfiles.remaining(100000,105000)==0,"expired timestamp hidden");
        check(SkinTargets.fromDefinition("smp/totem_of_undying/creeper").equals("totem_of_undying"),"material inherited from directory");
        check(SkinTargets.fromDefinition("wither_totem").equals("totem_of_undying"),"named standalone totem");
        check(SkinTargets.fromDefinition("weapons/diamond_sword/ruby").equals("diamond_sword"),"diamond sword style remains material-specific");
        check(SkinTargets.fromDefinition("weapons/netherite_sword/obsidian").equals("netherite_sword"),"netherite sword style remains material-specific");
        check(SkinTargets.fromDefinition("crossbow/skin").equals("crossbow"),"crossbow not confused with bow");
        check(SkinTargets.fromDefinition("icons/skills/smp_icon_totem_luck").isEmpty(),"UI icon not a wearable skin");
        var composite=ModelVariants.parse(json("{\"model\":{\"type\":\"minecraft:composite\",\"models\":[{\"type\":\"minecraft:model\",\"model\":\"test:totem_base\"},{\"type\":\"minecraft:model\",\"model\":\"test:totem_overlay\"}]}}"));
        check(composite.size()==1&&composite.getFirst().label().equals("test:totem_base"),"composite model retained as one full skin");
        var declarations=List.of(
            new SkinIndex.Definition("minecraft:diamond_sword",json("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"test:item/ruby\"}}"),"server/test"),
            new SkinIndex.Definition("test:weapons/ruby",json("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"test:item/ruby\"}}"),"server/test"),
            new SkinIndex.Definition("astrum:smp/totem_of_undying/creeper",json("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"test:item/creeper\"}}"),"server/test"),
            new SkinIndex.Definition("minecraft:wither_totem",json("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"test:item/wither\"}}"),"server/test"),
            new SkinIndex.Definition("test:icons/totem",json("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"test:item/creeper\"}}"),"server/test"));
        var malformed=new SkinIndex.Definition("test:totems/broken",json("{\"model\":{\"type\":\"minecraft:range_dispatch\",\"property\":\"minecraft:custom_model_data\",\"index\":\"invalid\"}}"),"server/test");
        var mixed=new ArrayList<>(declarations);mixed.add(malformed);
        check(SkinIndex.build(mixed).size()==4,"one malformed definition does not discard the valid catalog");
        var indexed=SkinIndex.build(declarations);
        check(indexed.size()==4,"standalone namespaced/minecraft definitions included; UI excluded");
        check(indexed.stream().anyMatch(e->e.definition().id().equals("test:weapons/ruby")&&e.material().equals("diamond_sword")),"material recovered by actual canonical model association");
        check(indexed.stream().filter(e->e.material().equals("totem_of_undying")).count()==2,"both standalone totems indexed");
        check(indexed.stream().noneMatch(e->e.material().equals("netherite_sword")),"no cross-material leakage");
        String cached=System.getenv("PYZ1K_TEST_PACK");
        if(cached!=null){
            int parsed=0,variants=0;List<SkinIndex.Definition> realDefinitions=new ArrayList<>();
            try(var pack=new ZipFile(cached)){
                var entries=pack.entries();while(entries.hasMoreElements()){
                    var entry=entries.nextElement();String name=entry.getName();if(!name.startsWith("assets/")||!name.contains("/items/")||!name.endsWith(".json"))continue;
                    try(var r=new InputStreamReader(pack.getInputStream(entry),StandardCharsets.UTF_8)){
                        var definition=JsonParser.parseReader(r).getAsJsonObject();variants+=ModelVariants.parse(definition).size();parsed++;
                        String resource=name.substring(7);int slash=resource.indexOf('/');String namespace=resource.substring(0,slash);String path=resource.substring(slash+7,resource.length()-5);
                        realDefinitions.add(new SkinIndex.Definition(namespace+":"+path,definition,"server/test"));
                    }
                }
            }
            check(parsed>0&&variants>0,"real server pack definitions parsed");
            var catalog=SkinIndex.build(realDefinitions);long totems=catalog.stream().filter(e->e.material().equals("totem_of_undying")).count();
            check(catalog.size()>0&&totems>1,"real server pack exposes multiple totem skins");
            System.out.println("Real resource pack: "+parsed+" definitions / "+variants+" variants parsed; catalog "+catalog.size()+" entries, including "+totems+" totems");
        }
        System.out.println("PASS "+checks+" extension logic checks");
    }
}
