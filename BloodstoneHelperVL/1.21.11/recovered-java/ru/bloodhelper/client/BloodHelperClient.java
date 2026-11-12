/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client;

import java.io.IOException;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_437;
import ru.bloodhelper.client.access.ClientControlService;
import ru.bloodhelper.client.access.ControlStatusScreen;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.assistant.AssistantNotice;
import ru.bloodhelper.client.assistant.AssistantService;
import ru.bloodhelper.client.compat.ClientDistribution;
import ru.bloodhelper.client.compat.ExternalHudMods;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.discord.DiscordPresenceModule;
import ru.bloodhelper.client.gui.BloodHelperScreen;
import ru.bloodhelper.client.gui.BloodstonePackNotice;
import ru.bloodhelper.client.gui.ChatHudEditor;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.gui.PvpEndNotice;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.CooldownTracker;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.InventorySnapshot;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.hud.Notifications;
import ru.bloodhelper.client.hud.SessionTracker;
import ru.bloodhelper.client.hud.WatermarkWidget;
import ru.bloodhelper.client.inventory.InventoryLayoutStore;
import ru.bloodhelper.client.lyrics.KineticLyricsRenderer;
import ru.bloodhelper.client.lyrics.LyricsService;
import ru.bloodhelper.client.modules.AbilityGuideModule;
import ru.bloodhelper.client.modules.BackgroundAlerts;
import ru.bloodhelper.client.modules.ChatDraftsModule;
import ru.bloodhelper.client.modules.ChatMentionsModule;
import ru.bloodhelper.client.modules.ChatTools;
import ru.bloodhelper.client.modules.CombatStateModule;
import ru.bloodhelper.client.modules.CooldownReadyModule;
import ru.bloodhelper.client.modules.CoordinateLinksModule;
import ru.bloodhelper.client.modules.DeathRecapModule;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.FreeLookModule;
import ru.bloodhelper.client.modules.InventoryProfiles;
import ru.bloodhelper.client.modules.InventorySearch;
import ru.bloodhelper.client.modules.LegendarySoundVolume;
import ru.bloodhelper.client.modules.LocalMusicScreen;
import ru.bloodhelper.client.modules.LunarNameTags;
import ru.bloodhelper.client.modules.MediaSourceScreen;
import ru.bloodhelper.client.modules.MusicPlayerScreen;
import ru.bloodhelper.client.modules.MusicSearchScreen;
import ru.bloodhelper.client.modules.OpponentCooldownTracker;
import ru.bloodhelper.client.modules.OptimizerModule;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.modules.QuickCommandsModule;
import ru.bloodhelper.client.modules.SizeTimerModule;
import ru.bloodhelper.client.modules.SlotLockModule;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.VisualHitboxes;
import ru.bloodhelper.client.modules.WaypointsModule;
import ru.bloodhelper.client.modules.WorldVisuals;
import ru.bloodhelper.client.modules.ZoomModule;
import ru.bloodhelper.client.music.MediaService;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.MusicControlBindings;
import ru.bloodhelper.client.policy.ClientControlPolicy;
import ru.bloodhelper.client.policy.RemoteModulePolicy;
import ru.bloodhelper.client.resourcepack.BloodstoneResourcePack;
import ru.bloodhelper.client.skins.VeritiAnimation;
import ru.bloodhelper.client.tiers.TierLookupCommand;
import ru.bloodhelper.client.tiers.TierService;
import ru.bloodhelper.client.visuals.LegendaryVisuals;

public final class BloodHelperClient
implements ClientModInitializer {
    public static final String MOD_ID = "bloodhelper";
    public static final String VERSION = "1.3.8";
    private static final MediaService MEDIA = new MediaService();
    private static final LyricsService LYRICS = new LyricsService();
    private static class_304 menu;
    private static ChatHudEditor chatEditor;
    private static class_408 chatOwner;

    public void onInitializeClient() {
        ConfigStore.init(FabricLoader.getInstance().getConfigDir());
        InventoryLayoutStore.initialize(FabricLoader.getInstance().getConfigDir());
        ClientControlService.initialize(class_310.method_1551().method_1548().method_1676());
        RemoteModulePolicy.initialize();
        TierLookupCommand.register();
        ClientDistribution.initialize();
        LunarNameTags.register();
        AssistantService.get().initialize(FabricLoader.getInstance().getConfigDir());
        OptimizerModule.settingsChanged();
        LegendarySoundVolume.settingsChanged();
        BloodstoneResourcePack.initialize(FabricLoader.getInstance().getGameDir());
        BloodstonePackNotice.initialize(BloodHelperClient.config().resourcePack.enabled);
        UiLanguage.register();
        class_304.class_11900 category = class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)MOD_ID, (String)"main"));
        menu = KeyBindingHelper.registerKeyBinding((class_304)new class_304(ClientDistribution.menuBindingId(), class_3675.class_307.field_1668, ClientDistribution.defaultMenuKey(), category));
        MusicControlBindings.register(category);
        AbilityGuideModule.register(category);
        QuickCommandsModule.register(category);
        WaypointsModule.register(category);
        StreamerMode.register(category);
        ZoomModule.register(category);
        FreeLookModule.register(category);
        ChatMentionsModule.register();
        StaffAccess.register(FabricLoader.getInstance().getConfigDir());
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.CHAT, (class_2960)class_2960.method_60655((String)MOD_ID, (String)"hud_behind_chat"), HudRenderer::renderBehindChat);
        HudElementRegistry.attachElementAfter((class_2960)VanillaHudElements.CHAT, (class_2960)class_2960.method_60655((String)MOD_ID, (String)"hud"), HudRenderer::render);
        HudElementRegistry.attachElementAfter((class_2960)class_2960.method_60655((String)MOD_ID, (String)"hud"), (class_2960)class_2960.method_60655((String)MOD_ID, (String)"pack_notice"), (graphics, delta) -> {
            if (class_310.method_1551().field_1755 == null) {
                PvpEndNotice.render(graphics);
                BloodstonePackNotice.render(graphics, -1.0, -1.0);
            }
        });
        SessionTracker.register();
        KineticLyricsRenderer.register(LYRICS, MEDIA, BloodHelperClient::config);
        LegendaryVisuals.register();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            DiscordPresenceModule.joined();
            WatermarkWidget.joined();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            AssistantService.get().clearConversation();
            DiscordPresenceModule.disconnected();
            StaffAccess.disconnected();
            WatermarkWidget.disconnected();
            BackgroundAlerts.onDisconnected(client);
            ChatDraftsModule.reset();
            CoordinateLinksModule.reset();
            CombatStateModule.reset();
            DropGuardModule.reset();
            MEDIA.setDucking(false, BloodHelperClient.config().musicDucking.reductionPercent, BloodHelperClient.config().musicDucking);
            ZoomModule.reset();
            FreeLookModule.reset();
            PlayerMetrics.reset();
            TierService.reset();
            InventoryProfiles.reset();
            ChatMentionsModule.reset();
            CooldownReadyModule.reset();
            FeatureAlerts.clear();
            OpponentCooldownTracker.clear();
            SizeTimerModule.reset();
            WaypointsModule.reset();
            LegendaryVisuals.reset();
            WorldVisuals.reset();
            InventorySnapshot.clearCache();
            CooldownTracker.clearCache();
        }));
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ExternalHudMods.initialize());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long restrictions = ClientControlPolicy.restrictionGeneration();
            if (ClientControlService.tick()) {
                BloodHelperClient.settingsChanged();
            }
            boolean controlEnabled = ClientControlPolicy.enabled();
            if (restrictions != ClientControlPolicy.restrictionGeneration()) {
                if (client.field_1755 != null && client.field_1755.getClass().getName().startsWith("ru.bloodhelper.client.") && !(client.field_1755 instanceof ControlStatusScreen)) {
                    client.method_1507((class_437)(controlEnabled ? null : new ControlStatusScreen()));
                }
                if (!controlEnabled) {
                    MEDIA.suspendPlayback();
                    MusicWidget.cancelInteraction();
                    MusicWidget.clearControls();
                }
            }
            if (controlEnabled && client.field_1755 instanceof ControlStatusScreen) {
                client.method_1507((class_437)new BloodHelperScreen(null));
            }
            StaffAccess.tick(client);
            ExternalHudMods.tick();
            VisualHitboxes.tick(client);
            PlayerMetrics.tick(client);
            VeritiAnimation.tick(client);
            TierService.tick(client);
            InventoryProfiles.tick(client);
            FreeLookModule.tick(client);
            if (RemoteModulePolicy.tick()) {
                BloodHelperClient.settingsChanged();
            }
            AssistantService.get().configure(BloodHelperClient.config().aiAssistant.enabled, BloodHelperClient.config().aiAssistant.threads);
            AssistantNotice.tick(client, BloodHelperClient.config().aiAssistant.enabled, !"en".equals(BloodHelperClient.config().language));
            OptimizerModule.tick(client);
            CombatStateModule.tick(client);
            BackgroundAlerts.tick(client);
            ChatDraftsModule.tick(client);
            CoordinateLinksModule.tick(client);
            DropGuardModule.tick(client);
            if (menu.method_1436()) {
                BloodHelperClient.consumeMenuClicks();
                if (client.field_1755 == null) {
                    client.method_1507(controlEnabled ? new BloodHelperScreen(null) : new ControlStatusScreen());
                }
            }
            MusicControlBindings.tick(client);
            AbilityGuideModule.tick(client);
            QuickCommandsModule.tick(client);
            WaypointsModule.tick(client);
            LegendaryVisuals.tick(client);
            WorldVisuals.tick(client);
            SizeTimerModule.tick(client);
            StreamerMode.tick(client);
            ZoomModule.tick(client);
            FeatureAlerts.tick(client);
            ChatMentionsModule.tick(client);
            CooldownReadyModule.tick(client);
            Notifications.tick(client, BloodHelperClient.config());
            SessionTracker.tick(client, BloodHelperClient.config());
            DeathRecapModule.tick(client);
            ChatTools.tick(client);
            OpponentCooldownTracker.tick(client, BloodHelperClient.config());
            MEDIA.setControlsAllowed(RemoteModulePolicy.allows("music", BloodHelperClient.config().serverSafeMode));
            boolean combatDucking = BloodHelperClient.config().musicDucking.enabled && CombatStateModule.active();
            MEDIA.setDucking(combatDucking, BloodHelperClient.config().musicDucking.reductionPercent, BloodHelperClient.config().musicDucking);
            MEDIA.setPreferredSource(BloodHelperClient.config().music.preferredSource);
            MEDIA.setEnabled(controlEnabled && (BloodHelperClient.config().music.enabled || BloodHelperClient.config().musicDucking.enabled || DiscordPresenceModule.needsMedia() || client.field_1755 instanceof MediaSourceScreen || client.field_1755 instanceof MusicPlayerScreen || client.field_1755 instanceof MusicSearchScreen || client.field_1755 instanceof LocalMusicScreen));
            DiscordPresenceModule.tick(client, MEDIA.snapshot());
            LYRICS.update(MEDIA.snapshot(), BloodHelperClient.config().music, controlEnabled && client.field_1755 instanceof MusicPlayerScreen);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof class_408)) {
                PvpEndNotice.clear();
            }
            InventoryProfiles.attach(screen);
            InventorySearch.attach(screen);
            SlotLockModule.attach(screen);
            ScreenMouseEvents.allowMouseClick((class_437)screen).register((s, event) -> !BloodstonePackNotice.click(event));
            ScreenMouseEvents.allowMouseClick((class_437)screen).register((s, event) -> !StaffAccess.hudClick(event.comp_4798(), event.comp_4799(), event.method_74245()));
            if (screen instanceof class_408) {
                ChatHudEditor editor;
                boolean sameScreen;
                class_408 chatScreen = (class_408)screen;
                boolean bl = sameScreen = chatOwner == chatScreen && chatEditor != null;
                if (chatEditor != null) {
                    if (sameScreen) {
                        chatEditor.resized();
                    } else {
                        chatEditor.close();
                    }
                }
                chatEditor = editor = sameScreen ? chatEditor : new ChatHudEditor();
                chatOwner = chatScreen;
                ScreenEvents.afterRender((class_437)screen).register((s, graphics, mouseX, mouseY, delta) -> editor.render(graphics, mouseX, mouseY));
                ScreenEvents.remove((class_437)screen).register(s -> {
                    editor.close();
                    if (chatEditor == editor) {
                        chatEditor = null;
                        chatOwner = null;
                    }
                });
                ScreenMouseEvents.allowMouseClick((class_437)screen).register((s, event) -> !editor.click(event));
                ScreenMouseEvents.allowMouseDrag((class_437)screen).register((s, event, dx, dy) -> !editor.drag(event));
                ScreenMouseEvents.allowMouseRelease((class_437)screen).register((s, event) -> !editor.release(event));
                ScreenMouseEvents.allowMouseScroll((class_437)screen).register((s, x, y, horizontal, vertical) -> !editor.scroll(x, y, vertical));
                ScreenKeyboardEvents.allowKeyPress((class_437)screen).register((s, event) -> !editor.keyPressed(event));
            } else if (!(screen instanceof HudEditorScreen)) {
                ScreenEvents.afterRender((class_437)screen).register((s, graphics, mouseX, mouseY, delta) -> HudRenderer.renderScreen(graphics));
                ScreenMouseEvents.allowMouseClick((class_437)screen).register((s, event) -> !MusicWidget.click(event.comp_4798(), event.comp_4799(), event.method_74245()));
                ScreenMouseEvents.allowMouseDrag((class_437)screen).register((s, event, dx, dy) -> !MusicWidget.drag(event.comp_4798(), event.comp_4799(), event.method_74245()));
                ScreenMouseEvents.allowMouseRelease((class_437)screen).register((s, event) -> !MusicWidget.release(event.method_74245()));
                ScreenMouseEvents.allowMouseScroll((class_437)screen).register((s, x, y, horizontal, vertical) -> !MusicWidget.capturesScroll(x, y));
                ScreenEvents.remove((class_437)screen).register(s -> {
                    MusicWidget.cancelInteraction();
                    MusicWidget.clearControls();
                });
            }
            ScreenEvents.afterRender((class_437)screen).register((s, graphics, mouseX, mouseY, delta) -> {
                PvpEndNotice.render(graphics);
                BloodstonePackNotice.render(graphics, mouseX, mouseY);
            });
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ClientControlService.close();
            StaffAccess.close();
            InventoryLayoutStore.close();
            AssistantService.get().shutdown();
            NotificationInbox.flush();
            DiscordPresenceModule.close();
            ConfigStore.save();
            OpponentCooldownTracker.clear();
            BackgroundAlerts.close();
            ChatDraftsModule.reset();
            CoordinateLinksModule.reset();
            InventorySnapshot.clearCache();
            CooldownTracker.clearCache();
            LYRICS.close();
            MEDIA.close();
            BloodstoneResourcePack.close();
        });
    }

    public static ClientConfig config() {
        return RemoteModulePolicy.runtime();
    }

    public static boolean isChatHudEditing() {
        return chatEditor != null && chatEditor.isEditing();
    }

    public static void saveConfig() {
        ConfigStore.requestSave();
        BloodHelperClient.settingsChanged();
    }

    private static void settingsChanged() {
        ExternalHudMods.tick();
        VisualHitboxes.tick(class_310.method_1551());
        AssistantService.get().configure(BloodHelperClient.config().aiAssistant.enabled, BloodHelperClient.config().aiAssistant.threads);
        OptimizerModule.settingsChanged();
        DiscordPresenceModule.tick(class_310.method_1551(), MEDIA.snapshot());
        BackgroundAlerts.settingsChanged();
        ChatDraftsModule.settingsChanged();
        CoordinateLinksModule.settingsChanged();
        LegendarySoundVolume.settingsChanged();
        BloodstoneResourcePack.settingsChanged();
        BloodstonePackNotice.settingsChanged(BloodHelperClient.config().resourcePack.enabled);
    }

    public static String menuKeyName() {
        return menu == null ? ClientDistribution.defaultMenuKeyName() : menu.method_16007().getString();
    }

    public static boolean menuMatches(class_11908 event) {
        return menu != null && menu.method_1417(event);
    }

    public static boolean menuMatches(class_11909 event) {
        return menu != null && menu.method_1433(event);
    }

    public static void consumeMenuClicks() {
        if (menu != null) {
            while (menu.method_1436()) {
            }
        }
    }

    public static void setMenuKey(class_3675.class_306 key) {
        if (menu == null) {
            return;
        }
        menu.method_1422(key);
        class_304.method_1426();
        BloodHelperClient.consumeMenuClicks();
        if (key.method_1442() == class_3675.class_307.field_1668) {
            ConfigStore.get().menuKey = key.method_1444();
        }
        class_310.method_1551().field_1690.method_1640();
        BloodHelperClient.saveConfig();
    }

    public static void resetMenuKey() {
        if (menu != null) {
            BloodHelperClient.setMenuKey(menu.method_1429());
        }
    }

    public static List<String> profiles() {
        return ConfigStore.profiles();
    }

    public static void saveProfile(String name) throws IOException {
        ConfigStore.saveProfile(name);
    }

    public static boolean loadProfile(String name) {
        boolean result = ConfigStore.loadProfile(name);
        if (result) {
            BloodHelperClient.saveConfig();
        }
        return result;
    }

    public static boolean deleteProfile(String name) {
        return ConfigStore.deleteProfile(name);
    }

    public static void openHudEditor(class_437 parent) {
        class_310.method_1551().method_1507(ClientControlPolicy.enabled() ? new HudEditorScreen(parent) : new ControlStatusScreen());
    }

    public static MediaService media() {
        return MEDIA;
    }

    public static LyricsService lyrics() {
        return LYRICS;
    }

    public static String mediaStatus() {
        return MEDIA.status();
    }

    public static void musicCommand(String command) {
        switch (command) {
            case "previous": {
                MEDIA.previous();
                break;
            }
            case "next": {
                MEDIA.next();
                break;
            }
            default: {
                MEDIA.playPause();
            }
        }
    }

    public static long playbackPosition() {
        return BloodHelperClient.playbackPosition(MEDIA.snapshot());
    }

    public static long playbackPosition(MediaSnapshot sample) {
        return sample.playbackPosition();
    }
}

