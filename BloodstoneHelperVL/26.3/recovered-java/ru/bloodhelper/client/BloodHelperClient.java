/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
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
    private static KeyMapping menu;
    private static ChatHudEditor chatEditor;
    private static ChatScreen chatOwner;

    public void onInitializeClient() {
        ConfigStore.init(FabricLoader.getInstance().getConfigDir());
        InventoryLayoutStore.initialize(FabricLoader.getInstance().getConfigDir());
        ClientControlService.initialize(Minecraft.getInstance().getUser().getName());
        RemoteModulePolicy.initialize();
        TierLookupCommand.register();
        AssistantService.get().initialize(FabricLoader.getInstance().getConfigDir());
        OptimizerModule.settingsChanged();
        LegendarySoundVolume.settingsChanged();
        BloodstoneResourcePack.initialize(FabricLoader.getInstance().getGameDir());
        BloodstonePackNotice.initialize(BloodHelperClient.config().resourcePack.enabled);
        ClientDistribution.initialize();
        LunarNameTags.register();
        UiLanguage.register();
        KeyMapping.Category category = KeyMapping.Category.register((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"main"));
        menu = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping(ClientDistribution.menuBindingId(), InputConstants.Type.KEYBOARD, ClientDistribution.defaultMenuKey(), category));
        MusicControlBindings.register(category);
        AbilityGuideModule.register(category);
        QuickCommandsModule.register(category);
        WaypointsModule.register(category);
        StreamerMode.register(category);
        ZoomModule.register(category);
        FreeLookModule.register(category);
        ChatMentionsModule.register();
        StaffAccess.register(FabricLoader.getInstance().getConfigDir());
        HudElementRegistry.attachElementBefore((Identifier)VanillaHudElements.CHAT, (Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"hud_behind_chat"), HudRenderer::renderBehindChat);
        HudElementRegistry.attachElementAfter((Identifier)VanillaHudElements.CHAT, (Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"hud"), HudRenderer::render);
        HudElementRegistry.attachElementAfter((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"hud"), (Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"pack_notice"), (graphics, delta) -> {
            if (Minecraft.getInstance().gui.screen() == null) {
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
                if (client.gui.screen() != null && client.gui.screen().getClass().getName().startsWith("ru.bloodhelper.client.") && !(client.gui.screen() instanceof ControlStatusScreen)) {
                    client.gui.setScreen((Screen)(controlEnabled ? null : new ControlStatusScreen()));
                }
                if (!controlEnabled) {
                    MEDIA.suspendPlayback();
                    MusicWidget.cancelInteraction();
                    MusicWidget.clearControls();
                }
            }
            if (controlEnabled && client.gui.screen() instanceof ControlStatusScreen) {
                client.gui.setScreen((Screen)new BloodHelperScreen(null));
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
            if (menu.consumeClick()) {
                BloodHelperClient.consumeMenuClicks();
                if (client.gui.screen() == null) {
                    client.gui.setScreen(controlEnabled ? new BloodHelperScreen(null) : new ControlStatusScreen());
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
            MEDIA.setEnabled(controlEnabled && (BloodHelperClient.config().music.enabled || BloodHelperClient.config().musicDucking.enabled || DiscordPresenceModule.needsMedia() || client.gui.screen() instanceof MediaSourceScreen || client.gui.screen() instanceof MusicPlayerScreen || client.gui.screen() instanceof MusicSearchScreen || client.gui.screen() instanceof LocalMusicScreen));
            DiscordPresenceModule.tick(client, MEDIA.snapshot());
            LYRICS.update(MEDIA.snapshot(), BloodHelperClient.config().music, controlEnabled && client.gui.screen() instanceof MusicPlayerScreen);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ChatScreen)) {
                PvpEndNotice.clear();
            }
            InventoryProfiles.attach(screen);
            InventorySearch.attach(screen);
            SlotLockModule.attach(screen);
            ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, event) -> !BloodstonePackNotice.click(event));
            ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, event) -> !StaffAccess.hudClick(event.x(), event.y(), event.button()));
            if (screen instanceof ChatScreen) {
                ChatHudEditor editor;
                boolean sameScreen;
                ChatScreen chatScreen = (ChatScreen)screen;
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
                ScreenEvents.afterExtract((Screen)screen).register((s, graphics, mouseX, mouseY, delta) -> editor.render(graphics, mouseX, mouseY));
                ScreenEvents.remove((Screen)screen).register(s -> {
                    editor.close();
                    if (chatEditor == editor) {
                        chatEditor = null;
                        chatOwner = null;
                    }
                });
                ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, event) -> !editor.click(event));
                ScreenMouseEvents.allowMouseDrag((Screen)screen).register((s, event, dx, dy) -> !editor.drag(event));
                ScreenMouseEvents.allowMouseRelease((Screen)screen).register((s, event) -> !editor.release(event));
                ScreenMouseEvents.allowMouseScroll((Screen)screen).register((s, x, y, horizontal, vertical) -> !editor.scroll(x, y, vertical));
                ScreenKeyboardEvents.allowKeyPress((Screen)screen).register((s, event) -> !editor.keyPressed(event));
            } else if (!(screen instanceof HudEditorScreen)) {
                ScreenEvents.afterExtract((Screen)screen).register((s, graphics, mouseX, mouseY, delta) -> HudRenderer.renderScreen(graphics));
                ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, event) -> !MusicWidget.click(event.x(), event.y(), event.button()));
                ScreenMouseEvents.allowMouseDrag((Screen)screen).register((s, event, dx, dy) -> !MusicWidget.drag(event.x(), event.y(), event.button()));
                ScreenMouseEvents.allowMouseRelease((Screen)screen).register((s, event) -> !MusicWidget.release(event.button()));
                ScreenMouseEvents.allowMouseScroll((Screen)screen).register((s, x, y, horizontal, vertical) -> !MusicWidget.capturesScroll(x, y));
                ScreenEvents.remove((Screen)screen).register(s -> {
                    MusicWidget.cancelInteraction();
                    MusicWidget.clearControls();
                });
            }
            ScreenEvents.afterExtract((Screen)screen).register((s, graphics, mouseX, mouseY, delta) -> {
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
        VisualHitboxes.tick(Minecraft.getInstance());
        AssistantService.get().configure(BloodHelperClient.config().aiAssistant.enabled, BloodHelperClient.config().aiAssistant.threads);
        OptimizerModule.settingsChanged();
        DiscordPresenceModule.tick(Minecraft.getInstance(), MEDIA.snapshot());
        BackgroundAlerts.settingsChanged();
        ChatDraftsModule.settingsChanged();
        CoordinateLinksModule.settingsChanged();
        LegendarySoundVolume.settingsChanged();
        BloodstoneResourcePack.settingsChanged();
        BloodstonePackNotice.settingsChanged(BloodHelperClient.config().resourcePack.enabled);
    }

    public static String menuKeyName() {
        return menu == null ? ClientDistribution.defaultMenuKeyName() : menu.getTranslatedKeyMessage().getString();
    }

    public static boolean menuMatches(KeyEvent event) {
        return menu != null && menu.matches(event);
    }

    public static boolean menuMatches(MouseButtonEvent event) {
        return menu != null && menu.matchesMouse(event);
    }

    public static void consumeMenuClicks() {
        if (menu != null) {
            while (menu.consumeClick()) {
            }
        }
    }

    public static void setMenuKey(InputConstants.Key key) {
        if (menu == null) {
            return;
        }
        menu.setKey(key);
        KeyMapping.resetMapping();
        BloodHelperClient.consumeMenuClicks();
        if (key.getType() == InputConstants.Type.KEYBOARD) {
            ConfigStore.get().menuKey = key.getValue();
        }
        Minecraft.getInstance().options.save();
        BloodHelperClient.saveConfig();
    }

    public static void resetMenuKey() {
        if (menu != null) {
            BloodHelperClient.setMenuKey(menu.getDefaultKey());
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

    public static void openHudEditor(Screen parent) {
        Minecraft.getInstance().gui.setScreen(ClientControlPolicy.enabled() ? new HudEditorScreen(parent) : new ControlStatusScreen());
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

