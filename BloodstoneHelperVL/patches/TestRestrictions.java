import ru.bloodhelper.client.vl.VLRestrictions;
import java.util.*;
public final class TestRestrictions {
 public static void main(String[] args)throws Exception{
  int checks=0;
  for(String id:List.of("legendaryVisuals.loot","legendaryVisuals.lootSettings","legendaryVisuals.beams","legendaryVisuals.legendary","legendarySize.dropped","legendarySize.hand.dragon_katana","legendarySize.groundScale","legendarySize.selected","legendarySize.allDropped")){if(!VLRestrictions.blockedOption(id)||!VLRestrictions.blockedOption("search."+id))throw new AssertionError(id);checks+=2;}
  for(String id:List.of("legendaryVisuals.trails","legendaryVisuals.orbital","legendaryVisuals.shadow","opponentCooldowns.enabled","betterNameVisibility.enabled","hitboxes.enabled")){if(VLRestrictions.blockedOption(id))throw new AssertionError(id);checks++;}
  Object removed=new Object(),kept=new Object();VLRestrictions.markRow(removed,"legendaryVisuals.loot");VLRestrictions.markRow(kept,"legendaryVisuals.trails");if(!VLRestrictions.cleanRows(List.of(removed,kept)).equals(List.of(kept)))throw new AssertionError("row filter");checks++;
  for(String id:List.of("dragon_katana","mirror_blade","arbitrary_unknown_legendary")){if(VLRestrictions.safeScale(5f,id)!=1f)throw new AssertionError(id);checks++;}
  for(String id:List.of("ground:any","selected:ordinary")){if(VLRestrictions.safeScale(2f,id)!=1f)throw new AssertionError(id);checks++;}
  System.out.println("PASS "+checks+" targeted option and size restriction checks");
 }
}
