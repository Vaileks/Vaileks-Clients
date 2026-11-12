package pyz1k.extensions;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Values are seconds, as used by the supplied server plugin's ConfigUtils. */
public final class BloodCooldownProfiles {
    public record Mode(String id,String label,boolean serverOnly,Map<String,Double> values,String source,String sha256){
        public double seconds(String field){return serverOnly?0:values.getOrDefault(field,0d);}
    }
    public static final List<Mode> ALL=load();
    private static List<Mode> load(){
        try(var stream=Objects.requireNonNull(BloodCooldownProfiles.class.getResourceAsStream("/bloodsword-presets.json"));var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)){
            return List.of(new Gson().fromJson(reader,Mode[].class));
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    public static Mode mode(String name){return ALL.stream().filter(m->m.label().equals(name)).findFirst().orElse(ALL.getLast());}
    public static String time(double seconds,boolean precise){
        if(!Double.isFinite(seconds)||seconds<=0)return "0с";
        if(seconds>=60){int whole=(int)Math.ceil(seconds);return String.format(Locale.ROOT,"%d:%02d",whole/60,whole%60);}
        return precise&&seconds<10?String.format(Locale.ROOT,"%.1fс",Math.ceil(seconds*10)/10):String.format(Locale.ROOT,"%dс",(int)Math.ceil(seconds));
    }
    public static double remaining(long endMillis,long nowMillis){return endMillis<=nowMillis?0:Math.min(86400,(endMillis-nowMillis)/1000d);}
}
