package pyz1k.render;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.*;
/** Scissors are captured with the extraction state; no direct OpenGL calls. */
public final class ScissorUtil {
 private static final Deque<GuiGraphicsExtractor> STACK=new ArrayDeque<>();
 public static void push(GuiGraphicsExtractor c,float x,float y,float w,float h){c.enableScissor((int)Math.floor(x),(int)Math.floor(y),(int)Math.ceil(x+w),(int)Math.ceil(y+h));STACK.push(c);}
 public static void pop(){if(!STACK.isEmpty())STACK.pop().disableScissor();}
 public static void clear(){while(!STACK.isEmpty())pop();}
 public static boolean isEnabled(){return !STACK.isEmpty();}
}
