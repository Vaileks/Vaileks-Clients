/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.utilites.color;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pyz1k.module.setting.ColorSetting;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class ClientTheme {
    public static final String ACCENT = "accent";
    public static final String ACCENT_SECOND = "accentSecond";
    public static final String MENU_BACKGROUND = "menuBackground";
    public static final String SIDEBAR_BACKGROUND = "sidebarBackground";
    public static final String SETTINGS_BACKGROUND = "settingsBackground";
    public static final String OUTLINE = "outline";
    public static final String PRIMARY_TEXT = "primaryText";
    public static final String SECONDARY_TEXT = "secondaryText";
    public static final String SCROLLBAR = "scrollbar";
    public static final String HUD_BACKGROUND = "hudBackground";
    public static final String HUD_INSET = "hudInset";
    public static final String HUD_OUTLINE = "hudOutline";
    public static final String HUD_SHADOW = "hudShadow";
    public static final String HUD_TEXT = "hudText";
    public static final String HUD_SECONDARY_TEXT = "hudSecondaryText";
    private static final ColorSetting ACCENT_SETTING = new ColorSetting("Акцент визуалов", ColorUtil.rgba(115, 115, 203, 255));
    private static final ColorSetting ACCENT_SECOND_SETTING = new ColorSetting("Дополнительный акцент", ColorUtil.rgba(190, 190, 238, 255));
    private static final ColorSetting MENU_BACKGROUND_SETTING = new ColorSetting("Фон ClickGUI", ColorUtil.rgba(8, 9, 12, 255));
    private static final ColorSetting SIDEBAR_BACKGROUND_SETTING = new ColorSetting("Фон боковой панели", ColorUtil.rgba(10, 11, 14, 255));
    private static final ColorSetting SETTINGS_BACKGROUND_SETTING = new ColorSetting("Фон настроек", ColorUtil.rgba(13, 14, 18, 255));
    private static final ColorSetting OUTLINE_SETTING = new ColorSetting("Обводки ClickGUI", ColorUtil.rgba(255, 255, 255, 20));
    private static final ColorSetting PRIMARY_TEXT_SETTING = new ColorSetting("Основной текст GUI", ColorUtil.rgba(244, 244, 248, 255));
    private static final ColorSetting SECONDARY_TEXT_SETTING = new ColorSetting("Вторичный текст GUI", ColorUtil.rgba(104, 103, 109, 255));
    private static final ColorSetting SCROLLBAR_SETTING = new ColorSetting("Полоса прокрутки", ColorUtil.rgba(178, 178, 214, 220));
    private static final ColorSetting HUD_BACKGROUND_SETTING = new ColorSetting("Фон HUD", ColorUtil.rgba(7, 8, 11, 166));
    private static final ColorSetting HUD_INSET_SETTING = new ColorSetting("Внутренний слой HUD", ColorUtil.rgba(24, 25, 30, 38));
    private static final ColorSetting HUD_OUTLINE_SETTING = new ColorSetting("Обводки HUD", ColorUtil.rgba(255, 255, 255, 20));
    private static final ColorSetting HUD_SHADOW_SETTING = new ColorSetting("Тень HUD", ColorUtil.rgba(4, 4, 6, 92));
    private static final ColorSetting HUD_TEXT_SETTING = new ColorSetting("Основной текст HUD", ColorUtil.rgba(244, 244, 247, 255));
    private static final ColorSetting HUD_SECONDARY_TEXT_SETTING = new ColorSetting("Вторичный текст HUD", ColorUtil.rgba(155, 155, 164, 255));
    private static final LinkedHashMap<String, ColorSetting> EDITABLE = new LinkedHashMap();
    private static boolean lightMode;
    private static boolean rainbowMode;
    private static String activeThemeName;

    private ClientTheme() {
    }

    public static ColorSetting getAccentSetting() {
        return ACCENT_SETTING;
    }

    public static List<ColorSetting> getEditableSettings() {
        return List.copyOf(EDITABLE.values());
    }

    public static String keyOf(ColorSetting setting) {
        for (Map.Entry<String, ColorSetting> entry : EDITABLE.entrySet()) {
            if (entry.getValue() != setting) continue;
            return entry.getKey();
        }
        return null;
    }

    public static int getSecondAccentColor() {
        return ACCENT_SECOND_SETTING.getColor();
    }

    public static void setAccentColor(int color) {
        rainbowMode = false;
        int opaque = ColorUtil.withAlpha(color, 255);
        ACCENT_SETTING.setColor(opaque);
        ACCENT_SECOND_SETTING.setColor(ClientTheme.deriveSecondAccent(opaque));
        ClientTheme.applyPalette();
    }

    public static void syncSetting(ColorSetting setting) {
        if (!rainbowMode && setting != null && EDITABLE.containsValue(setting)) {
            ClientTheme.applyPalette();
        }
    }

    public static void syncFromSetting() {
        ClientTheme.applyPalette();
    }

    public static boolean isLightMode() {
        return lightMode;
    }

    public static void setLightMode(boolean light) {
        rainbowMode = false;
        lightMode = light;
        activeThemeName = light ? "Light" : "Dark";
        ClientTheme.applyColors(light ? ClientTheme.lightPreset() : ClientTheme.darkPreset());
    }

    public static String getActiveThemeName() {
        return activeThemeName;
    }

    public static boolean isRainbowMode() {
        return rainbowMode;
    }

    public static void enableRainbowMode() {
        rainbowMode = false;
        lightMode = false;
        ClientTheme.applyColors(ClientTheme.darkPreset());
        rainbowMode = true;
        activeThemeName = "Rainbow";
        ClientTheme.updateDynamicColors();
    }

    public static void updateDynamicColors() {
        if (rainbowMode) {
            float hue = (float)(System.currentTimeMillis() % 6000L) / 6000.0f;
            int first = 0xFF000000 | Color.HSBtoRGB(hue, 0.78f, 1.0f) & 0xFFFFFF;
            int second = 0xFF000000 | Color.HSBtoRGB((hue + 0.13f) % 1.0f, 0.64f, 1.0f) & 0xFFFFFF;
            ClientColors.CATEGORY_ACCENT = first;
            ClientColors.CATEGORY_ACCENT_SECOND = second;
            ClientColors.CATEGORY_UNDERLINE = second;
        }
    }

    public static void setActiveThemeName(String name) {
        if (name != null && !name.isBlank()) {
            activeThemeName = name;
        }
    }

    public static Map<String, Integer> captureColors() {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<String, Integer>();
        EDITABLE.forEach((key, setting) -> colors.put((String)key, setting.getColor()));
        return colors;
    }

    public static void applyColors(Map<String, Integer> colors) {
        if (colors != null) {
            colors.forEach((key, value) -> {
                ColorSetting setting = EDITABLE.get(key);
                if (setting != null && value != null) {
                    setting.setColor((int)value);
                }
            });
        }
        ClientTheme.applyPalette();
    }

    public static Map<String, Integer> darkPreset() {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<String, Integer>();
        colors.put(ACCENT, ColorUtil.rgba(115, 115, 203, 255));
        colors.put(ACCENT_SECOND, ColorUtil.rgba(190, 190, 238, 255));
        colors.put(MENU_BACKGROUND, ColorUtil.rgba(8, 9, 12, 255));
        colors.put(SIDEBAR_BACKGROUND, ColorUtil.rgba(10, 11, 14, 255));
        colors.put(SETTINGS_BACKGROUND, ColorUtil.rgba(13, 14, 18, 255));
        colors.put(OUTLINE, ColorUtil.rgba(255, 255, 255, 20));
        colors.put(PRIMARY_TEXT, ColorUtil.rgba(244, 244, 248, 255));
        colors.put(SECONDARY_TEXT, ColorUtil.rgba(104, 103, 109, 255));
        colors.put(SCROLLBAR, ColorUtil.rgba(178, 178, 214, 220));
        colors.put(HUD_BACKGROUND, ColorUtil.rgba(7, 8, 11, 166));
        colors.put(HUD_INSET, ColorUtil.rgba(24, 25, 30, 38));
        colors.put(HUD_OUTLINE, ColorUtil.rgba(255, 255, 255, 20));
        colors.put(HUD_SHADOW, ColorUtil.rgba(4, 4, 6, 92));
        colors.put(HUD_TEXT, ColorUtil.rgba(244, 244, 247, 255));
        colors.put(HUD_SECONDARY_TEXT, ColorUtil.rgba(155, 155, 164, 255));
        return colors;
    }

    public static Map<String, Integer> lightPreset() {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<String, Integer>();
        colors.put(ACCENT, ColorUtil.rgba(96, 96, 190, 255));
        colors.put(ACCENT_SECOND, ColorUtil.rgba(132, 132, 211, 255));
        colors.put(MENU_BACKGROUND, ColorUtil.rgba(244, 245, 248, 255));
        colors.put(SIDEBAR_BACKGROUND, ColorUtil.rgba(250, 250, 252, 255));
        colors.put(SETTINGS_BACKGROUND, ColorUtil.rgba(232, 233, 238, 255));
        colors.put(OUTLINE, ColorUtil.rgba(24, 27, 35, 28));
        colors.put(PRIMARY_TEXT, ColorUtil.rgba(25, 27, 33, 255));
        colors.put(SECONDARY_TEXT, ColorUtil.rgba(111, 113, 122, 255));
        colors.put(SCROLLBAR, ColorUtil.rgba(92, 94, 112, 210));
        colors.put(HUD_BACKGROUND, ColorUtil.rgba(246, 247, 250, 190));
        colors.put(HUD_INSET, ColorUtil.rgba(213, 216, 224, 48));
        colors.put(HUD_OUTLINE, ColorUtil.rgba(20, 24, 32, 24));
        colors.put(HUD_SHADOW, ColorUtil.rgba(22, 25, 34, 48));
        colors.put(HUD_TEXT, ColorUtil.rgba(27, 29, 35, 255));
        colors.put(HUD_SECONDARY_TEXT, ColorUtil.rgba(93, 96, 106, 255));
        return colors;
    }

    private static void applyPalette() {
        ClientColors.CATEGORY_ACCENT = ACCENT_SETTING.getColor();
        ClientColors.CATEGORY_ACCENT_SECOND = ACCENT_SECOND_SETTING.getColor();
        ClientColors.CATEGORY_UNDERLINE = ACCENT_SECOND_SETTING.getColor();
        ClientColors.MENU_BACKGROUND = MENU_BACKGROUND_SETTING.getColor();
        ClientColors.CATEGORY_BACKGROUND = SIDEBAR_BACKGROUND_SETTING.getColor();
        ClientColors.SETTINGS_BACKGROUND = SETTINGS_BACKGROUND_SETTING.getColor();
        ClientColors.CATEGORY_BACKGROUND_OUTLINE = OUTLINE_SETTING.getColor();
        ClientColors.SETTINGS_OUTLINE = OUTLINE_SETTING.getColor();
        ClientColors.TEXT_PRIMARY = PRIMARY_TEXT_SETTING.getColor();
        ClientColors.CATEGORY_INACTIVE = SECONDARY_TEXT_SETTING.getColor();
        ClientColors.SCROLLBAR = SCROLLBAR_SETTING.getColor();
        ClientColors.HUD_BACKGROUND = HUD_BACKGROUND_SETTING.getColor();
        ClientColors.HUD_INSET = HUD_INSET_SETTING.getColor();
        ClientColors.HUD_OUTLINE = HUD_OUTLINE_SETTING.getColor();
        ClientColors.HUD_SHADOW = HUD_SHADOW_SETTING.getColor();
        ClientColors.HUD_TEXT = HUD_TEXT_SETTING.getColor();
        ClientColors.HUD_SECONDARY_TEXT = HUD_SECONDARY_TEXT_SETTING.getColor();
        HudVisualStyle.applyThemePalette(lightMode);
        ClientTheme.updateDynamicColors();
    }

    private static int deriveSecondAccent(int color) {
        int bright = ColorUtil.interpolate(color, ColorUtil.rgba(255, 255, 255, 255), 0.42f);
        return ColorUtil.rgba(Math.max(ColorUtil.red(color), ColorUtil.red(bright)), Math.max(ColorUtil.green(color), ColorUtil.green(bright)), Math.max(ColorUtil.blue(color), ColorUtil.blue(bright)), 255);
    }

    static {
        activeThemeName = "Dark";
        EDITABLE.put(ACCENT, ACCENT_SETTING);
        EDITABLE.put(ACCENT_SECOND, ACCENT_SECOND_SETTING);
        EDITABLE.put(MENU_BACKGROUND, MENU_BACKGROUND_SETTING);
        EDITABLE.put(SIDEBAR_BACKGROUND, SIDEBAR_BACKGROUND_SETTING);
        EDITABLE.put(SETTINGS_BACKGROUND, SETTINGS_BACKGROUND_SETTING);
        EDITABLE.put(OUTLINE, OUTLINE_SETTING);
        EDITABLE.put(PRIMARY_TEXT, PRIMARY_TEXT_SETTING);
        EDITABLE.put(SECONDARY_TEXT, SECONDARY_TEXT_SETTING);
        EDITABLE.put(SCROLLBAR, SCROLLBAR_SETTING);
        EDITABLE.put(HUD_BACKGROUND, HUD_BACKGROUND_SETTING);
        EDITABLE.put(HUD_INSET, HUD_INSET_SETTING);
        EDITABLE.put(HUD_OUTLINE, HUD_OUTLINE_SETTING);
        EDITABLE.put(HUD_SHADOW, HUD_SHADOW_SETTING);
        EDITABLE.put(HUD_TEXT, HUD_TEXT_SETTING);
        EDITABLE.put(HUD_SECONDARY_TEXT, HUD_SECONDARY_TEXT_SETTING);
    }
}

