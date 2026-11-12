package pyz1k.extensions;
import java.util.*;
public final class InventorySearchQuery {
 private final List<String> words;
 public InventorySearchQuery(String query){String s=query==null?"":query.strip().toLowerCase(Locale.ROOT);words=s.isEmpty()?List.of():List.of(s.split("\\s+"));}
 public boolean matches(String name,String id){if(words.isEmpty())return false;String text=(name+" "+id).toLowerCase(Locale.ROOT);for(String word:words)if(!text.contains(word))return false;return true;}
 public static boolean armor(String id){return id.endsWith("_helmet")||id.endsWith("_chestplate")||id.endsWith("_leggings")||id.endsWith("_boots");}
}
