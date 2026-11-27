package pyz1k.extensions;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
public final class LootGloow {
 private static final Map<String,Boolean> LEGACY_EPICS=new HashMap<>();
 public static void clearCache(){LEGACY_EPICS.clear();}
 private static boolean verifiedLegacyEpic(String item,float value,LootPolicy.Kind kind){
  String key=item+"|"+value+"|"+kind;
  return LEGACY_EPICS.computeIfAbsent(key,k->{
   try {
    var id=Identifier.of(item);var file=Identifier.of(id.getNamespace(),"items/"+id.getPath()+".json");
    var resource=MinecraftClient.getInstance().getResourceManager().getResource(file).orElseThrow();
    if(resource.getPackId().equals("vanilla"))return false;
    try(var in=resource.getInputStream()){
     byte[] bytes=in.readNBytes(1_048_577);if(bytes.length>1_048_576)return false;
     var json=com.google.gson.JsonParser.parseString(new String(bytes,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
     String expected=kind==LootPolicy.Kind.BALLISTA?"astrum:item/battle_royale/ballista/(default|arrow|pulling/[012])":"civilization:item/tools/flaming_crossbow/(default|arrow|pulling/[012])";
     return ModelVariants.parse(json).stream().anyMatch(variant->variant.data().floats().equals(List.of(value))&&variant.label().matches(expected));
    }
   }catch(Exception malformed){return false;}
  });
 }

 public static LootPolicy.Kind kind(ItemEntity entity) {
  var m=FeatureModules.loot;if(m==null||!m.isEnable()||entity==null)return LootPolicy.Kind.NONE;var mc=MinecraftClient.getInstance();
  if(m==null||!m.isEnable()||mc.player==null||entity==null)return LootPolicy.Kind.NONE;
  float d=m.distance.getValue();if(entity.squaredDistanceTo(mc.player)>d*d)return LootPolicy.Kind.NONE;
  var stack=entity.getStack();if(stack.isEmpty())return LootPolicy.Kind.NONE;
  var id=Registries.ITEM.getId(stack.getItem());var model=stack.get(DataComponentTypes.ITEM_MODEL);
  var base=stack.getItem().getComponents().get(DataComponentTypes.ITEM_MODEL);
  var data=stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
  boolean other=data!=null&&(!data.flags().isEmpty()||!data.strings().isEmpty()||!data.colors().isEmpty());
  boolean legend=false;var custom=stack.get(DataComponentTypes.CUSTOM_DATA);
  if(custom!=null){var pdc=custom.copyNbt().getCompoundOrEmpty("PublicBukkitValues");for(String key:pdc.getKeys()){
   String value=pdc.getString(key,"").toLowerCase(Locale.ROOT);
   if(key.toLowerCase(Locale.ROOT).contains("legendary")||Set.of("kristalniymech","bloodsword","pauchiiklinok","kosasmerti","lyogkayabulava","original","copy").contains(value)){legend=true;break;}
  }}
  var kind=LootPolicy.classify(id.toString(),model==null?"":model.toString(),!Objects.equals(model,base),data==null?List.of():data.floats(),other,legend);
  var title=stack.get(DataComponentTypes.CUSTOM_NAME);var lore=stack.get(DataComponentTypes.LORE);
  if(kind==LootPolicy.Kind.ORDINARY&&(title!=null||custom!=null||(lore!=null&&!lore.lines().isEmpty())))return LootPolicy.Kind.NONE;
  if(title!=null){String name=title.getString().toLowerCase(Locale.ROOT);if(name.contains("легендар")||name.contains("legendary")||name.contains("зеркальн")||name.contains("дзеркальн"))return LootPolicy.Kind.NONE;}
  if((kind==LootPolicy.Kind.BALLISTA||kind==LootPolicy.Kind.FLAMING)&&Objects.equals(model,base)&&(data==null||data.floats().size()!=1||!verifiedLegacyEpic(id.toString(),data.floats().getFirst(),kind)))return LootPolicy.Kind.NONE;
  if(kind==LootPolicy.Kind.TOTEM&&!m.totems.isValue())return LootPolicy.Kind.NONE;
  if(kind==LootPolicy.Kind.BALLISTA&&!m.ballista.isValue()||kind==LootPolicy.Kind.SHIELD&&!m.shield.isValue()||kind==LootPolicy.Kind.FLAMING&&!m.flaming.isValue())return LootPolicy.Kind.NONE;
  return kind;
 }
 public static boolean glow(ItemEntity e){return FeatureModules.loot!=null&&FeatureModules.loot.glow.isValue()&&kind(e)!=LootPolicy.Kind.NONE;}
 public static int color(ItemEntity e){var k=kind(e);return k==LootPolicy.Kind.ORDINARY?FeatureModules.loot.color.getColor():k==LootPolicy.Kind.TOTEM?FeatureModules.loot.totemColor.getColor():FeatureModules.loot.epicColor.getColor();}
 public static float scale(ItemEntity e){var m=FeatureModules.loot;if(m==null||!m.isEnable()||m.scale.getValue()==1)return 1;return kind(e)==LootPolicy.Kind.NONE?1:m.scale.getValue();}
 public static ItemStack oldMirror(ItemStack stack){
  var m=FeatureModules.oldMirror;if(m==null||!m.isEnable()||stack.isEmpty())return stack;
  var model=stack.get(DataComponentTypes.ITEM_MODEL);
  if(model==null||!LootPolicy.mirror(model.toString()))return stack;
  ItemStack copy=stack.copy();copy.set(DataComponentTypes.ITEM_MODEL,Identifier.of("pyz1k:legacy_mirror"));return copy;
 }
}
