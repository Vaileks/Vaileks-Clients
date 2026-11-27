package pyz1k.extensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.utilites.color.ClientColors;
/** Independently implemented behavior of the supplied indicator, using vanilla item icons. */
public final class BlastProtectionHud extends AbstractHudElement {
 private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 private static int level,count;private static ItemStack icon=ItemStack.EMPTY;
 public BlastProtectionHud(){super("blast_protection_indicator","BlastProtectionIndicator",8,205,174,29);}
 public static void tick(){
  var m=FeatureModules.blast;var mc=Minecraft.getInstance();if(m==null||!m.isEnable()||mc.player==null){level=0;count=0;icon=ItemStack.EMPTY;return;}
  level=0;count=0;icon=ItemStack.EMPTY;
  for(var slot:ARMOR){
   if(m.leggingsOnly.isValue()&&slot!=EquipmentSlot.LEGS)continue;
   ItemStack stack=mc.player.getItemBySlot(slot);for(var entry:stack.getEnchantments().entrySet())if(entry.getKey().is(Enchantments.BLAST_PROTECTION)&&entry.getIntValue()>0){count++;if(entry.getIntValue()>level){level=entry.getIntValue();icon=stack;}break;}
  }
 }
 public static int level(){return level;}public static int count(){return count;}
 @Override public void render(GuiGraphicsExtractor ctx,int width,int height,float delta){
  tick();
  var m=FeatureModules.blast;boolean editor=canInteractInEditor(Minecraft.getInstance());if(m==null||!m.isEnable()||ScreenshotMode.active()||count==0&&!editor){setBounds(0,0);return;}
  if("Оригинал".equals(m.indicatorStyle.getValue())){float size=16*m.scale.getValue();setBounds(size,size);RenderUtil.drawImage(ctx,"textures/gui/blast/main.png",getX(),getY(),size,size,0,0xffffffff,0);return;}
  float s=m.scale.getValue(),w=m.text.isValue()?174:29;setBounds(w*s,29*s);var mat=ctx.pose();mat.pushMatrix();mat.translate(getX(),getY());mat.scale(s,s);HudVisualStyle.drawPanel(ctx,0,0,w,29,1);
  var item=icon.isEmpty()?new ItemStack(Items.DIAMOND_LEGGINGS):icon;SkinCatalog.drawOriginal(()->ctx.item(item,6,6));
  if(m.text.isValue()){RenderUtil.drawText(ctx,"Взрывоустойчивость "+(level==0?"—":level),29,6,ClientColors.HUD_TEXT,GFonts.MEDIUM,7);RenderUtil.drawText(ctx,"Надето предметов: "+count,29,17,ClientColors.HUD_SECONDARY_TEXT,GFonts.MEDIUM,6);}
  mat.popMatrix();
 }
}
