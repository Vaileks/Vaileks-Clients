package pyz1k.extensions.pvp;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.*;
public final class HubScreen extends Screen {
 final Screen parent;int page;
 public HubScreen(Screen p){super(Text.literal("PvP Mods"));parent=p;}
 protected void init(){
  List<Feature> features=new ArrayList<>(Core.roots.keySet());int capacity=Math.max(1,(height-90)/24),pages=Math.max(1,(features.size()+capacity-1)/capacity);page=Math.min(page,pages-1);
  for(int i=page*capacity;i<Math.min(features.size(),(page+1)*capacity);i++){Feature f=features.get(i);addDrawableChild(ButtonWidget.builder(Text.literal(f.title+"  ·  "+(Core.on(f)?"Вкл.":"Выкл.")),b->client.setScreen(new SettingsScreen(this,f))).dimensions(width/2-160,48+(i-page*capacity)*24,320,20).build());}
  addDrawableChild(ButtonWidget.builder(Text.literal("←"),b->{page=Math.floorMod(page-1,pages);clearAndInit();}).dimensions(width/2-120,height-29,40,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Готово"),b->close()).dimensions(width/2-70,height-29,140,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("→"),b->{page=(page+1)%pages;clearAndInit();}).dimensions(width/2+80,height-29,40,20).build());
 }
 public void render(DrawContext c,int x,int y,float d){c.fill(0,0,width,height,0xEF181B22);super.render(c,x,y,d);c.drawCenteredTextWithShadow(textRenderer,title,width/2,14,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,"PvPTools · настройки модулей клиента",width/2,29,0xFF78DCFF);}
 public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
}

