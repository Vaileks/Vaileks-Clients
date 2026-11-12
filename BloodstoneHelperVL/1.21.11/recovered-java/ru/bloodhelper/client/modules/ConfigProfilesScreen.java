/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.modules.ChatTools;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class ConfigProfilesScreen
extends ModuleDetailScreen {
    private List<String> profiles = List.of();
    private final GuiTextField name = new GuiTextField(0, 0, 100, 24, "");
    private String selected = "";
    private String notice = "";
    private String deleteCandidate = "";
    private long deleteUntil;

    public ConfigProfilesScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u041a\u043e\u043d\u0444\u0438\u0433\u0438", "Configs"));
        this.name.setMaxLength(40);
        this.name.setTextSize(11.0f);
        this.selected = ConfigStore.activeProfile();
        this.refresh();
        this.name.setValue(this.selected);
    }

    @Override
    protected int preferredPanelWidth() {
        return 720;
    }

    @Override
    protected int preferredPanelHeight() {
        return 440;
    }

    private void refresh() {
        this.profiles = ConfigStore.profiles();
        if (!this.profiles.contains(this.selected)) {
            this.selected = this.profiles.isEmpty() ? "" : this.profiles.getFirst();
        }
    }

    private void select(String value) {
        this.selected = value;
        this.name.setValue(value);
        this.name.setFocused(false);
        this.deleteCandidate = "";
        this.notice = "";
    }

    public void method_29638(List<Path> paths) {
        this.importFiles(paths);
    }

    private void importFiles(List<Path> files) {
        if (files.size() > 64) {
            this.notice = HudStyle.tr("\u0417\u0430 \u0440\u0430\u0437 \u043c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0434\u043e 64 \u0444\u0430\u0439\u043b\u043e\u0432", "Add up to 64 files at once");
            return;
        }
        int imported = 0;
        int failed = 0;
        String firstFailure = "";
        for (Path file : files) {
            try {
                this.selected = ConfigStore.importProfile(file);
                ++imported;
            }
            catch (IOException | RuntimeException error) {
                ++failed;
                if (!firstFailure.isEmpty()) continue;
                firstFailure = file.getFileName().toString();
            }
        }
        this.refresh();
        this.name.setValue(this.selected);
        this.notice = files.isEmpty() ? HudStyle.tr("\u0412 config-imports \u043f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0444\u0430\u0439\u043b\u043e\u0432 .json", "No .json files in config-imports yet") : HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e: ", "Added: ") + imported + (String)(failed == 0 ? "" : HudStyle.tr(" \u00b7 \u041d\u0435 \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u043d\u043e: ", " \u00b7 Failed: ") + failed + " (" + firstFailure + ")");
    }

    private void importFolder() {
        try {
            this.importFiles(ConfigStore.profileImportFiles());
        }
        catch (IOException failure) {
            this.failed(failure);
        }
    }

    private void openFolder(boolean exports) {
        try {
            class_156.method_668().method_60932(exports ? ConfigStore.profileExportDirectory() : ConfigStore.profileImportDirectory());
        }
        catch (IOException failure) {
            this.failed(failure);
        }
    }

    private void create() {
        try {
            ConfigStore.createProfile(this.name.getValue());
            this.selected = ConfigStore.activeProfile();
            this.refresh();
            this.name.setFocused(false);
            this.notice = HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u043d\u0430 \u043a\u043e\u043f\u0438\u044f \u0442\u0435\u043a\u0443\u0449\u0438\u0445 \u043d\u0430\u0441\u0442\u0440\u043e\u0435\u043a: ", "Created a copy of current settings: ") + this.selected;
        }
        catch (IOException | RuntimeException failure) {
            this.failed(failure);
        }
    }

    private void rename() {
        if (this.selected.isEmpty()) {
            return;
        }
        try {
            ConfigStore.renameProfile(this.selected, this.name.getValue());
            this.selected = this.name.getValue().strip();
            this.refresh();
            this.notice = HudStyle.tr("\u041a\u043e\u043d\u0444\u0438\u0433 \u043f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d", "Config renamed");
        }
        catch (IOException | RuntimeException failure) {
            this.failed(failure);
        }
    }

    private void activate() {
        if (this.selected.isEmpty()) {
            return;
        }
        try {
            ConfigStore.switchProfile(this.selected);
            BloodHelperClient.saveConfig();
            MusicWidget.resetMotion();
            ChatTools.refreshTimestampColors();
            this.refresh();
            this.notice = HudStyle.tr("\u0410\u043a\u0442\u0438\u0432\u0435\u043d: ", "Active: ") + this.selected;
        }
        catch (IOException | RuntimeException failure) {
            this.failed(failure);
        }
    }

    private void save() {
        try {
            ConfigStore.saveActiveProfile();
            this.notice = HudStyle.tr("\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u043a\u043e\u043d\u0444\u0438\u0433 \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d", "Active config saved");
        }
        catch (IOException | RuntimeException failure) {
            this.failed(failure);
        }
    }

    private void export() {
        if (this.selected.isEmpty()) {
            return;
        }
        try {
            Path file = ConfigStore.exportProfile(this.selected);
            this.notice = HudStyle.tr("\u042d\u043a\u0441\u043f\u043e\u0440\u0442: config-exports/", "Exported: config-exports/") + String.valueOf(file.getFileName());
        }
        catch (IOException | RuntimeException failure) {
            this.failed(failure);
        }
    }

    private void delete() {
        if (this.selected.isEmpty()) {
            return;
        }
        if (!this.selected.equals(this.deleteCandidate) || System.currentTimeMillis() >= this.deleteUntil) {
            this.deleteCandidate = this.selected;
            this.deleteUntil = System.currentTimeMillis() + 5000L;
            this.notice = HudStyle.tr("\u041d\u0430\u0436\u043c\u0438 \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0435\u0449\u0451 \u0440\u0430\u0437. \u0422\u0435\u043a\u0443\u0449\u0438\u0435 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043e\u0441\u0442\u0430\u043d\u0443\u0442\u0441\u044f.", "Click delete again. Current settings will stay.");
            return;
        }
        this.notice = ConfigStore.deleteProfile(this.selected) ? HudStyle.tr("\u041a\u043e\u043d\u0444\u0438\u0433 \u0443\u0434\u0430\u043b\u0451\u043d", "Config deleted") : HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433", "Could not delete config");
        this.deleteCandidate = "";
        this.refresh();
        this.name.setValue(this.selected);
    }

    private void failed(Exception failure) {
        this.notice = failure instanceof FileAlreadyExistsException ? HudStyle.tr("\u042d\u0442\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0443\u0436\u0435 \u0437\u0430\u043d\u044f\u0442\u043e \u2014 \u0432\u044b\u0431\u0435\u0440\u0438 \u0434\u0440\u0443\u0433\u043e\u0435", "This name already exists \u2014 choose another") : (failure instanceof IllegalArgumentException ? HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: 1\u201340 \u0431\u0443\u043a\u0432, \u0446\u0438\u0444\u0440, \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u0432, _ \u0438\u043b\u0438 -", "Name: 1\u201340 letters, digits, spaces, _ or -") : HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0444\u0430\u0439\u043b \u0438 \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u043f\u0430\u043f\u043a\u0435.", "Action failed. Check the file and folder permissions."));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u041a\u043e\u043d\u0444\u0438\u0433\u0438", "Configs"), mx, my);
        int col = Math.max(110, Math.min(246, this.panelWidth / 3));
        int x = this.left + col + 24;
        int w = Math.max(90, this.panelWidth - col - 40);
        int half = Math.max(1, (w - 8) / 2);
        int y = this.bodyTop;
        String active = ConfigStore.activeProfile();
        this.text(g, HudStyle.tr("\u0411\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0430 \u00b7 ", "Library \u00b7 ") + this.profiles.size(), this.left + 14, y + 3, -1381908);
        this.button(g, x, y, half, HudStyle.tr("\u041f\u0430\u043f\u043a\u0430 \u0438\u043c\u043f\u043e\u0440\u0442\u0430", "Import folder"), mx, my, () -> this.openFolder(false));
        this.button(g, x + half + 8, y, half, HudStyle.tr("\u0418\u043c\u043f\u043e\u0440\u0442 \u0438\u0437 \u043f\u0430\u043f\u043a\u0438", "Import folder files"), mx, my, this::importFolder);
        int listTop = y + 26;
        this.maximumScroll = Math.max(0, this.profiles.size() * 29 - Math.max(1, this.bodyBottom - listTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.method_44379(this.left + 10, listTop, this.left + col + 14, this.bodyBottom);
        try {
            int row = listTop - this.scroll;
            for (String profile : this.profiles) {
                if (row >= listTop && row + 21 <= this.bodyBottom) {
                    if (profile.equals(this.selected)) {
                        UiDraw.rounded(g, this.left + 11, row - 2, col + 4, 25, 6, ThemeColors.accent());
                    }
                    String label = (profile.equals(active) ? "\u25cf " : "") + profile;
                    this.button(g, this.left + 14, row, col - 2, label, mx, my, () -> this.select(profile));
                }
                row += 29;
            }
            if (this.profiles.isEmpty()) {
                this.text(g, HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0439 \u0438\u043b\u0438 \u0438\u043c\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u0439", "Create or import one"), this.left + 14, listTop + 6, -7105633);
            }
        }
        finally {
            g.method_44380();
        }
        this.text(g, CustomFont.regular().ellipsize(HudStyle.tr("\u0410\u043a\u0442\u0438\u0432\u0435\u043d: ", "Active: ") + (active.isEmpty() ? HudStyle.tr("\u0431\u0435\u0437 \u0438\u043c\u0435\u043d\u0438", "unnamed") : active), 11.0f, w), x, y += 38, -1381908);
        this.text(g, HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043d\u043e\u0432\u043e\u0433\u043e / \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u043a\u043e\u043d\u0444\u0438\u0433\u0430", "New / selected config name"), x, y += 24, -7105633);
        UiDraw.border(g, x, y += 17, w, 27, 5, ThemeColors.surface(-15395556), this.name.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-7829352));
        this.name.setX(x + 7);
        this.name.setY(y + 1);
        this.name.setWidth(w - 14);
        this.name.setHeight(25);
        this.name.render(g, mx, my, delta);
        this.button(g, x, y += 36, half, HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u0438\u0437 \u0442\u0435\u043a\u0443\u0449\u0435\u0433\u043e", "Copy current settings"), mx, my, this::create);
        if (!this.selected.isEmpty()) {
            this.button(g, x + half + 8, y, half, HudStyle.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "Rename selected"), mx, my, this::rename);
        }
        y += 29;
        if (!this.selected.isEmpty()) {
            this.button(g, x, y, half, this.selected.equals(active) ? HudStyle.tr("\u0423\u0436\u0435 \u0430\u043a\u0442\u0438\u0432\u0435\u043d", "Already active") : HudStyle.tr("\u041f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f", "Switch to selected"), mx, my, this::activate);
        }
        if (!active.isEmpty()) {
            this.button(g, x + half + 8, y, half, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439", "Save active config"), mx, my, this::save);
        }
        y += 29;
        if (!this.selected.isEmpty()) {
            this.button(g, x, y, half, HudStyle.tr("\u042d\u043a\u0441\u043f\u043e\u0440\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e", "Export selected"), mx, my, this::export);
        }
        this.button(g, x + half + 8, y, half, HudStyle.tr("\u041f\u0430\u043f\u043a\u0430 \u044d\u043a\u0441\u043f\u043e\u0440\u0442\u0430", "Export folder"), mx, my, () -> this.openFolder(true));
        y += 29;
        if (!this.selected.isEmpty()) {
            this.button(g, x, y, half, this.selected.equals(this.deleteCandidate) && System.currentTimeMillis() < this.deleteUntil ? HudStyle.tr("\u0422\u043e\u0447\u043d\u043e \u0443\u0434\u0430\u043b\u0438\u0442\u044c?", "Confirm delete?") : HudStyle.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439", "Delete selected"), mx, my, this::delete);
        }
        this.button(g, x + half + 8, y, half, HudStyle.tr("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a", "Refresh list"), mx, my, this::refresh);
        y += 33;
        for (String line : ConfigProfilesScreen.wrap(HudStyle.tr("\u041f\u0440\u0438 \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f. \u041b\u0438\u0447\u043d\u044b\u0435 \u043c\u0435\u0442\u043a\u0438, \u0442\u0435\u043a\u0441\u0442\u044b \u043a\u043e\u043c\u0430\u043d\u0434 \u0438 \u0434\u0430\u043d\u043d\u044b\u0435 \u043c\u0443\u0437\u044b\u043a\u0438 \u043e\u0441\u0442\u0430\u044e\u0442\u0441\u044f \u0443 \u0442\u0435\u0431\u044f.", "Switching saves your edits. Personal waypoints, command texts and music data stay local."), w)) {
            if (y + 12 < this.bodyBottom) {
                this.text(g, line, x, y, -7105633);
            }
            y += 15;
        }
        this.footer(g, this.notice.isEmpty() ? HudStyle.tr("\u041f\u0435\u0440\u0435\u0442\u0430\u0449\u0438 \u043d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e .json \u0441\u044e\u0434\u0430 \u00b7 \u0434\u043e 512 \u041a\u0411 \u043a\u0430\u0436\u0434\u044b\u0439 \u00b7 \u0438\u043c\u043f\u043e\u0440\u0442 \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u043a\u043e\u043f\u0438\u0438", "Drop .json files here \u00b7 512 KB each \u00b7 importing adds copies") : this.notice);
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        this.name.setFocused(false);
        if (this.name.mouseClicked(event, doubled)) {
            return true;
        }
        return super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        return this.name.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 event) {
        return this.name.mouseReleased(event) || super.panelMouseReleased(event);
    }

    public boolean method_25404(class_11908 event) {
        if (event.comp_4795() == 257 && this.name.isFocused()) {
            this.create();
            return true;
        }
        return this.name.keyPressed(event) || super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        return this.name.charTyped(event) || super.method_25400(event);
    }
}

