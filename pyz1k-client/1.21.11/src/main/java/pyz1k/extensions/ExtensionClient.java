package pyz1k.extensions;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.resource.*;
import org.slf4j.*;

public final class ExtensionClient implements ClientModInitializer {
    public static final Logger LOG=LoggerFactory.getLogger("Pyz1k-Vaileks");
    public static final BloodSwordHud BLOOD_HUD=new BloodSwordHud();
    private static boolean ready;
    private static ClientPlayNetworkHandler connection;
    @Override public void onInitializeClient(){
        ExtensionConfig.load();ServerProfiles.initialize();
        var resources=MinecraftClient.getInstance().getResourceManager();
        if(resources instanceof ReloadableResourceManagerImpl reloadable)reloadable.registerReloader((SynchronousResourceReloader)SkinCatalog::reload);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(mc->{
            if(ExtensionConfig.get().automaticProfiles)ServerProfiles.save();ScreenshotMode.reset();
        });
        new pyz1k.extensions.pvp.Core().onInitializeClient();
        ready=true;LOG.info("Pyz1k extensions by Vaileks ready");
    }
    public static void tick(){
        if(!ready)return;
        var mc=MinecraftClient.getInstance();ClientPlayNetworkHandler current=mc.world==null?null:mc.getNetworkHandler();
        if(connection!=current){
            connection=current;
            String key=current!=null&&mc.getCurrentServerEntry()!=null?mc.getCurrentServerEntry().address.toLowerCase(java.util.Locale.ROOT):"";
            ServerProfiles.switchServer(key);SkinCatalog.session(key);FeatureAlerts.reset();BloodSwordCooldownTracker.clear();
            if(current==null)WelcomeOverlay.disconnected();else WelcomeOverlay.joined();
        }
        pyz1k.extensions.pvp.Core.tick();
        WelcomeOverlay.tick();
        FeatureAlerts.tick();BloodSwordCooldownTracker.tick();
        if(FeatureModules.skins!=null&&FeatureModules.skins.isEnable())SkinCatalog.ensureLoaded();
    }
}
