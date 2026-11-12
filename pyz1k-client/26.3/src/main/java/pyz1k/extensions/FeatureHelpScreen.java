package pyz1k.extensions;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.utilites.color.ClientColors;

/** Explanations are laid out only when this screen opens, resizes or selects a topic. */
public final class FeatureHelpScreen extends Screen {
 private final Screen parent;
 private final List<FunctionDescriptions.Entry> entries=FunctionDescriptions.all();
 private List<String> body=List.of(),titles=List.of();
 private int selected,listOffset,bodyOffset;
 private float side,right,bodyWidth;
 public FeatureHelpScreen(Screen parent){super(Component.literal("Описание функций"));this.parent=parent;
  for(int i=0;i<entries.size();i++)if(entries.get(i).id().equals(FunctionDescriptions.hovered)){selected=i;break;}
 }
 @Override protected void init(){
  side=Math.min(155,Math.max(100,width*.33f));right=side+20;bodyWidth=Math.max(35,width-right-20);
  var names=new ArrayList<String>();for(var e:entries)names.add(trim(e.title(),side-18,7));titles=List.copyOf(names);
  choose(selected);listOffset=Math.clamp(selected-rows()/2,0,Math.max(0,entries.size()-rows()));
 }
 private int rows(){return Math.max(1,(height-82)/17);}
 private int bodyRows(){return Math.max(1,(height-96)/12);}
 public void choose(int index){selected=Math.clamp(index,0,entries.size()-1);if(selected<listOffset)listOffset=selected;else if(selected>=listOffset+rows())listOffset=selected-rows()+1;bodyOffset=0;body=wrap(entries.get(selected).text(),bodyWidth,8);}
 public static List<String> wrap(String text,float width,float size){
  var lines=new ArrayList<String>();String line="";
  for(String word:text.split("\\s+")){String next=line.isEmpty()?word:line+" "+word;
   if(!line.isEmpty()&&RenderUtil.getTextWidth(next,GFonts.MEDIUM,size)>width){lines.add(line);line=word;}else line=next;
  }if(!line.isEmpty())lines.add(line);return List.copyOf(lines);
 }
 private static String trim(String s,float width,float size){
  if(RenderUtil.getTextWidth(s,GFonts.MEDIUM,size)<=width)return s;
  while(!s.isEmpty()&&RenderUtil.getTextWidth(s+"…",GFonts.MEDIUM,size)>width)s=s.substring(0,s.length()-1);return s+"…";
 }
 @Override public void extractRenderState(GuiGraphicsExtractor ctx,int mx,int my,float delta){
  ctx.fill(0,0,width,height,0xf511141b);
  RenderUtil.drawText(ctx,"Справка pyz1k-client",12,12,ClientColors.TEXT_PRIMARY,GFonts.SEMIBOLD,11);
  RenderUtil.drawText(ctx,"Выбери функцию слева. Колесо прокручивает список или текст.",12,29,ClientColors.CATEGORY_INACTIVE,GFonts.MEDIUM,6);
  for(int i=listOffset;i<Math.min(entries.size(),listOffset+rows());i++){
   float y=47+(i-listOffset)*17;boolean active=i==selected;
   RenderUtil.drawRoundRect(ctx,8,y,side,16,3,active?ClientColors.CATEGORY_ACCENT:ClientColors.SETTINGS_BACKGROUND);
   RenderUtil.drawText(ctx,titles.get(i),13,y+4,active?0xffffffff:ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,7);
  }
  RenderUtil.drawText(ctx,trim(entries.get(selected).title(),bodyWidth,10),right,48,ClientColors.CATEGORY_ACCENT_SECOND,GFonts.SEMIBOLD,10);
  ctx.enableScissor((int)right,64,width-10,height-30);
  for(int i=bodyOffset;i<Math.min(body.size(),bodyOffset+bodyRows());i++)RenderUtil.drawText(ctx,body.get(i),right,68+(i-bodyOffset)*12,ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,8);
  ctx.disableScissor();
  if(body.size()>bodyRows())RenderUtil.drawText(ctx,"Текст: "+(bodyOffset+1)+"–"+Math.min(body.size(),bodyOffset+bodyRows())+" / "+body.size(),right,height-40,ClientColors.CATEGORY_INACTIVE,GFonts.MEDIUM,6);
  RenderUtil.drawRoundRect(ctx,8,height-25,75,19,3,ClientColors.SETTINGS_BACKGROUND);
  RenderUtil.drawText(ctx,"Назад · Esc",14,height-19,ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,7);
  RenderUtil.drawText(ctx,(selected+1)+" / "+entries.size()+" · ↑↓ выбор · Page Up/Down текст",94,height-19,ClientColors.CATEGORY_INACTIVE,GFonts.MEDIUM,6);
 }
 @Override public boolean mouseClicked(MouseButtonEvent c,boolean twice){
  if(pyz1k.compat.LegacyInput.fromNativeMouse(c.button())==0){if(c.y()>=height-25&&c.x()>=8&&c.x()<=83){onClose();return true;}
   if(c.x()>=8&&c.x()<=side+8&&c.y()>=47){int index=listOffset+(int)((c.y()-47)/17);if(index<entries.size()&&index<listOffset+rows()){choose(index);return true;}}
  }return super.mouseClicked(c,twice);
 }
 @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
  int steps=(int)-Math.signum(vertical)*3;
  if(mx<right)listOffset=Math.clamp(listOffset+steps,0,Math.max(0,entries.size()-rows()));else bodyOffset=Math.clamp(bodyOffset+steps,0,Math.max(0,body.size()-bodyRows()));return true;
 }
 @Override public boolean keyPressed(KeyEvent key){
  int code=pyz1k.compat.LegacyInput.fromNativeKey(key.key());
  if(code==264||code==265){choose(selected+(code==264?1:-1));listOffset=Math.clamp(selected-rows()/2,0,Math.max(0,entries.size()-rows()));return true;}
  if(code==266||code==267){bodyOffset=Math.clamp(bodyOffset+(code==267?1:-1)*bodyRows(),0,Math.max(0,body.size()-bodyRows()));return true;}
  if(code==256||code==290){onClose();return true;}return super.keyPressed(key);
 }
 @Override public void onClose(){Minecraft.getInstance().gui.setScreen(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
