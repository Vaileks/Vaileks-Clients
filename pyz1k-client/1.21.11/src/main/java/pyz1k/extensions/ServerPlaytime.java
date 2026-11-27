package pyz1k.extensions;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.MinecraftClient;
import pyz1k.config.ConfigManager;
/** Local only; no requests, no retroactive estimates and no per-frame disk access. */
public final class ServerPlaytime {
 private static final Path FILE=ConfigManager.getRootDir().resolve("config/server-playtime.json");
 private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
 private static final ProgressLogic.Clock CLOCK=new ProgressLogic.Clock();
 private static final ExecutorService IO=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"pyz1k-playtime-save");t.setDaemon(true);return t;});
 private static final class Data {boolean recording=true;Map<String,Long> totals=new HashMap<>();Map<String,String> labels=new HashMap<>();}
 public record Entry(String key,String name,long millis){}
 private static Data data=new Data();private static String current="";private static long lastSave;private static boolean dirty;
 public static void initialize(){if(Files.exists(FILE))try(var reader=Files.newBufferedReader(FILE)){Data loaded=JSON.fromJson(reader,Data.class);if(loaded!=null){if(loaded.totals==null)loaded.totals=new HashMap<>();if(loaded.labels==null)loaded.labels=new HashMap<>();loaded.totals.entrySet().removeIf(e->e.getKey()==null||e.getValue()==null||e.getValue()<0);data=loaded;}}catch(Exception e){ExtensionClient.LOG.warn("Cannot load server playtime",e);}lastSave=System.nanoTime();}
 public static void tick(){
  var mc=MinecraftClient.getInstance();
  if(data.recording&&mc.world!=null&&mc.player!=null&&mc.getCurrentServerEntry()!=null&&mc.getNetworkHandler()!=null)observe(mc.getCurrentServerEntry().address,mc.player.getUuid().toString(),mc.player.getName().getString(),System.nanoTime());
  else observe("","","",System.nanoTime());
 }
 static void observe(String address,String account,String player,long now){
  String normalized=ProgressLogic.normalizeServer(address);String next=data.recording&&!normalized.isEmpty()?account+"\n"+normalized:"";
  if(!next.isEmpty())data.labels.putIfAbsent(next,normalized+" · "+player);
  long add=CLOCK.advance(next,now);if(!current.equals(next))save();current=next;
  if(add>0){data.totals.merge(next,add,(a,b)->a>Long.MAX_VALUE-b?Long.MAX_VALUE:a+b);dirty=true;}
  if(now-lastSave>=60_000_000_000L){lastSave=now;save();}
 }

 public static boolean recording(){return data.recording;}
 public static void recording(boolean value){tick();data.recording=value;CLOCK.advance("",System.nanoTime());current="";dirty=true;save();}
 public static String currentKey(){return current;}
 public static long currentMillis(){return data.totals.getOrDefault(current,0L);}
 public static List<Entry> entries(){return data.totals.entrySet().stream().map(e->new Entry(e.getKey(),data.labels.getOrDefault(e.getKey(),e.getKey().substring(e.getKey().indexOf('\n')+1)),e.getValue())).sorted(Comparator.comparingLong(Entry::millis).reversed().thenComparing(Entry::name)).toList();}
 public static void save(){if(!dirty)return;dirty=false;String snapshot=JSON.toJson(data);IO.execute(()->{try{Files.createDirectories(FILE.getParent());Path temp=FILE.resolveSibling(FILE.getFileName()+".tmp");Files.writeString(temp,snapshot);try{Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}}catch(IOException e){ExtensionClient.LOG.warn("Cannot save server playtime",e);}});}
 public static void stop(){tick();save();IO.shutdown();try{IO.awaitTermination(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
}
