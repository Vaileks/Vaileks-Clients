package ru.bloodhelper.client.vl;
import java.util.*;
public final class VLRestrictions {
 private VLRestrictions(){}
 private static final Map<Object,Boolean> HIDDEN=Collections.synchronizedMap(new WeakHashMap<>());
 public static Object[] visibleModules(Object[] values){var result=new ArrayList<Object>();for(Object v:values)if(!((Enum<?>)v).name().equals("LEGENDARY_SIZE"))result.add(v);return result.toArray((Object[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(),result.size()));}
 public static boolean hideButton(String id){return id.equals("about.boosty")||id.equals("policy.releases");}
 public static boolean blockedOption(String id){String s=id.startsWith("search.")?id.substring(7):id;return s.startsWith("legendarySize.")||s.startsWith("legendaryVisuals.loot")||Set.of("legendaryVisuals.beams","legendaryVisuals.names","legendaryVisuals.legendary","legendaryVisuals.legendaryColor","legendaryVisuals.totems","legendaryVisuals.totemColor","legendaryVisuals.enchantedApples","legendaryVisuals.appleColor").contains(s);}
 public static Object markRow(Object row,String id){if(blockedOption(id))HIDDEN.put(row,true);return row;}
 public static List<?> cleanRows(List<?> rows){var out=new ArrayList<Object>(rows.size());for(Object row:rows){Object key=row;if(row.getClass().getSimpleName().equals("SettingHit")){try{var accessor=row.getClass().getDeclaredMethod("row");accessor.setAccessible(true);key=accessor.invoke(row);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}if(!HIDDEN.containsKey(key))out.add(row);}return out;}
 public static float safeScale(float value,String id){return 1f;}
 public static void enforce(Object cfg){try{Object visual=cfg.getClass().getField("legendaryVisuals").get(cfg);if(visual!=null)visual.getClass().getField("loot").setBoolean(visual,false);Object size=cfg.getClass().getField("legendarySize").get(cfg);if(size!=null){for(String key:List.of("enabled","enlargeDropped","scaleAllDropped"))size.getClass().getField(key).setBoolean(size,false);size.getClass().getField("droppedScale").setDouble(size,1.0);}}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
}
