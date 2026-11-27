/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.extensions.pvp;

import pyz1k.extensions.pvp.Config;
import pyz1k.extensions.pvp.Core;
import pyz1k.extensions.pvp.Feature;
import pyz1k.extensions.pvp.ReportScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import pyz1k.compat.LegacyInput;

public final class SettingsScreen
extends Screen {
    final Screen parent;
    final Feature feature;
    Config draft;
    int page = 0;
    int colorTarget = 0;
    int captureKey = 0;
    boolean dragging;
    double dragX;
    double dragY;
    int[] bounds;

    public SettingsScreen(Screen parent, Feature feature) {
        super((Component)Component.literal((String)feature.title));
        this.parent = parent;
        this.feature = feature;
        this.draft = Core.config(feature).copy();
    }

    void refresh() {
        this.rebuildWidgets();
    }

    String yes(boolean b) {
        return b ? "\u0414\u0430" : "\u041d\u0435\u0442";
    }

    int cw() {
        return Math.min(170, (this.width - 24) / 2 - 4);
    }

    int left() {
        return this.width / 2 - this.cw() - 4;
    }

    void button(int col, int row, String name, Runnable fn) {
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)name), b -> fn.run()).bounds(this.left() + col * (this.cw() + 8), 56 + row * 24, this.cw(), 20).build());
    }

    void toggle(int col, int row, String label, BooleanSupplier get, Runnable change) {
        this.button(col, row, label + ": " + this.yes(get.getAsBoolean()), () -> {
            change.run();
            this.refresh();
        });
    }

    void slider(int col, int row, final String name, final double min, final double max, DoubleSupplier get, final DoubleConsumer set, final boolean decimal) {
        this.addRenderableWidget(new AbstractSliderButton( this.left() + col * (this.cw() + 8), 56 + row * 24, this.cw(), 20, (Component)Component.empty(), (get.getAsDouble() - min) / (max - min)){
            {
                this.updateMessage();
            }

            protected void updateMessage() {
                this.setMessage((Component)Component.literal((String)(name + ": " + (decimal ? String.format(Locale.ROOT, "%.2f", min + this.value * (max - min)) : String.valueOf((int)Math.round(min + this.value * (max - min)))))));
            }

            protected void applyValue() {
                set.accept(decimal ? min + this.value * (max - min) : (double)Math.round(min + this.value * (max - min)));
                this.updateMessage();
            }
        });
    }

    protected void init() {
        int i;
        int tw = Math.min(110, (this.width - 24) / 3 - 7);
        String[] tabs = new String[]{"\u0412\u043d\u0435\u0448\u043d\u0438\u0439 \u0432\u0438\u0434", "\u0424\u0443\u043d\u043a\u0446\u0438\u0438", "\u041f\u043e\u0437\u0438\u0446\u0438\u044f HUD"};
        for (i = 0; i < 3; ++i) {
            int n = i;
            Button b2 = Button.builder((Component)Component.literal((String)tabs[i]), v -> {
                this.page = n;
                this.captureKey = 0;
                this.refresh();
            }).bounds(this.width / 2 - (tw * 3 + 14) / 2 + i * (tw + 7), 28, tw, 20).build();
            b2.active = this.page != i;
            this.addRenderableWidget(b2);
        }
        if (this.page == 0) {
            this.toggle(0, 0, "\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043e", () -> this.draft.enabled, () -> {
                this.draft.enabled = !this.draft.enabled;
            });
            this.toggle(1, 0, "\u0422\u0435\u043d\u044c \u0442\u0435\u043a\u0441\u0442\u0430", () -> this.draft.shadow, () -> {
                this.draft.shadow = !this.draft.shadow;
            });
            this.toggle(0, 1, "\u0424\u043e\u043d HUD", () -> this.draft.background, () -> {
                this.draft.background = !this.draft.background;
            });
            this.toggle(1, 1, "\u0418\u043a\u043e\u043d\u043a\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", () -> this.draft.icons, () -> {
                this.draft.icons = !this.draft.icons;
            });
            this.slider(0, 2, "\u0420\u0430\u0437\u043c\u0435\u0440 HUD", 0.5, 2.5, () -> this.draft.scale, v -> {
                this.draft.scale = v;
            }, true);
            this.button(1, 2, "\u0426\u0432\u0435\u0442 \u0442\u0435\u043a\u0441\u0442\u0430", () -> {
                this.colorTarget = 0;
                this.page = 3;
                this.refresh();
            });
            this.button(0, 3, "\u0426\u0432\u0435\u0442 \u0430\u043a\u0446\u0435\u043d\u0442\u0430 / \u0442\u0440\u0435\u0432\u043e\u0433\u0438", () -> {
                this.colorTarget = 1;
                this.page = 3;
                this.refresh();
            });
            this.button(1, 3, "\u041f\u043e\u0437\u0438\u0446\u0438\u044f HUD", () -> {
                this.page = 2;
                this.refresh();
            });
        } else if (this.page == 1) {
            if (this.feature == Feature.COMBAT_FOCUS || this.feature == Feature.HUD_ON_DEMAND) {
                this.toggle(0, 0, "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c scoreboard", () -> this.draft.hideScoreboard, () -> {
                    this.draft.hideScoreboard = !this.draft.hideScoreboard;
                });
            } else if (Set.of(Feature.READY_AGAIN, Feature.EFFECT_ALARM, Feature.ARMOR_PANIC, Feature.TOTEM_PULSE, Feature.OFFHAND_ALERT).contains((Object)this.feature)) {
                this.toggle(0, 0, "\u0417\u0432\u0443\u043a\u043e\u0432\u043e\u0439 \u0441\u0438\u0433\u043d\u0430\u043b", () -> this.draft.sound, () -> {
                    this.draft.sound = !this.draft.sound;
                });
            }
            this.button(1, 0, "\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435: " + SettingsScreen.keyName(this.draft.actionKey), () -> {
                this.captureKey = 1;
                this.refresh();
            });
            if (Set.of(Feature.ARMOR_PANIC, Feature.EFFECT_ALARM, Feature.REFILL_REMINDER, Feature.TOTEM_PULSE, Feature.FIGHT_RECAP, Feature.FIGHT_TIMELINE, Feature.COMBAT_FOCUS, Feature.READY_AGAIN, Feature.INVENTORY_SEARCH_FLASH).contains((Object)this.feature)) {
                this.slider(0, 1, this.thresholdName(), 1.0, this.feature == Feature.ARMOR_PANIC ? 100.0 : 120.0, () -> this.draft.threshold, v -> {
                    this.draft.threshold = (int)v;
                }, false);
            }
            this.button(1, 1, this.modeName(), () -> {
                this.draft.mode = (this.draft.mode + 1) % (this.feature==Feature.INVENTORY_SEARCH_FLASH?4:3);
                if (this.feature != Feature.OFFHAND_ALERT && this.feature != Feature.INVENTORY_SEARCH_FLASH) {
                    this.draft.x = (double)this.draft.mode / 2.0;
                    this.draft.manualPosition = true;
                }
                this.refresh();
            });
            if(this.feature==Feature.INVENTORY_SEARCH_FLASH){
             var query=addRenderableWidget(new net.minecraft.client.gui.components.EditBox(font,left(),104,cw()*2+8,20,Component.literal("Название или ID предмета")));
             query.setMaxLength(80);query.setHint(Component.literal("По названию: слова или minecraft:item"));query.setValue(draft.searchQuery);query.setResponder(v->draft.searchQuery=v);
             toggle(1,3,"Пока открыт инвентарь",()->draft.searchPersistent,()->draft.searchPersistent=!draft.searchPersistent);
            }
            if (this.feature == Feature.HUD_ON_DEMAND) {
                this.button(0, 2, "\u0423\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0442\u044c: " + SettingsScreen.keyName(this.draft.holdKey), () -> {
                    this.captureKey = 2;
                    this.refresh();
                });
            }
            if (this.feature == Feature.COMBAT_FOCUS || this.feature == Feature.HUD_ON_DEMAND) {
                this.toggle(1, 2, "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c bossbar", () -> this.draft.hideBossbar, () -> {
                    this.draft.hideBossbar = !this.draft.hideBossbar;
                });
                this.toggle(1, 3, "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442\u044b", () -> this.draft.hideEffects, () -> {
                    this.draft.hideEffects = !this.draft.hideEffects;
                });
                this.toggle(0, 4, "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u0438\u043c\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430", () -> this.draft.hideItemName, () -> {
                    this.draft.hideItemName = !this.draft.hideItemName;
                });
            }
            if (this.feature == Feature.KIT_CHECK || this.feature == Feature.RESUPPLY_LIST || this.feature == Feature.HOTBAR_MEMORY) {
                this.button(0, 2, "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0438\u0439 " + (this.feature == Feature.HOTBAR_MEMORY ? "\u0445\u043e\u0442\u0431\u0430\u0440" : "\u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442"), () -> {
                    Core.capture(this.feature, this.draft);
                    this.refresh();
                });
                this.button(1, 2, "\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u043e\u0431\u0440\u0430\u0437\u0435\u0446", () -> {
                    this.draft.kit.clear();
                    this.draft.hotbar.clear();
                    this.refresh();
                });
            }
            this.button(0, 3, this.actionName(), () -> {
                Core.save(this.feature, this.draft.copy());
                Core.action(this.feature);
                this.draft = Core.config(this.feature).copy();
            });
            if (this.feature == Feature.FIGHT_RECAP || this.feature == Feature.FIGHT_TIMELINE) {
                this.button(1, 3, "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u043e\u0442\u0447\u0451\u0442", () -> this.minecraft.gui.setScreen((Screen)new ReportScreen(this, "\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u0431\u043e\u0439", this.lastFight())));
                this.button(0, 4, "\u042d\u043a\u0441\u043f\u043e\u0440\u0442 \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0435\u0433\u043e \u0431\u043e\u044f", () -> Core.exportFight(true));
            }
            if (this.feature == Feature.RESUPPLY_LIST || this.feature == Feature.KIT_CHECK) {
                this.button(1, 3, "\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u0435\u0441\u044c \u0441\u043f\u0438\u0441\u043e\u043a", () -> {
                    if (this.minecraft.player != null) {
                        this.minecraft.gui.setScreen((Screen)new ReportScreen(this, this.feature.title, Core.reportRows(Core.missing(this.draft, this.feature == Feature.KIT_CHECK))));
                    }
                });
            }
            if (this.feature == Feature.POTION_IDENTITY) {
                this.button(1, 3, "\u041e\u0431\u043e\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f \u0437\u0435\u043b\u0438\u0439", () -> this.minecraft.gui.setScreen((Screen)new ReportScreen(this, "\u041e\u0431\u043e\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f", List.of("\u0421 \u2014 \u0441\u0438\u043b\u0430; \u0411 \u2014 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c; \u041e \u2014 \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c", "\u041b \u2014 \u043b\u0435\u0447\u0435\u043d\u0438\u0435; \u0420 \u2014 \u0440\u0435\u0433\u0435\u043d\u0435\u0440\u0430\u0446\u0438\u044f; \u0417 \u2014 \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435", "\u0421\u043b \u2014 \u0441\u043b\u0430\u0431\u043e\u0441\u0442\u044c; \u042f \u2014 \u044f\u0434; ? \u2014 \u0434\u0440\u0443\u0433\u043e\u0439 \u044d\u0444\u0444\u0435\u043a\u0442", "\u0426\u0438\u0444\u0440\u0430 \u043f\u043e\u0441\u043b\u0435 \u0431\u0443\u043a\u0432\u044b \u2014 \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u044d\u0444\u0444\u0435\u043a\u0442\u0430"))));
            }
            if (this.feature == Feature.SERVER_PROFILES) {
                this.button(1, 3, "\u0412\u0441\u0435 \u043f\u0440\u043e\u0444\u0438\u043b\u0438", () -> this.minecraft.gui.setScreen((Screen)new ReportScreen(this, "\u041f\u0440\u043e\u0444\u0438\u043b\u0438", this.profileLines())));
            }
        } else if (this.page == 2) {
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0410\u0432\u0442\u043e\u043f\u043e\u0437\u0438\u0446\u0438\u044f"), b -> {
                this.draft.manualPosition = false;
                this.refresh();
            }).bounds(this.width / 2 - 65, this.height - 54, 130, 20).build());
        } else if (this.page == 3) {
            this.button(0, 0, this.colorTarget == 0 ? "\u0426\u0432\u0435\u0442 \u0442\u0435\u043a\u0441\u0442\u0430" : "\u0426\u0432\u0435\u0442 \u0430\u043a\u0446\u0435\u043d\u0442\u0430", () -> {
                this.colorTarget = 1 - this.colorTarget;
                this.refresh();
            });
            for (i = 0; i < 3; ++i) {
                int shift = 16 - i * 8;
                String label = (new String[]{"\u041a\u0440\u0430\u0441\u043d\u044b\u0439", "\u0417\u0435\u043b\u0451\u043d\u044b\u0439", "\u0421\u0438\u043d\u0438\u0439"})[i];
                this.slider(0, i + 1, label, 0.0, 255.0, () -> (this.colorTarget == 0 ? this.draft.color : this.draft.accent) >> shift & 0xFF, v -> {
                    int color = this.colorTarget == 0 ? this.draft.color : this.draft.accent;
                    color = color & ~(255 << shift) | (int)v << shift;
                    if (this.colorTarget == 0) {
                        this.draft.color = color;
                    } else {
                        this.draft.accent = color;
                    }
                }, false);
            }
            this.button(1, 0, "\u0411\u0435\u043b\u044b\u0439", () -> {
                this.setColor(0xFFFFFF);
                this.refresh();
            });
            this.button(1, 1, "\u0413\u043e\u043b\u0443\u0431\u043e\u0439", () -> {
                this.setColor(7920895);
                this.refresh();
            });
            this.button(1, 2, "\u041a\u0440\u0430\u0441\u043d\u044b\u0439", () -> {
                this.setColor(16737655);
                this.refresh();
            });
            this.button(1, 3, "\u0417\u0435\u043b\u0451\u043d\u044b\u0439", () -> {
                this.setColor(7399072);
                this.refresh();
            });
        }
        int fy = this.height - 28;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0421\u0431\u0440\u043e\u0441"), b -> {
            Config def = Config.defaults(this.feature);
            def.kit = this.draft.kit;
            def.hotbar = this.draft.hotbar;
            def.notes = this.draft.notes;
            def.profiles = this.draft.profiles;
            this.draft = def;
            this.refresh();
        }).bounds(this.width / 2 - 111, fy, 70, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u041e\u0442\u043c\u0435\u043d\u0430"), b -> this.onClose()).bounds(this.width / 2 - 35, fy, 70, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0413\u043e\u0442\u043e\u0432\u043e"), b -> {
            Core.save(this.feature, this.draft);
            this.onClose();
        }).bounds(this.width / 2 + 41, fy, 70, 20).build());
    }

    void setColor(int color) {
        if (this.colorTarget == 0) {
            this.draft.color = color;
        } else {
            this.draft.accent = color;
        }
    }

    static String keyName(int key) {
        if (key < 0) {
            return "\u043d\u0435 \u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u043e";
        }
        String name = LegacyInput.glfwGetKeyName((int)key, (int)0);
        return name == null ? (key == 342 ? "Left Alt" : (key == 346 ? "Right Alt" : "Key " + key)) : name.toUpperCase(Locale.ROOT);
    }

    String thresholdName() {
        return switch (this.feature) {
            case Feature.ARMOR_PANIC -> "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c, %";
            case Feature.EFFECT_ALARM -> "\u041e\u0441\u0442\u0430\u043b\u043e\u0441\u044c, \u0441\u0435\u043a.";
            case Feature.REFILL_REMINDER -> "\u041c\u0430\u043b\u043e \u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435, \u0448\u0442.";
            case Feature.TOTEM_PULSE -> "\u041c\u0430\u043b\u043e \u0442\u043e\u0442\u0435\u043c\u043e\u0432, \u0448\u0442.";
            case Feature.FIGHT_RECAP, Feature.FIGHT_TIMELINE, Feature.COMBAT_FOCUS -> "\u041a\u043e\u043d\u0435\u0446 \u0431\u043e\u044f \u0431\u0435\u0437 \u0443\u0440\u043e\u043d\u0430, \u0441";
            case Feature.READY_AGAIN, Feature.INVENTORY_SEARCH_FLASH -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c, \u0441\u0435\u043a.";
            default -> "\u041f\u043e\u0440\u043e\u0433 (\u0434\u043b\u044f \u0442\u0440\u0435\u0432\u043e\u0433)";
        };
    }

    String modeName() {
        return switch (this.feature) {
            case Feature.OFFHAND_ALERT -> "\u041e\u0436\u0438\u0434\u0430\u0435\u0442\u0441\u044f: " + (new String[]{"\u0422\u043e\u0442\u0435\u043c", "\u0429\u0438\u0442", "\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e"})[this.draft.mode];
            case Feature.INVENTORY_SEARCH_FLASH -> "\u041f\u043e\u0438\u0441\u043a: " + Core.searchCategory(this.draft.mode);
            default -> "\u041f\u043e\u0437\u0438\u0446\u0438\u044f: " + (new String[]{"\u0421\u043b\u0435\u0432\u0430", "\u041f\u043e \u0446\u0435\u043d\u0442\u0440\u0443", "\u0421\u043f\u0440\u0430\u0432\u0430"})[this.draft.mode];
        };
    }

    String actionName() {
        return switch (this.feature) {
            case Feature.FIGHT_RECAP, Feature.FIGHT_TIMELINE -> "\u041d\u0430\u0447\u0430\u0442\u044c / \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u0442\u044c \u0431\u043e\u0439";
            case Feature.KIT_CHECK -> "\u041f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442";
            case Feature.RESUPPLY_LIST -> "\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u043f\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435";
            case Feature.DUEL_NOTES -> "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u0430\u043c\u0435\u0442\u043a\u0443";
            case Feature.INVENTORY_SEARCH_FLASH -> "\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044e";
            case Feature.PEARL_LANDING_TIMER -> "\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0431\u0440\u043e\u0441\u043a\u043e\u0432";
            default -> "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438 \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0438\u0442\u044c";
        };
    }

    List<String> lastFight() {
        ArrayList<String> out = new ArrayList<String>();
        out.add("\u041f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a: " + Core.lastOpponent);
        out.add("\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c: " + Core.seconds(Core.lastDuration));
        out.add("\u0422\u043e\u0442\u0435\u043c\u043e\u0432: " + Core.lastTotems);
        for (Map.Entry<String, Integer> entry : Core.lastUsed.entrySet()) {
            out.add(entry.getKey() + ": " + String.valueOf(entry.getValue()));
        }
        for (Core.Event event : Core.lastTimeline) {
            out.add(Core.seconds(event.ms()) + "  " + event.text());
        }
        return out;
    }

    List<String> profileLines() {
        ArrayList<String> out = new ArrayList<String>();
        out.add("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439: " + Core.context);
        for (Feature f : Core.roots.keySet()) {
            for (String name : Core.roots.get((Object)((Object)f)).profiles.keySet()) {
                out.add(f.title + " \u00b7 " + name);
            }
        }
        return out;
    }

    String help() {
        return switch (this.feature) {
            case Feature.KIT_CHECK, Feature.RESUPPLY_LIST -> "\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u0441\u0432\u043e\u0439 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442. \u041e\u0431\u0440\u0430\u0437\u0435\u0446 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0441\u044f \u043f\u043e \u043a\u043d\u043e\u043f\u043a\u0435 \u00ab\u0413\u043e\u0442\u043e\u0432\u043e\u00bb.";
            case Feature.HOTBAR_MEMORY -> "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442 \u0442\u0438\u043f\u044b \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043f\u043e \u0441\u043b\u043e\u0442\u0430\u043c. \u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0438 \u0438\u0437\u043d\u043e\u0441 \u043d\u0435 \u0443\u0447\u0438\u0442\u044b\u0432\u0430\u044e\u0442\u0441\u044f.";
            case Feature.HUD_ON_DEMAND -> "\u041f\u0430\u043d\u0435\u043b\u0438 PvP-\u043c\u043e\u0434\u043e\u0432 \u0438 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b Minecraft \u0432\u0438\u0434\u043d\u044b \u043f\u043e \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u044e. \u0422\u0440\u0435\u0432\u043e\u0433\u0438 \u043e\u0441\u0442\u0430\u044e\u0442\u0441\u044f.";
            case Feature.COMBAT_FOCUS -> "\u0412 \u0431\u043e\u044e \u0441\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b Minecraft \u0438 \u0432\u0442\u043e\u0440\u043e\u0441\u0442\u0435\u043f\u0435\u043d\u043d\u044b\u0435 \u043f\u0430\u043d\u0435\u043b\u0438 PvP-\u043c\u043e\u0434\u043e\u0432.";
            case Feature.SERVER_PROFILES -> "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u044d\u0442\u0438\u0445 PvP-\u043c\u043e\u0434\u043e\u0432 \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0440\u0430\u0437\u0434\u0435\u043b\u044f\u044e\u0442\u0441\u044f \u043f\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430\u043c \u0438 \u043c\u0438\u0440\u0430\u043c.";
            case Feature.PEARL_LANDING_TIMER -> "\u0422\u043e\u0447\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u0432 \u0442\u0438\u043a\u0430\u0445 \u0438\u0437\u043c\u0435\u0440\u044f\u0435\u0442\u0441\u044f \u043f\u0440\u0438 \u0441\u0442\u043e\u043b\u043a\u043d\u043e\u0432\u0435\u043d\u0438\u0438 \u0436\u0435\u043c\u0447\u0443\u0433\u0430 \u0432 \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u043e\u043c \u043c\u0438\u0440\u0435.";
            case Feature.FIGHT_RECAP, Feature.FIGHT_TIMELINE -> "\u0410\u0432\u0442\u043e\u0441\u0442\u0430\u0440\u0442 \u043f\u0440\u0438 PvP-\u0443\u0440\u043e\u043d\u0435. \u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u043d\u0430\u0447\u0438\u043d\u0430\u0435\u0442 \u0438\u043b\u0438 \u0437\u0430\u0432\u0435\u0440\u0448\u0430\u0435\u0442 \u0437\u0430\u043f\u0438\u0441\u044c.";
            case Feature.DUEL_NOTES -> "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u043e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0437\u0430\u043c\u0435\u0442\u043a\u0438. \u041a\u0430\u0436\u0434\u0430\u044f \u043e\u0442\u043c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f \u0441\u0440\u0430\u0437\u0443.";
            case Feature.POTION_IDENTITY -> "\u0411\u0443\u043a\u0432\u0430 \u044d\u0444\u0444\u0435\u043a\u0442\u0430 \u0438 \u0435\u0433\u043e \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u0432\u044b\u0432\u043e\u0434\u044f\u0442\u0441\u044f \u0432 \u0441\u043b\u043e\u0442\u0430\u0445 \u0432\u0430\u0448\u0435\u0433\u043e \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f \u0438 \u0445\u043e\u0442\u0431\u0430\u0440\u0430.";
            default -> this.feature.description;
        };
    }

    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        c.fill(0, 0, this.width, this.height, -401073374);
        c.centeredText(this.font, this.title, this.width / 2, 10, -1);
        super.extractRenderState(c, mx, my, delta);
        if (this.page == 2) {
            c.centeredText(this.font, "\u041f\u0435\u0440\u0435\u0442\u0430\u0449\u0438\u0442\u0435 HUD. \u041a\u043e\u043b\u0435\u0441\u043e \u2014 \u0440\u0430\u0437\u043c\u0435\u0440.", this.width / 2, 57, -2631721);
            this.bounds = Core.panel(c, this.feature, this.draft, Core.preview(this.feature), true);
        } else {
            if (this.page != 3 && this.height >= 280) {
                int y = this.page == 1 ? 183 : 157;
                c.textWithWordWrap(this.font, (FormattedText)Component.literal((String)this.help()), this.left(), y, this.cw() * 2 + 8, -4536871);
            }
            if (this.height >= 330 || this.height >= 235 && (this.page == 0 || this.page == 3)) {
                Config sample = this.draft.copy();
                sample.x = 0.5;
                sample.y = 1.0;
                List<Core.Row> sampleRows = Core.preview(this.feature);
                sampleRows = sampleRows.subList(0, Math.min(sampleRows.size(), this.height >= 330 ? 2 : 1));
                c.pose().pushMatrix();
                c.pose().translate(0.0f, -40.0f);
                Core.panel(c, this.feature, sample, sampleRows, true);
                c.pose().popMatrix();
            }
        }
        if (this.captureKey > 0) {
            c.fill(this.width / 2 - 175, this.height / 2 - 24, this.width / 2 + 175, this.height / 2 + 24, -14077374);
            c.centeredText(this.font, "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u00b7 Delete \u2014 \u0443\u0431\u0440\u0430\u0442\u044c \u00b7 Esc \u2014 \u043e\u0442\u043c\u0435\u043d\u0430", this.width / 2, this.height / 2 - 4, -1);
        }
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean twice) {
        if (this.page == 2 && LegacyInput.fromNativeMouse(click.button()) == 0 && this.bounds != null && click.x() >= (double)this.bounds[0] && click.x() <= (double)(this.bounds[0] + this.bounds[2]) && click.y() >= (double)this.bounds[1] && click.y() <= (double)(this.bounds[1] + this.bounds[3])) {
            this.dragging = true;
            this.dragX = click.x() - (double)this.bounds[0];
            this.dragY = click.y() - (double)this.bounds[1];
            return true;
        }
        return super.mouseClicked(click, twice);
    }

    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (this.dragging && this.bounds != null) {
            this.draft.manualPosition = true;
            this.draft.x = Math.clamp((click.x() - this.dragX) / (double)Math.max(1, this.width - this.bounds[2]), 0.0, 1.0);
            this.draft.y = Math.clamp((click.y() - this.dragY) / (double)Math.max(1, this.height - this.bounds[3]), 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        this.dragging = false;
        return super.mouseReleased(click);
    }

    public boolean mouseScrolled(double x, double y, double h, double v) {
        if (this.page == 2) {
            this.draft.scale = Math.clamp(this.draft.scale + v * 0.05, 0.5, 2.5);
            return true;
        }
        return super.mouseScrolled(x, y, h, v);
    }

    public boolean keyPressed(KeyEvent input) {
        if (this.captureKey > 0) {
            if (LegacyInput.fromNativeKey(input.key()) != 256) {
                int key;
                int n = key = LegacyInput.fromNativeKey(input.key()) == 261 || LegacyInput.fromNativeKey(input.key()) == 259 ? -1 : LegacyInput.fromNativeKey(input.key());
                if (key == 297) {
                    Core.notice("F8 \u0432\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u0440\u0435\u0436\u0438\u043c \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u043e\u0432");
                    return true;
                }
                if (this.captureKey == 1) {
                    this.draft.actionKey = key;
                } else {
                    this.draft.holdKey = key;
                }
            }
            this.captureKey = 0;
            this.refresh();
            return true;
        }
        return super.keyPressed(input);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

