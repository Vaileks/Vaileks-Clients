package pyz1k.extensions;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class SkinCatalog {
    public static final List<Item> TARGETS=List.of(Items.DIAMOND_SWORD,Items.NETHERITE_SWORD,Items.BOW,Items.CROSSBOW,Items.TOTEM_OF_UNDYING,Items.MACE);
    public record Skin(String key,Identifier model,ModelVariants.Data data,String name,String pack,String inferredTarget){
        public ItemStack preview(Item target){ItemStack stack=new ItemStack(target);apply(stack);return stack;}
        public void apply(ItemStack stack){
            stack.set(DataComponentTypes.ITEM_MODEL,model);
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(data.floats(),data.flags(),data.strings(),data.colors()));
        }
    }
    private static volatile List<Skin> entries=List.of();
    private static volatile int generation;
    private static final Map<String,Skin> selected=new HashMap<>();
    private static String server="";
    private static final ThreadLocal<Boolean> PREVIEW=ThreadLocal.withInitial(()->false);
    private static boolean dirty=true;
    private static int failures;
    public static void reload(ResourceManager resources){
        LootGloow.clearCache();dirty=true;}
    public static void session(String key){server=key;selected.clear();entries=List.of();dirty=true;}
    public static void ensureLoaded(){
        if(server.isEmpty()||!dirty)return;dirty=false;
        long started=System.nanoTime();ArrayList<Skin> found=new ArrayList<>();failures=0;
        ResourceManager resources=MinecraftClient.getInstance().getResourceManager();
        ArrayList<SkinIndex.Definition> definitions=new ArrayList<>();
        try {
            for(var entry:resources.findResources("items",id->id.getPath().endsWith(".json")).entrySet()){
                if(definitions.size()>=8192)break;
                Identifier file=entry.getKey();Resource resource=entry.getValue();
                // Definitions shipped by this mod are cosmetics, not server weapon skins.
                if(file.getNamespace().equals("pyz1k")||resource.getPackId().equals("vanilla"))continue;
                String path=file.getPath().substring(6,file.getPath().length()-5);
                boolean serverPack=resource.getPackId().startsWith("server/")||resource.getPackId().startsWith("server_");
                if(!serverPack)continue;
                try(var stream=resource.getInputStream()){
                    byte[] bytes=stream.readNBytes(1_048_577);
                    if(bytes.length>1_048_576){failures++;continue;}
                    JsonObject root=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
                    Identifier model=Identifier.of(file.getNamespace(),path);
                    definitions.add(new SkinIndex.Definition(model.toString(),root,resource.getPackId()));
                } catch(Exception e){failures++;}
            }
        }catch(Exception e){ExtensionClient.LOG.warn("Cannot index server item skins",e);}
        Map<String,String> fingerprints=new HashMap<>();Gson json=new Gson();
        for(var entry:SkinIndex.build(definitions)){
            var definition=entry.definition();var variant=entry.variant();
            Identifier model=Identifier.of(definition.id());
            String label=variant.label().isBlank()?model.getPath():variant.label();
            String key=model+"|"+json.toJson(variant.data())+"|"+fingerprints.computeIfAbsent(definition.id(),id->digest(definition.json().toString()));
            found.add(new Skin(key,model,variant.data(),label,definition.pack(),entry.material()));
        }
        found.sort(Comparator.comparing(Skin::name));entries=List.copyOf(found);generation++;
        selected.clear();var saved=ExtensionConfig.get().skins.getOrDefault(server,Map.of());
        for(var entry:saved.entrySet())found.stream().filter(s->s.key.equals(entry.getValue())).findFirst().filter(s->SkinTargets.matches(s.inferredTarget(),Identifier.of(entry.getKey()).getPath())).ifPresent(s->selected.put(entry.getKey(),s));
        ExtensionClient.LOG.info("Skin catalog: {} variants, {} invalid definitions, {} ms",found.size(),failures,(System.nanoTime()-started)/1_000_000);
    }
    private static String digest(String value){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)),0,8);}catch(Exception e){throw new IllegalStateException(e);}
    }
    public static boolean compatible(Skin skin,Item target){
        return skin!=null&&SkinTargets.matches(skin.inferredTarget(),Registries.ITEM.getId(target).getPath());
    }
    public static List<Skin> entries(){return entries;}
    public static int generation(){return generation;}
    public static int failures(){return failures;}
    public static boolean onServer(){return !server.isEmpty();}
    public static Skin chosen(Item target){return selected.get(Registries.ITEM.getId(target).toString());}
    public static void select(Item target,Skin skin){
        if(server.isEmpty()||(skin!=null&&!compatible(skin,target)))return;
        String item=Registries.ITEM.getId(target).toString();
        Map<String,String> choices=ExtensionConfig.get().skins.computeIfAbsent(server,k->new HashMap<>());
        if(skin==null){selected.remove(item);choices.remove(item);}else{selected.put(item,skin);choices.put(item,skin.key);}
        ExtensionConfig.save();
    }
    public static String server(){return server;}
    public static boolean favorite(Skin skin){return ExtensionConfig.get().favorites.contains(skin.key());}
    public static void toggleFavorite(Skin skin){
        var favorites=ExtensionConfig.get().favorites;if(!favorites.remove(skin.key()))favorites.add(skin.key());ExtensionConfig.save();
    }
    public static List<String> presetNames(){return ExtensionConfig.get().skinPresets.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();}
    public static boolean savePreset(String name){
        name=name.strip();if(server.isEmpty()||name.isEmpty()||name.length()>40)return false;
        var config=ExtensionConfig.get();if(config.skinPresets.size()>=64&&!config.skinPresets.containsKey(name))return false;
        config.skinPresets.put(name,new HashMap<>(config.skins.getOrDefault(server,Map.of())));ExtensionConfig.save();return true;
    }
    public static int applyPreset(String name){
        ensureLoaded();if(server.isEmpty())return 0;var preset=ExtensionConfig.get().skinPresets.get(name);if(preset==null)return 0;
        int applied=0;
        for(Item target:TARGETS){
            String key=preset.get(Registries.ITEM.getId(target).toString());
            Skin skin=entries.stream().filter(s->s.key().equals(key)&&compatible(s,target)).findFirst().orElse(null);
            select(target,skin);if(skin!=null)applied++;
        }
        return applied;
    }
    public static void deletePreset(String name){ExtensionConfig.get().skinPresets.remove(name);ExtensionConfig.save();}
    public static ItemStack displayCopy(ItemStack stack,ItemDisplayContext context,net.minecraft.util.HeldItemContext holder){
        var module=FeatureModules.skins;if(module==null||!module.isEnable())return stack;var mc=MinecraftClient.getInstance();
        if(PREVIEW.get()||module==null||!module.isEnable()||mc.player==null||server.isEmpty()||stack.isEmpty())return stack;
        boolean gui=context==ItemDisplayContext.GUI;
        if(gui){if(!module.inventory.isValue()||!isLocalStack(stack,mc))return stack;}
        else if(holder==null||holder.getEntity()!=mc.player)return stack;
        Skin skin=chosen(stack.getItem());if(!compatible(skin,stack.getItem()))return stack;
        ItemStack copy=stack.copy();skin.apply(copy);return copy;
    }
    private static boolean isLocalStack(ItemStack stack,MinecraftClient mc){
        for(int i=0;i<mc.player.getInventory().size();i++)if(mc.player.getInventory().getStack(i)==stack)return true;
        return mc.player.currentScreenHandler.getCursorStack()==stack;
    }
    public static void drawOriginal(Runnable action){boolean prev=PREVIEW.get();PREVIEW.set(true);try{action.run();}finally{PREVIEW.set(prev);}}
}
