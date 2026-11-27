/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  net.minecraft.class_156
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_156;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.AppearancePreset;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.modules.ChatTools;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class AppearanceShareScreen
extends ModuleDetailScreen {
    private List<Path> files = List.of();
    private AppearancePreset selected;
    private AppearancePreset previous;
    private ClientConfig candidate;
    private JsonObject candidateJson;
    private static final Gson GSON = new Gson();
    private String name = "";
    private String notice = "";
    private boolean combat;

    private void candidate(ClientConfig value) {
        this.candidate = value;
        this.candidateJson = GSON.toJsonTree((Object)value).getAsJsonObject();
    }

    public AppearanceShareScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u041e\u0431\u043c\u0435\u043d \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435\u043c", "Share appearance"));
        this.candidate(AppearancePreset.capture(BloodHelperClient.config()).preview(BloodHelperClient.config()));
        this.refresh();
    }

    @Override
    protected int preferredPanelWidth() {
        return 680;
    }

    @Override
    protected int preferredPanelHeight() {
        return 430;
    }

    private void refresh() {
        try {
            this.files = ConfigStore.appearances();
        }
        catch (IOException failure) {
            this.notice = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0439", "Could not open the appearance folder");
        }
    }

    public void method_29638(List<Path> paths) {
        if (paths.size() != 1) {
            this.notice = HudStyle.tr("\u041f\u0435\u0440\u0435\u0442\u0430\u0449\u0438 \u043e\u0434\u0438\u043d \u0444\u0430\u0439\u043b \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f .json", "Drop one appearance .json file");
            return;
        }
        this.preview(paths.getFirst());
    }

    private void preview(Path path) {
        try {
            AppearancePreset preset = AppearancePreset.read(path);
            ClientConfig next = preset.preview(BloodHelperClient.config());
            this.selected = preset;
            this.candidate(next);
            this.name = path.getFileName().toString();
            this.notice = "";
        }
        catch (IOException | RuntimeException failure) {
            this.selected = null;
            this.notice = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435: \u043d\u0443\u0436\u0435\u043d \u0444\u0430\u0439\u043b BloodHelper \u0434\u043e 256 \u041a\u0411", "Invalid BloodHelper appearance file (maximum 256 KB)");
        }
    }

    private void export() {
        try {
            Path file = ConfigStore.exportAppearance();
            this.refresh();
            this.notice = HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e: ", "Saved: ") + String.valueOf(file.getFileName());
        }
        catch (IOException | RuntimeException failure) {
            this.notice = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0444\u0430\u0439\u043b", "Could not save the file");
        }
    }

    private void apply() {
        if (this.selected == null) {
            return;
        }
        this.previous = AppearancePreset.capture(BloodHelperClient.config());
        ConfigStore.applyAppearance(this.selected);
        MusicWidget.resetMotion();
        ChatTools.refreshTimestampColors();
        this.selected = null;
        this.notice = HudStyle.tr("\u041e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u043e", "Appearance applied");
    }

    private void undo() {
        if (this.previous == null) {
            return;
        }
        ConfigStore.applyAppearance(this.previous);
        MusicWidget.resetMotion();
        ChatTools.refreshTimestampColors();
        this.previous = null;
        this.selected = null;
        this.candidate(AppearancePreset.capture(BloodHelperClient.config()).preview(BloodHelperClient.config()));
        this.name = "";
        this.notice = HudStyle.tr("\u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0435\u0435 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e", "Previous appearance restored");
    }

    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u041e\u0431\u043c\u0435\u043d \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435\u043c", "Share appearance"), mx, my);
        int column = Math.min(216, this.panelWidth / 3);
        int gap = 14;
        int right = this.left + column + gap;
        int rw = this.panelWidth - column - 2 * gap;
        this.button(g, this.left + 14, this.bodyTop, column - 14, HudStyle.tr("\u042d\u043a\u0441\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0435\u0435", "Export current appearance"), mx, my, this::export);
        this.button(g, right, this.bodyTop, (rw - 8) / 2, HudStyle.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Open folder"), mx, my, () -> {
            try {
                class_156.method_668().method_60932(ConfigStore.appearanceDirectory());
            }
            catch (IOException failure) {
                this.notice = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Could not open folder");
            }
        });
        this.button(g, right + (rw + 8) / 2, this.bodyTop, (rw - 8) / 2, HudStyle.tr("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a", "Refresh files"), mx, my, this::refresh);
        int savedTop = this.bodyTop;
        int savedBottom = this.bodyBottom;
        this.bodyTop += 38;
        this.bodyBottom -= 36;
        int y = this.bodyTop;
        this.text(g, HudStyle.tr("\u0424\u0430\u0439\u043b\u044b \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f", "Appearance files"), this.left + 14, y, -7105633);
        this.maximumScroll = Math.max(0, this.files.size() * 29 - Math.max(1, this.bodyBottom - (y += 20)));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.method_44379(this.left + 10, y, right - 8, this.bodyBottom);
        int rowY = y - this.scroll;
        for (Path file : this.files) {
            if (rowY >= y && rowY + 21 <= this.bodyBottom) {
                Path target = file;
                this.button(g, this.left + 14, rowY, column - 14, file.getFileName().toString(), mx, my, () -> this.preview(target));
            }
            rowY += 29;
        }
        if (this.files.isEmpty()) {
            this.text(g, HudStyle.tr("\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0444\u0430\u0439\u043b\u043e\u0432", "No files yet"), this.left + 14, y + 5, -7105633);
        }
        g.method_44380();
        String previewName = this.name.isEmpty() ? HudStyle.tr("\u0422\u0435\u043a\u0443\u0449\u0435\u0435 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435", "Current appearance") : this.name;
        this.text(g, CustomFont.regular().ellipsize(previewName, 11.0f, rw), right, this.bodyTop, -1381908);
        this.drawPreview(g, right, this.bodyTop + 20, rw, Math.max(50, this.bodyBottom - this.bodyTop - 50));
        this.button(g, right, this.bodyBottom - 21, Math.min(150, rw), this.combat ? HudStyle.tr("HUD: \u0432 \u0431\u043e\u044e", "HUD: combat") : HudStyle.tr("HUD: \u043e\u0431\u044b\u0447\u043d\u044b\u0439", "HUD: normal"), mx, my, () -> {
            this.combat = !this.combat;
        });
        this.bodyTop = savedTop;
        this.bodyBottom = savedBottom;
        if (this.selected != null) {
            this.button(g, right + Math.max(0, rw - 146), this.bodyBottom - 22, Math.min(rw, 146), HudStyle.tr("\u041f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c", "Apply"), mx, my, this::apply);
        }
        if (this.previous != null) {
            this.button(g, this.left + 14, this.bodyBottom - 22, column - 14, HudStyle.tr("\u0412\u0435\u0440\u043d\u0443\u0442\u044c \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0435\u0435", "Restore previous"), mx, my, this::undo);
        }
        this.footer(g, this.notice.isEmpty() ? HudStyle.tr("\u041f\u0435\u0440\u0435\u0442\u0430\u0449\u0438 \u0444\u0430\u0439\u043b .json \u0441\u044e\u0434\u0430 \u0438\u043b\u0438 \u043f\u043e\u043b\u043e\u0436\u0438 \u0435\u0433\u043e \u0432 \u043f\u0430\u043f\u043a\u0443 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0439", "Drop a .json file here or put it in the appearance folder") : this.notice);
    }

    private void drawPreview(class_332 g, int x, int y, int w, int h) {
        boolean light = "LIGHT".equals(this.candidate.themeMode);
        int fg = light ? -14998480 : -986378;
        int muted = light ? -12365987 : -4274222;
        UiDraw.rounded(g, x, y, w, h, 8, light ? -4732463 : -14274243);
        g.method_44379(x, y, x + w, y + h);
        for (int stripe = 0; stripe < w; stripe += 24) {
            g.method_25294(x + stripe, y, x + stripe + 10, y + h, light ? -5455928 : -14669259);
        }
        int sampleWidth = Math.min(w - 20, 250);
        int sampleX = x + (w - sampleWidth) / 2;
        if (this.candidate.background && this.candidate.hudDetails) {
            LiquidGlass.surface(g, sampleX, y + 12, sampleWidth, 62, 12, UiDraw.alpha(ThemeColors.surface(this.candidate.backgroundColor, this.candidate.accent, light), this.candidate.backgroundAlpha), this.candidate);
        }
        UiDraw.rounded(g, sampleX + 10, y + 22, 26, 26, 6, this.candidate.accent);
        AppearanceShareScreen.raw(g, "BloodstoneHelperVL", sampleX + 46, y + 22, 12.0f, fg);
        AppearanceShareScreen.raw(g, HudStyle.tr("\u041f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f", "Appearance preview"), sampleX + 46, y + 40, 9.0f, muted);
        UiDraw.rounded(g, sampleX + 12, y + 63, Math.max(1, sampleWidth - 24), 2, 1, muted);
        UiDraw.rounded(g, sampleX + 12, y + 63, Math.max(1, sampleWidth / 2 - 12), 2, 1, this.candidate.accent);
        int mapY = y + 92;
        int mapH = h - 104;
        if (mapH > 35) {
            AppearanceShareScreen.raw(g, HudStyle.tr("\u0421\u0445\u0435\u043c\u0430 \u0440\u0430\u0441\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u044f HUD", "HUD placement map"), x + 10, y + 80, 9.0f, muted);
            UiDraw.outline(g, x + 10, mapY, w - 20, mapH, 4, muted);
            JsonObject config = this.candidateJson;
            for (String id : AppearancePreset.WIDGETS) {
                String align;
                JsonObject part = AppearancePreset.previewWidget(config, id, this.combat);
                if (part == null) continue;
                JsonObject position = part.getAsJsonObject("position");
                int pw = (int)Math.clamp(44.0 * position.get("scale").getAsDouble(), 18.0, (double)Math.max(18, (w - 20) / 2));
                int ph = 12;
                double px = position.get("x").getAsDouble() * (double)(w - 20);
                double py = position.get("y").getAsDouble() * (double)mapH;
                String string = align = part.has("alignment") ? part.get("alignment").getAsString() : "CENTER";
                px -= "CENTER".equals(align) ? (double)pw / 2.0 : ("RIGHT".equals(align) ? (double)pw : 0.0);
                if (part.has("bottom") && part.get("bottom").getAsBoolean()) {
                    py = (double)mapH - py - (double)ph;
                }
                int bx = x + 10 + (int)Math.clamp(px, 0.0, (double)Math.max(0, w - 20 - pw));
                int by = mapY + (int)Math.clamp(py, 0.0, (double)Math.max(0, mapH - ph));
                UiDraw.rounded(g, bx, by, pw, ph, 3, -535685344);
                UiDraw.outline(g, bx, by, pw, ph, 3, this.candidate.accent);
                AppearanceShareScreen.raw(g, CustomFont.regular().ellipsize(AppearanceShareScreen.widgetName(id), 7.0f, pw - 4), bx + 2, by + 2, 7.0f, -920586);
            }
        }
        g.method_44380();
    }

    private static String widgetName(String id) {
        return switch (id) {
            case "cooldowns" -> HudStyle.tr("\u041a\u0414", "CD");
            case "pvp" -> "PvP";
            case "music" -> HudStyle.tr("\u041c\u0443\u0437\u044b\u043a\u0430", "Music");
            case "armor" -> HudStyle.tr("\u0411\u0440\u043e\u043d\u044f", "Armor");
            case "itemCounter" -> HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", "Items");
            case "notifications" -> HudStyle.tr("\u0423\u0432\u0435\u0434.", "Alerts");
            case "performance" -> "FPS";
            case "session" -> HudStyle.tr("\u0421\u0435\u0441\u0441\u0438\u044f", "Session");
            case "effects" -> HudStyle.tr("\u042d\u0444\u0444\u0435\u043a\u0442\u044b", "Effects");
            case "waypoints" -> HudStyle.tr("\u041c\u0435\u0442\u043a\u0438", "Marks");
            default -> "WM";
        };
    }

    private static void raw(class_332 g, String text, float x, float y, float size, int color) {
        CustomFont.regular().drawRaw(g, text, x, y, size, color);
    }
}

