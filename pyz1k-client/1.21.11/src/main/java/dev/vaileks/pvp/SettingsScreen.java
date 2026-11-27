package pyz1k.extensions.pvp;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.function.*;
import static pyz1k.extensions.pvp.Feature.*;

public final class SettingsScreen extends Screen {
 final Screen parent;final Feature feature;Config draft;int page=0,colorTarget=0,captureKey=0;boolean dragging;double dragX,dragY;int[] bounds;
 public SettingsScreen(Screen parent,Feature feature){super(Text.literal(feature.title));this.parent=parent;this.feature=feature;draft=Core.config(feature).copy();}
 void refresh(){clearAndInit();}
 String yes(boolean b){return b?"Да":"Нет";}
 int cw(){return Math.min(170,(width-24)/2-4);}
 int left(){return width/2-cw()-4;}
 void button(int col,int row,String name,Runnable fn){addDrawableChild(ButtonWidget.builder(Text.literal(name),b->{fn.run();}).dimensions(left()+col*(cw()+8),56+row*24,cw(),20).build());}
 void toggle(int col,int row,String label,BooleanSupplier get,Runnable change){button(col,row,label+": "+yes(get.getAsBoolean()),()->{change.run();refresh();});}
 void slider(int col,int row,String name,double min,double max,DoubleSupplier get,DoubleConsumer set,boolean decimal){
  addDrawableChild(new SliderWidget(left()+col*(cw()+8),56+row*24,cw(),20,Text.empty(),(get.getAsDouble()-min)/(max-min)){
   {updateMessage();}
   protected void updateMessage(){setMessage(Text.literal(name+": "+(decimal?String.format(Locale.ROOT,"%.2f",min+value*(max-min)):String.valueOf((int)Math.round(min+value*(max-min))))));}
   protected void applyValue(){set.accept(decimal?min+value*(max-min):Math.round(min+value*(max-min)));updateMessage();}
  });
 }
 protected void init(){
  int tw=Math.min(110,(width-24)/3-7);String[] tabs={"Внешний вид","Функции","Позиция HUD"};
  for(int i=0;i<3;i++){int n=i;var b=ButtonWidget.builder(Text.literal(tabs[i]),v->{page=n;captureKey=0;refresh();}).dimensions(width/2-(tw*3+14)/2+i*(tw+7),28,tw,20).build();b.active=page!=i;addDrawableChild(b);}
  if(page==0){
   toggle(0,0,"Включено",()->draft.enabled,()->draft.enabled=!draft.enabled);
   toggle(1,0,"Тень текста",()->draft.shadow,()->draft.shadow=!draft.shadow);
   toggle(0,1,"Фон HUD",()->draft.background,()->draft.background=!draft.background);
   toggle(1,1,"Иконки предметов",()->draft.icons,()->draft.icons=!draft.icons);
   slider(0,2,"Размер HUD",0.5,2.5,()->draft.scale,v->draft.scale=v,true);
   button(1,2,"Цвет текста",()->{colorTarget=0;page=3;refresh();});
   button(0,3,"Цвет акцента / тревоги",()->{colorTarget=1;page=3;refresh();});
   button(1,3,"Позиция HUD",()->{page=2;refresh();});
  }else if(page==1){
   if(feature==COMBAT_FOCUS||feature==HUD_ON_DEMAND)toggle(0,0,"Скрывать scoreboard",()->draft.hideScoreboard,()->draft.hideScoreboard=!draft.hideScoreboard);
   else if(Set.of(READY_AGAIN,EFFECT_ALARM,ARMOR_PANIC,TOTEM_PULSE,OFFHAND_ALERT).contains(feature))toggle(0,0,"Звуковой сигнал",()->draft.sound,()->draft.sound=!draft.sound);
   button(1,0,"Действие: "+keyName(draft.actionKey),()->{captureKey=1;refresh();});
   if(Set.of(ARMOR_PANIC,EFFECT_ALARM,REFILL_REMINDER,TOTEM_PULSE,FIGHT_RECAP,FIGHT_TIMELINE,COMBAT_FOCUS,READY_AGAIN,INVENTORY_SEARCH_FLASH).contains(feature))slider(0,1,thresholdName(),1,feature==ARMOR_PANIC?100:120,()->draft.threshold,v->draft.threshold=(int)v,false);
   button(1,1,modeName(),()->{draft.mode=(draft.mode+1)%(feature==INVENTORY_SEARCH_FLASH?4:3);if(feature!=OFFHAND_ALERT&&feature!=INVENTORY_SEARCH_FLASH){draft.x=draft.mode/2.0;draft.manualPosition=true;}refresh();});
   if(feature==INVENTORY_SEARCH_FLASH){
    var query=addDrawableChild(new TextFieldWidget(textRenderer,left(),104,cw()*2+8,20,Text.literal("Название или ID предмета")));
    query.setMaxLength(80);query.setPlaceholder(Text.literal("По названию: слова или minecraft:item"));query.setText(draft.searchQuery);query.setChangedListener(v->draft.searchQuery=v);
    toggle(1,3,"Пока открыт инвентарь",()->draft.searchPersistent,()->draft.searchPersistent=!draft.searchPersistent);
   }
   if(feature==HUD_ON_DEMAND)button(0,2,"Удерживать: "+keyName(draft.holdKey),()->{captureKey=2;refresh();});
   if(feature==COMBAT_FOCUS||feature==HUD_ON_DEMAND){
    toggle(1,2,"Скрывать bossbar",()->draft.hideBossbar,()->draft.hideBossbar=!draft.hideBossbar);
    toggle(1,3,"Скрывать эффекты",()->draft.hideEffects,()->draft.hideEffects=!draft.hideEffects);
    toggle(0,4,"Скрывать имя предмета",()->draft.hideItemName,()->draft.hideItemName=!draft.hideItemName);
   }
   if(feature==KIT_CHECK||feature==RESUPPLY_LIST||feature==HOTBAR_MEMORY){
    button(0,2,"Сохранить текущий "+(feature==HOTBAR_MEMORY?"хотбар":"комплект"),()->{Core.capture(feature,draft);refresh();});
    button(1,2,"Очистить образец",()->{draft.kit.clear();draft.hotbar.clear();refresh();});
   }
   button(0,3,actionName(),()->{Core.save(feature,draft.copy());Core.action(feature);draft=Core.config(feature).copy();});
   if(feature==FIGHT_RECAP||feature==FIGHT_TIMELINE){
    button(1,3,"Открыть последний отчёт",()->client.setScreen(new ReportScreen(this,"Последний бой",lastFight())));
    button(0,4,"Экспорт последнего боя",()->Core.exportFight(true));
   }
   if(feature==RESUPPLY_LIST||feature==KIT_CHECK)button(1,3,"Показать весь список",()->{if(client.player!=null)client.setScreen(new ReportScreen(this,feature.title,Core.reportRows(Core.missing(draft,feature==KIT_CHECK))));});
   if(feature==POTION_IDENTITY)button(1,3,"Обозначения зелий",()->client.setScreen(new ReportScreen(this,"Обозначения",List.of("С — сила; Б — скорость; О — огнестойкость","Л — лечение; Р — регенерация; З — замедление","Сл — слабость; Я — яд; ? — другой эффект","Цифра после буквы — уровень эффекта"))));
   if(feature==SERVER_PROFILES)button(1,3,"Все профили",()->client.setScreen(new ReportScreen(this,"Профили",profileLines())));
  }else if(page==2){
   addDrawableChild(ButtonWidget.builder(Text.literal("Автопозиция"),b->{draft.manualPosition=false;refresh();}).dimensions(width/2-65,height-54,130,20).build());
   // The whole play area is reserved for the live draggable preview.
  }else if(page==3){
   button(0,0,colorTarget==0?"Цвет текста":"Цвет акцента",()->{colorTarget=1-colorTarget;refresh();});
   for(int i=0;i<3;i++){final int shift=16-i*8;String label=new String[]{"Красный","Зелёный","Синий"}[i];slider(0,i+1,label,0,255,()->((colorTarget==0?draft.color:draft.accent)>>shift)&255,v->{int color=colorTarget==0?draft.color:draft.accent;color=(color&~(255<<shift))|((int)v<<shift);if(colorTarget==0)draft.color=color;else draft.accent=color;},false);}
   button(1,0,"Белый",()->{setColor(0xFFFFFF);refresh();});button(1,1,"Голубой",()->{setColor(0x78DCFF);refresh();});button(1,2,"Красный",()->{setColor(0xFF6577);refresh();});button(1,3,"Зелёный",()->{setColor(0x70E6A0);refresh();});
  }
  int fy=height-28;
  addDrawableChild(ButtonWidget.builder(Text.literal("Сброс"),b->{Config def=Config.defaults(feature);def.kit=draft.kit;def.hotbar=draft.hotbar;def.notes=draft.notes;def.profiles=draft.profiles;draft=def;refresh();}).dimensions(width/2-111,fy,70,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Отмена"),b->close()).dimensions(width/2-35,fy,70,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Готово"),b->{Core.save(feature,draft);close();}).dimensions(width/2+41,fy,70,20).build());
 }
 void setColor(int color){if(colorTarget==0)draft.color=color;else draft.accent=color;}
 static String keyName(int key){if(key<0)return "не назначено";String name=GLFW.glfwGetKeyName(key,0);return name==null?(key==GLFW.GLFW_KEY_LEFT_ALT?"Left Alt":key==GLFW.GLFW_KEY_RIGHT_ALT?"Right Alt":"Key "+key):name.toUpperCase(Locale.ROOT);}
 String thresholdName(){return switch(feature){case ARMOR_PANIC->"Прочность, %";case EFFECT_ALARM->"Осталось, сек.";case REFILL_REMINDER->"Мало в хотбаре, шт.";case TOTEM_PULSE->"Мало тотемов, шт.";case FIGHT_RECAP,FIGHT_TIMELINE,COMBAT_FOCUS->"Конец боя без урона, с";case READY_AGAIN,INVENTORY_SEARCH_FLASH->"Показывать, сек.";default->"Порог (для тревог)";};}
 String modeName(){return switch(feature){case OFFHAND_ALERT->"Ожидается: "+new String[]{"Тотем","Щит","Золотое яблоко"}[draft.mode];case INVENTORY_SEARCH_FLASH->"Поиск: "+Core.searchCategory(draft.mode);default->"Позиция: "+new String[]{"Слева","По центру","Справа"}[draft.mode];};}
 String actionName(){return switch(feature){case FIGHT_RECAP,FIGHT_TIMELINE->"Начать / закончить бой";case KIT_CHECK->"Проверить комплект";case RESUPPLY_LIST->"Показать пополнение";case DUEL_NOTES->"Добавить заметку";case INVENTORY_SEARCH_FLASH->"Сменить категорию";case PEARL_LANDING_TIMER->"История бросков";default->"Сохранить и переключить";};}
 List<String> lastFight(){List<String> out=new ArrayList<>();out.add("Противник: "+Core.lastOpponent);out.add("Длительность: "+Core.seconds(Core.lastDuration));out.add("Тотемов: "+Core.lastTotems);for(var e:Core.lastUsed.entrySet())out.add(e.getKey()+": "+e.getValue());for(var e:Core.lastTimeline)out.add(Core.seconds(e.ms())+"  "+e.text());return out;}
 List<String> profileLines(){List<String> out=new ArrayList<>();out.add("Активный: "+Core.context);for(Feature f:Core.roots.keySet())for(String name:Core.roots.get(f).profiles.keySet())out.add(f.title+" · "+name);return out;}
 String help(){return switch(feature){
  case KIT_CHECK,RESUPPLY_LIST->"Сначала сохраните свой комплект. Образец сохранится по кнопке «Готово».";
  case HOTBAR_MEMORY->"Сохраняет типы предметов по слотам. Количество и износ не учитываются.";
  case HUD_ON_DEMAND->"Панели PvP-модов и выбранные элементы Minecraft видны по удержанию. Тревоги остаются.";
  case COMBAT_FOCUS->"В бою скрывает выбранные элементы Minecraft и второстепенные панели PvP-модов.";
  case SERVER_PROFILES->"Настройки этих PvP-модов автоматически разделяются по серверам и мирам.";
  case PEARL_LANDING_TIMER->"Точное время в тиках измеряется при столкновении жемчуга в одиночном мире.";
  case FIGHT_RECAP,FIGHT_TIMELINE->"Автостарт при PvP-уроне. Клавиша действия начинает или завершает запись.";
  case DUEL_NOTES->"Клавиша действия открывает заметки. Каждая отметка сохраняется сразу.";
  case POTION_IDENTITY->"Буква эффекта и его уровень выводятся в слотах вашего инвентаря и хотбара.";
  default->feature.description;
 };}
 public void render(DrawContext c,int mx,int my,float delta){
  // One background pass; no vanilla blur to conflict with HUD previews.
  c.fill(0,0,width,height,0xE8181B22);
  c.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);
  super.render(c,mx,my,delta);
  if(page==2){
   c.drawCenteredTextWithShadow(textRenderer,"Перетащите HUD. Колесо — размер.",width/2,57,0xFFD7D7D7);
   bounds=Core.panel(c,feature,draft,Core.preview(feature),true);
  }else{
   if(page!=3&&height>=280){int y=page==1?183:157;c.drawWrappedTextWithShadow(textRenderer,Text.literal(help()),left(),y,cw()*2+8,0xFFBAC5D9);}
   if(height>=330||(height>=235&&(page==0||page==3))){Config sample=draft.copy();sample.x=0.5;sample.y=1;
   // Keep preview above footer even for a long live panel.
   List<Core.Row> sampleRows=Core.preview(feature);sampleRows=sampleRows.subList(0,Math.min(sampleRows.size(),height>=330?2:1));
   c.getMatrices().pushMatrix();c.getMatrices().translate(0,-40);Core.panel(c,feature,sample,sampleRows,true);c.getMatrices().popMatrix();}
  }
  if(captureKey>0){c.fill(width/2-175,height/2-24,width/2+175,height/2+24,0xFF293242);c.drawCenteredTextWithShadow(textRenderer,"Нажмите клавишу · Delete — убрать · Esc — отмена",width/2,height/2-4,0xFFFFFFFF);}
 }
 public boolean mouseClicked(Click click,boolean twice){
  if(page==2&&click.button()==0&&bounds!=null&&click.x()>=bounds[0]&&click.x()<=bounds[0]+bounds[2]&&click.y()>=bounds[1]&&click.y()<=bounds[1]+bounds[3]){dragging=true;dragX=click.x()-bounds[0];dragY=click.y()-bounds[1];return true;}return super.mouseClicked(click,twice);
 }
 public boolean mouseDragged(Click click,double dx,double dy){if(dragging&&bounds!=null){draft.manualPosition=true;draft.x=Math.clamp((click.x()-dragX)/Math.max(1,width-bounds[2]),0,1);draft.y=Math.clamp((click.y()-dragY)/Math.max(1,height-bounds[3]),0,1);return true;}return super.mouseDragged(click,dx,dy);}
 public boolean mouseReleased(Click click){dragging=false;return super.mouseReleased(click);}
 public boolean mouseScrolled(double x,double y,double h,double v){if(page==2){draft.scale=Math.clamp(draft.scale+v*0.05,0.5,2.5);return true;}return super.mouseScrolled(x,y,h,v);}
 public boolean keyPressed(KeyInput input){if(captureKey>0){if(input.key()!=GLFW.GLFW_KEY_ESCAPE){int key=input.key()==GLFW.GLFW_KEY_DELETE||input.key()==GLFW.GLFW_KEY_BACKSPACE?-1:input.key();if(key==GLFW.GLFW_KEY_F8){Core.notice("F8 включает режим скриншотов");return true;}if(captureKey==1)draft.actionKey=key;else draft.holdKey=key;}captureKey=0;refresh();return true;}return super.keyPressed(input);}
 public void close(){client.setScreen(parent);}
 public boolean shouldPause(){return false;}
}

