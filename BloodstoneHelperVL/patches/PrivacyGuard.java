package ru.bloodhelper.client.vl;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class PrivacyGuard {
 private static volatile List<String> identities=List.of();
 private static volatile long refreshed;
 private static volatile boolean known;
 private PrivacyGuard(){}
 private static Object invoke(Object o,String method)throws ReflectiveOperationException{return o.getClass().getMethod(method).invoke(o);}
 private static synchronized void refresh(){
  long now=System.nanoTime();if(now-refreshed<2_000_000_000L&&refreshed!=0)return;refreshed=now;var ids=new ArrayList<String>();
  try{
   boolean nativeNames;Class<?> minecraft;try{minecraft=Class.forName("net.minecraft.class_310");nativeNames=false;}catch(ClassNotFoundException e){minecraft=Class.forName("net.minecraft.client.Minecraft");nativeNames=true;}
   Object mc=minecraft.getMethod(nativeNames?"getInstance":"method_1551").invoke(null);Object user=invoke(mc,nativeNames?"getUser":"method_1548");
   Object name=invoke(user,nativeNames?"getName":"method_1676"),uuid=invoke(user,nativeNames?"getProfileId":"method_44717");
   if(name!=null)ids.add(name.toString().toLowerCase(Locale.ROOT));if(uuid!=null){ids.add(uuid.toString().toLowerCase(Locale.ROOT));ids.add(uuid.toString().replace("-","").toLowerCase(Locale.ROOT));}
   Object server=invoke(mc,nativeNames?"getCurrentServer":"method_1558");if(server!=null){Object address=server.getClass().getField(nativeNames?"ip":"field_3761").get(server);if(address!=null)ids.add(address.toString().toLowerCase(Locale.ROOT));}
   known=true;
  }catch(ReflectiveOperationException|LinkageError e){known=false;}
  identities=List.copyOf(ids);
 }
 public static boolean sensitive(String url,List<String> ids){String decoded=URLDecoder.decode(url,StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);for(String id:ids)if(id!=null&&id.length()>=3&&decoded.contains(id.toLowerCase(Locale.ROOT)))return true;return false;}
 public static void checkUri(URI uri){if(uri==null)return;checkUrl(uri.toString());}
 public static void checkUrlObject(URL url){if(url!=null)checkUrl(url.toExternalForm());}
 public static void checkUrl(String url){
  URI uri=URI.create(url);String host=uri.getHost();if(host==null)return;
  if(host.equalsIgnoreCase("localhost")||host.equals("127.0.0.1")||host.equals("[::1]")||host.equals("::1"))return;
  if(host.equals("178.22.24.133"))throw new SecurityException("Blocked device registration service");
  refresh();
  if(sensitive(url,identities))throw new SecurityException("Blocked request containing local player identity or server address");
  if(!known&&Set.of("api.atiers.net","mytiers.ru","api.minecraftservices.com","api.mojang.com","mctiers.com","pvptiers.com","mc-heads.net","api.mcsrvstat.us").contains(host.toLowerCase(Locale.ROOT)))throw new SecurityException("Player identity unavailable; profile request blocked");
 }
}
