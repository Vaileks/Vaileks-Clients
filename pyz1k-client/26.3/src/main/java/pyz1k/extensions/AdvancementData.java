package pyz1k.extensions;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;

public final class AdvancementData {
 public record Entry(String id,String title,String description,ProgressLogic.Checklist checklist){public String criterionName(String id){return AdvancementData.label(this.id,id);}}
 private static final List<String> PRIORITY=List.of("minecraft:adventure/adventuring_time","minecraft:husbandry/balanced_diet","minecraft:husbandry/bred_all_animals","minecraft:adventure/kill_all_mobs");
 public static List<Entry> read(){
  var network=Minecraft.getInstance().getConnection();if(network==null)return List.of();
  var handler=network.getAdvancements();var progress=handler.progress();var result=new ArrayList<Entry>();
  for(var node:handler.tree().nodes()){
   var holder=node.holder();var value=holder.value();var display=value.display();if(display.isEmpty()||value.requirements().requirements().isEmpty())continue;
   var p=progress.get(holder);if(display.get().hidden()&&(p==null||!p.isDone()))continue;
   var done=new HashSet<String>();if(p!=null)p.getCompletedCriteria().forEach(done::add);
   result.add(new Entry(holder.id().toString(),display.get().title().getString(),display.get().description().getString(),new ProgressLogic.Checklist(value.requirements().requirements(),done,p!=null)));
  }
  result.sort(Comparator.comparingInt((Entry e)->{int p=PRIORITY.indexOf(e.id());return p<0?99:p;}).thenComparing(Entry::title));return List.copyOf(result);
 }
 public static String label(String advancement,String criterion){
  try{Identifier id=Identifier.parse(criterion);String kind=advancement.endsWith("adventuring_time")?"biome":advancement.endsWith("balanced_diet")?"item":advancement.endsWith("bred_all_animals")||advancement.endsWith("kill_all_mobs")||advancement.endsWith("complete_catalogue")?"entity":"";
   String key=kind+"."+id.getNamespace()+"."+id.getPath().replace('/','.');if(!kind.isEmpty()&&Language.getInstance().has(key))return Component.translatable(key).getString();
  }catch(IllegalArgumentException ignored){}
  return criterion.replace("minecraft:","").replace('_',' ');
 }
}
