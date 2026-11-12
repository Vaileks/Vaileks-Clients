package pyz1k.extensions;
import java.util.*;

/** Infer only an explicit material/family, never a generic sword shared across materials. */
public final class SkinTargets {
    private static final Set<String> MATERIALS=Set.of("diamond_sword","netherite_sword","bow","crossbow","totem_of_undying","mace");
    private SkinTargets(){}
    public static boolean excluded(String path){
        return Arrays.stream(path.toLowerCase(Locale.ROOT).split("/")).anyMatch(s->Set.of("icon","icons","menu","menus","gui","hud","hidden").contains(s));
    }
    public static String fromDefinition(String path){
        if(excluded(path))return "";
        String s="/"+path.toLowerCase(Locale.ROOT)+"/";
        LinkedHashSet<String> candidates=new LinkedHashSet<>();
        for(String material:MATERIALS)if(token(s,material))candidates.add(material);
        if(token(s,"totem")||token(s,"totems"))candidates.add("totem_of_undying");
        if(token(s,"maces"))candidates.add("mace");
        if(token(s,"bows"))candidates.add("bow");
        if(token(s,"crossbows"))candidates.add("crossbow");
        return candidates.size()==1?candidates.iterator().next():"";
    }
    private static boolean token(String path,String value){return path.matches(".*(?:^|[/_.-])"+value+"(?:$|[/_.-]).*");}
    public static boolean matches(String assigned,String material){return MATERIALS.contains(material)&&material.equals(assigned);}
}
