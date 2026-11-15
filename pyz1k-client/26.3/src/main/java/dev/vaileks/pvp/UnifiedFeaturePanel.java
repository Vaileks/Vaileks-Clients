package pyz1k.extensions.pvp;

import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.module.base.Module;
import pyz1k.extensions.*;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.utilites.color.ClientColors;
import static pyz1k.extensions.pvp.Feature.*;

/** A section of the client's existing menu, never a second Minecraft screen. */
public final class UnifiedFeaturePanel {
 private record Row(String label,String value,Runnable action,DoubleConsumer slide,double min,double max,DoubleSupplier get){}
 private record Hit(float x,float y,float w,float h,Runnable run){boolean contains(double mx,double my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}}
 private final List<Row> rows=new ArrayList<>();private final List<Hit> hits=new ArrayList<>();
 private Config pending;private Module module;private Feature feature;private int page,scroll,category,state,capture;private String editing="",query="",note="",selected="",reportTitle="";private List<String> report=List.of();
 private float x,y,w,h;private int visible;private Row drag;private float dragX,dragW;private long refreshAt;private List<AdvancementData.Entry> advancements=List.of();
 public static boolean supports(Module m){return m instanceof PvpModules.FeatureModule||m instanceof FeatureModules.Playtime||m instanceof FeatureModules.Advancements;}
 public void select(Module m){if(module==m)return;release();module=m;feature=PvpModules.feature(m);page=scroll=capture=category=state=0;editing=query=note=selected=reportTitle="";report=List.of();refreshAt=0;drag=null;}
 public void actions(){page=3;scroll=0;}
 public int page(){return page;}
 public String query(){return feature==INVENTORY_SEARCH_FLASH?Core.config(feature).searchQuery:query;}
 public void render(GuiGraphicsExtractor c,float px,float py,float pw,float ph,int mx,int my,float alpha,Runnable back){
  x=px+12;y=py+9;w=pw-24;h=ph-17;hits.clear();rows.clear();
  button(c,"‹ Назад",x,y,51,19,back,false);text(c,module.getName(),x+61,y+5,9,ClientColors.CATEGORY_ACCENT_SECOND);
  if(feature!=null){String[] tabs={"Функция","Вид","HUD","Действия"};for(int i=0;i<4;i++){int p=i;button(c,tabs[i],x+i*(w+4)/4,y+24,(w-12)/4,18,()->{page=p;scroll=0;editing="";capture=0;report=List.of();},page==p);}}
  else {button(c,"Поиск: "+(query.isEmpty()?"все":query),x,y+24,w,18,()->editing="progress",editing.equals("progress"));}
  if(!report.isEmpty()){action("‹ К настройкам",reportTitle,()->{report=List.of();scroll=0;});for(String s:report)label(s,"");}
  else if(feature!=null)buildPvp();else if(module instanceof FeatureModules.Playtime)buildPlaytime();else buildAdvancements();
  visible=Math.max(1,(int)((h-62)/21));scroll=Math.clamp(scroll,0,Math.max(0,rows.size()-visible));
  float top=y+47;
  for(int i=0;i<visible&&scroll+i<rows.size();i++){Row r=rows.get(scroll+i);float ry=top+i*21;
   RenderUtil.drawRoundRect(c,x,ry,w-5,19,3,ClientColors.SETTINGS_BACKGROUND);
   if(r.slide!=null){float t=(float)Math.clamp((r.get.getAsDouble()-r.min)/(r.max-r.min),0,1);RenderUtil.drawRoundRect(c,x+5,ry+17,(w-15)*t,1,0.5f,ClientColors.CATEGORY_ACCENT);}
   String val=r.value;float valueW=Math.min((w-18)*.55f,RenderUtil.getTextWidth(val,GFonts.MEDIUM,7));text(c,trim(r.label,w-valueW-23,7),x+6,ry+6,7,ClientColors.TEXT_PRIMARY);text(c,trim(val,valueW,7),x+w-11-valueW,ry+6,7,ClientColors.CATEGORY_ACCENT_SECOND);
   if(r.action!=null||r.slide!=null)hits.add(new Hit(x,ry,w-5,19,()->{if(r.slide!=null){drag=r;dragX=x+5;dragW=w-15;setDrag(mx);}else{editing="";capture=0;r.action.run();}}));
  }
  if(rows.size()>visible){float th=(visible*21f)*visible/rows.size();RenderUtil.drawRoundRect(c,x+w-2,top+(visible*24f-th)*scroll/Math.max(1,rows.size()-visible),2,th,1,ClientColors.SCROLLBAR);}
  String footer=capture>0?"Нажми клавишу · Delete — убрать · Esc — отмена":!editing.isEmpty()?"Ввод текста · Enter — готово · Ctrl+V — вставить":"Изменения сохраняются сразу · Колесо — прокрутка";
  text(c,trim(footer,w,6),x,y+h-9,6,ClientColors.CATEGORY_INACTIVE);
 }
 private void buildPvp(){
  Config cfg=pending!=null?pending:Core.config(feature);
  if(page==0){
   toggle("Включено",()->PvpModules.enabled(feature),()->PvpModules.enable(feature,!PvpModules.enabled(feature)));
   if(Set.of(READY_AGAIN,EFFECT_ALARM,ARMOR_PANIC,TOTEM_PULSE,OFFHAND_ALERT).contains(feature))toggle("Звуковой сигнал",()->cfg.sound,()->change(v->v.sound=!v.sound));
   if(Set.of(ARMOR_PANIC,EFFECT_ALARM,REFILL_REMINDER,TOTEM_PULSE,FIGHT_RECAP,FIGHT_TIMELINE,COMBAT_FOCUS,READY_AGAIN,INVENTORY_SEARCH_FLASH).contains(feature))slider(thresholdName(),1,feature==ARMOR_PANIC?100:120,()->cfg.threshold,v->change(a->a.threshold=(int)Math.round(v)),false);
   if(feature==OFFHAND_ALERT)action("Предмет в левой руке",new String[]{"Тотем","Щит","Золотое яблоко"}[cfg.mode],()->change(a->a.mode=(a.mode+1)%3));
   if(feature==INVENTORY_SEARCH_FLASH){action("Категория поиска",Core.searchCategory(cfg.mode),()->change(a->a.mode=(a.mode+1)%4));action("Название / ID",cfg.searchQuery.isEmpty()?"Ввести…":cfg.searchQuery,()->editing="inventory");toggle("Пока открыт инвентарь",()->cfg.searchPersistent,()->change(a->a.searchPersistent=!a.searchPersistent));}
   action("Клавиша действия",capture==1?"Нажми клавишу…":keyName(cfg.actionKey),()->capture=1);
   if(feature==HUD_ON_DEMAND)action("Удерживать HUD",capture==2?"Нажми клавишу…":keyName(cfg.holdKey),()->capture=2);
   if(feature==COMBAT_FOCUS||feature==HUD_ON_DEMAND){toggle("Скрывать scoreboard",()->cfg.hideScoreboard,()->change(a->a.hideScoreboard=!a.hideScoreboard));toggle("Скрывать bossbar",()->cfg.hideBossbar,()->change(a->a.hideBossbar=!a.hideBossbar));toggle("Скрывать эффекты",()->cfg.hideEffects,()->change(a->a.hideEffects=!a.hideEffects));toggle("Скрывать имя предмета",()->cfg.hideItemName,()->change(a->a.hideItemName=!a.hideItemName));}
   if(feature==KIT_CHECK||feature==RESUPPLY_LIST||feature==HOTBAR_MEMORY)captureRows(cfg);
   action("Описание функции","Читать",()->showReport(module.getName(),FeatureHelpScreen.wrap(FunctionDescriptions.get(module.getName(),module.getDesc()),w-20,7)));
  }else if(page==1){
   toggle("Тень текста",()->cfg.shadow,()->change(a->a.shadow=!a.shadow));toggle("Фон HUD",()->cfg.background,()->change(a->a.background=!a.background));toggle("Иконки предметов",()->cfg.icons,()->change(a->a.icons=!a.icons));slider("Размер HUD",.5,2.5,()->cfg.scale,v->change(a->a.scale=v),true);
   colorRows("Текст",false,cfg);colorRows("Акцент",true,cfg);
  }else if(page==2){toggle("Своя позиция",()->cfg.manualPosition,()->change(a->a.manualPosition=!a.manualPosition));slider("По горизонтали",0,100,()->cfg.x*100,v->change(a->{a.x=v/100;a.manualPosition=true;}),false);slider("По вертикали",0,100,()->cfg.y*100,v->change(a->{a.y=v/100;a.manualPosition=true;}),false);slider("Размер HUD",.5,2.5,()->cfg.scale,v->change(a->a.scale=v),true);action("Автопозиция","Сбросить",()->change(a->a.manualPosition=false));
  }else {
   if(feature==KIT_CHECK||feature==RESUPPLY_LIST||feature==HOTBAR_MEMORY)captureRows(cfg);
   if(feature==KIT_CHECK||feature==RESUPPLY_LIST)action("Проверить комплект","Открыть список",()->showReport("Комплект",Core.reportRows(Core.missing(cfg,feature==KIT_CHECK))));
   if(feature==FIGHT_RECAP||feature==FIGHT_TIMELINE){action("Запись боя",Core.recording()?"Завершить":"Начать",()->Core.action(feature));action("Последний бой","Открыть отчёт",()->showReport("Последний бой",lastFight()));action("Экспорт боя","Сохранить JSON",()->Core.exportFight(true));}
   if(feature==PEARL_LANDING_TIMER)action("История бросков","Открыть",()->showReport("Броски жемчуга",new ArrayList<>(Core.pearlResults)));
   if(feature==DUEL_NOTES){for(String tag:List.of("Плохой рефилл","Забыл эффект","Сломалась броня","Ошибка раскладки","Не хватило ресурсов"))action(tag,"+1 · "+cfg.notes.getOrDefault(tag,0),()->change(a->a.notes.merge(tag,1,Integer::sum)));action("Своя заметка",note.isEmpty()?"Ввести…":note,()->editing="note");action("Добавить заметку","Сохранить",()->{if(!note.isBlank()){String value=note.strip();change(a->a.notes.merge(value,1,Integer::sum));note="";}});action("Статистика заметок","Открыть",()->showReport("Заметки",cfg.notes.entrySet().stream().sorted((a,b)->Integer.compare(b.getValue(),a.getValue())).map(e->e.getKey()+": "+e.getValue()).toList()));}
   if(feature==POTION_IDENTITY)for(String s:List.of("С — сила; Б — скорость; О — огнестойкость","Л — лечение; Р — регенерация; З — замедление","Сл — слабость; Я — яд; ? — другой эффект","Цифра после буквы — уровень эффекта"))label(s,"");
   if(feature==INVENTORY_SEARCH_FLASH)action("Категория поиска",Core.searchCategory(cfg.mode),()->Core.action(feature));
   action("Настройки функции","Сбросить",()->{Config d=Config.defaults(feature);d.enabled=cfg.enabled;d.kit=cfg.kit;d.hotbar=cfg.hotbar;d.notes=cfg.notes;d.profiles=cfg.profiles;Core.save(feature,d);});
  }
 }
 private void captureRows(Config cfg){action("Сохранить текущий "+(feature==HOTBAR_MEMORY?"хотбар":"комплект"),"Сохранить",()->{if(Core.mc().player==null){Core.notice("Зайди в мир, чтобы сохранить образец");return;}change(a->Core.capture(feature,a));});action("Образец",String.valueOf((feature==HOTBAR_MEMORY?cfg.hotbar:cfg.kit).size())+" предметов",()->showReport("Сохранённый образец",(feature==HOTBAR_MEMORY?cfg.hotbar:cfg.kit).stream().map(s->s.name()+" × "+s.count()).toList()));action("Очистить образец","Очистить",()->change(a->{a.kit.clear();a.hotbar.clear();}));}
 private void colorRows(String label,boolean accent,Config cfg){for(int i=0;i<3;i++){int shift=16-i*8;String name=label+": "+new String[]{"красный","зелёный","синий"}[i];slider(name,0,255,()->((accent?cfg.accent:cfg.color)>>shift)&255,v->change(a->{int rgb=accent?a.accent:a.color;rgb=(rgb&~(255<<shift))|((int)Math.round(v)<<shift);if(accent)a.accent=rgb;else a.color=rgb;}),false);}}
 private static String keyName(int key){if(key<0)return "не назначена";String name=pyz1k.compat.LegacyInput.glfwGetKeyName(key,0);return name==null?"Key "+key:name.toUpperCase(Locale.ROOT);}
 private String thresholdName(){return switch(feature){case ARMOR_PANIC->"Прочность, %";case EFFECT_ALARM->"Осталось, сек.";case REFILL_REMINDER->"Мало в хотбаре, шт.";case TOTEM_PULSE->"Мало тотемов, шт.";case FIGHT_RECAP,FIGHT_TIMELINE,COMBAT_FOCUS->"Конец боя без урона, с";default->"Показывать, сек.";};}
 private void change(Consumer<Config> edit){Config cfg=(pending!=null?pending:Core.config(feature)).copy();edit.accept(cfg);if(drag!=null)pending=cfg;else Core.save(feature,cfg);}
 private void showReport(String title,List<String> lines){reportTitle=title;report=lines.isEmpty()?List.of("Пока нет записей"):List.copyOf(lines);scroll=0;editing="";}
 private List<String> lastFight(){List<String> out=new ArrayList<>();out.add("Противник: "+Core.lastOpponent);out.add("Длительность: "+Core.seconds(Core.lastDuration));out.add("Тотемов: "+Core.lastTotems);Core.lastUsed.forEach((k,v)->out.add(k+": "+v));for(var e:Core.lastTimeline)out.add(Core.seconds(e.ms())+"  "+e.text());return out;}
 private void buildPlaytime(){toggle("Учёт времени",ServerPlaytime::recording,()->ServerPlaytime.recording(!ServerPlaytime.recording()));label("Текущий сервер",ProgressLogic.duration(ServerPlaytime.currentMillis()));var entries=ServerPlaytime.entries().stream().filter(e->query.isBlank()||e.name().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).toList();for(var e:entries)label(e.name(),ProgressLogic.duration(e.millis()));if(entries.isEmpty())label("Зайди на сервер, чтобы начать учёт","");label("История хранится локально; AFK учитывается","");}
 private void buildAdvancements(){
  long now=System.nanoTime();if(now-refreshAt>1_000_000_000L){advancements=AdvancementData.read();refreshAt=now;}
  action("Категория",new String[]{"Все достижения","Биомы","Еда","Разведение","Убийства"}[category],()->{category=(category+1)%5;selected="";scroll=0;});
  if(!selected.isEmpty()){var e=advancements.stream().filter(a->a.id().equals(selected)).findFirst().orElse(null);action("‹ Список достижений","Назад",()->{selected="";scroll=0;});if(e==null){label("Достижение больше недоступно","");return;}var list=e.checklist();label(e.title(),list.known()?list.fulfilled()+" / "+list.groups().size():"Нет данных");action("Показывать",new String[]{"Все критерии","Только осталось","Только готово"}[state],()->{state=(state+1)%3;scroll=0;});list.names().stream().filter(id->query.isBlank()||e.criterionName(id).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).filter(id->state==0||state==1&&list.state(id)==ProgressLogic.State.REMAINING||state==2&&list.state(id)==ProgressLogic.State.DONE).sorted(Comparator.comparing(e::criterionName)).forEach(id->label(e.criterionName(id),switch(list.state(id)){case DONE->"Готово";case REMAINING->"Осталось";case ALTERNATIVE->"Альтернатива";case UNKNOWN->"Нет данных";}));
  }else {for(var e:advancements){boolean match=switch(category){case 1->e.id().endsWith("adventuring_time");case 2->e.id().endsWith("balanced_diet");case 3->e.id().endsWith("bred_all_animals");case 4->e.id().endsWith("kill_all_mobs");default->true;};if(!match||!query.isBlank()&&!e.title().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))&&e.checklist().names().stream().noneMatch(id->e.criterionName(id).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))))continue;action(e.title(),e.checklist().known()?e.checklist().fulfilled()+" / "+e.checklist().groups().size():"Нет данных",()->{selected=e.id();scroll=0;});}if(advancements.isEmpty())label("Сервер ещё не прислал доступные достижения","");}
 }
 private void label(String a,String b){rows.add(new Row(a,b,null,null,0,0,null));}
 private void action(String a,String b,Runnable run){rows.add(new Row(a,b,run,null,0,0,null));}
 private void toggle(String a,BooleanSupplier get,Runnable run){action(a,get.getAsBoolean()?"ВКЛ":"ВЫКЛ",run);}
 private void slider(String label,double min,double max,DoubleSupplier get,DoubleConsumer set,boolean decimal){rows.add(new Row(label,decimal?String.format(Locale.ROOT,"%.2f",get.getAsDouble()):String.valueOf((int)Math.round(get.getAsDouble())),null,set,min,max,get));}
 private void setDrag(double mx){if(drag!=null)drag.slide.accept(drag.min+Math.clamp((mx-dragX)/dragW,0,1)*(drag.max-drag.min));}
 public boolean click(double mx,double my,int button){if(button!=0||mx<x||mx>x+w||my<y||my>y+h)return false;for(Hit hit:List.copyOf(hits))if(hit.contains(mx,my)){hit.run.run();return true;}editing="";return true;}
 public boolean scroll(double mx,double my,double amount){if(mx<x||mx>x+w||my<y||my>y+h)return false;scroll=Math.clamp(scroll+(amount>0?-2:amount<0?2:0),0,Math.max(0,rows.size()-visible));return true;}
 public boolean drag(double mx,double my){if(drag==null)return false;setDrag(mx);return true;}
 public boolean release(){boolean handled=drag!=null;drag=null;if(pending!=null&&feature!=null){Core.save(feature,pending);pending=null;}return handled;}
 public boolean key(int key,boolean ctrl,String clipboard){if(capture>0){if(key!=256){if(key==297){Core.notice("F8 используется режимом скриншотов");return true;}int value=key==259||key==261?-1:key;change(a->{if(capture==1)a.actionKey=value;else a.holdKey=value;});}capture=0;return true;}if(editing.isEmpty())return false;if(key==257||key==256){editing="";return true;}if(key==259){String v=value();if(!v.isEmpty())setText(v.substring(0,v.offsetByCodePoints(v.length(),-1)));return true;}if(key==261){setText("");return true;}if(ctrl&&key==86){setText(value()+clipboard);return true;}return true;}
 public boolean character(int cp){if(module==null||editing.isEmpty())return false;if(!Character.isISOControl(cp)&&Character.isValidCodePoint(cp))setText(value()+new String(Character.toChars(cp)));return true;}
 private String value(){return editing.equals("inventory")?Core.config(feature).searchQuery:editing.equals("note")?note:query;}
 private void setText(String v){String text=v.replaceAll("[\\p{Cntrl}]","");text=text.substring(0,Math.min(text.length(),80));if(editing.equals("inventory")){String s=text;change(a->a.searchQuery=s);}else if(editing.equals("note"))note=text;else {query=text;scroll=0;}}
 private void button(GuiGraphicsExtractor c,String s,float bx,float by,float bw,float bh,Runnable run,boolean active){RenderUtil.drawRoundRect(c,bx,by,bw,bh,3,active?ClientColors.CATEGORY_BACKGROUND:ClientColors.SETTINGS_BACKGROUND);text(c,trim(s,bw-10,7),bx+5,by+(bh-7)/2,7,active?ClientColors.CATEGORY_ACCENT_SECOND:ClientColors.TEXT_PRIMARY);if(active)RenderUtil.drawRoundRect(c,bx+4,by+bh-2,bw-8,1,0.5f,ClientColors.CATEGORY_ACCENT);hits.add(new Hit(bx,by,bw,bh,run));}
 private static void text(GuiGraphicsExtractor c,String s,float x,float y,float size,int color){RenderUtil.drawText(c,s,x,y,color,GFonts.MEDIUM,size);}
 private static String trim(String s,float width,float size){if(width<7)return "";if(RenderUtil.getTextWidth(s,GFonts.MEDIUM,size)<=width)return s;int n=s.length();while(n>0&&RenderUtil.getTextWidth(s.substring(0,n)+"…",GFonts.MEDIUM,size)>width)n--;return s.substring(0,n)+"…";}
}
