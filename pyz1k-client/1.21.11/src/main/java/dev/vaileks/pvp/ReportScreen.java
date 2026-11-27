package pyz1k.extensions.pvp;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import java.util.*;
import org.lwjgl.glfw.GLFW;
public final class ReportScreen extends Screen {
 final Screen parent;final List<String> lines;int offset;
 public ReportScreen(Screen parent,String title,List<String> lines){super(Text.literal(title));this.parent=parent;this.lines=List.copyOf(lines);}
 protected void init(){addDrawableChild(ButtonWidget.builder(Text.literal("Назад"),b->close()).dimensions(width/2-50,height-28,100,20).build());}
 int visible(){return Math.max(1,(height-90)/14);}void scroll(int dy){offset=Math.clamp(offset+dy,0,Math.max(0,lines.size()-visible()));}
 public void render(DrawContext c,int x,int y,float d){c.fill(0,0,width,height,0xF0181B22);super.render(c,x,y,d);c.drawCenteredTextWithShadow(textRenderer,title,width/2,14,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,"Колесо / ↑↓ · "+lines.size()+" строк",width/2,29,0xFF78DCFF);c.enableScissor(12,45,width-12,height-36);for(int i=offset;i<Math.min(lines.size(),offset+visible());i++)c.drawTextWithShadow(textRenderer,lines.get(i),18,48+(i-offset)*14,0xFFE3EAF5);c.disableScissor();}
 public boolean mouseScrolled(double x,double y,double h,double v){scroll((int)-Math.signum(v)*3);return true;}
 public boolean keyPressed(KeyInput k){if(k.key()==GLFW.GLFW_KEY_DOWN){scroll(1);return true;}if(k.key()==GLFW.GLFW_KEY_UP){scroll(-1);return true;}return super.keyPressed(k);}
 public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
}

