package pyz1k.extensions.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.module.base.Module;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.CategoryBarSection;
import pyz1k.screen.menu.sections.ModuleGridSection;
import pyz1k.extensions.*;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import java.util.*;
@Mixin(value=ModuleGridSection.class,remap=false)
public abstract class ModuleHelpMixin {
 @Shadow private boolean bindPopupOpen;
 @Shadow private boolean colorPopupOpen;
 @Unique private int help$mx,help$my;
 @Unique private Module help$candidate,help$previous;
 @Unique private long help$since;
 @Unique private List<String> help$lines=List.of();
 @Unique private float help$width;
 @Inject(method="render",at=@At("HEAD"),remap=false)
 private void help$start(GuiGraphicsExtractor c,UiAnimationEngine a,float x,float y,float w,float h,int mx,int my,float alpha,CategoryBarSection.TopTab tab,CallbackInfo ci){help$mx=mx;help$my=my;help$candidate=null;}
 @Inject(method="renderCompactRow",at=@At("TAIL"),remap=false)
 private void help$hover(GuiGraphicsExtractor c,UiAnimationEngine a,Module module,float x,float y,float w,float h,int slot,boolean animate,float alpha,CallbackInfo ci){if(alpha>.8f&&help$mx>=x&&help$mx<x+w&&help$my>=y&&help$my<y+h)help$candidate=module;}
 @Inject(method="render",at=@At("TAIL"),remap=false)
 private void help$show(GuiGraphicsExtractor c,UiAnimationEngine a,float px,float py,float pw,float ph,int mx,int my,float alpha,CategoryBarSection.TopTab tab,CallbackInfo ci){
  long now=System.nanoTime();
  if(help$candidate!=help$previous){help$previous=help$candidate;help$since=now;help$lines=List.of();}
  if(help$candidate==null||bindPopupOpen||colorPopupOpen)return;
  FunctionDescriptions.hovered=help$candidate.getName();
  if(now-help$since<450000000L)return;
  var window=Minecraft.getInstance().getWindow();
  float width=Math.min(250,window.getGuiScaledWidth()-20);
  if(help$lines.isEmpty()||help$width!=width){help$width=width;help$lines=FeatureHelpScreen.wrap(help$candidate.getDesc(),width-16,7);}
  int visible=Math.min(help$lines.size(),Math.max(1,(window.getGuiScaledHeight()-65)/10));
  float height=34+visible*10,x=Math.max(5,Math.min(mx+10,window.getGuiScaledWidth()-width-5)),y=Math.max(5,Math.min(my+15,window.getGuiScaledHeight()-height-5));
  RenderUtil.drawRoundRect(c,x,y,width,height,4,0xf219202c);
  RenderUtil.drawText(c,help$candidate.getName(),x+8,y+7,0xffa5d5ff,GFonts.SEMIBOLD,8);
  for(int i=0;i<visible;i++)RenderUtil.drawText(c,help$lines.get(i),x+8,y+22+i*10,0xffeef1f7,GFonts.MEDIUM,7);
  RenderUtil.drawText(c,"F1 — открыть справку",x+8,y+height-10,0xffaab7cb,GFonts.MEDIUM,6);
 }
}
