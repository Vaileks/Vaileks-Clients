package pyz1k.extensions;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;

public final class FeatureAlerts {
    private record Notice(String title,Text detail,ItemStack icon,long until){}
    private static final ArrayDeque<Notice> notices=new ArrayDeque<>();
    private static Set<String> low=Set.of();
    private static int ticks;
    private static long lastSound;
    public static void reset(){low=Set.of();notices.clear();ticks=0;lastSound=0;}
    public static void notice(String title,Text detail,ItemStack icon,boolean sound){
        while(notices.size()>=4)notices.removeFirst();
        long now=System.currentTimeMillis();notices.addLast(new Notice(title,detail.copy(),icon.copy(),now+6000));
        if(sound&&now-lastSound>=2000){lastSound=now;MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.BLOCK_NOTE_BLOCK_PLING,1.2f));}
    }
    public static void tick(){
        if(FeatureModules.durability==null||!FeatureModules.durability.isEnable()){if(!low.isEmpty())low=Set.of();return;}
        var mc=MinecraftClient.getInstance();if(mc.player==null||mc.world==null){reset();return;}if(++ticks%4!=0)return;
        var durability=FeatureModules.durability;
        if(durability!=null&&durability.isEnable()){
            Set<String> now=new HashSet<>();
            checkDurability("main",mc.player.getMainHandStack(),now,durability);
            checkDurability("off",mc.player.getOffHandStack(),now,durability);
            if(durability.armor.isValue())for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))checkDurability(slot.getName(),mc.player.getEquippedStack(slot),now,durability);
            low=now;
        }else low=Set.of();
    }
    private static void checkDurability(String slot,ItemStack stack,Set<String> now,FeatureModules.Durability module){
        if(!stack.isDamageable()||!ClientFeatureLogic.lowDurability(stack.getMaxDamage(),stack.getDamage(),Math.round(module.threshold.getValue())))return;
        String key=slot+":"+signature(stack);now.add(key);
        if(!low.contains(key))notice("Низкая прочность: "+Math.max(0,stack.getMaxDamage()-stack.getDamage())+" / "+stack.getMaxDamage(),stack.getName(),stack,module.sound.isValue());
    }
    private static String signature(ItemStack stack){return Registries.ITEM.getId(stack.getItem())+"|"+stack.get(DataComponentTypes.ITEM_MODEL)+"|"+stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)+"|"+stack.getName().getString();}
    public static void render(DrawContext ctx){
        if(notices.isEmpty())return;
        if(ScreenshotMode.active()||MinecraftClient.getInstance().options.hudHidden)return;
        long now=System.currentTimeMillis();notices.removeIf(n->n.until<now);
        var mc=MinecraftClient.getInstance();int x=Math.max(4,mc.getWindow().getScaledWidth()-250),y=38;
        for(var n:notices){
            HudVisualStyle.drawPanel(ctx,x,y,242,39,1);
            RenderUtil.drawText(ctx,n.title,x+29,y+6,ClientColors.HUD_TEXT,GFonts.MEDIUM,7);
            var lines=mc.textRenderer.wrapLines(n.detail,204);if(!lines.isEmpty())ctx.drawText(mc.textRenderer,lines.getFirst(),x+29,y+21,ClientColors.HUD_SECONDARY_TEXT,false);
            int iy=y+11;SkinCatalog.drawOriginal(()->ctx.drawItem(n.icon,x+7,iy));y+=44;
        }
    }
}
