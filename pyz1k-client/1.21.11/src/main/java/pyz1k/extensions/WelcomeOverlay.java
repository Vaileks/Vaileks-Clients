package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.*;
import java.util.*;

public final class WelcomeOverlay {
    public static final String MESSAGE="АНЧСП Vaileks, он желает вам только добра";
    private static final Set<String> NAMES=Set.of("tukisg","trard","darkcake");
    private static long started;
    private static boolean pending;
    public static boolean matches(String name){return name!=null&&NAMES.contains(name.toLowerCase(Locale.ROOT));}
    public static void joined(){pending=true;started=0;}
    public static void disconnected(){pending=false;started=0;}
    public static void tick(){
        if(!pending)return;
        var mc=MinecraftClient.getInstance();
        if(pending&&mc.player!=null){pending=false;if(matches(mc.player.getName().getString()))started=System.nanoTime();}
    }
    public static void render(DrawContext ctx){
        if(started==0)return;
        float elapsed=(System.nanoTime()-started)/1_000_000_000f;
        if(elapsed>=8){started=0;return;}
        float alpha=Math.max(0,Math.min(1,Math.min(elapsed/.3f,(8-elapsed)/.6f)));
        var mc=MinecraftClient.getInstance();float size=11;
        float available=mc.getWindow().getScaledWidth()-30;
        float textWidth=RenderUtil.getTextWidth(MESSAGE,GFonts.MEDIUM,size);
        if(textWidth>available){size*=available/textWidth;textWidth=available;}
        float w=textWidth+20,x=(mc.getWindow().getScaledWidth()-w)/2f;
        HudVisualStyle.drawPanel(ctx,x,22,w,34,alpha);
        RenderUtil.drawText(ctx,MESSAGE,x+10,34,ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT_SECOND,alpha),GFonts.MEDIUM,size);
    }
}
