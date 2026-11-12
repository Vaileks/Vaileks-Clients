package pyz1k.extensions;
import java.util.*;

/** Bounded, deterministic state used by alerts. */
public final class ClientFeatureLogic {
    private ClientFeatureLogic(){}
    public static boolean lowDurability(int max,int damage,int threshold){
        return max>0 && damage>=0 && Math.max(0,max-damage)*100L<=max*(long)threshold;
    }
    public static String profileName(String server){
        try{return "server_"+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(server.getBytes(java.nio.charset.StandardCharsets.UTF_8)),0,10);}
        catch(Exception e){throw new IllegalStateException(e);}
    }
}
