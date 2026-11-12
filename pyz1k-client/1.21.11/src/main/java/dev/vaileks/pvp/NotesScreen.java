package pyz1k.extensions.pvp;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import java.util.*;
public final class NotesScreen extends Screen {
 final Screen parent;TextFieldWidget custom;
 public NotesScreen(Screen p){super(Text.literal("Duel Notes"));parent=p;}
 protected void init(){
  Config cfg=Core.config(Feature.DUEL_NOTES);String[] tags={"Плохой рефилл","Забыл эффект","Сломалась броня","Ошибка раскладки","Не хватило ресурсов"};
  for(int i=0;i<tags.length;i++){String tag=tags[i];addDrawableChild(ButtonWidget.builder(Text.literal(tag+" · "+cfg.notes.getOrDefault(tag,0)),b->note(tag)).dimensions(width/2-150,40+i*23,300,20).build());}
  custom=addDrawableChild(new TextFieldWidget(textRenderer,width/2-150,158,220,20,Text.literal("Своя заметка")));custom.setMaxLength(80);
  addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"),b->{if(!custom.getText().isBlank())note(custom.getText().trim());}).dimensions(width/2+75,158,75,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Статистика"),b->{List<String> lines=new ArrayList<>();cfg.notes.entrySet().stream().sorted((a,v)->Integer.compare(v.getValue(),a.getValue())).forEach(e->lines.add(e.getKey()+": "+e.getValue()));client.setScreen(new ReportScreen(this,"Повторяющиеся ошибки",lines));}).dimensions(width/2-150,185,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Назад"),b->close()).dimensions(width/2+5,185,145,20).build());
 }
 void note(String tag){Config cfg=Core.config(Feature.DUEL_NOTES);cfg.notes.merge(tag,1,Integer::sum);Core.save(Feature.DUEL_NOTES,cfg);Core.notice("Заметка сохранена: "+tag);clearAndInit();}
 public void render(DrawContext c,int x,int y,float d){c.fill(0,0,width,height,0xF0181B22);super.render(c,x,y,d);c.drawCenteredTextWithShadow(textRenderer,title,width/2,15,0xFFFFFFFF);}
 public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
}

