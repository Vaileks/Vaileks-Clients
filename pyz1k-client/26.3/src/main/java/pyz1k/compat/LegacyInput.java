package pyz1k.compat;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import java.util.*;
/** Keep existing user keybind codes while the platform uses SDL scancodes. */
public final class LegacyInput {
 private static final Map<Integer,Integer> KEYS=new HashMap<>(),REVERSE=new HashMap<>();
 static {
  for(int i=0;i<26;i++)put(65+i,4+i);
  for(int i=1;i<=9;i++)put(48+i,29+i);put(48,39);
  for(int i=0;i<12;i++)put(290+i,58+i);
  for(int i=0;i<12;i++)put(302+i,104+i);
  int[][] pairs={{32,44},{39,52},{44,54},{45,45},{46,55},{47,56},{59,51},{61,46},{91,47},{92,49},{93,48},{96,53},{256,41},{257,40},{258,43},{259,42},{260,73},{261,76},{262,79},{263,80},{264,81},{265,82},{266,75},{267,78},{268,74},{269,77},{280,57},{281,71},{282,83},{283,70},{284,72},{340,225},{341,224},{342,226},{343,227},{344,229},{345,228},{346,230},{347,231},{320,98},{321,89},{322,90},{323,91},{324,92},{325,93},{326,94},{327,95},{328,96},{329,97},{330,99},{331,84},{332,85},{333,86},{334,87},{335,88},{336,103}};
  for(var pair:pairs)put(pair[0],pair[1]);
 }
 private static void put(int old,int nativeCode){KEYS.put(old,nativeCode);REVERSE.put(nativeCode,old);}
 public static int fromNativeKey(int key){return REVERSE.getOrDefault(key,-1);}
 public static int fromNativeMouse(int key){return switch(key){case 1->0;case 3->1;case 2->2;default->key-1;};}
 public static int glfwGetKey(long ignored,int key){Integer code=KEYS.get(key);return code!=null&&InputConstants.isKeyDown(code)?1:0;}
 public static int glfwGetMouseButton(long ignored,int button){var m=Minecraft.getInstance().mouseHandler;return (button==0?m.isLeftPressed():button==1?m.isRightPressed():button==2&&m.isMiddlePressed())?1:0;}
 public static String glfwGetKeyName(int code,int unused){return code>=32&&code<=96?Character.toString((char)code):null;}
}
