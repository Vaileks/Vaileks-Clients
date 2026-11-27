package pyz1k.extensions.pvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static pyz1k.extensions.pvp.Feature.*;

public final class Core implements ClientModInitializer {
 public static final Logger LOG=LoggerFactory.getLogger("vaileks-pvp-tools");
 public static final EnumMap<Feature,Config> roots=new EnumMap<>(Feature.class);
 static final EnumMap<Feature,List<Row>> rows=new EnumMap<>(Feature.class);
 static final Map<Integer,Boolean> keys=new HashMap<>();
 static final Map<Identifier,ItemStack> cooldowns=new HashMap<>();
 static final Set<String> effectWarned=new HashSet<>(),armorWarned=new HashSet<>();
 static final Map<Feature,Long> soundTimes=new EnumMap<>(Feature.class);
 static final List<Event> timeline=new ArrayList<>(),lastTimeline=new ArrayList<>();
 static final Map<String,Integer> used=new LinkedHashMap<>(),lastUsed=new LinkedHashMap<>();
 static final List<String> pearlResults=new ArrayList<>();
 static final Map<UUID,Long> pearlStarts=new HashMap<>();
 static final Map<EquipmentSlot,ItemStack> armorBefore=new EnumMap<>(EquipmentSlot.class);
 static long fightStart,lastCombat,lastDuration,totemUntil,searchUntil,notificationUntil;
 static int fightTotems,lastTotems,ticks,smokeStep;
 static String context="",notification="",opponent="",lastOpponent="";
 static boolean initialized,wasDead,wasInventory;
 static volatile UUID playerId;
 static Screen previousScreen;
 public record Row(ItemStack icon,String label,String value,boolean critical) {
  public Row(String label,String value,boolean critical){this(ItemStack.EMPTY,label,value,critical);}
 }
 public record Event(long ms,String text) {}
 public void onInitializeClient() {
  for(Feature f:Feature.values())if(f!=SERVER_PROFILES){
   roots.put(f,Config.load(f));rows.put(f,new ArrayList<>());
  }
  initialized=true;LOG.info("PvP Tools loaded: {}",roots.keySet());
 }
 static MinecraftClient mc(){return MinecraftClient.getInstance();}
 static long now(){return System.nanoTime()/1_000_000;}
 public static boolean has(Feature f){return roots.containsKey(f);}
 public static boolean on(Feature f){return PvpModules.enabled(f);}
 public static boolean recording(){return fightStart>0&&(on(FIGHT_RECAP)||on(FIGHT_TIMELINE));}
 public static boolean any(){return PvpModules.any();}
 static String profile(){return onProfiles()?context:"";}
 static boolean onProfiles(){return pyz1k.extensions.ExtensionConfig.get().automaticProfiles;}
 public static Config config(Feature f){
  Config root=roots.get(f);if(root==null)return Config.defaults(f);root.enabled=on(f);
  if(f==SERVER_PROFILES||!onProfiles()||context.isEmpty())return root;
  Config active=root.profiles.computeIfAbsent(context,k->{Config copy=root.copy();copy.profiles.clear();return copy;});active.enabled=on(f);return active;
 }
 public static void save(Feature f,Config c){
  c.sanitize(f);PvpModules.enable(f,c.enabled);
  if(f!=SERVER_PROFILES&&onProfiles()&&!context.isEmpty()){c.profiles.clear();roots.get(f).profiles.put(context,c);}
  else roots.put(f,c);
  Config.write(f,roots.get(f));
 }
 public static void notice(String s){notification=s;notificationUntil=now()+3500;}
 static boolean pressed(int key){return key>=32&&key<=348&&GLFW.glfwGetKey(mc().getWindow().getHandle(),key)==GLFW.GLFW_PRESS;}
 static boolean edge(int key){boolean down=pressed(key),old=keys.getOrDefault(key,false);keys.put(key,down);return down&&!old;}
 public static void tick(){
  if(!initialized||!any())return;
  MinecraftClient c=mc();ticks++;
  String next=c.getCurrentServerEntry()!=null?"server:"+c.getCurrentServerEntry().address:c.getServer()!=null?"world:"+c.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT).toAbsolutePath().normalize():"";
  if(!next.equals(context)){
   flush();context=next;resetSession();
  }
  if(c.player==null||c.world==null){playerId=null;return;}
  playerId=c.player.getUuid();
  boolean inv=c.currentScreen instanceof HandledScreen;
  if(on(INVENTORY_SEARCH_FLASH)&&inv&&!wasInventory){searchUntil=now()+(long)config(INVENTORY_SEARCH_FLASH).threshold*1000;}
  wasInventory=inv;
  for(Feature f:roots.keySet()){
   Config cfg=config(f);if(cfg.actionKey>0){boolean hit=edge(cfg.actionKey);if(hit&&on(f)&&!(c.currentScreen instanceof SettingsScreen)&&(c.currentScreen==null||f==INVENTORY_SEARCH_FLASH))action(f);}
  }
  boolean dead=c.player.isDead();
  if(dead&&!wasDead){event("Смерть");resupplyPending=true;finishFight();}
  wasDead=dead;
  int timeout=on(FIGHT_RECAP)?config(FIGHT_RECAP).threshold:on(FIGHT_TIMELINE)?config(FIGHT_TIMELINE).threshold:on(COMBAT_FOCUS)?config(COMBAT_FOCUS).threshold:15;
  if(fightStart>0&&now()-lastCombat>timeout*1000L)finishFight();
  if(recording())for(EquipmentSlot slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.HUMANOID_ARMOR){
   ItemStack current=c.player.getEquippedStack(slot),before=armorBefore.put(slot,current.copy());
   if(before!=null&&!before.isEmpty()&&current.isEmpty())event("Снята / сломана броня: "+before.getName().getString());
  }
  if(ticks%4==0)updateRows();
 }
 public static void disabled(Feature f){
  var list=rows.get(f);if(list!=null)list.clear();
  if((f==Feature.FIGHT_RECAP||f==Feature.FIGHT_TIMELINE||f==Feature.COMBAT_FOCUS)&&!on(Feature.FIGHT_RECAP)&&!on(Feature.FIGHT_TIMELINE)&&!on(Feature.COMBAT_FOCUS)){finishFight();armorBefore.clear();}
  switch(f){
   case READY_AGAIN -> {cooldowns.clear();readyMessages.clear();}
   case EFFECT_ALARM -> effectWarned.clear();
   case ARMOR_PANIC -> armorWarned.clear();
   case OFFHAND_ALERT -> offhandBad=false;
   case REFILL_REMINDER -> refillTracked.clear();
   case INVENTORY_SEARCH_FLASH -> {InventorySearch.clear();wasInventory=false;searchUntil=0;}
   default -> {}
  }
 }
 static void flush(){for(Feature f:roots.keySet())Config.write(f,roots.get(f));}
 static void resetSession(){
  if(fightStart>0)finishFight();cooldowns.clear();effectWarned.clear();armorWarned.clear();pearlStarts.clear();pearlResults.clear();InventorySearch.clear();
  fightStart=lastCombat=totemUntil=searchUntil=0;wasDead=wasInventory=resupplyPending=offhandBad=false;refillTracked.clear();readyMessages.clear();armorBefore.clear();for(List<Row> r:rows.values())r.clear();
 }
 static ItemStack icon(Feature f){return new ItemStack(Registries.ITEM.get(Identifier.of(f.icon)));}
 static Row row(ItemStack s,String label,String value,boolean critical){return new Row(s.copy(),label,value,critical);}
 static List<Row> list(Feature f){return rows.get(f);}
 static void add(Feature f,Row r){if(has(f))list(f).add(r);}
 public static String key(ItemStack s){
  if(s.isEmpty())return "";
  StringBuilder b=new StringBuilder(Registries.ITEM.getId(s.getItem()).toString()).append('|').append(s.getName().getString());
  PotionContentsComponent p=s.get(DataComponentTypes.POTION_CONTENTS);
  if(p!=null)for(StatusEffectInstance e:p.getEffects())b.append('|').append(e.getEffectType().getIdAsString()).append(':').append(e.getAmplifier()).append(':').append(e.getDuration());
  return b.toString();
 }
 public static List<ItemStack> inventory(){
  List<ItemStack> out=new ArrayList<>(37);var p=Core.mc().player;if(p==null)return out;
  for(int i=0;i<36;i++)out.add(p.getInventory().getStack(i));out.add(p.getOffHandStack());return out;
 }
 static int count(Item item){int total=0;for(ItemStack s:inventory())if(s.isOf(item))total+=s.getCount();return total;}
 static Map<String,Integer> counts(){Map<String,Integer> out=new LinkedHashMap<>();for(ItemStack s:inventory())if(!s.isEmpty())out.merge(key(s),s.getCount(),Integer::sum);return out;}
 public static void capture(Feature f,Config cfg){
  if(mc().player==null){notice("Сохраните комплект, находясь в мире");return;}
  List<Config.SavedItem> out=new ArrayList<>();PlayerInventory inv=mc().player.getInventory();
  if(f==HOTBAR_MEMORY){for(int i=0;i<9;i++){ItemStack s=inv.getStack(i);out.add(new Config.SavedItem(key(s),Registries.ITEM.getId(s.getItem()).toString(),s.isEmpty()?"Пусто":s.getName().getString(),s.getCount(),i));}cfg.hotbar=out;}
  else {
   Map<String,Config.SavedItem> map=new LinkedHashMap<>();
   for(ItemStack s:inventory())if(!s.isEmpty()){String k=key(s);Config.SavedItem old=map.get(k);map.put(k,new Config.SavedItem(k,Registries.ITEM.getId(s.getItem()).toString(),s.getName().getString(),s.getCount()+(old==null?0:old.count()),-1));}
   out.addAll(map.values());
   for(EquipmentSlot slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.HUMANOID_ARMOR){ItemStack s=mc().player.getEquippedStack(slot);if(!s.isEmpty())out.add(new Config.SavedItem(key(s),Registries.ITEM.getId(s.getItem()).toString(),s.getName().getString(),1,slot.getIndex()));}
   cfg.kit=out;
  }
  notice("Сохранено: "+f.title);
 }
 static List<Row> missing(Config cfg,boolean equipment){
  List<Row> out=new ArrayList<>();Map<String,Integer> current=counts();
  for(Config.SavedItem saved:cfg.kit){
   int lack;
   if(saved.slot()>=0){if(!equipment)continue;ItemStack actual=ItemStack.EMPTY;for(EquipmentSlot slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.HUMANOID_ARMOR&&slot.getIndex()==saved.slot())actual=mc().player.getEquippedStack(slot);lack=key(actual).equals(saved.key())?0:1;}
   else lack=Math.max(0,saved.count()-current.getOrDefault(saved.key(),0));
   if(lack>0)out.add(row(new ItemStack(Registries.ITEM.get(Identifier.of(saved.item()))),saved.name(),saved.slot()>=0?"Не надето":"−"+lack,true));
  }
  return out;
 }
 static void signal(Feature f){
  if(!on(f)||!config(f).sound||now()-soundTimes.getOrDefault(f,0L)<1500)return;
  soundTimes.put(f,now());mc().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK,1.2f));
 }
 static void updateRows(){
  for(List<Row> r:rows.values())r.clear();
  InventorySearch.tick();
  var p=mc().player;var inv=p.getInventory();long t=now();
  if(on(READY_AGAIN)){
   var manager=p.getItemCooldownManager();
   for(ItemStack s:inventory())if(!s.isEmpty()&&manager.isCoolingDown(s))cooldowns.put(manager.getGroup(s),s.copy());
   for(var it=cooldowns.entrySet().iterator();it.hasNext();){var e=it.next();if(!manager.isCoolingDown(e.getValue())){
    readyMessages.add(new TimedRow(t+config(READY_AGAIN).threshold*1000L,row(e.getValue(),e.getValue().getName().getString(),"Готово",true)));signal(READY_AGAIN);it.remove();
   }}
   readyMessages.removeIf(r->r.until<t);for(TimedRow r:readyMessages)add(READY_AGAIN,r.row);
  }
  if(on(EFFECT_ALARM)){
   Set<String> active=new HashSet<>();
   for(StatusEffectInstance e:p.getStatusEffects()){
    String id=e.getEffectType().getIdAsString();active.add(id);int sec=(int)Math.ceil(e.getDuration()/20.0);boolean low=!e.isInfinite()&&sec<=config(EFFECT_ALARM).threshold;
    if(low){add(EFFECT_ALARM,row(icon(EFFECT_ALARM),e.getEffectType().value().getName().getString(),sec+" с",true));if(effectWarned.add(id))signal(EFFECT_ALARM);}else effectWarned.remove(id);
   }effectWarned.retainAll(active);
  }
  if(on(REFILL_REMINDER)){
   Map<String,Integer> hot=new LinkedHashMap<>(),reserve=new LinkedHashMap<>();Map<String,ItemStack> examples=new HashMap<>();
   for(int i=0;i<36;i++){ItemStack s=inv.getStack(i);if(s.isEmpty()||!s.isStackable()&&!(s.getItem() instanceof PotionItem))continue;
    if(!(s.getItem() instanceof PotionItem)&&!s.isOf(Items.ENDER_PEARL)&&!s.isOf(Items.GOLDEN_APPLE)&&!s.isOf(Items.ENCHANTED_GOLDEN_APPLE)&&!s.isOf(Items.END_CRYSTAL)&&!s.isOf(Items.OBSIDIAN)&&!s.isOf(Items.EXPERIENCE_BOTTLE))continue;
    String k=key(s);(i<9?hot:reserve).merge(k,s.getCount(),Integer::sum);examples.put(k,s.copy());
   }
   // Remember hotbar identities even when the last item has just been used.
   for(var e:hot.entrySet())refillTracked.add(e.getKey());
   for(String k:refillTracked)if(hot.getOrDefault(k,0)<=config(REFILL_REMINDER).threshold&&reserve.getOrDefault(k,0)>0){ItemStack s=examples.get(k);if(s!=null)add(REFILL_REMINDER,row(s,s.getName().getString(),hot.getOrDefault(k,0)+" / запас "+reserve.get(k),true));}
  }
  if(on(ARMOR_PANIC))for(EquipmentSlot slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.HUMANOID_ARMOR){
   ItemStack s=p.getEquippedStack(slot);String k=slot.getName();
   if(!s.isEmpty()&&s.isDamageable()){int left=s.getMaxDamage()-s.getDamage();int pc=(int)Math.ceil(100.0*left/s.getMaxDamage());if(pc<=config(ARMOR_PANIC).threshold){add(ARMOR_PANIC,row(s,s.getName().getString(),left+" · "+pc+"%",true));if(armorWarned.add(k))signal(ARMOR_PANIC);}else armorWarned.remove(k);}else armorWarned.remove(k);
  }
  if(on(KIT_CHECK)){
   Config cfg=config(KIT_CHECK);if(cfg.kit.isEmpty())add(KIT_CHECK,new Row("Комплект","Сохраните в настройках",false));else {List<Row> diff=missing(cfg,true);list(KIT_CHECK).addAll(diff);if(diff.isEmpty())add(KIT_CHECK,new Row("Комплект","Готов",false));}
  }
  if(on(RESUPPLY_LIST)&&wasDead&& !config(RESUPPLY_LIST).kit.isEmpty())list(RESUPPLY_LIST).addAll(missing(config(RESUPPLY_LIST),false));
  if(on(RESUPPLY_LIST)&&!wasDead&&resupplyPending){List<Row> diff=missing(config(RESUPPLY_LIST),false);list(RESUPPLY_LIST).addAll(diff);if(diff.isEmpty())resupplyPending=false;}
  if(on(TOTEM_PULSE)&&t<totemUntil){int n=count(Items.TOTEM_OF_UNDYING);add(TOTEM_PULSE,row(icon(TOTEM_PULSE),"Тотемов осталось",String.valueOf(n),n<=config(TOTEM_PULSE).threshold));}
  if(on(OFFHAND_ALERT)){
   Config cfg=config(OFFHAND_ALERT);Item target=switch(cfg.mode){case 1->Items.SHIELD;case 2->Items.GOLDEN_APPLE;default->Items.TOTEM_OF_UNDYING;};ItemStack hand=p.getOffHandStack();
   if(!hand.isOf(target)){add(OFFHAND_ALERT,row(new ItemStack(target),"Левая рука",hand.isEmpty()?"Пусто":"Другой предмет",true));if(!offhandBad)signal(OFFHAND_ALERT);offhandBad=true;}else offhandBad=false;
  }
  if(on(HOTBAR_MEMORY)){
   Config cfg=config(HOTBAR_MEMORY);int wrong=0;for(var saved:cfg.hotbar)if(saved.slot()>=0&&saved.slot()<9&&!saved.key().equals(key(inv.getStack(saved.slot()))))wrong++;
   if(cfg.hotbar.isEmpty())add(HOTBAR_MEMORY,new Row("Раскладка","Сохраните в настройках",false));else if(wrong>0)add(HOTBAR_MEMORY,new Row("Не на своих местах",wrong+" сл.",true));
  }
  if(on(PEARL_LANDING_TIMER)){
   if(mc().getServer()==null)add(PEARL_LANDING_TIMER,new Row("Тренировка","Только одиночный мир",false));
   else {if(!pearlResults.isEmpty())add(PEARL_LANDING_TIMER,new Row("Последний бросок",pearlResults.get(pearlResults.size()-1),false));if(!pearlStarts.isEmpty())add(PEARL_LANDING_TIMER,new Row("Жемчуг","В полёте",false));}
  }
  if(on(FIGHT_RECAP)){
   if(fightStart>0)add(FIGHT_RECAP,new Row("Бой: "+opponent,seconds(t-fightStart),false));
   else if(lastDuration>0){add(FIGHT_RECAP,new Row("Последний бой",seconds(lastDuration),false));add(FIGHT_RECAP,new Row("Сработало тотемов",String.valueOf(lastTotems),false));for(var e:lastUsed.entrySet())add(FIGHT_RECAP,new Row(e.getKey(),"×"+e.getValue(),false));}
  }
  if(on(FIGHT_TIMELINE)){
   List<Event> source=fightStart>0?timeline:lastTimeline;for(int i=Math.max(0,source.size()-5);i<source.size();i++){Event e=source.get(i);add(FIGHT_TIMELINE,new Row(seconds(e.ms),e.text,false));}
  }
  if(on(DUEL_NOTES)){int total=config(DUEL_NOTES).notes.values().stream().mapToInt(Integer::intValue).sum();if(total>0)add(DUEL_NOTES,new Row("Заметки",total+" · открыть в настройках",false));}
  if(on(COMBAT_FOCUS)&&fightStart>0)add(COMBAT_FOCUS,new Row("Combat Focus","Бой",false));
  if(on(SERVER_PROFILES))add(SERVER_PROFILES,new Row("Профиль",context.startsWith("server:")?context.substring(7):"Одиночный мир",false));
  pearlStarts.entrySet().removeIf(e->t-e.getValue()>120_000);
 }
 static final List<TimedRow> readyMessages=new ArrayList<>();
 static final Set<String> refillTracked=new HashSet<>();
 record TimedRow(long until,Row row) {}
 static boolean offhandBad,resupplyPending;
 static String seconds(long ms){return String.format(Locale.ROOT,"%.1f с",ms/1000.0);}
 static void startFight(String name){if(fightStart==0){fightStart=now();timeline.clear();used.clear();fightTotems=0;opponent=name;event("Начало: "+name);}lastCombat=now();}
 static void event(String text){if(fightStart>0){timeline.add(new Event(now()-fightStart,text));if(timeline.size()>500)timeline.remove(0);}}
 static void finishFight(){
  if(fightStart==0)return;event("Конец записи");lastDuration=now()-fightStart;lastTotems=fightTotems;lastOpponent=opponent;lastUsed.clear();lastUsed.putAll(used);lastTimeline.clear();lastTimeline.addAll(timeline);fightStart=0;
  if(on(FIGHT_RECAP)||on(FIGHT_TIMELINE))exportFight(false);
 }
 public static void status(EntityStatusS2CPacket packet){
  if(!initialized||!(on(TOTEM_PULSE)||recording())||mc().world==null||mc().player==null)return;
  if(packet.getStatus()==35&&packet.getEntity(mc().world)==mc().player){totemUntil=now()+5000;fightTotems++;event("Сработал тотем");signal(TOTEM_PULSE);}
 }
 public static void damage(EntityDamageS2CPacket packet){
  if(!initialized||mc().world==null||mc().player==null||!(on(FIGHT_RECAP)||on(FIGHT_TIMELINE)||on(COMBAT_FOCUS)))return;
  var target=mc().world.getEntityById(packet.entityId());var source=packet.createDamageSource(mc().world).getAttacker();
  if(target instanceof PlayerEntity&&source instanceof PlayerEntity&&(target==mc().player||source==mc().player))startFight((target==mc().player?source:target).getName().getString());
 }
 public static void used(ItemStack s,boolean consumed){
  if(!initialized||!recording()||mc().player==null||s.isEmpty())return;
  if(!consumed&&!s.isOf(Items.ENDER_PEARL)&&!s.isOf(Items.SPLASH_POTION)&&!s.isOf(Items.LINGERING_POTION)&&!s.isOf(Items.EXPERIENCE_BOTTLE))return;
  if(fightStart>0){String name=s.getName().getString();used.merge(name,1,Integer::sum);event((consumed?"Использовано: ":"Бросок: ")+name);}
 }
 public static void pearlTick(EnderPearlEntity pearl){
  UUID local=playerId;
  if(!initialized||!on(PEARL_LANDING_TIMER)||pearl.age>1||pearl.getEntityWorld().isClient()||local==null||pearl.getOwner()==null||!pearl.getOwner().getUuid().equals(local))return;
  UUID id=pearl.getUuid();mc().execute(()->{if(on(PEARL_LANDING_TIMER)&&mc().getServer()!=null)pearlStarts.putIfAbsent(id,now());});
 }
 public static void pearlLand(EnderPearlEntity pearl){
  if(!initialized||!on(PEARL_LANDING_TIMER)||pearl.getEntityWorld().isClient()||playerId==null||pearl.getOwner()==null||!pearl.getOwner().getUuid().equals(playerId))return;
  int age=pearl.age;UUID id=pearl.getUuid();mc().execute(()->{if(!on(PEARL_LANDING_TIMER)||mc().getServer()==null)return;pearlStarts.remove(id);String result=String.format(Locale.ROOT,"%.2f с · %d тиков",age/20.0,age);pearlResults.add(result);if(pearlResults.size()>50)pearlResults.remove(0);notice("Жемчуг: "+result);});
 }
 public static void action(Feature f){
  if(mc().player==null){notice("Эта функция доступна в мире");return;}
  switch(f){
   case KIT_CHECK -> {updateRows();mc().setScreen(new ReportScreen(mc().currentScreen,"Проверка комплекта",reportRows(list(f))));}
   case RESUPPLY_LIST -> {resupplyPending=true;updateRows();mc().setScreen(new ReportScreen(mc().currentScreen,"Пополнение комплекта",reportRows(missing(config(f),false))));}
   case FIGHT_RECAP,FIGHT_TIMELINE -> {if(fightStart==0)startFight("Ручная запись");else{finishFight();notice("Запись боя сохранена");}}
   case DUEL_NOTES -> mc().setScreen(new NotesScreen(mc().currentScreen));
   case INVENTORY_SEARCH_FLASH -> {Config cfg=config(f);cfg.mode=(cfg.mode+1)%4;save(f,cfg);searchUntil=now()+cfg.threshold*1000L;notice("Поиск: "+searchCategory(cfg.mode));}
   case PEARL_LANDING_TIMER -> mc().setScreen(new ReportScreen(mc().currentScreen,"История бросков",new ArrayList<>(pearlResults)));
   default -> {Config cfg=config(f);cfg.enabled=!cfg.enabled;save(f,cfg);notice(f.title+": "+(cfg.enabled?"вкл.":"выкл."));}
  }
 }
 static List<String> reportRows(List<Row> rows){List<String> lines=new ArrayList<>();for(Row r:rows)lines.add(r.label+"  "+r.value);if(lines.isEmpty())lines.add("Всё на месте");return lines;}
 public static void exportFight(boolean feedback){
  if(lastDuration==0){if(feedback)notice("Завершённых записей пока нет");return;}
  try{
   Path dir=FabricLoader.getInstance().getGameDir().resolve("pvp-tools/reports");Files.createDirectories(dir);
   Map<String,Object> report=new LinkedHashMap<>();report.put("created",Instant.now().toString());report.put("opponent",lastOpponent);report.put("durationMs",lastDuration);report.put("totems",lastTotems);report.put("clientUses",lastUsed);report.put("events",lastTimeline);
   String filename="fight-"+DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now())+".json";
   Files.writeString(dir.resolve(filename),Config.JSON.toJson(report));if(feedback)notice("Сохранено в pvp-tools/reports/");
  }catch(Exception e){LOG.error("Report export failed",e);notice("Не удалось сохранить отчёт");}
 }
 public static void render(DrawContext ctx){
  if(!initialized||!any()||pyz1k.extensions.ScreenshotMode.active()||mc().player==null||mc().options.hudHidden||mc().currentScreen instanceof SettingsScreen||mc().currentScreen instanceof HubScreen)return;
  int autoY=8,autoColumn=0;
  for(Feature f:roots.keySet())if(on(f)){
   List<Row> source=rows.get(f);if(source==null||source.isEmpty())continue;
   List<Row> visible=new ArrayList<>();for(Row r:source)if(visible(f,r))visible.add(r);if(!visible.isEmpty()){
    Config layout=config(f);
    if(!layout.manualPosition){
     layout=layout.layoutCopy();int h=(int)Math.ceil((18+Math.min(visible.size(),8)*19+(visible.size()>8?12:0))*layout.scale);
     if(autoY+h>ctx.getScaledWindowHeight()-45&&autoY>8){autoColumn++;autoY=8;}
     layout.x=autoColumn%2==0?0.02:0.98;layout.y=Math.clamp(autoY/(double)Math.max(1,ctx.getScaledWindowHeight()-h),0,1);autoY+=h+6;
    }
    panel(ctx,f,layout,visible,false);
   }
  }
  if(now()<notificationUntil)ctx.drawCenteredTextWithShadow(mc().textRenderer,notification,ctx.getScaledWindowWidth()/2,ctx.getScaledWindowHeight()-65,0xFFFFFFFF);
 }
 public static boolean hideVanilla(String part){
  if(!initialized||!(on(COMBAT_FOCUS)||on(HUD_ON_DEMAND))||mc().player==null)return false;
  for(Feature f:new Feature[]{COMBAT_FOCUS,HUD_ON_DEMAND})if(on(f)){
   Config cfg=config(f);boolean active=f==COMBAT_FOCUS?fightStart>0:!pressed(cfg.holdKey);
   if(active&&switch(part){case "scoreboard"->cfg.hideScoreboard;case "bossbar"->cfg.hideBossbar;case "effects"->cfg.hideEffects;default->cfg.hideItemName;})return true;
  }
  return false;
 }
 static boolean visible(Feature f,Row r){
  if(r.critical)return true;
  if(on(HUD_ON_DEMAND)&&!pressed(config(HUD_ON_DEMAND).holdKey))return false;
  return !(on(COMBAT_FOCUS)&&fightStart>0&&f!=COMBAT_FOCUS&&f!=FIGHT_RECAP&&f!=FIGHT_TIMELINE);
 }
 public static int[] panel(DrawContext ctx,Feature f,Config cfg,List<Row> source,boolean preview){
  int max=mc().textRenderer.getWidth(f.title);int lines=Math.min(source.size(),8);
  for(int i=0;i<lines;i++){Row r=source.get(i);max=Math.max(max,mc().textRenderer.getWidth((cfg.icons?"":r.label+"  ")+r.value)+(cfg.icons?22:0));}
  int w=max+14,h=18+lines*19+(source.size()>8?12:0);
  double scale=cfg.scale;int sw=ctx.getScaledWindowWidth(),sh=ctx.getScaledWindowHeight();
  int x=(int)(cfg.x*Math.max(0,sw-w*scale)),y=(int)(cfg.y*Math.max(0,sh-h*scale));
  ctx.getMatrices().pushMatrix();ctx.getMatrices().translate(x,y);ctx.getMatrices().scale((float)scale,(float)scale);
  if(cfg.background||preview){ctx.fill(0,0,w,h,0xCC171B22);ctx.drawStrokedRectangle(0,0,w,h,0xFF707B90);}
  ctx.drawText(mc().textRenderer,f.title,7,5,0xFF000000|cfg.accent,cfg.shadow);
  int cy=18;
  for(int i=0;i<lines;i++){
   Row row=source.get(i);int color=0xFF000000|(row.critical?cfg.accent:cfg.color);
   if(cfg.icons&&!row.icon.isEmpty()){ctx.drawItem(row.icon,6,cy);ctx.drawText(mc().textRenderer,row.value,28,cy+4,color,cfg.shadow);}
   else ctx.drawText(mc().textRenderer,row.label+"  "+row.value,7,cy+4,color,cfg.shadow);
   cy+=19;
  }
  if(source.size()>8)ctx.drawText(mc().textRenderer,"Ещё "+(source.size()-8)+" · открыть список",7,cy,0xFFAAAAAA,cfg.shadow);
  ctx.getMatrices().popMatrix();return preview?new int[]{x,y,(int)Math.ceil(w*scale),(int)Math.ceil(h*scale)}:null;
 }
 public static List<Row> preview(Feature f){
  if(rows.containsKey(f)&&!rows.get(f).isEmpty())return rows.get(f);
  return List.of(row(icon(f),switch(f){case ARMOR_PANIC->"Алмазный нагрудник";case EFFECT_ALARM->"Сила II";case TOTEM_PULSE->"Тотемов осталось";case OFFHAND_ALERT->"Левая рука";default->"Пример";},switch(f){case READY_AGAIN->"Готово";case ARMOR_PANIC->"31 · 6%";case EFFECT_ALARM->"8 с";case TOTEM_PULSE->"2";default->"12";},true));
 }
 public static void hotbarSlot(DrawContext ctx,ItemStack stack,int x,int y){
  if(!initialized||!(on(HOTBAR_MEMORY)||on(POTION_IDENTITY))||mc().player==null)return;
  potionLabel(ctx,stack,x,y);
  if(on(HOTBAR_MEMORY)){
   int slot=(x-(ctx.getScaledWindowWidth()/2-90))/20;
   if(slot>=0&&slot<9)for(var saved:config(HOTBAR_MEMORY).hotbar)if(saved.slot()==slot&&!saved.key().equals(key(stack)))ctx.drawStrokedRectangle(x-1,y-1,18,18,0xFF000000|config(HOTBAR_MEMORY).accent);
  }
 }
 public static void inventorySlot(DrawContext ctx,Slot slot){
  if(!initialized||!(on(HOTBAR_MEMORY)||on(POTION_IDENTITY)||on(INVENTORY_SEARCH_FLASH))||mc().player==null||slot.inventory!=mc().player.getInventory())return;
  potionLabel(ctx,slot.getStack(),slot.x,slot.y);
  if(on(HOTBAR_MEMORY)&&slot.getIndex()<9)for(var saved:config(HOTBAR_MEMORY).hotbar)if(saved.slot()==slot.getIndex()&&!saved.key().equals(key(slot.getStack())))ctx.drawStrokedRectangle(slot.x-1,slot.y-1,18,18,0xFF000000|config(HOTBAR_MEMORY).accent);
  if(InventorySearch.matches(slot.getStack())){
   ctx.fill(slot.x,slot.y,slot.x+16,slot.y+16,0x44000000|config(INVENTORY_SEARCH_FLASH).accent);ctx.drawStrokedRectangle(slot.x-1,slot.y-1,18,18,0xFF000000|config(INVENTORY_SEARCH_FLASH).accent);
  }
 }
 static String searchCategory(int mode){return switch(mode){case 1->"Броня";case 2->"Жемчуг";case 3->"По названию";default->"Лечение";};}
 static boolean matchesCategory(ItemStack s,int mode){
  if(s.isEmpty())return false;
  if(mode==2)return s.isOf(Items.ENDER_PEARL);
  if(mode==1)return pyz1k.extensions.InventorySearchQuery.armor(Registries.ITEM.getId(s.getItem()).toString());
  if(s.isOf(Items.GOLDEN_APPLE)||s.isOf(Items.ENCHANTED_GOLDEN_APPLE))return true;
  PotionContentsComponent p=s.get(DataComponentTypes.POTION_CONTENTS);if(p!=null)for(StatusEffectInstance e:p.getEffects())if(e.getEffectType().equals(StatusEffects.INSTANT_HEALTH)||e.getEffectType().equals(StatusEffects.REGENERATION))return true;
  return false;
 }
 static void potionLabel(DrawContext ctx,ItemStack s,int x,int y){
  if(!on(POTION_IDENTITY))return;PotionContentsComponent p=s.get(DataComponentTypes.POTION_CONTENTS);if(p==null)return;
  String label="";for(StatusEffectInstance e:p.getEffects()){
   label=e.getEffectType().equals(StatusEffects.STRENGTH)?"С":e.getEffectType().equals(StatusEffects.SPEED)?"Б":e.getEffectType().equals(StatusEffects.FIRE_RESISTANCE)?"О":e.getEffectType().equals(StatusEffects.INSTANT_HEALTH)?"Л":e.getEffectType().equals(StatusEffects.REGENERATION)?"Р":e.getEffectType().equals(StatusEffects.SLOWNESS)?"З":e.getEffectType().equals(StatusEffects.WEAKNESS)?"Сл":e.getEffectType().equals(StatusEffects.POISON)?"Я":"?";
   if(e.getAmplifier()>0)label+=(e.getAmplifier()+1);break;
  }
  if(!label.isEmpty()){Config cfg=config(POTION_IDENTITY);ctx.fill(x,y,x+mc().textRenderer.getWidth(label)+2,y+9,0xC0171B22);ctx.drawText(mc().textRenderer,label,x+1,y,0xFF000000|cfg.accent,cfg.shadow);}
 }
}

