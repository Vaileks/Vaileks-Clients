/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.HudEditorSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudInteraction;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;

final class HudEditorControls {
    private final HudInteraction interaction;
    private final Map<String, HudRenderer.Bounds> buttons = new LinkedHashMap<String, HudRenderer.Bounds>();

    HudEditorControls(HudInteraction interaction) {
        this.interaction = interaction;
    }

    void clear() {
        this.buttons.clear();
    }

    void render(class_332 g, int x, int y, int width, int mouseX, int mouseY) {
        this.buttons.clear();
        HudEditorSettings settings = BloodHelperClient.config().hudEditor;
        String[] keys = new String[]{"undo", "redo", "lock", "snap", "guides", "gap"};
        String[] labels = new String[]{HudStyle.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Undo"), HudStyle.tr("\u041f\u043e\u0432\u0442\u043e\u0440", "Redo"), this.interaction.allSelectedLocked() ? HudStyle.tr("\u0420\u0430\u0437\u0431\u043b\u043e\u043a.", "Unlock") : HudStyle.tr("\u0417\u0430\u043c\u043e\u043a", "Lock"), HudStyle.tr("\u041c\u0430\u0433\u043d\u0438\u0442", "Snap"), HudStyle.tr("\u041b\u0438\u043d\u0438\u0438", "Guides"), settings.gap + " px"};
        CustomFont font = CustomFont.medium();
        int accent = ThemeColors.icon(BloodHelperClient.config().accent);
        for (int i = 0; i < keys.length; ++i) {
            boolean hovered;
            int left = x + i * width / keys.length;
            int right = x + (i + 1) * width / keys.length - 3;
            HudRenderer.Bounds box = new HudRenderer.Bounds(left, y, Math.max(1, right - left), 20);
            this.buttons.put(keys[i], box);
            boolean enabled = switch (keys[i]) {
                case "undo" -> this.interaction.canUndo();
                case "redo" -> this.interaction.canRedo();
                case "lock" -> {
                    if (this.interaction.selectedCount() > 0) {
                        yield true;
                    }
                    yield false;
                }
                default -> true;
            };
            boolean selected = switch (keys[i]) {
                case "lock" -> this.interaction.allSelectedLocked();
                case "snap" -> settings.snapping;
                case "guides" -> settings.guides;
                default -> false;
            };
            boolean bl = hovered = box.contains(mouseX, mouseY) && enabled;
            int surface = selected ? UiDraw.blend(-14407632, accent, 0.17) : (hovered ? -13420479 : -14407632);
            UiDraw.border(g, left, y, box.width(), 20, 5, ThemeColors.surface(surface), selected ? UiDraw.alpha(accent, 160) : ThemeColors.surface(-12235947));
            String text = font.ellipsize(labels[i], 8.0f, box.width() - 6);
            font.draw(g, text, (float)left + ((float)box.width() - font.width(text, 8.0f)) / 2.0f, y + 6, 8.0f, enabled ? -921101 : -4802625);
        }
    }

    boolean click(class_11909 event) {
        if (event.method_74245() != 0) {
            return false;
        }
        for (Map.Entry<String, HudRenderer.Bounds> e : this.buttons.entrySet()) {
            if (!e.getValue().contains(event.comp_4798(), event.comp_4799())) continue;
            switch (e.getKey()) {
                case "undo": {
                    this.interaction.undo();
                    break;
                }
                case "redo": {
                    this.interaction.redo();
                    break;
                }
                case "lock": {
                    this.interaction.toggleLock();
                    break;
                }
                case "snap": {
                    this.interaction.toggleSnapping();
                    break;
                }
                case "guides": {
                    this.interaction.toggleGuides();
                    break;
                }
                case "gap": {
                    this.interaction.cycleGap();
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            return true;
        }
        return false;
    }

    boolean contains(double x, double y) {
        for (HudRenderer.Bounds b : this.buttons.values()) {
            if (!b.contains(x, y)) continue;
            return true;
        }
        return false;
    }
}

