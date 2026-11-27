package pyz1k.extensions;
import java.util.*;
/** Fail closed for every custom item except the explicitly supported epic identities. */
public final class LootPolicy {
 public enum Kind { NONE, ORDINARY, BALLISTA, SHIELD, FLAMING }
 public static boolean mirror(String model) {return Set.of("bloodstone:tools/mirror_blade/default/mirror_blade","legendary:tools/mirror_blade/default/mirror_blade","ulegendary:kristalniymech","minecraft:kristalniymech").contains(model);}
 public static Kind classify(String item,String model,boolean customModel,List<Float> floats,boolean otherData,boolean legendary) {
  if(legendary||mirror(model)||model.startsWith("pyz1k:legacy_mirror"))return Kind.NONE;
  if(item.equals("minecraft:shield")&&Set.of("bloodstone:tools/netherite_shield/default/netherite_shield","astrum:battle_royale/netherite_shield").contains(model))return Kind.SHIELD;
  if(item.equals("minecraft:crossbow")){
   if(model.equals("astrum:battle_royale/ballista"))return Kind.BALLISTA;
   if(model.equals("astrum:battle_royale/flaming_crossbow")||model.equals("civilization:tools/flaming_crossbow/default")||model.equals("civilization:tools/flaming_crossbow/arrow")||model.matches("civilization:tools/flaming_crossbow/pulling/[012]"))return Kind.FLAMING;
   if(!customModel&&!otherData&&floats.size()==1){float n=floats.getFirst();if(n==90)return Kind.BALLISTA;if(n==6000||n==3)return Kind.FLAMING;}
  }
  return !customModel&&floats.isEmpty()&&!otherData?Kind.ORDINARY:Kind.NONE;
 }
}
