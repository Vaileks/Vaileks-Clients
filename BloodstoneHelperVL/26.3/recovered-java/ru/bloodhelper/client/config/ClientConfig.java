/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.bloodhelper.client.config.AbilityGuideSettings;
import ru.bloodhelper.client.config.AiAssistantSettings;
import ru.bloodhelper.client.config.AspectRatioSettings;
import ru.bloodhelper.client.config.BackgroundAlertsSettings;
import ru.bloodhelper.client.config.ChatCalculatorSettings;
import ru.bloodhelper.client.config.ChatDraftsSettings;
import ru.bloodhelper.client.config.ChatMentionsSettings;
import ru.bloodhelper.client.config.ChatViolationsSettings;
import ru.bloodhelper.client.config.CirclesSettings;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.config.CombatTimelineSettings;
import ru.bloodhelper.client.config.CooldownReadySettings;
import ru.bloodhelper.client.config.CoordinateLinksSettings;
import ru.bloodhelper.client.config.CrosshairSettings;
import ru.bloodhelper.client.config.DeathScreenshotSettings;
import ru.bloodhelper.client.config.DiscordPresenceSettings;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.config.GeneralHudSettings;
import ru.bloodhelper.client.config.HeadsSettings;
import ru.bloodhelper.client.config.HudEditorSettings;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.config.InventoryUiSettings;
import ru.bloodhelper.client.config.LegendarySizeSettings;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.config.LyricsPreferences;
import ru.bloodhelper.client.config.MotionBlurSettings;
import ru.bloodhelper.client.config.MusicDuckingSettings;
import ru.bloodhelper.client.config.NameVisibilitySettings;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.config.OptimizerSettings;
import ru.bloodhelper.client.config.PlayerDisplaySettings;
import ru.bloodhelper.client.config.QuickCommandsSettings;
import ru.bloodhelper.client.config.RenderQualitySettings;
import ru.bloodhelper.client.config.SanctionSettings;
import ru.bloodhelper.client.config.ShulkerPreviewSettings;
import ru.bloodhelper.client.config.SizeTimerSettings;
import ru.bloodhelper.client.config.SkinsSettings;
import ru.bloodhelper.client.config.SlotLockSettings;
import ru.bloodhelper.client.config.SpectatorTimerSettings;
import ru.bloodhelper.client.config.StaffChatSettings;
import ru.bloodhelper.client.config.StaffCheckSettings;
import ru.bloodhelper.client.config.StaffRulebookSettings;
import ru.bloodhelper.client.config.StaffStatsSettings;
import ru.bloodhelper.client.config.StaffWorkspaceSettings;
import ru.bloodhelper.client.config.StreamerSettings;
import ru.bloodhelper.client.config.WatermarkSettings;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.config.WorldVisualSettings;
import ru.bloodhelper.client.config.ZoomSettings;
import ru.bloodhelper.client.modules.KillEffectSoundCatalog;
import ru.bloodhelper.client.music.MediaSources;
import ru.bloodhelper.client.policy.RemoteModulePolicy;
import ru.bloodhelper.client.vl.VLRestrictions;

public final class ClientConfig {
    public NotificationSoundSettings notificationSounds = new NotificationSoundSettings();
    public ChatCalculatorSettings chatCalculator = new ChatCalculatorSettings();
    public StaffCheckSettings staffChecks = new StaffCheckSettings();
    public StaffChatSettings staffChat = new StaffChatSettings();
    public StaffRulebookSettings staffRulebook = new StaffRulebookSettings();
    public SanctionSettings sanctions = new SanctionSettings();
    public HudItemControlsSettings hudItemControls = new HudItemControlsSettings();
    public StaffWorkspaceSettings staffWorkspace = new StaffWorkspaceSettings();
    public WorldVisualSettings worldVisuals = new WorldVisualSettings();
    public CrosshairSettings crosshair = new CrosshairSettings();
    public GeneralHudSettings generalHud = new GeneralHudSettings();
    public PlayerDisplaySettings.CombatMetric reachDisplay = new PlayerDisplaySettings.CombatMetric();
    public PlayerDisplaySettings.CombatMetric comboCounter = ClientConfig.combatMetric(0.6, 0.64);
    public PlayerDisplaySettings.Saturation saturation = new PlayerDisplaySettings.Saturation();
    public PlayerDisplaySettings.Hitboxes hitboxes = new PlayerDisplaySettings.Hitboxes();
    public PlayerDisplaySettings.FreeLook freeLook = new PlayerDisplaySettings.FreeLook();
    public PlayerDisplaySettings.OtherTiers otherTiers = new PlayerDisplaySettings.OtherTiers();
    public static final int DEFAULT_ACCENT = -3401955;
    public static final double DEFAULT_HUD_SCALE = 0.65;
    public static final double MIN_HUD_SCALE = 0.35;
    public static final double MAX_HUD_SCALE = 2.0;
    public int schemaVersion = 4;
    public boolean serverSafeMode = false;
    public AiAssistantSettings aiAssistant = new AiAssistantSettings();
    public AspectRatioSettings aspectRatio = new AspectRatioSettings();
    public MotionBlurSettings motionBlur = new MotionBlurSettings();
    public OptimizerSettings optimizer = new OptimizerSettings();
    public NameVisibilitySettings betterNameVisibility = new NameVisibilitySettings();
    public String language = "ru";
    public int accent = -3401955;
    public String themeMode = "DARK";
    public HudEditorSettings hudEditor = new HudEditorSettings();
    public RenderQualitySettings renderQuality = new RenderQualitySettings();
    public boolean textShadow = true;
    public boolean background = true;
    public boolean hudAboveChat = true;
    public boolean hudMenuEffects = true;
    public boolean hideHudInPauseMenu = false;
    public boolean hudAutoReadability = false;
    public boolean inventorySearch = false;
    public boolean inventoryProfiles = false;
    public InventoryUiSettings inventoryUi = new InventoryUiSettings();
    public SkinsSettings skins = new SkinsSettings();
    public boolean screenshotAutoCopy = false;
    public SpectatorTimerSettings spectatorTimer = new SpectatorTimerSettings();
    public StaffStatsSettings staffStats = new StaffStatsSettings();
    public ChatViolationsSettings chatViolations = new ChatViolationsSettings();
    public boolean nicknameCopy = false;
    public boolean commandLayout = false;
    public boolean screenshotActions = false;
    public boolean hudDetails = true;
    public boolean simpleHud;
    public Map<String, Boolean> hudTitles = new LinkedHashMap<String, Boolean>();
    public int backgroundColor = -15725298;
    public int backgroundAlpha = 200;
    public boolean animations = true;
    public String surfaceStyle = "LIQUID_GLASS";
    public boolean reglassEnabled = false;
    public boolean reglassFullGui = true;
    public boolean reglassChatMessages = true;
    public double glassBlur = 6.0;
    public double glassRefraction = 3.0;
    public double guiScale = 1.0;
    public int menuKey = 344;
    public CooldownSettings cooldowns = new CooldownSettings();
    public OpponentCooldownSettings opponentCooldowns = new OpponentCooldownSettings();
    public LegendarySoundSettings legendarySounds = new LegendarySoundSettings();
    public LegendarySizeSettings legendarySize = new LegendarySizeSettings();
    public LegendaryVisualSettings legendaryVisuals = new LegendaryVisualSettings();
    public BloodstonePackSettings resourcePack = new BloodstonePackSettings();
    public PvpSettings pvp = new PvpSettings();
    public MusicSettings music = new MusicSettings();
    public ArmorSettings armor = new ArmorSettings();
    public ItemCounterSettings itemCounter = new ItemCounterSettings();
    public NotificationSettings notifications = new NotificationSettings();
    public PerformanceSettings performance = new PerformanceSettings();
    public SessionSettings session = new SessionSettings();
    public EffectsSettings effects = new EffectsSettings();
    public DeathRecapSettings deathRecap = new DeathRecapSettings();
    public boolean loginGuard = true;
    public HeadsSettings heads = new HeadsSettings();
    public CirclesSettings bloodstoneCircles = new CirclesSettings();
    public ChatToolsSettings chatTools = new ChatToolsSettings();
    public AbilityGuideSettings abilityGuide = new AbilityGuideSettings();
    public QuickCommandsSettings quickCommands = new QuickCommandsSettings();
    public WaypointSettings waypoints = new WaypointSettings();
    public SizeTimerSettings sizeTimer = new SizeTimerSettings();
    public StreamerSettings streamer = new StreamerSettings();
    public ZoomSettings zoom = new ZoomSettings();
    public DeathScreenshotSettings deathScreenshot = new DeathScreenshotSettings();
    public ChatMentionsSettings chatMentions = new ChatMentionsSettings();
    public CooldownReadySettings cooldownReady = new CooldownReadySettings();
    public CombatTimelineSettings combatTimeline = new CombatTimelineSettings();
    public CombatHudSettings combatHud = new CombatHudSettings();
    public MusicDuckingSettings musicDucking = new MusicDuckingSettings();
    public DropGuardSettings dropGuard = new DropGuardSettings();
    public SlotLockSettings slotLocks = new SlotLockSettings();
    public ShulkerPreviewSettings shulkerPreview = new ShulkerPreviewSettings();
    public ChatDraftsSettings chatDrafts = new ChatDraftsSettings();
    public CoordinateLinksSettings coordinateLinks = new CoordinateLinksSettings();
    public BackgroundAlertsSettings backgroundAlerts = new BackgroundAlertsSettings();
    public DiscordPresenceSettings discordPresence = new DiscordPresenceSettings();
    public WatermarkSettings watermark = new WatermarkSettings();

    public ClientConfig() {
        VLRestrictions.enforce(this);
    }

    private static PlayerDisplaySettings.CombatMetric combatMetric(double x, double y) {
        PlayerDisplaySettings.CombatMetric value = new PlayerDisplaySettings.CombatMetric();
        value.position = new HudPosition(x, y);
        return value;
    }

    private static WidgetSettings hudMetric(double x, double y) {
        WidgetSettings value = new WidgetSettings();
        value.position = new HudPosition(x, y);
        return value;
    }

    public boolean hudTitle(String key) {
        return !this.simpleHud && this.hudDetails && !Boolean.FALSE.equals(this.hudTitles.get(key));
    }

    public void setHudTitle(String key, boolean show) {
        if (show) {
            this.hudTitles.remove(key);
        } else {
            this.hudTitles.put(key, false);
        }
    }

    public boolean serverAllowsModule(String id) {
        return RemoteModulePolicy.allows(id, this.serverSafeMode);
    }

    public boolean betterNameVisibilityEnabled() {
        return this.serverAllowsModule("betterNameVisibility") && this.betterNameVisibility.enabled;
    }

    public boolean legendaryVisualsEnabled() {
        return this.serverAllowsModule("legendaryVisuals") && this.legendaryVisuals.enabled;
    }

    public boolean opponentCooldownsEnabled() {
        return this.serverAllowsModule("opponentCooldowns") && this.opponentCooldowns.enabled;
    }

    public boolean pvpTimerEnabled() {
        return this.combatHud.enabled && this.pvp.enabled;
    }

    public boolean combatLayoutEnabled() {
        return this.combatHud.enabled && this.combatHud.useCombatLayout;
    }

    public void sanitize() {
        boolean compactUpgrade;
        if (this.staffRulebook == null) {
            this.staffRulebook = new StaffRulebookSettings();
        }
        this.staffRulebook.sanitize();
        if (this.staffChat == null) {
            this.staffChat = new StaffChatSettings();
        }
        this.staffChat.sanitize();
        if (this.staffChecks == null) {
            this.staffChecks = new StaffCheckSettings();
        }
        this.staffChecks.sanitize();
        if (this.chatCalculator == null) {
            this.chatCalculator = new ChatCalculatorSettings();
        }
        this.chatCalculator.sanitize();
        if (this.notificationSounds == null) {
            this.notificationSounds = new NotificationSoundSettings();
        }
        this.notificationSounds.sanitize();
        if (this.sanctions == null) {
            this.sanctions = new SanctionSettings();
        }
        this.sanctions.sanitize();
        if (this.hudItemControls == null) {
            this.hudItemControls = new HudItemControlsSettings();
        }
        this.hudItemControls.sanitize();
        if (this.staffWorkspace == null) {
            this.staffWorkspace = new StaffWorkspaceSettings();
        }
        this.staffWorkspace.sanitize();
        if (this.worldVisuals == null) {
            this.worldVisuals = new WorldVisualSettings();
        }
        this.worldVisuals.sanitize();
        this.screenshotAutoCopy = false;
        if (this.spectatorTimer == null) {
            this.spectatorTimer = new SpectatorTimerSettings();
        }
        ClientConfig.widget(this.spectatorTimer, new SpectatorTimerSettings());
        if (this.staffStats == null) {
            this.staffStats = new StaffStatsSettings();
        }
        ClientConfig.widget(this.staffStats, new StaffStatsSettings());
        if (this.chatViolations == null) {
            this.chatViolations = new ChatViolationsSettings();
        }
        this.chatViolations.sanitize();
        if (this.crosshair == null) {
            this.crosshair = new CrosshairSettings();
        }
        this.crosshair.sanitize();
        if (this.generalHud == null) {
            this.generalHud = new GeneralHudSettings();
        }
        this.generalHud.sanitize();
        if (this.reachDisplay == null) {
            this.reachDisplay = new PlayerDisplaySettings.CombatMetric();
        }
        this.reachDisplay.sanitize();
        if (this.comboCounter == null) {
            this.comboCounter = ClientConfig.combatMetric(0.6, 0.64);
        }
        this.comboCounter.sanitize();
        if (this.saturation == null) {
            this.saturation = new PlayerDisplaySettings.Saturation();
        }
        ClientConfig.sanitizeWidget(this.saturation, new PlayerDisplaySettings.Saturation());
        if (this.hitboxes == null) {
            this.hitboxes = new PlayerDisplaySettings.Hitboxes();
        }
        this.hitboxes.sanitize();
        if (this.freeLook == null) {
            this.freeLook = new PlayerDisplaySettings.FreeLook();
        }
        this.freeLook.sanitize();
        if (this.skins == null) {
            this.skins = new SkinsSettings();
        }
        this.skins.sanitize();
        if (this.otherTiers == null) {
            this.otherTiers = new PlayerDisplaySettings.OtherTiers();
        }
        this.otherTiers.sanitize();
        if (this.aiAssistant == null) {
            this.aiAssistant = new AiAssistantSettings();
        }
        this.aiAssistant.sanitize();
        if (this.aspectRatio == null) {
            this.aspectRatio = new AspectRatioSettings();
        }
        this.aspectRatio.sanitize();
        if (this.motionBlur == null) {
            this.motionBlur = new MotionBlurSettings();
        }
        this.motionBlur.normalize();
        if (this.hudTitles == null) {
            this.hudTitles = new LinkedHashMap<String, Boolean>();
        }
        this.hudTitles.entrySet().removeIf(e -> e.getKey() == null || ((String)e.getKey()).length() > 96 || e.getValue() == null);
        if (this.hudTitles.size() > 256) {
            Iterator<Map.Entry<String, Boolean>> iterator = this.hudTitles.entrySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (i >= 256) {
                    iterator.remove();
                }
                ++i;
            }
        }
        if (this.optimizer == null) {
            this.optimizer = new OptimizerSettings();
        }
        this.optimizer.sanitize();
        if (this.betterNameVisibility == null) {
            this.betterNameVisibility = new NameVisibilitySettings();
        }
        this.betterNameVisibility.sanitize();
        boolean bl = compactUpgrade = this.schemaVersion < 3;
        if (this.schemaVersion < 2) {
            if ((this.accent & 0xFFFFFF) == 14761807) {
                this.accent = -3401955;
            }
            if ((this.backgroundColor & 0xFFFFFF) == 0x101116) {
                this.backgroundColor = -15725298;
            }
        }
        if (this.schemaVersion < 4 && Double.isFinite(this.guiScale) && Math.abs(this.guiScale - 1.0) > 1.0E-4) {
            this.guiScale /= 0.7;
        }
        this.schemaVersion = 4;
        this.language = "en".equals(this.language) ? "en" : "ru";
        this.accent |= 0xFF000000;
        this.themeMode = ClientConfig.oneOf(this.themeMode, "DARK", "LIGHT");
        if (this.hudEditor == null) {
            this.hudEditor = new HudEditorSettings();
        }
        this.hudEditor.sanitize();
        if (this.renderQuality == null) {
            this.renderQuality = new RenderQualitySettings();
        }
        this.renderQuality.sanitize();
        this.backgroundColor |= 0xFF000000;
        this.backgroundAlpha = Math.clamp((long)this.backgroundAlpha, 0, 255);
        this.surfaceStyle = ClientConfig.oneOf(this.surfaceStyle, "LIQUID_GLASS", "MATTE", "OUTLINE");
        this.glassBlur = ClientConfig.finite(this.glassBlur, 6.0, 0.0, 16.0);
        this.glassRefraction = ClientConfig.finite(this.glassRefraction, 3.0, 0.0, 10.0);
        this.guiScale = ClientConfig.finite(this.guiScale, 1.0, 0.65, 2.0);
        if (this.menuKey < 32 || this.menuKey > 348) {
            this.menuKey = 344;
        }
        if (this.cooldowns == null) {
            this.cooldowns = new CooldownSettings();
        }
        if (this.opponentCooldowns == null) {
            this.opponentCooldowns = new OpponentCooldownSettings();
        }
        if (this.legendarySounds == null) {
            this.legendarySounds = new LegendarySoundSettings();
        }
        if (this.legendarySize == null) {
            this.legendarySize = new LegendarySizeSettings();
        }
        this.legendarySize.sanitize();
        if (this.resourcePack == null) {
            this.resourcePack = new BloodstonePackSettings();
        }
        this.legendarySounds.volume = ClientConfig.finite(this.legendarySounds.volume, 1.0, 0.0, 1.0);
        this.legendarySounds.minecraftVolume = ClientConfig.finite(this.legendarySounds.minecraftVolume, 1.0, 0.0, 1.0);
        if (this.legendarySounds.abilities == null) {
            this.legendarySounds.abilities = new LinkedHashMap<String, Double>();
        }
        LinkedHashMap<String, Double> safeSoundVolumes = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : this.legendarySounds.abilities.entrySet()) {
            if (safeSoundVolumes.size() >= 256) break;
            String string = entry.getKey();
            if (string == null || !string.matches("[a-z0-9_.-]{1,80}")) continue;
            safeSoundVolumes.put(string, entry.getValue() == null ? 1.0 : ClientConfig.finite(entry.getValue(), 1.0, 0.0, 1.0));
        }
        this.legendarySounds.abilities = safeSoundVolumes;
        this.legendarySounds.killEffectsMode = ClientConfig.oneOf(this.legendarySounds.killEffectsMode, "ALL", "SELECTED");
        this.legendarySounds.killEffectsVolume = ClientConfig.finite(this.legendarySounds.killEffectsVolume, 1.0, 0.0, 1.0);
        LinkedHashMap<String, Double> safeKillVolumes = new LinkedHashMap<String, Double>();
        if (this.legendarySounds.killEffects != null) {
            for (Map.Entry<String, Double> entry : this.legendarySounds.killEffects.entrySet()) {
                if (!KillEffectSoundCatalog.contains(entry.getKey())) continue;
                safeKillVolumes.put(entry.getKey(), entry.getValue() == null ? 1.0 : ClientConfig.finite(entry.getValue(), 1.0, 0.0, 1.0));
            }
        }
        this.legendarySounds.killEffects = safeKillVolumes;
        this.opponentCooldowns.scale = ClientConfig.finite(this.opponentCooldowns.scale, 1.0, 0.5, 2.0);
        this.opponentCooldowns.maxDistance = ClientConfig.finite(this.opponentCooldowns.maxDistance, 32.0, 8.0, 64.0);
        this.opponentCooldowns.maxVisible = Math.clamp((long)this.opponentCooldowns.maxVisible, 1, 8);
        if (this.pvp == null) {
            this.pvp = new PvpSettings();
        }
        if (this.music == null) {
            this.music = new MusicSettings();
        }
        this.music.hudLyricLines = Math.clamp((long)this.music.hudLyricLines, 1, 3);
        this.cooldowns.position = ClientConfig.position(this.cooldowns.position, 0.02, 0.35);
        this.pvp.position = ClientConfig.position(this.pvp.position, 0.98, 0.08);
        this.music.position = ClientConfig.position(this.music.position, 0.5, 0.035);
        this.cooldowns.alignment = ClientConfig.alignment(this.cooldowns.alignment);
        this.cooldowns.layout = ClientConfig.oneOf(this.cooldowns.layout, "COMPACT", "DETAILED");
        this.pvp.alignment = ClientConfig.alignment(this.pvp.alignment);
        if (this.pvp.cooldownGroup == null || !this.pvp.cooldownGroup.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            this.pvp.cooldownGroup = "minecraft:trident";
        }
        this.music.preferredSource = MediaSources.cleanId(this.music.preferredSource);
        this.music.lyricsSource = ClientConfig.oneOf(this.music.lyricsSource, "AUTO", "LRCLIB", "KUGOU", "NETEASE", "LYRICS_OVH");
        this.music.lyricsMode = ClientConfig.oneOf(this.music.lyricsMode, "OFF", "ISLAND", "WORLD", "BOTH");
        this.music.lyricAnimation = ClientConfig.oneOf(this.music.lyricAnimation, "FLOW", "TYPEWRITER", "POP", "SLIDE", "FADE");
        this.music.lyricColor = ClientConfig.oneOf(this.music.lyricColor, "THEME", "GRADIENT", "WHITE");
        this.music.lyricOffsetMs = Math.clamp((long)this.music.lyricOffsetMs, -5000, 5000);
        if (this.music.lyricAdjustments == null) {
            this.music.lyricAdjustments = new LyricsPreferences();
        }
        this.music.lyricAdjustments.sanitize();
        this.music.lyricLines = Math.clamp((long)this.music.lyricLines, 1, 6);
        this.music.lyricDistance = ClientConfig.finite(this.music.lyricDistance, 6.0, 1.2, 20.0);
        this.music.lyricSize = ClientConfig.finite(this.music.lyricSize, 1.0, 0.4, 3.0);
        this.music.lyricSpread = ClientConfig.finite(this.music.lyricSpread, 65.0, 20.0, 85.0);
        this.music.lyricFloat = ClientConfig.finite(this.music.lyricFloat, 0.3, 0.0, 2.0);
        if (this.armor == null) {
            this.armor = new ArmorSettings();
        }
        if (this.itemCounter == null) {
            this.itemCounter = new ItemCounterSettings();
        }
        if (this.notifications == null) {
            this.notifications = new NotificationSettings();
        }
        if (this.performance == null) {
            this.performance = new PerformanceSettings();
        }
        if (this.session == null) {
            this.session = new SessionSettings();
        }
        ClientConfig.widget(this.armor, new ArmorSettings());
        ClientConfig.widget(this.itemCounter, new ItemCounterSettings());
        ClientConfig.widget(this.notifications, new NotificationSettings());
        ClientConfig.widget(this.performance, new PerformanceSettings());
        ClientConfig.widget(this.session, new SessionSettings());
        this.armor.layout = ClientConfig.oneOf(this.armor.layout, "GROUPED", "SEPARATE");
        this.armor.orientation = ClientConfig.oneOf(this.armor.orientation, "HORIZONTAL", "VERTICAL");
        if (this.armor.parts == null) {
            this.armor.parts = new LinkedHashMap<String, HudPosition>();
        }
        this.armor.parts.keySet().removeIf(key -> key == null || !List.of("head", "chest", "legs", "feet").contains(key));
        for (String string : List.of("head", "chest", "legs", "feet")) {
            HudPosition hudPosition = ClientConfig.armorPartDefault(string);
            this.armor.parts.put(string, ClientConfig.position(this.armor.parts.get(string), hudPosition.x, hudPosition.y));
        }
        this.itemCounter.layout = ClientConfig.oneOf(this.itemCounter.layout, "GROUPED", "SEPARATE");
        this.itemCounter.orientation = ClientConfig.oneOf(this.itemCounter.orientation, "HORIZONTAL", "VERTICAL");
        this.armor.durabilityMode = ClientConfig.oneOf(this.armor.durabilityMode, "PERCENT", "POINTS");
        this.armor.warnPercent = Math.clamp((long)this.armor.warnPercent, 1, 90);
        this.itemCounter.lowCount = Math.clamp((long)this.itemCounter.lowCount, 0, 64);
        if (this.itemCounter.items == null) {
            this.itemCounter.items = new ItemCounterSettings().items;
        }
        this.itemCounter.items = new ArrayList<String>(this.itemCounter.items.stream().filter(Objects::nonNull).map(String::strip).filter(id -> id.length() <= 128 && id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")).distinct().limit(8L).toList());
        if (this.itemCounter.parts == null) {
            this.itemCounter.parts = new LinkedHashMap<String, HudPosition>();
        }
        LinkedHashMap<String, HudPosition> linkedHashMap = new LinkedHashMap<String, HudPosition>();
        for (String string : this.itemCounter.items) {
            HudPosition defaults = ClientConfig.itemPartDefault(this.itemCounter.items.indexOf(string));
            linkedHashMap.put(string, ClientConfig.position(this.itemCounter.parts.get(string), defaults.x, defaults.y));
        }
        for (Map.Entry<String, HudPosition> entry : this.itemCounter.parts.entrySet()) {
            if (linkedHashMap.size() >= 256) break;
            String id4 = entry.getKey();
            if (id4 == null || id4.length() > 128 || !id4.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || linkedHashMap.containsKey(id4)) continue;
            HudPosition hudPosition = ClientConfig.itemPartDefault(0);
            linkedHashMap.put(id4, ClientConfig.position(entry.getValue(), hudPosition.x, hudPosition.y));
        }
        this.itemCounter.parts = linkedHashMap;
        this.notifications.effectSeconds = Math.clamp((long)this.notifications.effectSeconds, 1, 60);
        this.notifications.durationSeconds = Math.clamp((long)this.notifications.durationSeconds, 2, 10);
        this.notifications.maxVisible = Math.clamp((long)this.notifications.maxVisible, 1, 4);
        if (this.effects == null) {
            this.effects = new EffectsSettings();
        }
        ClientConfig.widget(this.effects, new EffectsSettings());
        this.effects.maxVisible = Math.clamp((long)this.effects.maxVisible, 1, 12);
        if (this.deathRecap == null) {
            this.deathRecap = new DeathRecapSettings();
        }
        this.deathRecap.historyLimit = Math.clamp((long)this.deathRecap.historyLimit, 1, 30);
        if (this.heads == null) {
            this.heads = new HeadsSettings();
        }
        if (this.bloodstoneCircles == null) {
            this.bloodstoneCircles = new CirclesSettings();
        }
        if (this.chatTools == null) {
            this.chatTools = new ChatToolsSettings();
        }
        this.chatTools.maxMessages = Math.clamp((long)this.chatTools.maxMessages, 100, 2000);
        if (this.abilityGuide == null) {
            this.abilityGuide = new AbilityGuideSettings();
        }
        if (this.quickCommands == null) {
            this.quickCommands = new QuickCommandsSettings();
        }
        if (this.waypoints == null) {
            this.waypoints = new WaypointSettings();
        }
        if (this.legendaryVisuals == null) {
            this.legendaryVisuals = new LegendaryVisualSettings();
        }
        this.legendaryVisuals.sanitize();
        if (this.sizeTimer == null) {
            this.sizeTimer = new SizeTimerSettings();
        }
        ClientConfig.widget(this.sizeTimer, new SizeTimerSettings());
        this.sizeTimer.sanitize();
        if (this.streamer == null) {
            this.streamer = new StreamerSettings();
        }
        this.abilityGuide.sanitize();
        this.quickCommands.sanitize();
        this.waypoints.sanitize();
        this.streamer.sanitize();
        if (this.zoom == null) {
            this.zoom = new ZoomSettings();
        }
        if (this.deathScreenshot == null) {
            this.deathScreenshot = new DeathScreenshotSettings();
        }
        this.zoom.sanitize();
        this.deathScreenshot.sanitize();
        if (this.chatMentions == null) {
            this.chatMentions = new ChatMentionsSettings();
        }
        if (this.cooldownReady == null) {
            this.cooldownReady = new CooldownReadySettings();
        }
        if (this.combatTimeline == null) {
            this.combatTimeline = new CombatTimelineSettings();
        }
        if (this.combatHud == null) {
            this.combatHud = new CombatHudSettings();
        }
        if (this.musicDucking == null) {
            this.musicDucking = new MusicDuckingSettings();
        }
        if (this.dropGuard == null) {
            this.dropGuard = new DropGuardSettings();
        }
        this.chatMentions.sanitize();
        this.cooldownReady.sanitize();
        this.combatTimeline.sanitize();
        this.combatHud.sanitize();
        this.combatHud.migrateModules(this.pvp);
        this.combatHud.migrateCooldownPreset(this.cooldowns);
        this.musicDucking.sanitize();
        this.dropGuard.sanitize();
        if (this.slotLocks == null) {
            this.slotLocks = new SlotLockSettings();
        }
        this.slotLocks.sanitize();
        if (this.inventoryUi == null) {
            this.inventoryUi = new InventoryUiSettings();
        }
        this.inventoryUi.sanitize();
        if (this.shulkerPreview == null) {
            this.shulkerPreview = new ShulkerPreviewSettings();
        }
        if (this.chatDrafts == null) {
            this.chatDrafts = new ChatDraftsSettings();
        }
        if (this.coordinateLinks == null) {
            this.coordinateLinks = new CoordinateLinksSettings();
        }
        if (this.backgroundAlerts == null) {
            this.backgroundAlerts = new BackgroundAlertsSettings();
        }
        this.shulkerPreview.sanitize();
        this.chatDrafts.sanitize();
        this.coordinateLinks.sanitize();
        this.backgroundAlerts.sanitize();
        if (this.discordPresence == null) {
            this.discordPresence = new DiscordPresenceSettings();
        }
        this.discordPresence.sanitize();
        if (this.watermark == null) {
            this.watermark = new WatermarkSettings();
        }
        ClientConfig.widget(this.watermark, new WatermarkSettings());
        this.watermark.sanitize();
        this.performance.sanitizeParts();
        this.session.mode = ClientConfig.oneOf(this.session.mode, "FFA", "SMP", "MEETUPS");
        if (this.session.killMessageTemplate == null) {
            this.session.killMessageTemplate = "";
        }
        this.session.killMessageTemplate = this.session.killMessageTemplate.replaceAll("[\\p{Cntrl}]", "").strip();
        if (this.session.killMessageTemplate.length() > 256) {
            this.session.killMessageTemplate = "";
        }
        if (compactUpgrade) {
            for (HudPosition hudPosition : new HudPosition[]{this.cooldowns.position, this.pvp.position, this.music.position, this.armor.position, this.itemCounter.position, this.notifications.position, this.performance.position, this.session.position}) {
                if (hudPosition.scale != 1.0) continue;
                hudPosition.scale = 0.65;
            }
        }
        VLRestrictions.enforce(this);
    }

    private static HudPosition armorPartDefault(String slot) {
        int index = switch (slot) {
            case "chest" -> 1;
            case "legs" -> 2;
            case "feet" -> 3;
            default -> 0;
        };
        return new HudPosition(0.02, 0.55 + (double)index * 0.1);
    }

    private static HudPosition itemPartDefault(int index) {
        return new HudPosition(0.28 + (double)(index % 2) * 0.22, 0.8 - (double)(index / 2) * 0.1);
    }

    public static void sanitizeWidget(WidgetSettings value, WidgetSettings defaults) {
        ClientConfig.widget(value, defaults);
    }

    private static void widget(WidgetSettings value, WidgetSettings defaults) {
        value.position = ClientConfig.position(value.position, defaults.position.x, defaults.position.y);
        value.alignment = ClientConfig.oneOf(value.alignment, defaults.alignment, "LEFT", "CENTER", "RIGHT");
    }

    private static HudPosition position(HudPosition value, double x, double y) {
        if (value == null) {
            value = new HudPosition(x, y);
        }
        value.x = ClientConfig.finite(value.x, x, 0.0, 1.0);
        value.y = ClientConfig.finite(value.y, y, 0.0, 1.0);
        value.scale = ClientConfig.finite(value.scale, 0.65, 0.35, 2.0);
        return value;
    }

    private static double finite(double value, double fallback, double min, double max) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }

    private static String alignment(String value) {
        return ClientConfig.oneOf(value, "LEFT", "CENTER", "RIGHT");
    }

    private static String oneOf(String value, String ... options) {
        for (String option : options) {
            if (!option.equals(value)) continue;
            return option;
        }
        return options[0];
    }

    public static final class CooldownSettings {
        public boolean enabled = false;
        public String layout = "COMPACT";
        public boolean nearCrosshair = false;
        public boolean showNames = true;
        public boolean showProgress = true;
        public boolean showUnknown = true;
        public boolean shadow = true;
        public boolean background = true;
        public String alignment = "LEFT";
        public boolean bottom = false;
        public HudPosition position = new HudPosition(0.02, 0.35);
    }

    public static final class OpponentCooldownSettings {
        public boolean enabled = false;
        public double scale = 1.0;
        public double maxDistance = 32.0;
        public int maxVisible = 4;
        public boolean background = true;
        public boolean abilityIcons = true;
    }

    public static final class LegendarySoundSettings {
        public boolean enabled = false;
        public double volume = 1.0;
        public double minecraftVolume = 1.0;
        public Map<String, Double> abilities = new LinkedHashMap<String, Double>();
        public String killEffectsMode = "ALL";
        public double killEffectsVolume = 1.0;
        public Map<String, Double> killEffects = new LinkedHashMap<String, Double>();

        public double killEffectVolume(String id) {
            Double value = this.killEffects == null ? null : this.killEffects.get(id);
            return value == null ? 1.0 : ClientConfig.finite(value, 1.0, 0.0, 1.0);
        }

        public double abilityVolume(String id) {
            Double value = this.abilities == null ? null : this.abilities.get(id);
            return value == null ? 1.0 : ClientConfig.finite(value, 1.0, 0.0, 1.0);
        }
    }

    public static final class BloodstonePackSettings {
        public boolean enabled = true;
    }

    public static final class PvpSettings {
        public boolean enabled = false;
        public boolean endNotification = true;
        public String cooldownGroup = "minecraft:trident";
        public boolean showProgress = true;
        public boolean shadow = true;
        public boolean background = true;
        public String alignment = "RIGHT";
        public boolean bottom = false;
        public HudPosition position = new HudPosition(0.98, 0.08);
    }

    public static final class MusicSettings {
        public int hudLyricLines = 1;
        public String preferredSource = "";
        public boolean enabled = false;
        public boolean showCover = true;
        public boolean showProgress = true;
        public boolean showControls = true;
        public boolean hideWhenIdle = false;
        public boolean compact = false;
        public String lyricsMode = "ISLAND";
        public String lyricsSource = "AUTO";
        public String lyricAnimation = "FLOW";
        public String lyricColor = "THEME";
        public boolean splitLyrics = true;
        public int lyricOffsetMs = 0;
        public LyricsPreferences lyricAdjustments = new LyricsPreferences();
        public int lyricLines = 3;
        public double lyricDistance = 6.0;
        public double lyricSize = 1.0;
        public boolean lyricGlow = true;
        public boolean lyricShadow = true;
        public double lyricSpread = 65.0;
        public double lyricFloat = 0.3;
        public HudPosition position = new HudPosition(0.5, 0.035);
    }

    public static final class ArmorSettings
    extends WidgetSettings {
        public String layout = "GROUPED";
        public String orientation = "HORIZONTAL";
        public Map<String, HudPosition> parts = new LinkedHashMap<String, HudPosition>();
        public String durabilityMode = "PERCENT";
        public boolean showNames = false;
        public boolean hideWhenEmpty = true;
        public int warnPercent = 20;

        public HudPosition partPosition(String slot) {
            return this.parts.computeIfAbsent(slot, key -> ClientConfig.armorPartDefault(key));
        }

        public ArmorSettings() {
            this.position = new HudPosition(0.02, 0.63);
        }
    }

    public static final class ItemCounterSettings
    extends WidgetSettings {
        public String layout = "GROUPED";
        public String orientation = "HORIZONTAL";
        public Map<String, HudPosition> parts = new LinkedHashMap<String, HudPosition>();
        public List<String> items = new ArrayList<String>(List.of("minecraft:totem_of_undying", "minecraft:ender_pearl", "minecraft:golden_apple", "minecraft:arrow"));
        public boolean showNames = false;
        public boolean hideZero = false;
        public int lowCount = 3;

        public HudPosition partPosition(String itemId) {
            return this.parts.computeIfAbsent(itemId, key -> ClientConfig.itemPartDefault(Math.max(0, this.items.indexOf(key))));
        }

        public ItemCounterSettings() {
            this.position = new HudPosition(0.5, 0.12);
            this.alignment = "CENTER";
            this.bottom = true;
        }
    }

    public static final class NotificationSettings
    extends WidgetSettings {
        public boolean armorWarnings = true;
        public boolean itemWarnings = true;
        public boolean effectWarnings = true;
        public boolean effectUpdates = true;
        public boolean sound = false;
        public int effectSeconds = 5;
        public int durationSeconds = 4;
        public int maxVisible = 3;

        public NotificationSettings() {
            this.position = new HudPosition(0.98, 0.78);
            this.alignment = "RIGHT";
        }
    }

    public static final class PerformanceSettings
    extends WidgetSettings {
        public boolean showFps = true;
        public boolean showPing = true;
        public boolean showFrameTime = true;
        public boolean showGraph = true;
        public String layout = "GROUPED";
        public Map<String, HudPosition> parts = new LinkedHashMap<String, HudPosition>();

        public HudPosition partPosition(String key) {
            int index = switch (key) {
                case "ping" -> 1;
                case "frame" -> 2;
                case "graph" -> 3;
                default -> 0;
            };
            return this.parts.computeIfAbsent(key, ignored -> new HudPosition(0.02 + (double)index * 0.22, 0.13));
        }

        public void sanitizeParts() {
            if (!"SEPARATE".equals(this.layout)) {
                this.layout = "GROUPED";
            }
            if (this.parts == null) {
                this.parts = new LinkedHashMap<String, HudPosition>();
            }
            this.parts.entrySet().removeIf(e -> e.getKey() == null || !Set.of("fps", "ping", "frame", "graph").contains(e.getKey()) || e.getValue() == null);
            this.parts.replaceAll((key, value) -> ClientConfig.position(value, 0.02, 0.13));
        }

        public PerformanceSettings() {
            this.position = new HudPosition(0.02, 0.035);
        }
    }

    public static final class SessionSettings
    extends WidgetSettings {
        public String mode = "FFA";
        public boolean showTime = true;
        public boolean showKd = true;
        public boolean showStreak = true;
        public boolean resetOnDisconnect = true;
        public String killMessageTemplate = "";

        public SessionSettings() {
            this.position = new HudPosition(0.98, 0.35);
            this.alignment = "RIGHT";
        }
    }

    public static final class EffectsSettings
    extends WidgetSettings {
        public boolean showHud = true;
        public boolean showNames = true;
        public boolean showLevel = true;
        public boolean sortByTime = true;
        public boolean hideVanilla = false;
        public int maxVisible = 5;

        public EffectsSettings() {
            this.position = new HudPosition(0.98, 0.51);
            this.alignment = "RIGHT";
        }
    }

    public static final class DeathRecapSettings {
        public boolean enabled = false;
        public boolean showCoordinates = true;
        public boolean showArmor = true;
        public boolean showEffects = true;
        public int historyLimit = 10;
    }

    public static final class ChatToolsSettings {
        public boolean enabled = false;
        public boolean timestamps = true;
        public boolean groupDuplicates = true;
        public int maxMessages = 500;
    }

    public static class HudPosition {
        public double x;
        public double y;
        public double scale = 0.65;

        public HudPosition() {
        }

        public HudPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class WidgetSettings {
        public boolean enabled = false;
        public boolean background = true;
        public boolean shadow = true;
        public String alignment = "LEFT";
        public boolean bottom = false;
        public HudPosition position = new HudPosition(0.02, 0.5);
    }
}

