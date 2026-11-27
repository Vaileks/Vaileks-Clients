package pyz1k.extensions;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import pyz1k.config.ConfigManager;
public final class ExtensionConfig {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE=ConfigManager.getRootDir().resolve("config/vaileks-skins.json");
    private static ExtensionConfig data=new ExtensionConfig();
    public Map<String,Map<String,String>> skins=new HashMap<>();
    public Set<String> favorites=new LinkedHashSet<>();
    public Map<String,Map<String,String>> skinPresets=new LinkedHashMap<>();
    public boolean automaticProfiles;
    public boolean baselineSaved;
    public Set<String> serverProfiles=new HashSet<>();
    public void normalize(){
        if(skins==null)skins=new HashMap<>();skins.values().removeIf(Objects::isNull);
        if(favorites==null)favorites=new LinkedHashSet<>();
        if(skinPresets==null)skinPresets=new LinkedHashMap<>();skinPresets.values().removeIf(Objects::isNull);
        if(serverProfiles==null)serverProfiles=new HashSet<>();
    }
    public static ExtensionConfig get(){return data;}
    public static void load(){
        if(!Files.exists(FILE))return;
        try(var r=Files.newBufferedReader(FILE,StandardCharsets.UTF_8)){
            ExtensionConfig loaded=JSON.fromJson(r,ExtensionConfig.class);if(loaded!=null){loaded.normalize();data=loaded;}
        }catch(Exception e){ExtensionClient.LOG.warn("Cannot load skin selections",e);}
    }
    public static void save(){
        try {
            Files.createDirectories(FILE.getParent());Path temp=FILE.resolveSibling(FILE.getFileName()+".tmp");
            Files.writeString(temp,JSON.toJson(data),StandardCharsets.UTF_8);
            try{Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}
        }catch(IOException e){ExtensionClient.LOG.warn("Cannot save skin selections",e);}
    }
}
