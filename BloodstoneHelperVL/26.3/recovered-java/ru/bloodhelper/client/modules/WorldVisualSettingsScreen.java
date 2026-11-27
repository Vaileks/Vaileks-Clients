/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.modules;

import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.WorldVisualSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class WorldVisualSettingsScreen
extends ModuleDetailScreen {
    private final Section section;
    private final GuiTextField input = new GuiTextField(0, 0, 100, 21, "");
    private final List<Slider> sliders = new ArrayList<Slider>();
    private Slider dragging;
    private boolean updatingInput;
    private int viewportTop;
    private int viewportBottom;
    private List<BiomeRow> biomes;
    private Object biomeRegistryLevel;

    public WorldVisualSettingsScreen(Screen parent, Section section) {
        super(parent, switch (section.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Time Changer";
            case 1 -> "Weather Changer";
            case 2 -> "FullBright";
            case 3 -> "Ambience";
        });
        this.section = section;
        this.input.setMaxLength(section == Section.TIME ? 5 : 160);
        this.input.setTextSize(11.0f);
        if (section == Section.TIME) {
            this.input.setValue(Integer.toString(ConfigStore.get().worldVisuals.customTime));
        }
        this.input.setResponder(value -> {
            int time;
            if (this.updatingInput) {
                return;
            }
            this.scroll = 0;
            if (section == Section.TIME && value.matches("\\d{1,5}") && (time = Integer.parseInt(value)) <= 23999) {
                WorldVisualSettings s = ConfigStore.get().worldVisuals;
                s.customTime = time;
                s.timePreset = "CUSTOM";
                this.save();
            }
        });
    }

    @Override
    protected int preferredPanelHeight() {
        return this.section == Section.AMBIENCE ? 410 : 300;
    }

    @Override
    protected int preferredPanelWidth() {
        return 560;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        s = ConfigStore.get().worldVisuals;
        switch (this.section.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                v0 = HudStyle.tr("\u0412\u0440\u0435\u043c\u044f \u0441\u0443\u0442\u043e\u043a \u00b7 Time Changer", "Time Changer");
                break;
            }
            case 1: {
                v0 = HudStyle.tr("\u041f\u043e\u0433\u043e\u0434\u0430 \u00b7 Weather Changer", "Weather Changer");
                break;
            }
            case 2: {
                v0 = "FullBright";
                break;
            }
            case 3: {
                v0 = HudStyle.tr("\u041e\u043a\u0440\u0443\u0436\u0435\u043d\u0438\u0435 \u0431\u0438\u043e\u043c\u0430 \u00b7 Ambience", "Biome atmosphere \u00b7 Ambience");
            }
        }
        this.frame(g, v0, mx, my);
        this.sliders.clear();
        this.button(g, this.left + 14, this.bodyTop, 120, this.enabled(s) != false ? HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Enabled") : HudStyle.tr("\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Disabled"), mx, my, (Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$renderPanel$0(ru.bloodhelper.client.config.WorldVisualSettings ), ()V)((WorldVisualSettingsScreen)this, (WorldVisualSettings)s));
        this.text(g, CustomFont.regular().ellipsize(HudStyle.tr("\u0418\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0432\u0438\u0434\u043d\u044b \u0442\u043e\u043b\u044c\u043a\u043e \u0432\u0430\u043c", "Changes are visible only to you"), 11.0f, Math.max(0, this.panelWidth - 160)), this.left + 146, this.bodyTop + 6, -7105633);
        this.bodyTop += 32;
        this.viewportTop = this.bodyTop;
        this.viewportBottom = this.bodyBottom;
        contentActionsStart = this.actions.size();
        if (this.section != Section.AMBIENCE) {
            this.scrollbar(g, this.section == Section.TIME ? 145 : 160);
            g.enableScissor(this.left + 10, this.viewportTop, this.left + this.panelWidth - 10, this.viewportBottom);
            this.bodyTop -= this.scroll;
        }
        try {
            switch (this.section.ordinal()) {
                case 0: {
                    this.time(g, mx, my, delta, s);
                    ** break;
lbl33:
                    // 1 sources

                    break;
                }
                case 1: {
                    this.weather(g, mx, my, s);
                    ** break;
lbl37:
                    // 1 sources

                    break;
                }
                case 2: {
                    this.slider(g, this.bodyTop + 30, HudStyle.tr("\u042f\u0440\u043a\u043e\u0441\u0442\u044c", "Brightness"), s.brightnessPercent, 0, 100, "%", (IntConsumer)LambdaMetafactory.metafactory(null, null, null, (I)V, lambda$renderPanel$1(ru.bloodhelper.client.config.WorldVisualSettings int ), (I)V)((WorldVisualSettings)s));
                    this.paragraph(g, HudStyle.tr("\u041f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0442\u0451\u043c\u043d\u044b\u0445 \u0443\u0447\u0430\u0441\u0442\u043a\u043e\u0432 \u043c\u0438\u0440\u0430. \u041f\u043e\u0441\u043b\u0435 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u0432\u0435\u0440\u043d\u0451\u0442\u0441\u044f \u043e\u0431\u044b\u0447\u043d\u043e\u0435 \u043e\u0441\u0432\u0435\u0449\u0435\u043d\u0438\u0435.", "Brightens dark areas of the world. Disable to restore normal lighting."), this.bodyTop + 90);
                    ** break;
lbl42:
                    // 1 sources

                    break;
                }
                case 3: {
                    this.ambience(g, mx, my, delta, s);
                    break;
                }
                ** default:
lbl47:
                // 1 sources

                break;
            }
        }
        finally {
            if (this.section != Section.AMBIENCE) {
                this.bodyTop = this.viewportTop;
                g.disableScissor();
                for (i = this.actions.size() - 1; i >= contentActionsStart; --i) {
                    if (((ModuleDetailScreen.Action)this.actions.get(i)).y() >= this.viewportTop && ((ModuleDetailScreen.Action)this.actions.get(i)).y() + ((ModuleDetailScreen.Action)this.actions.get(i)).height() <= this.viewportBottom) continue;
                    this.actions.remove(i);
                }
            }
        }
        this.footer(g, this.enabled(s) != false ? HudStyle.tr("\u041f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0432 \u043c\u0438\u0440\u0435 \u00b7 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438", "World preview \u00b7 settings save automatically") : HudStyle.tr("\u041c\u043e\u0434\u0443\u043b\u044c \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d \u00b7 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u044b \u043c\u043e\u0436\u043d\u043e \u043d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c \u0437\u0430\u0440\u0430\u043d\u0435\u0435", "Module disabled \u00b7 you can adjust preferences in advance"));
    }

    private void time(GuiGraphicsExtractor g, int mx, int my, float delta, WorldVisualSettings s) {
        int width = Math.max(30, (this.panelWidth - 46) / 4);
        for (int i = 0; i < WorldVisualSettings.TIMES.size(); ++i) {
            String mode;
            String label = switch (mode = WorldVisualSettings.TIMES.get(i)) {
                case "DAY" -> HudStyle.tr("\u0414\u0435\u043d\u044c", "Day");
                case "EVENING" -> HudStyle.tr("\u0412\u0435\u0447\u0435\u0440", "Evening");
                case "NIGHT" -> HudStyle.tr("\u041d\u043e\u0447\u044c", "Night");
                default -> HudStyle.tr("\u0421\u0432\u043e\u0451", "Custom");
            };
            int x = this.left + 14 + i * (width + 6);
            this.button(g, x, this.bodyTop, width, label, mx, my, () -> {
                s.timePreset = mode;
                this.save();
            });
            if (!mode.equals(s.timePreset)) continue;
            UiDraw.outline(g, x, this.bodyTop, width, 21, 6, ThemeColors.accent());
        }
        this.slider(g, this.bodyTop + 47, HudStyle.tr("\u0421\u0432\u043e\u0451 \u0432\u0440\u0435\u043c\u044f", "Custom time"), s.customTime, 0, 23999, "", v -> {
            s.customTime = v;
            s.timePreset = "CUSTOM";
            this.updatingInput = true;
            try {
                this.input.setValue(Integer.toString(v));
            }
            finally {
                this.updatingInput = false;
            }
        });
        this.text(g, HudStyle.tr("\u0422\u043e\u0447\u043d\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 (0\u201323999)", "Exact value (0\u201323999)"), this.left + 14, this.bodyTop + 92, -7105633);
        this.field(g, this.bodyTop + 110, Math.min(150, this.panelWidth - 28), mx, my, delta, "0\u201323999");
        if (!this.input.getValue().matches("\\d{1,5}") || Integer.parseInt(this.input.getValue()) > 23999) {
            this.text(g, HudStyle.tr("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0447\u0438\u0441\u043b\u043e \u043e\u0442 0 \u0434\u043e 23999", "Enter a number from 0 to 23999"), this.left + 176, this.bodyTop + 116, -812133);
        }
    }

    private void weather(GuiGraphicsExtractor g, int mx, int my, WorldVisualSettings s) {
        int width = Math.max(30, (this.panelWidth - 40) / 3);
        for (int i = 0; i < WorldVisualSettings.WEATHER.size(); ++i) {
            String mode;
            String label = switch (mode = WorldVisualSettings.WEATHER.get(i)) {
                case "RAIN" -> HudStyle.tr("\u0414\u043e\u0436\u0434\u044c", "Rain");
                case "SNOW" -> HudStyle.tr("\u0421\u043d\u0435\u0433", "Snow");
                default -> HudStyle.tr("\u042f\u0441\u043d\u043e", "Clear");
            };
            int x = this.left + 14 + i * (width + 6);
            this.button(g, x, this.bodyTop, width, label, mx, my, () -> {
                s.weather = mode;
                this.save();
            });
            if (!mode.equals(s.weather)) continue;
            UiDraw.outline(g, x, this.bodyTop, width, 21, 6, ThemeColors.accent());
        }
        this.slider(g, this.bodyTop + 47, HudStyle.tr("\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u043e\u0441\u0430\u0434\u043a\u043e\u0432", "Precipitation intensity"), s.precipitationPercent, 10, 100, "%", v -> {
            s.precipitationPercent = v;
        });
        this.paragraph(g, HudStyle.tr("\u0414\u043e\u0436\u0434\u044c \u0438 \u0441\u043d\u0435\u0433 \u0432\u0438\u0434\u043d\u044b \u043d\u0430 \u043e\u0442\u043a\u0440\u044b\u0442\u043e\u043c \u0432\u043e\u0437\u0434\u0443\u0445\u0435. \u0412 \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u044f\u0445 \u0431\u0435\u0437 \u043f\u043e\u0433\u043e\u0434\u044b \u043e\u0441\u0430\u0434\u043a\u0438 \u043d\u0435 \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u044e\u0442\u0441\u044f.", "Rain and snow appear outdoors. Dimensions without weather stay unchanged."), this.bodyTop + 106);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void ambience(GuiGraphicsExtractor g, int mx, int my, float delta, WorldVisualSettings s) {
        this.field(g, this.bodyTop, this.panelWidth - 28, mx, my, delta, HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u0431\u0438\u043e\u043c\u0430 \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e \u0438\u043b\u0438 ID", "Search biome name or ID"));
        this.bodyTop += 31;
        if (this.minecraft.level == null) {
            this.paragraph(g, HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439\u0442\u0435 \u043c\u0438\u0440, \u0447\u0442\u043e\u0431\u044b \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u0431\u0438\u043e\u043c \u0438\u0437 \u0435\u0433\u043e \u0441\u043f\u0438\u0441\u043a\u0430. \u0421\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u044b\u0439 \u0431\u0438\u043e\u043c: ", "Join a world to choose one of its biomes. Saved biome: ") + s.biome, this.bodyTop + 12);
            return;
        }
        if (this.biomes == null || this.biomeRegistryLevel != this.minecraft.level) {
            this.biomeRegistryLevel = this.minecraft.level;
            this.biomes = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BIOME).keySet().stream().map(id -> {
                String key = "biome." + id.getNamespace() + "." + id.getPath().replace('/', '.');
                String name = Component.translatable((String)key).getString();
                return new BiomeRow(id.toString(), name.equals(key) ? id.toString() : name);
            }).sorted(Comparator.comparing(row -> !row.id.equals("minecraft:pale_garden")).thenComparing(BiomeRow::name)).toList();
        }
        String query = this.input.getValue().strip().toLowerCase(Locale.ROOT);
        List<BiomeRow> rows = this.biomes.stream().filter(row -> row.id.contains(query) || row.name.toLowerCase(Locale.ROOT).contains(query)).toList();
        this.scrollbar(g, Math.max(45, rows.size() * 43));
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (rows.isEmpty()) {
                this.text(g, HudStyle.tr("\u0411\u0438\u043e\u043c\u044b \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b", "No biomes found"), this.left + 18, this.bodyTop + 8, -7105633);
            }
            for (int i = Math.max(0, this.scroll / 43); i < rows.size(); ++i) {
                int y = this.bodyTop + i * 43 - this.scroll;
                if (y >= this.bodyBottom) {
                    break;
                }
                BiomeRow row2 = rows.get(i);
                boolean selected = row2.id.equals(s.biome);
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 38, 6, ThemeColors.surface(selected ? -12898246 : -14211282));
                this.text(g, CustomFont.regular().ellipsize(row2.name, 11.0f, this.panelWidth - 138), this.left + 23, y + 6, -1381908);
                CustomFont.regular().draw(g, CustomFont.regular().ellipsize(row2.id, 9.0f, this.panelWidth - 138), this.left + 23, y + 22, 9.0f, -7105633);
                if (y < this.bodyTop || y + 31 > this.bodyBottom) continue;
                this.button(g, this.left + this.panelWidth - 101, y + 8, 78, selected ? HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d", "Selected") : HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u0442\u044c", "Select"), mx, my, () -> {
                    s.biome = row.id;
                    this.save();
                });
            }
        }
        finally {
            g.disableScissor();
        }
    }

    private void field(GuiGraphicsExtractor g, int y, int width, int mx, int my, float delta, String hint) {
        UiDraw.rounded(g, this.left + 14, y, width, 23, 6, ThemeColors.surface(-14211282));
        this.input.setX(this.left + 21);
        this.input.setY(y + 1);
        this.input.setWidth(width - 14);
        this.input.setHeight(21);
        this.input.setHint(hint);
        this.input.render(g, mx, my, delta);
    }

    private void paragraph(GuiGraphicsExtractor g, String message, int y) {
        for (String line : WorldVisualSettingsScreen.wrap(message, this.panelWidth - 40)) {
            if (y + 14 > this.bodyBottom) break;
            this.text(g, line, this.left + 18, y, -7105633);
            y += 15;
        }
    }

    private void slider(GuiGraphicsExtractor g, int y, String label, int value, int min, int max, String suffix, IntConsumer change) {
        int x = this.left + 18;
        int width = Math.max(16, this.panelWidth - 40);
        this.text(g, label + ": " + value + suffix, x, y, -1381908);
        float fraction = Math.clamp((float)(value - min) / (float)(max - min), 0.0f, 1.0f);
        UiDraw.rounded(g, x, y + 24, width, 3, 1, ThemeColors.surface(-11184801));
        UiDraw.rounded(g, x, y + 24, Math.round((float)width * fraction), 3, 1, ThemeColors.accent());
        UiDraw.rounded(g, x + Math.round((float)width * fraction) - 4, y + 20, 8, 11, 4, -1381908);
        this.sliders.add(new Slider(x, y + 16, width, min, max, change));
    }

    private boolean enabled(WorldVisualSettings s) {
        return switch (this.section.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> s.timeEnabled;
            case 1 -> s.weatherEnabled;
            case 2 -> s.fullBrightEnabled;
            case 3 -> s.ambienceEnabled;
        };
    }

    private void setEnabled(WorldVisualSettings s, boolean value) {
        switch (this.section.ordinal()) {
            case 0: {
                s.timeEnabled = value;
                break;
            }
            case 1: {
                s.weatherEnabled = value;
                break;
            }
            case 2: {
                s.fullBrightEnabled = value;
                break;
            }
            case 3: {
                s.ambienceEnabled = value;
            }
        }
    }

    private void save() {
        BloodHelperClient.saveConfig();
    }

    private void changeSlider(double x) {
        if (this.dragging == null) {
            return;
        }
        this.dragging.change.accept((int)Math.round((double)this.dragging.min + (double)(this.dragging.max - this.dragging.min) * Math.clamp((x - (double)this.dragging.x) / (double)this.dragging.width, 0.0, 1.0)));
    }

    private void finishDrag() {
        if (this.dragging != null) {
            this.dragging = null;
            this.save();
        }
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent e, boolean doubled) {
        boolean inside;
        boolean bl = inside = e.y() >= (double)this.viewportTop && e.y() < (double)this.viewportBottom;
        if ((this.section == Section.TIME || this.section == Section.AMBIENCE) && inside && this.input.mouseClicked(e, doubled)) {
            return true;
        }
        if (!inside) {
            this.input.setFocused(false);
        }
        if (e.button() == 1 && inside) {
            for (Slider slider : this.sliders) {
                if (!(e.x() >= (double)(slider.x - 5)) || !(e.x() <= (double)(slider.x + slider.width + 5)) || !(e.y() >= (double)slider.y) || !(e.y() < (double)(slider.y + 21))) continue;
                this.dragging = slider;
                this.changeSlider(e.x());
                return true;
            }
        }
        return super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (this.dragging != null) {
            this.changeSlider(e.x());
            return true;
        }
        return this.input.mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent e) {
        if (this.dragging != null) {
            this.finishDrag();
            return true;
        }
        return this.input.mouseReleased(e) || super.panelMouseReleased(e);
    }

    public boolean keyPressed(KeyEvent e) {
        return this.input.keyPressed(e) || super.keyPressed(e);
    }

    public boolean charTyped(CharacterEvent e) {
        return this.input.charTyped(e) || super.charTyped(e);
    }

    public void removed() {
        this.finishDrag();
    }

    private static /* synthetic */ void lambda$renderPanel$1(WorldVisualSettings s, int v) {
        s.brightnessPercent = v;
    }

    private /* synthetic */ void lambda$renderPanel$0(WorldVisualSettings s) {
        this.setEnabled(s, !this.enabled(s));
        this.save();
    }

    public static enum Section {
        TIME,
        WEATHER,
        FULLBRIGHT,
        AMBIENCE;

    }

    private record BiomeRow(String id, String name) {
    }

    private record Slider(int x, int y, int width, int min, int max, IntConsumer change) {
    }
}

