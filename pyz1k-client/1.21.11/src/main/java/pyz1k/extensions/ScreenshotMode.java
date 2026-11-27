package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
public final class ScreenshotMode {
    private static boolean active,previous;
    public static boolean active(){return active;}
    public static void toggle(){
        var mc=MinecraftClient.getInstance();
        if(active){mc.options.hudHidden=previous;active=false;}
        else {previous=mc.options.hudHidden;active=true;mc.options.hudHidden=true;}
    }
    public static void reset(){if(active)toggle();}
}
