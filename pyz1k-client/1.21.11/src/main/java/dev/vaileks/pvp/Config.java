package pyz1k.extensions.pvp;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public final class Config {
 private transient Config layoutCache;
 Config layoutCopy(){
  if(layoutCache==null)layoutCache=new Config();Config c=layoutCache;
  c.scale=scale;c.shadow=shadow;c.background=background;c.icons=icons;c.color=color;c.accent=accent;c.x=x;c.y=y;return c;
 }

 public String searchQuery="";
 public boolean searchPersistent=true;
 public boolean enabled=false,shadow=true,icons=false,background=true,sound=false;
 public boolean manualPosition=false;
 public boolean hideScoreboard=true,hideBossbar=true,hideItemName=true,hideEffects=false;
 public double scale=0.9,x=0.03,y=0.15;
 public int color=0xFFFFFF,accent=0x78DCFF,threshold=3,mode=0,actionKey=-1,holdKey=72;
 public List<SavedItem> kit=new ArrayList<>(),hotbar=new ArrayList<>();
 public Map<String,Integer> notes=new LinkedHashMap<>();
 public Map<String,Config> profiles=new LinkedHashMap<>();
 public record SavedItem(String key,String item,String name,int count,int slot) {}
 static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
 static Path path(Feature f) { return FabricLoader.getInstance().getConfigDir().resolve("pyz1k-pvp").resolve(f.id+".json"); }
 static Config defaults(Feature f) { Config c=new Config();c.threshold=f.threshold;c.y=0.06+f.ordinal()*0.045;c.x=f.ordinal()>9?0.65:0.03;c.y=Math.min(c.y,0.83);return c; }
 static Config load(Feature f) {
  Path path=path(f);
  try {if(Files.exists(path)){Config c=JSON.fromJson(Files.readString(path),Config.class);if(c!=null){c.sanitize(f);return c;}}}
  catch(Exception e){Core.LOG.warn("Cannot read {}. Preserving file; using defaults.",path,e);try{Files.copy(path,path.resolveSibling(path.getFileName()+".invalid-"+System.currentTimeMillis()));}catch(Exception ignored){}}
  return defaults(f);
 }
 void sanitize(Feature f) {
  if(searchQuery==null)searchQuery="";if(searchQuery.length()>80)searchQuery=searchQuery.substring(0,80);

  scale=Double.isFinite(scale)?Math.clamp(scale,0.5,2.5):0.9;
  x=Double.isFinite(x)?Math.clamp(x,0,1):0.03;y=Double.isFinite(y)?Math.clamp(y,0,1):0.15;
  threshold=Math.clamp(threshold,1,120);mode=Math.floorMod(mode,f==Feature.INVENTORY_SEARCH_FLASH?4:3);color&=0xFFFFFF;accent&=0xFFFFFF;
  if(kit==null)kit=new ArrayList<>();if(hotbar==null)hotbar=new ArrayList<>();if(notes==null)notes=new LinkedHashMap<>();if(profiles==null)profiles=new LinkedHashMap<>();
  kit.removeIf(s->s==null||s.key()==null||s.item()==null||s.name()==null||s.count()<0);hotbar.removeIf(s->s==null||s.key()==null||s.item()==null||s.name()==null);
 }
 Config copy() {return JSON.fromJson(JSON.toJson(this),Config.class);}
 static void write(Feature f,Config c) {
  Path target=path(f),tmp=target.resolveSibling(target.getFileName()+".tmp");
  try {Files.createDirectories(target.getParent());Files.writeString(tmp,JSON.toJson(c));try{Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING);}}
  catch(Exception e){Core.LOG.error("Cannot save {}",target,e);Core.notice("Не удалось сохранить "+f.title);}
 }
}

