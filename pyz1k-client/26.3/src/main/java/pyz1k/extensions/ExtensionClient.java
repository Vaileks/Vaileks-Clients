package pyz1k.extensions;

import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyz1k.extensions.BloodSwordCooldownTracker;
import pyz1k.extensions.BloodSwordHud;
import pyz1k.extensions.ExtensionConfig;
import pyz1k.extensions.FeatureAlerts;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.extensions.ServerProfiles;
import pyz1k.extensions.SkinCatalog;
import pyz1k.extensions.WelcomeOverlay;

public final class ExtensionClient
implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger((String)"Pyz1k-Vaileks");
    public static final BloodSwordHud BLOOD_HUD = new BloodSwordHud();
    public static final BlastProtectionHud BLAST_HUD=new BlastProtectionHud();
    private static boolean ready;
    private static ClientPacketListener connection;

    public void onInitializeClient() {
        ExtensionConfig.load();
        ServerProfiles.initialize();ServerPlaytime.initialize();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        if (resources instanceof ReloadableResourceManager) {
            ReloadableResourceManager reloadable = (ReloadableResourceManager)resources;
            reloadable.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)SkinCatalog::reload));
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            if (ExtensionConfig.get().automaticProfiles) {
                ServerProfiles.save();
            }
            ServerPlaytime.stop();ScreenshotMode.reset();
        });
        new pyz1k.extensions.pvp.Core().onInitializeClient();
        ready = true;
        LOG.info("Pyz1k extensions by Vaileks ready");
    }

    public static void tick() {
        ClientPacketListener current;
        if (!ready) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener clientPacketListener = current = mc.level == null ? null : mc.getConnection();
        if (connection != current) {
            connection = current;
            String key = current != null && mc.getCurrentServer() != null ? mc.getCurrentServer().ip.toLowerCase(Locale.ROOT) : "";
            ServerProfiles.switchServer(key);
            SkinCatalog.session(key);
            FeatureAlerts.reset();
            BloodSwordCooldownTracker.clear();
            if (current == null) {
                WelcomeOverlay.disconnected();
            } else {
                WelcomeOverlay.joined();
            }
        }
        ServerPlaytime.tick();BlastProtectionHud.tick();
        pyz1k.extensions.pvp.Core.tick();
        WelcomeOverlay.tick();
        FeatureAlerts.tick();
        BloodSwordCooldownTracker.tick();
        if (FeatureModules.skins != null && FeatureModules.skins.isEnable()) {
            SkinCatalog.ensureLoaded();
        }
    }
}

