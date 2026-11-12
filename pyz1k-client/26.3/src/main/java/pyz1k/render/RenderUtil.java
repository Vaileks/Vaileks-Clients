package pyz1k.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.font.*;
import pyz1k.utilites.color.ColorUtil;
import java.util.*;

/** Native extraction backend: works with the OpenGL and Vulkan implementations in 26.3. */
public final class RenderUtil {
 private static final Map<String,Identifier> TEXTURES=new HashMap<>();
 private static final EnumMap<GFonts,Style> FONTS=new EnumMap<>(GFonts.class);
 private static void fill(GuiGraphicsExtractor c,float x,float y,float w,float h,int color){
  if(w<=0||h<=0||(color>>>24)==0)return;
  var p=c.pose();p.pushMatrix();p.translate(x,y);p.scale(w,h);c.fill(0,0,1,1,color);p.popMatrix();
 }
 private static float inset(float r,float y){return r<=0||y>=r?0:r-(float)Math.sqrt(Math.max(0,r*r-(r-y)*(r-y)));}
 public static void drawRoundRect(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color){drawRoundRect(c,x,y,w,h,new QuadRadiusState(r),color);}
 public static void drawRoundRect(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color,float smooth){drawRoundRect(c,x,y,w,h,r,color);}
 public static void drawRoundRect(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int color){
  float a=Math.max(0,Math.min(Math.min(w,h)/2,Math.max(Math.max(r.radius1(),r.radius2()),Math.max(r.radius3(),r.radius4()))));
  if(a<.1f){fill(c,x,y,w,h,color);return;}
  fill(c,x,y+a,w,h-2*a,color);
  int steps=Math.max(1,(int)Math.ceil(a*2));float row=a/steps;
  for(int i=0;i<steps;i++){
   float mid=(i+.5f)*row;
   float l=inset(r.radius1(),mid),rr=inset(r.radius4(),mid);
   fill(c,x+l,y+i*row,w-l-rr,row,color);
   l=inset(r.radius2(),mid);rr=inset(r.radius3(),mid);
   fill(c,x+l,y+h-(i+1)*row,w-l-rr,row,color);
  }
 }
 public static void drawRoundRect(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int color,float s){drawRoundRect(c,x,y,w,h,r,color);}
 public static void drawRoundRectGradient(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int left,int right){drawRoundRectGradient(c,x,y,w,h,r,left,right,1);}
 public static void drawRoundRectGradient(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int left,int right,float s){drawRoundRectGradientQuad(c,x,y,w,h,new QuadRadiusState(r),left,left,right,right,s);}
 public static void drawRoundRectGradientQuad(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int tl,int bl,int br,int tr){drawRoundRectGradientQuad(c,x,y,w,h,new QuadRadiusState(r),tl,bl,br,tr,1);}
 public static void drawRoundRectGradientQuad(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int tl,int bl,int br,int tr,float s){
  if(w<=0||h<=0)return;
  int columns=Math.max(1,Math.min(32,(int)Math.ceil(w)));float cw=w/columns;
  for(int i=0;i<columns;i++){
   float xx=(i+.5f)*cw;
   float top=Math.max(inset(r.radius1(),xx),inset(r.radius4(),w-xx));
   float bottom=Math.max(inset(r.radius2(),xx),inset(r.radius3(),w-xx));
   float height=h-top-bottom;if(height<=0)continue;
   int upper=ColorUtil.interpolate(tl,tr,(i+.5f)/columns),lower=ColorUtil.interpolate(bl,br,(i+.5f)/columns);
   var pose=c.pose();pose.pushMatrix();pose.translate(x+i*cw,y+top);pose.scale(cw,height);
   c.fillGradient(0,0,1,1,ColorUtil.interpolate(upper,lower,top/h),ColorUtil.interpolate(upper,lower,1-bottom/h));pose.popMatrix();
  }
 }
 public static void drawBorder(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color,float t,float in,float out){drawBorder(c,x,y,w,h,new QuadRadiusState(r),color,t,in,out);}
 public static void drawBorder(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int color,float t,float in,float out){
  t=Math.max(.55f,t);float a=Math.max(Math.max(r.radius1(),r.radius2()),Math.max(r.radius3(),r.radius4()));a=Math.min(a,Math.min(w,h)/2);
  fill(c,x+a,y,w-2*a,t,color);fill(c,x+a,y+h-t,w-2*a,t,color);
  fill(c,x,y+a,t,h-2*a,color);fill(c,x+w-t,y+a,t,h-2*a,color);
  int steps=Math.max(1,(int)Math.ceil(a*2));float row=a/steps;
  for(int i=0;i<steps;i++){
   float m=(i+.5f)*row;
   for(int corner=0;corner<4;corner++){
    float radius=switch(corner){case 0->r.radius1();case 1->r.radius2();case 2->r.radius3();default->r.radius4();};
    float outer=inset(radius,m),inner=t+inset(Math.max(0,radius-t),m-t);
    if(m<t)inner=w/2;
    float strip=Math.max(t,inner-outer);
    boolean right=corner>=2,bottom=corner==1||corner==2;
    fill(c,right?x+w-outer-strip:x+outer,bottom?y+h-(i+1)*row:y+i*row,Math.min(strip,w/2-outer),row,color);
   }
  }
 }
 public static void drawBlur(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color,float s,float blur){drawRoundRect(c,x,y,w,h,r,color);}
 public static void drawGlow(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color,float s,float spread){drawGlow(c,x,y,w,h,new QuadRadiusState(r),color,s,spread);}
 public static void drawGlow(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int color,float s,float spread){
  for(int i=4;i>=1;i--){float d=Math.max(1,spread)*i/4;drawRoundRect(c,x-d,y-d,w+2*d,h+2*d,new QuadRadiusState(r.radius1()+d,r.radius2()+d,r.radius3()+d,r.radius4()+d),ColorUtil.multiplyAlpha(color,.055f));}
 }
 public static void drawShadow(GuiGraphicsExtractor c,float x,float y,float w,float h,float r,int color,float s,float spread,float ox,float oy){drawGlow(c,x+ox,y+oy,w,h,r,color,s,spread);}
 public static void drawShadow(GuiGraphicsExtractor c,float x,float y,float w,float h,QuadRadiusState r,int color,float s,float spread,float ox,float oy){drawGlow(c,x+ox,y+oy,w,h,r,color,s,spread);}
 public static void drawImage(GuiGraphicsExtractor c,GpuTextureView texture,float x,float y,float w,float h,float r,int color,float s){drawRotatedImage(c,texture,x,y,w,h,0,r,color,s);}
 public static void drawRotatedImage(GuiGraphicsExtractor c,GpuTextureView texture,float x,float y,float w,float h,float rotation,float r,int color,float s){
  if(texture==null)return;var p=c.pose();p.pushMatrix();p.translate(x+w/2,y+h/2);p.rotate((float)Math.toRadians(rotation));p.translate(-w/2,-h/2);p.scale(w,h);
  c.blit(texture,RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),0,0,1,1,0,1,0,1);p.popMatrix();
 }
 public static void drawImage(GuiGraphicsExtractor c,Identifier texture,float x,float y,float w,float h,float r,int color,float s){drawImage(c,Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView(),x,y,w,h,r,color,s);}
 public static void drawRotatedImage(GuiGraphicsExtractor c,Identifier texture,float x,float y,float w,float h,float rot,float r,int color,float s){drawRotatedImage(c,Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView(),x,y,w,h,rot,r,color,s);}
 public static void drawImage(GuiGraphicsExtractor c,String texture,float x,float y,float w,float h,float r,int color,float s){drawImage(c,TEXTURES.computeIfAbsent(texture,p->Identifier.fromNamespaceAndPath("pyz1k",p)),x,y,w,h,r,color,s);}
 public static void drawRotatedImage(GuiGraphicsExtractor c,String texture,float x,float y,float w,float h,float rot,float r,int color,float s){drawRotatedImage(c,TEXTURES.computeIfAbsent(texture,p->Identifier.fromNamespaceAndPath("pyz1k",p)),x,y,w,h,rot,r,color,s);}
 public static void drawImageRegion(GuiGraphicsExtractor c,GpuTextureView t,float x,float y,float w,float h,float u,float v,float tw,float th,float r,int color,float s){
  if(t==null)return;var p=c.pose();p.pushMatrix();p.translate(x,y);p.scale(w,h);c.blit(t,RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),0,0,1,1,u/tw,(u+w)/tw,v/th,(v+h)/th);p.popMatrix();
 }
 public static void drawHeadImageRegion(GuiGraphicsExtractor c,GpuTextureView t,float x,float y,float w,float h,float u,float v,float tw,float th,float r,int color,float s){drawImageRegion(c,t,x,y,w,h,u,v,tw,th,r,color,s);}
 private static Style style(GFonts f){return FONTS.computeIfAbsent(f,k->Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("pyz1k",k.name().toLowerCase(Locale.ROOT)))));}
 private static final EnumMap<GFonts,Map<String,Component>> LABELS=new EnumMap<>(GFonts.class);
 private static Component component(String text,GFonts f){
  var cache=LABELS.computeIfAbsent(f,k->new LinkedHashMap<>(128,.75f,true){protected boolean removeEldestEntry(Map.Entry<String,Component> e){return size()>512;}});
  return cache.computeIfAbsent(text==null?"":text,t->Component.literal(t).withStyle(style(f)));
 }
 public static void drawText(GuiGraphicsExtractor c,String text,float x,float y,int color,GFonts f,int size){drawText(c,text,x,y,color,f,(float)size);}
 public static void drawText(GuiGraphicsExtractor c,String text,float x,float y,int color,GFonts f,float size){
  if(text==null||text.isEmpty()||size<=0)return;var p=c.pose();p.pushMatrix();p.translate(x,y);p.scale(size/9,size/9);c.text(Minecraft.getInstance().font,component(text,f),0,0,color,false);p.popMatrix();
 }
 public static void drawText(GuiGraphicsExtractor c,Component text,float x,float y,int color,GFonts f,int size){drawText(c,text.getString(),x,y,color,f,size);}
 public static void drawTextFadeRight(GuiGraphicsExtractor c,String text,float x,float y,int color,GFonts f,int size,float w,float fade){drawTextFadeRight(c,text,x,y,color,f,(float)size,w,fade);}
 public static void drawTextFadeRight(GuiGraphicsExtractor c,String text,float x,float y,int color,GFonts f,float size,float w,float fade){pushScissor(c,x,y,w,size+3);drawText(c,text,x,y,color,f,size);popScissor();}
 public static float getTextWidth(String text,GFonts f,int size){return getTextWidth(text,f,(float)size);}
 public static float getTextWidth(String text,GFonts f,float size){return Minecraft.getInstance().font.width(component(text,f))*size/9;}
 public static float getTextWidth(Component text,GFonts f,int size){return getTextWidth(text.getString(),f,size);}
 public static float getTextHeight(GFonts f,int size){return size;}
 public static float getTextHeight(GFonts f,float size){return size;}
 public static float getTextHeight(String t,GFonts f,int size){return size;}
 public static float getTextHeight(String t,GFonts f,float size){return size;}
 public static float getTextHeight(Component t,GFonts f,int size){return size;}
 public static void drawIcon(GuiGraphicsExtractor c,GIcons icon,float x,float y,float size,int color){if(icon!=null)drawImage(c,"textures/gui/icons/"+icon.name().toLowerCase(Locale.ROOT)+".png",x,y,size,size,0,color,1);}
 public static void drawIconGlyph(GuiGraphicsExtractor c,GIcons icon,float x,float y,float size,int color){if(icon!=null)drawText(c,String.valueOf(icon.glyph()),x,y,color,GFonts.NOTIFICATION_ICONS,size);}
 public static float getIconGlyphWidth(GIcons icon,float size){return icon==null?0:size;}
 public static void drawCircle(GuiGraphicsExtractor c,float x,float y,float diameter,int color){drawCircle(c,x,y,diameter,color,1);}
 public static void drawCircle(GuiGraphicsExtractor c,float x,float y,float diameter,int color,float smooth){drawRoundRect(c,x,y,diameter,diameter,diameter/2,color);}
 public static void drawRingProgress(GuiGraphicsExtractor c,float x,float y,float diameter,float progress,float thickness,int color,float smooth){
  int n=Math.max(1,Math.min(64,(int)(64*Math.max(0,Math.min(1,progress)))));float r=(diameter-thickness)/2;
  for(int i=0;i<n;i++){double a=-Math.PI/2+2*Math.PI*i/64;drawCircle(c,x+diameter/2+(float)Math.cos(a)*r-thickness/2,y+diameter/2+(float)Math.sin(a)*r-thickness/2,thickness,color);}
 }
 public static void pushScissor(GuiGraphicsExtractor c,float x,float y,float w,float h){ScissorUtil.push(c,x,y,w,h);}
 public static void popScissor(){ScissorUtil.pop();}
 public static void clearScissor(){ScissorUtil.clear();}
}
