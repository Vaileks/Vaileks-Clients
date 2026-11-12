package pyz1k.extensions;
import com.google.gson.JsonObject;
import java.util.*;

/** Index active item definitions, retaining their owning material and component selectors. */
public final class SkinIndex {
    public record Definition(String id,JsonObject json,String pack){}
    public record Entry(Definition definition,ModelVariants.Variant variant,String material){}
    public static List<Entry> build(List<Definition> definitions){
        record Parsed(Definition definition,List<ModelVariants.Variant> variants,String material){}
        List<Parsed> parsed=new ArrayList<>();Map<String,Set<String>> owners=new HashMap<>();
        int variantCount=0;
        var ordered=definitions.stream().sorted(Comparator.comparingInt((Definition d)->SkinTargets.fromDefinition(d.id().substring(d.id().indexOf(':')+1)).isEmpty()?1:0).thenComparing(Definition::id)).toList();
        for(var definition:ordered){
            if(variantCount>=32768)break;
            String path=definition.id().substring(definition.id().indexOf(':')+1);
            if(SkinTargets.excluded(path))continue;
            List<ModelVariants.Variant> variants;
            try{variants=ModelVariants.parse(definition.json());}catch(RuntimeException invalid){continue;}
            if(variants.size()>32768-variantCount)variants=variants.subList(0,32768-variantCount);
            variantCount+=variants.size();String material=SkinTargets.fromDefinition(path);
            parsed.add(new Parsed(definition,variants,material));
            if(!material.isEmpty())for(var variant:variants)if(!variant.label().isBlank())owners.computeIfAbsent(normalize(variant.label()),k->new HashSet<>()).add(material);
        }
        ArrayList<Entry> entries=new ArrayList<>();
        for(var value:parsed){
            Set<String> inherited=new HashSet<>();
            if(value.material().isEmpty())for(var variant:value.variants())inherited.addAll(owners.getOrDefault(normalize(variant.label()),Set.of()));
            String material=!value.material().isEmpty()?value.material():inherited.size()==1?inherited.iterator().next():"";
            if(material.isEmpty())continue;
            for(var variant:value.variants()){
                if(entries.size()>=8192)return List.copyOf(entries);
                entries.add(new Entry(value.definition(),variant,material));
            }
        }
        return List.copyOf(entries);
    }
    private static String normalize(String id){return id.isBlank()?"":id.contains(":")?id:"minecraft:"+id;}
}
