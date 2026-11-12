package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.utilites.color.ClientColors;
public final class BloodSwordHud extends AbstractHudElement {
    public BloodSwordHud(){super("blood_sword_cooldown","BloodSwordCooldown",8,134,240,55);}
    @Override public void render(DrawContext ctx,int width,int height,float delta){
        var module=FeatureModules.blood;if(module==null||ScreenshotMode.active())return;
        var timers=BloodSwordCooldownTracker.active();boolean editor=canInteractInEditor(MinecraftClient.getInstance());
        if(timers.isEmpty()&&!editor&&(!module.showIdle.isValue()||!BloodSwordCooldownTracker.present())){setBounds(0,0);return;}
        var mode=BloodCooldownProfiles.mode(module.mode.getValue());int count=Math.min(timers.size(),6);
        int body=Math.max(count,1)*26;int footer=mode.serverOnly()?12:24;float h=24+body+footer,scale=module.scale.getValue();
        setBounds(240*scale,h*scale);var matrix=ctx.getMatrices();matrix.pushMatrix();matrix.translate(getX(),getY());matrix.scale(scale,scale);
        HudVisualStyle.drawPanel(ctx,0,0,240,h,1);
        RenderUtil.drawText(ctx,"BloodSword · "+mode.label(),8,7,ClientColors.HUD_TEXT,GFonts.MEDIUM,8);
        if(count==0)RenderUtil.drawText(ctx,editor?"Превью · серверные таймеры":"Нет полученных активных таймеров",8,30,ClientColors.HUD_SECONDARY_TEXT,GFonts.MEDIUM,7);
        for(int i=0;i<count;i++){
            var timer=timers.get(i);int y=24+i*26;
            int iy=y+2;SkinCatalog.drawOriginal(()->ctx.drawItem(timer.stack(),7,iy));
            RenderUtil.drawText(ctx,timer.label(),29,y+2,ClientColors.HUD_TEXT,GFonts.MEDIUM,7);
            String time=BloodCooldownProfiles.time(timer.remaining(),module.precise.isValue());
            RenderUtil.drawText(ctx,time,231-RenderUtil.getTextWidth(time,GFonts.MEDIUM,8),y+2,timer.form().color,GFonts.MEDIUM,8);
            RenderUtil.drawRoundRect(ctx,29,y+15,202,3,1,ClientColors.SETTINGS_BACKGROUND);
            RenderUtil.drawRoundRect(ctx,29,y+15,202*timer.progress(),3,1,timer.form().color);
        }
        int fy=24+body;
        String info=mode.serverOnly()?"Только данные сервера":"Пресет КД: булава "+BloodCooldownProfiles.time(mode.seconds("cooldown"),false)+" · гарпун "+BloodCooldownProfiles.time(mode.seconds("bow_cooldown"),false)+" · тяга "+BloodCooldownProfiles.time(mode.seconds("bow_pull_cooldown"),false);
        RenderUtil.drawText(ctx,info,8,fy,ClientColors.HUD_SECONDARY_TEXT,GFonts.MEDIUM,6);
        if(!mode.serverOnly())RenderUtil.drawText(ctx,"Форма: булава "+BloodCooldownProfiles.time(mode.seconds("mace_duration"),false)+" · гарпун "+BloodCooldownProfiles.time(mode.seconds("bow_duration"),false)+" · переход "+BloodCooldownProfiles.time(mode.seconds("transition_duration"),true),8,fy+11,ClientColors.HUD_SECONDARY_TEXT,GFonts.MEDIUM,6);
        matrix.popMatrix();
    }
}
