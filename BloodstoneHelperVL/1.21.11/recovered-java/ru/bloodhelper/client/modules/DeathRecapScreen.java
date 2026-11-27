/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.EffectTime;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CombatTimelineRecorder;
import ru.bloodhelper.client.modules.DeathRecapModule;
import ru.bloodhelper.client.modules.DeathScreenshotModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class DeathRecapScreen
extends ModuleDetailScreen {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM  HH:mm:ss").withZone(ZoneId.systemDefault());
    private int selected;
    private boolean timelineView;
    private List<CombatTimelineRecorder.Event> cachedTimeline;
    private List<TimelineRow> cachedRows = List.of();
    private String cachedLanguage;
    private Object languageTables;

    @Override
    protected int preferredPanelHeight() {
        return DeathRecapModule.history().isEmpty() ? 174 : 410;
    }

    public DeathRecapScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0435 \u0441\u043c\u0435\u0440\u0442\u0438", "Death Recap"));
    }

    public void showTimeline() {
        this.timelineView = true;
        this.scroll = 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mouseX, int mouseY, float delta) {
        int contentHeight;
        int navigationEnd;
        int tabWidth;
        this.frame(g, HudStyle.tr("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0435 \u0441\u043c\u0435\u0440\u0442\u0438", "Death Recap"), mouseX, mouseY);
        List<DeathRecapModule.Snapshot> history = DeathRecapModule.history();
        this.selected = Math.clamp((long)this.selected, 0, Math.max(0, history.size() - 1));
        this.button(g, this.left + this.panelWidth - 104, this.top + 47, 90, HudStyle.tr("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Clear"), mouseX, mouseY, () -> {
            DeathRecapModule.clear();
            this.selected = 0;
            this.scroll = 0;
            this.timelineView = false;
        });
        if (history.size() > 1) {
            this.button(g, this.left + 14, this.top + 47, 26, "\u2039", mouseX, mouseY, () -> {
                this.selected = Math.max(0, this.selected - 1);
                this.scroll = 0;
            });
            this.button(g, this.left + 46, this.top + 47, 26, "\u203a", mouseX, mouseY, () -> {
                this.selected = Math.min(history.size() - 1, this.selected + 1);
                this.scroll = 0;
            });
            this.text(g, this.selected + 1 + " / " + history.size(), this.left + 82, this.top + 52, -7105633);
        }
        if (!history.isEmpty() && (tabWidth = Math.min(180, this.panelWidth - (navigationEnd = history.size() > 1 ? 154 : 14) - 114)) > 24) {
            this.button(g, this.left + navigationEnd, this.top + 47, tabWidth, this.timelineView ? HudStyle.tr("\u041e\u0431\u0437\u043e\u0440 \u0441\u043c\u0435\u0440\u0442\u0438", "Death overview") : HudStyle.tr("\u0425\u0440\u043e\u043d\u043e\u043b\u043e\u0433\u0438\u044f \u0431\u043e\u044f", "Combat Timeline"), mouseX, mouseY, () -> {
                this.timelineView = !this.timelineView;
                this.scroll = 0;
            });
        }
        this.bodyTop = this.top + 80;
        if (!history.isEmpty()) {
            DeathRecapModule.Snapshot selectedDeath = history.get(this.selected);
            boolean canMark = WaypointsModule.canMarkDeath(selectedDeath);
            boolean hasImage = selectedDeath.screenshotFile() != null;
            int firstWidth = Math.min(170, canMark && hasImage ? (this.panelWidth - 36) / 2 : this.panelWidth - 28);
            if (canMark) {
                this.button(g, this.left + 14, this.top + 76, firstWidth, HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u043c\u0435\u0441\u0442\u0430 \u0441\u043c\u0435\u0440\u0442\u0438", "Mark death location"), mouseX, mouseY, () -> WaypointsModule.markDeath(selectedDeath, this));
            }
            if (hasImage) {
                this.button(g, this.left + 14 + (canMark ? firstWidth + 8 : 0), this.top + 76, Math.min(170, canMark ? this.panelWidth - 36 - firstWidth : this.panelWidth - 28), HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442 \u0441\u043c\u0435\u0440\u0442\u0438", "View screenshot"), mouseX, mouseY, () -> DeathScreenshotModule.openScreenshot(this, selectedDeath.screenshotFile()));
            }
            if (canMark || hasImage) {
                this.bodyTop = this.top + 106;
            }
        }
        ClientConfig.DeathRecapSettings config = BloodHelperClient.config().deathRecap;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int y = this.bodyTop - this.scroll;
            if (history.isEmpty()) {
                List<String> explanation = DeathRecapScreen.wrap(config.enabled ? HudStyle.tr("\u041f\u043e\u0441\u043b\u0435 \u0442\u0432\u043e\u0435\u0439 \u0441\u043c\u0435\u0440\u0442\u0438 \u0437\u0434\u0435\u0441\u044c \u043f\u043e\u044f\u0432\u044f\u0442\u0441\u044f \u043c\u0435\u0441\u0442\u043e, \u0431\u0440\u043e\u043d\u044f \u0438 \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u043f\u0435\u0440\u0435\u0434 \u0441\u043c\u0435\u0440\u0442\u044c\u044e.", "After your death, this view will show the location, armor and effects observed before it.") : HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c, \u0447\u0442\u043e\u0431\u044b \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c \u0441\u0432\u043e\u0438 \u0441\u043c\u0435\u0440\u0442\u0438 \u0432 \u044d\u0442\u043e\u0439 \u0441\u0435\u0441\u0441\u0438\u0438.", "Enable this module to record your deaths during this session."), this.panelWidth - 40);
                for (String line : explanation) {
                    this.text(g, line, this.left + 20, y + 12, -7105633);
                    y += 15;
                }
                contentHeight = explanation.size() * 15 + 24;
            } else {
                DeathRecapModule.Snapshot snapshot = history.get(this.selected);
                CustomFont.medium().draw(g, DATE.format(snapshot.time()), this.left + 20, y, 12.0f, -1381908);
                y += 24;
                if (this.timelineView) {
                    y = this.timeline(g, snapshot.timeline(), y);
                    contentHeight = y - this.bodyTop + this.scroll + 10;
                } else {
                    if (snapshot.deathMessage() != null && !StreamerMode.hideChat() && !StreamerMode.hideNickname()) {
                        for (String line : DeathRecapScreen.wrap(UiLanguage.text(snapshot.deathMessage()), this.panelWidth - 40)) {
                            this.text(g, line, this.left + 20, y, -7105633);
                            y += 15;
                        }
                        y += 10;
                    }
                    if (config.showCoordinates && !StreamerMode.hideCoordinates()) {
                        this.text(g, "X " + snapshot.x() + "   Y " + snapshot.y() + "   Z " + snapshot.z(), this.left + 20, y, -1381908);
                        this.text(g, CustomFont.regular().ellipsize(DeathRecapScreen.dimension(snapshot.dimension()), 11.0f, this.panelWidth - 40), this.left + 20, y += 18, -7105633);
                        y += 27;
                    }
                    if (config.showArmor) {
                        CustomFont.medium().draw(g, HudStyle.tr("\u0411\u0440\u043e\u043d\u044f \u043f\u0435\u0440\u0435\u0434 \u0441\u043c\u0435\u0440\u0442\u044c\u044e", "Armor before death"), this.left + 20, y, 11.0f, -1381908);
                        y += 19;
                        for (DeathRecapModule.Armor piece : snapshot.armor()) {
                            if (!piece.item().method_7960()) {
                                g.method_51427(piece.item(), this.left + 20, y);
                            }
                            String name = piece.item().method_7960() ? DeathRecapScreen.slot(piece.slot().method_5923()) + " \u2014 " + HudStyle.tr("\u043f\u0443\u0441\u0442\u043e", "empty") : UiLanguage.text(piece.item().method_7964());
                            String condition = piece.item().method_7960() ? "" : piece.durability().display("PERCENT");
                            float valueWidth = CustomFont.medium().width(condition, 11.0f);
                            name = CustomFont.regular().ellipsize(name, 11.0f, Math.max(0.0f, (float)(this.panelWidth - 84) - valueWidth));
                            this.text(g, name, this.left + 44, y + 3, piece.item().method_7960() ? -7105633 : -1381908);
                            CustomFont.medium().draw(g, condition, (float)(this.left + this.panelWidth - 22) - valueWidth, y + 3, 11.0f, piece.durability().low(20) ? -1931896 : -7105633);
                            y += 23;
                        }
                        y += 10;
                    }
                    if (config.showEffects) {
                        CustomFont.medium().draw(g, HudStyle.tr("\u042d\u0444\u0444\u0435\u043a\u0442\u044b \u043f\u0435\u0440\u0435\u0434 \u0441\u043c\u0435\u0440\u0442\u044c\u044e", "Effects before death"), this.left + 20, y, 11.0f, -1381908);
                        y += 21;
                        if (snapshot.effects().isEmpty()) {
                            this.text(g, HudStyle.tr("\u041d\u0435\u0442 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432", "No effects"), this.left + 20, y, -7105633);
                            y += 18;
                        }
                        for (DeathRecapModule.Effect effect : snapshot.effects()) {
                            String duration = EffectTime.format(effect.ticks(), effect.infinite());
                            String name = UiLanguage.text(effect.name()) + (String)(effect.level() > 1 ? " " + effect.level() : "");
                            float durationWidth = CustomFont.regular().width(duration, 11.0f);
                            this.text(g, CustomFont.regular().ellipsize(name, 11.0f, Math.max(0.0f, (float)(this.panelWidth - 62) - durationWidth)), this.left + 20, y, -1381908);
                            this.text(g, duration, (int)((float)(this.left + this.panelWidth - 22) - durationWidth), y, -7105633);
                            y += 19;
                        }
                    }
                    contentHeight = y - this.bodyTop + this.scroll + 10;
                }
            }
        }
        finally {
            g.method_44380();
        }
        this.scrollbar(g, contentHeight);
        this.footer(g, this.timelineView ? HudStyle.tr("\u041d\u0430\u0431\u043b\u044e\u0434\u0435\u043d\u0438\u044f \u043a\u043b\u0438\u0435\u043d\u0442\u0430 \u00b7 2 HP = 1 \u0441\u0435\u0440\u0434\u0446\u0435 \u00b7 \u0411\u0435\u0437 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u0438\u044f \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0430 \u0443\u0440\u043e\u043d\u0430", "Client observations \u00b7 2 HP = 1 heart \u00b7 No inferred damage source") : HudStyle.tr("\u042d\u043a\u0438\u043f\u0438\u0440\u043e\u0432\u043a\u0430 \u0438 \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u0441 \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0435\u0433\u043e \u0436\u0438\u0432\u043e\u0433\u043e \u0442\u0438\u043a\u0430 \u00b7 \u0422\u043e\u043b\u044c\u043a\u043e \u0442\u0435\u043a\u0443\u0449\u0430\u044f \u0441\u0435\u0441\u0441\u0438\u044f", "Equipment and effects from the last living tick \u00b7 Current session only"));
    }

    private int timeline(class_332 g, List<CombatTimelineRecorder.Event> events, int y) {
        CustomFont.medium().draw(g, HudStyle.tr("\u0425\u0440\u043e\u043d\u043e\u043b\u043e\u0433\u0438\u044f \u0431\u043e\u044f", "Combat Timeline"), this.left + 20, y, 12.0f, -1381908);
        y += 24;
        if (events.isEmpty()) {
            for (String line : DeathRecapScreen.wrap(HudStyle.tr("\u0414\u043b\u044f \u044d\u0442\u043e\u0439 \u0441\u043c\u0435\u0440\u0442\u0438 \u0445\u0440\u043e\u043d\u043e\u043b\u043e\u0433\u0438\u044f \u043d\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u0430. \u0412\u043a\u043b\u044e\u0447\u0438 \u0435\u0451 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 Death Recap \u0434\u043e \u0431\u043e\u044f.", "No timeline was recorded for this death. Enable it in Death Recap before combat."), this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, y, -7105633);
                y += 16;
            }
            return y + 10;
        }
        this.text(g, HudStyle.tr("\u0421\u0435\u043a\u0443\u043d\u0434\u044b \u0434\u043e \u0441\u043c\u0435\u0440\u0442\u0438", "Seconds before death"), this.left + 20, y, -7105633);
        y += 22;
        String language = BloodHelperClient.config().language;
        Object tables = UiLanguage.cacheToken();
        if (this.cachedTimeline != events || !Objects.equals(this.cachedLanguage, language) || this.languageTables != tables) {
            this.cachedTimeline = events;
            this.cachedLanguage = language;
            this.languageTables = tables;
            this.cachedRows = events.stream().map(event -> new TimelineRow((String)(event.beforeDeathMillis() == 0L ? "0.0" : "\u2212" + DeathRecapScreen.number((double)event.beforeDeathMillis() / 1000.0)), DeathRecapScreen.describe(event), event.kind())).toList();
        }
        int index = 0;
        for (TimelineRow row : this.cachedRows) {
            if (y + 31 >= this.bodyTop && y < this.bodyBottom) {
                if ((index & 1) == 0) {
                    UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 30, 31, 5, ThemeColors.surface(1497052475));
                }
                UiDraw.rounded(g, this.left + 76, y + 7, 3, 17, 1, ThemeColors.surface(row.kind() == CombatTimelineRecorder.Kind.DEATH ? -3057562 : -8949124));
                CustomFont.regular().draw(g, row.time(), (float)(this.left + 68) - CustomFont.regular().width(row.time(), 10.0f), y + 10, 10.0f, -7105633);
                this.text(g, CustomFont.regular().ellipsize(row.description(), 11.0f, Math.max(1, this.panelWidth - 115)), this.left + 89, y + 9, -1381908);
            }
            y += 34;
            ++index;
        }
        return y + 8;
    }

    private static String describe(CombatTimelineRecorder.Event event) {
        String effect = event.effectKey().isEmpty() ? "" : UiLanguage.text((class_2561)class_2561.method_43471((String)event.effectKey()));
        return switch (event.kind()) {
            default -> throw new MatchException(null, null);
            case CombatTimelineRecorder.Kind.HEALTH_LOSS -> HudStyle.tr("\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435", "Health") + "  \u2212" + DeathRecapScreen.number(event.before() - event.after()) + " HP   \u00b7   " + DeathRecapScreen.number(event.before()) + " \u2192 " + DeathRecapScreen.number(event.after());
            case CombatTimelineRecorder.Kind.HEALTH_GAIN -> HudStyle.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435", "Health restored") + "  +" + DeathRecapScreen.number(event.after() - event.before()) + " HP   \u00b7   " + DeathRecapScreen.number(event.before()) + " \u2192 " + DeathRecapScreen.number(event.after());
            case CombatTimelineRecorder.Kind.ABSORPTION_LOSS -> HudStyle.tr("\u041f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u0435", "Absorption") + "  \u2212" + DeathRecapScreen.number(event.before() - event.after()) + " HP";
            case CombatTimelineRecorder.Kind.ABSORPTION_GAIN -> HudStyle.tr("\u041f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u0435", "Absorption") + "  +" + DeathRecapScreen.number(event.after() - event.before()) + " HP";
            case CombatTimelineRecorder.Kind.EFFECT_GAIN -> HudStyle.tr("\u041f\u043e\u043b\u0443\u0447\u0435\u043d \u044d\u0444\u0444\u0435\u043a\u0442: ", "Effect gained: ") + effect + DeathRecapScreen.level(event.afterLevel());
            case CombatTimelineRecorder.Kind.EFFECT_LOSS -> (event.observedExpiry() ? HudStyle.tr("\u0422\u0430\u0439\u043c\u0435\u0440 \u044d\u0444\u0444\u0435\u043a\u0442\u0430 \u0438\u0441\u0442\u0451\u043a: ", "Effect timer elapsed: ") : HudStyle.tr("\u042d\u0444\u0444\u0435\u043a\u0442 \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442: ", "Effect no longer active: ")) + effect + DeathRecapScreen.level(event.beforeLevel());
            case CombatTimelineRecorder.Kind.EFFECT_LEVEL -> effect + "  " + event.beforeLevel() + " \u2192 " + event.afterLevel();
            case CombatTimelineRecorder.Kind.TOTEM -> HudStyle.tr("\u0421\u0440\u0430\u0431\u043e\u0442\u0430\u043b \u0442\u043e\u0442\u0435\u043c", "Totem activated");
            case CombatTimelineRecorder.Kind.DEATH -> HudStyle.tr("\u0421\u043c\u0435\u0440\u0442\u044c", "Death");
        };
    }

    private static String level(int level) {
        return level > 1 ? " " + level : "";
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String slot(String name) {
        return switch (name) {
            case "head" -> HudStyle.tr("\u0428\u043b\u0435\u043c", "Helmet");
            case "chest" -> HudStyle.tr("\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", "Chestplate");
            case "legs" -> HudStyle.tr("\u041f\u043e\u043d\u043e\u0436\u0438", "Leggings");
            case "feet" -> HudStyle.tr("\u0411\u043e\u0442\u0438\u043d\u043a\u0438", "Boots");
            default -> name;
        };
    }

    private static String dimension(String value) {
        return switch (value) {
            case "minecraft:overworld" -> HudStyle.tr("\u0412\u0435\u0440\u0445\u043d\u0438\u0439 \u043c\u0438\u0440", "Overworld");
            case "minecraft:the_nether" -> HudStyle.tr("\u041d\u0435\u0437\u0435\u0440", "Nether");
            case "minecraft:the_end" -> HudStyle.tr("\u042d\u043d\u0434", "The End");
            default -> value;
        };
    }

    private record TimelineRow(String time, String description, CombatTimelineRecorder.Kind kind) {
    }
}

