import java.nio.file.*;import java.net.*;import java.util.*;import java.util.zip.*;
public final class TestRemovedModule {
 public static void main(String[] a)throws Exception {
  String name="ru.bloodhelper.client.gui.BloodHelperScreen$Module";
  try(var before=new URLClassLoader(new URL[]{Path.of(a[0]).toUri().toURL()},null);var after=new URLClassLoader(new URL[]{Path.of(a[1]).toUri().toURL()},null)){
   List<String> expected=Arrays.stream(Class.forName(name,true,before).getEnumConstants()).map(v->((Enum<?>)v).name()).filter(v->!v.equals("LEGENDARY_SIZE")).toList();
   Class<?> type=Class.forName(name,true,after);Object[] values=type.getEnumConstants();if(values.length!=66)throw new AssertionError("Wrong module count");
   var id=type.getDeclaredField("id");id.setAccessible(true);var oldId=Class.forName(name,true,before).getDeclaredField("id");oldId.setAccessible(true);Map<String,String> ids=new HashMap<>();for(Object v:Class.forName(name,true,before).getEnumConstants())ids.put(((Enum<?>)v).name(),(String)oldId.get(v));
   for(int i=0;i<values.length;i++){Enum<?> value=(Enum<?>)values[i];if(!value.name().equals(expected.get(i))||value.ordinal()!=i||!id.get(value).equals(ids.get(value.name())))throw new AssertionError("Module registry damaged: "+value);}
   try{type.getDeclaredField("LEGENDARY_SIZE");throw new AssertionError("Resizing still registered");}catch(NoSuchFieldException good){}
  }
  try(var z=new ZipFile(a[1])){for(var e:z.stream().toList())if(e.getName().contains("LegendarySizeModule")||e.getName().contains("HandItemScaleScreen")||e.getName().matches(".*(?:LootGlowMixin|LootColorMixin|LegendaryItemModelMixin|LegendaryItemRenderStateMixin)\\.class"))throw new AssertionError("Prohibited implementation remains "+e.getName());}
  System.out.println("PASS: resizing removed, all 66 retained module identities and ordinals valid; no resizing UI or render mixins");
 }
}
