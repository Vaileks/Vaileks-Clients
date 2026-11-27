package pyz1k.extensions;

import com.google.gson.*;
import java.util.*;

/** Pure bounded parser for the component-driven item definitions introduced in 1.21.4. */
public final class ModelVariants {
    public record Data(List<Float> floats,List<Boolean> flags,List<String> strings,List<Integer> colors) {
        public Data {floats=List.copyOf(floats);flags=List.copyOf(flags);strings=List.copyOf(strings);colors=List.copyOf(colors);}
        public static Data empty(){return new Data(List.of(),List.of(),List.of(),List.of());}
        public Data withFloat(int i,float v){return new Data(set(floats,i,v,0f),flags,strings,colors);}
        public Data withFlag(int i,boolean v){return new Data(floats,set(flags,i,v,false),strings,colors);}
        public Data withString(int i,String v){return new Data(floats,flags,set(strings,i,v,""),colors);}
        private static <T> List<T> set(List<T> old,int i,T value,T zero){
            ArrayList<T> n=new ArrayList<>(old);while(n.size()<=i)n.add(zero);n.set(i,value);return n;
        }
    }
    public record Variant(Data data,String label){}
    private ModelVariants(){}
    public static List<Variant> parse(JsonObject definition){
        LinkedHashMap<Data,Variant> out=new LinkedHashMap<>();
        walk(definition.get("model"),Data.empty(),out,0,new int[]{0});
        return List.copyOf(out.values());
    }
    private static void walk(JsonElement element,Data data,Map<Data,Variant> out,int depth,int[] budget){
        if(++budget[0]>8192||depth>32||out.size()>=512||element==null||!element.isJsonObject())return;
        JsonObject obj=element.getAsJsonObject();String type=str(obj,"type");
        String property=str(obj,"property");int i=obj.has("index")?obj.get("index").getAsInt():0;
        if(i<0||i>31)return;
        boolean custom=property.equals("minecraft:custom_model_data")||property.equals("custom_model_data");
        if(custom&&type.endsWith("range_dispatch")){
            float scale=obj.has("scale")?obj.get("scale").getAsFloat():1;
            if(!Float.isFinite(scale)||scale==0)return;
            if(obj.has("entries"))for(JsonElement e:obj.getAsJsonArray("entries")){
                JsonObject entry=e.getAsJsonObject();float v=entry.get("threshold").getAsFloat()/scale;
                if(Float.isFinite(v))walk(entry.get("model"),data.withFloat(i,v),out,depth+1,budget);
            }
            walk(obj.get("fallback"),data,out,depth+1,budget);return;
        }
        if(custom&&type.endsWith("select")){
            if(obj.has("cases"))for(JsonElement e:obj.getAsJsonArray("cases")){
                JsonObject entry=e.getAsJsonObject();JsonElement when=entry.get("when");
                if(when==null)continue;
                if(when.isJsonArray())for(JsonElement v:when.getAsJsonArray())walk(entry.get("model"),data.withString(i,v.getAsString()),out,depth+1,budget);
                else walk(entry.get("model"),data.withString(i,when.getAsString()),out,depth+1,budget);
            }
            walk(obj.get("fallback"),data,out,depth+1,budget);return;
        }
        if(custom&&type.endsWith("condition")){
            walk(obj.get("on_true"),data.withFlag(i,true),out,depth+1,budget);
            walk(obj.get("on_false"),data.withFlag(i,false),out,depth+1,budget);return;
        }
        // Pulling, charged, handedness and other animation branches are not separate skins.
        String label=obj.has("model")&&obj.get("model").isJsonPrimitive()?obj.get("model").getAsString():"";
        if(type.endsWith(":model")||type.equals("model")||type.endsWith("special")||type.endsWith("reference")){
            out.putIfAbsent(data,new Variant(data,label));return;
        }
        for(String key:List.of("fallback","on_false","on_true","model"))walk(obj.get(key),data,out,depth+1,budget);
        for(String key:List.of("models","entries","cases"))if(obj.has(key)&&obj.get(key).isJsonArray())
            for(JsonElement child:obj.getAsJsonArray(key)){
                if(child.isJsonObject()&&!child.getAsJsonObject().has("type")&&child.getAsJsonObject().has("model"))walk(child.getAsJsonObject().get("model"),data,out,depth+1,budget);
                else walk(child,data,out,depth+1,budget);
            }
    }
    private static String str(JsonObject o,String k){return o.has(k)&&o.get(k).isJsonPrimitive()?o.get(k).getAsString():"";}
}
