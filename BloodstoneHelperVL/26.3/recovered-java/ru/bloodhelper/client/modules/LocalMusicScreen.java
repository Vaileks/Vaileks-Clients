/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.modules;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.MusicPlayerScreen;
import ru.bloodhelper.client.music.LocalMusicController;
import ru.bloodhelper.client.music.LocalMusicLibrary;
import ru.bloodhelper.client.music.MediaService;

public final class LocalMusicScreen
extends ModuleDetailScreen {
    private final LocalMusicController music = BloodHelperClient.media().localMusic();
    private final GuiTextField path = new GuiTextField(0, 0, 100, 24, "");
    private final GuiTextField title = new GuiTextField(0, 0, 100, 24, "");
    private String selected = "";
    private String inputError = "";
    private int listTop;
    private int listBottom;

    public LocalMusicScreen(Screen parent) {
        super(parent, HudStyle.tr("\u041c\u043e\u044f \u043c\u0443\u0437\u044b\u043a\u0430", "My music"));
        this.path.setMaxLength(4096);
        this.path.setTextSize(11.0f);
        this.title.setMaxLength(120);
        this.title.setTextSize(11.0f);
        this.path.setHint(HudStyle.tr("\u041f\u0443\u0442\u044c \u043a \u0444\u0430\u0439\u043b\u0443 \u0438\u043b\u0438 \u043f\u0430\u043f\u043a\u0435 \u00b7 \u043c\u043e\u0436\u043d\u043e \u043f\u0435\u0440\u0435\u0442\u0430\u0449\u0438\u0442\u044c \u0444\u0430\u0439\u043b\u044b \u0432 \u043e\u043a\u043d\u043e", "File or folder path \u00b7 files can be dropped into the window"));
        this.title.setHint(HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430", "Track name"));
    }

    @Override
    protected int preferredPanelWidth() {
        return 740;
    }

    @Override
    protected int preferredPanelHeight() {
        return 500;
    }

    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        String notice;
        this.frame(g, HudStyle.tr("\u041c\u043e\u044f \u043c\u0443\u0437\u044b\u043a\u0430", "My music"), mx, my);
        int x = this.left + 14;
        int w = this.panelWidth - 28;
        int third = (w - 12) / 3;
        this.button(g, x, this.bodyTop, third, HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0444\u0430\u0439\u043b\u044b", "Add files"), mx, my, () -> this.music.choose(false, true, this.music::importPaths));
        this.button(g, x + third + 6, this.bodyTop, third, HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Add folder"), mx, my, () -> this.music.choose(true, false, this.music::importPaths));
        this.button(g, x + (third + 6) * 2, this.bodyTop, third, HudStyle.tr("\u041d\u0430\u0439\u0442\u0438 \u0432 \u043d\u043e\u0432\u043e\u0439 \u043f\u0430\u043f\u043a\u0435", "Relink folder"), mx, my, () -> this.music.choose(true, false, files -> this.music.relinkFolder((Path)files.getFirst())));
        int py = this.bodyTop + 29;
        int pw = Math.max(1, w - 194);
        LocalMusicScreen.field(g, this.path, x, py, pw, mx, my, delta);
        this.button(g, x + pw + 6, py + 1, 90, HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043f\u0443\u0442\u044c", "Import path"), mx, my, this::importPath);
        this.button(g, x + pw + 102, py + 1, 92, HudStyle.tr("\u041d\u0430\u0439\u0442\u0438 \u043f\u043e \u043f\u0443\u0442\u0438", "Relink path"), mx, my, this::repairPath);
        this.listTop = this.bodyTop + 66;
        this.listBottom = this.bodyBottom - 94;
        int savedTop = this.bodyTop;
        int savedBottom = this.bodyBottom;
        this.bodyTop = this.listTop;
        this.bodyBottom = this.listBottom;
        List<LocalMusicLibrary.Track> tracks = this.music.library().tracks();
        this.scrollbar(g, tracks.size() * 37);
        g.enableScissor(x, this.listTop, x + w, this.listBottom);
        if (tracks.isEmpty()) {
            this.text(g, this.music.busy() ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u043c \u0431\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0443\u2026", "Loading library\u2026") : HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u044c MP3, WAV \u0438\u043b\u0438 OGG. \u0420\u044f\u0434\u043e\u043c \u0441 \u0442\u0440\u0435\u043a\u043e\u043c \u043c\u043e\u0436\u043d\u043e \u043f\u043e\u043b\u043e\u0436\u0438\u0442\u044c .lrc.", "Add MP3, WAV or OGG. An adjacent .lrc file supplies lyrics."), x + 8, this.listTop + 17, -7105633);
        }
        for (int index = 0; index < tracks.size(); ++index) {
            LocalMusicLibrary.Track track = tracks.get(index);
            int y = this.listTop + index * 37 - this.scroll;
            if (y + 33 <= this.listTop || y >= this.listBottom) continue;
            UiDraw.rounded(g, x, y, w - 4, 33, 6, ThemeColors.surface(track.id().equals(this.selected) ? -12176321 : -14277074));
            String label = (track.favorite() ? "\u2665  " : "") + track.title();
            this.text(g, CustomFont.medium().ellipsize(label, 11.0f, Math.max(1, w - 114)), x + 8, y + 4, -1381908);
            String detail = this.music.missing(track.id()) ? HudStyle.tr("\u0424\u0430\u0439\u043b \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0451\u043d \u2014 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438 \u0441\u0441\u044b\u043b\u043a\u0443", "File moved \u2014 relink it") : track.file().getFileName().toString();
            CustomFont.regular().draw(g, CustomFont.regular().ellipsize(detail, 9.0f, Math.max(1, w - 114)), x + 8, y + 20, 9.0f, this.music.missing(track.id()) ? -1791355 : -7105633);
            if (y < this.listTop || y + 33 > this.listBottom) continue;
            this.actions.add(new ModuleDetailScreen.Action(x, y, Math.max(1, w - 98), 33, () -> this.select(track)));
            this.button(g, x + w - 89, y + 6, 80, HudStyle.tr("\u0418\u0433\u0440\u0430\u0442\u044c", "Play"), mx, my, () -> this.play(track));
        }
        g.disableScissor();
        this.bodyTop = savedTop;
        this.bodyBottom = savedBottom;
        LocalMusicLibrary.Track track = this.music.library().find(this.selected);
        int details = this.listBottom + 10;
        if (track == null) {
            this.text(g, HudStyle.tr("\u0412\u044b\u0431\u0435\u0440\u0438 \u0442\u0440\u0435\u043a, \u0447\u0442\u043e\u0431\u044b \u0438\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0438\u043b\u0438 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0441\u0441\u044b\u043b\u043a\u0443.", "Select a track to change its name or restore its file link."), x, details + 7, -7105633);
        } else {
            LocalMusicScreen.field(g, this.title, x, details, Math.max(1, w - 100), mx, my, delta);
            this.button(g, x + w - 94, details + 1, 94, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438\u043c\u044f", "Save name"), mx, my, this::rename);
            this.button(g, x, details + 33, Math.max(1, (w - 12) / 2), HudStyle.tr("\u0423\u043a\u0430\u0437\u0430\u0442\u044c \u0444\u0430\u0439\u043b \u0437\u0430\u043d\u043e\u0432\u043e", "Relink this file"), mx, my, () -> this.music.choose(false, false, files -> this.music.relink(track.id(), (Path)files.getFirst())));
            this.button(g, x + (w - 12) / 2 + 12, details + 33, Math.max(1, (w - 12) / 2), HudStyle.tr("\u0423\u0431\u0440\u0430\u0442\u044c \u0438\u0437 \u0431\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0438", "Remove from library"), mx, my, () -> {
                this.music.remove(track.id());
                this.selected = "";
            });
        }
        String string = !this.inputError.isEmpty() ? this.inputError : (notice = !this.music.player().error().isEmpty() ? this.music.player().error() : this.music.notice());
        this.footer(g, (String)(this.music.busy() ? HudStyle.tr("\u041e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0435\u043c \u0444\u0430\u0439\u043b\u044b\u2026", "Processing files\u2026") : (!notice.isEmpty() ? LocalMusicScreen.localizedNotice(notice) : tracks.size() + " / 1024" + HudStyle.tr(" \u0442\u0440\u0435\u043a\u043e\u0432 \u00b7 \u0444\u0430\u0439\u043b\u044b \u043e\u0441\u0442\u0430\u044e\u0442\u0441\u044f \u043d\u0430 \u0442\u0432\u043e\u0451\u043c \u043a\u043e\u043c\u043f\u044c\u044e\u0442\u0435\u0440\u0435", " tracks \u00b7 files stay on your computer"))));
    }

    private static String localizedNotice(String notice) {
        return switch (notice) {
            case "Saved" -> HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e", "Name saved");
            case "Removed from library; file kept" -> HudStyle.tr("\u0423\u0431\u0440\u0430\u043d\u043e \u0438\u0437 \u0431\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0438. \u0421\u0430\u043c \u0444\u0430\u0439\u043b \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d.", "Removed from library; file kept");
            case "Link restored" -> HudStyle.tr("\u0421\u0441\u044b\u043b\u043a\u0430 \u043d\u0430 \u0444\u0430\u0439\u043b \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430", "File link restored");
            case "Enter a track name" -> HudStyle.tr("\u0412\u0432\u0435\u0434\u0438 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430", "Enter a track name");
            case "Track no longer exists" -> HudStyle.tr("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u044b\u0431\u0435\u0440\u0438 \u0442\u0440\u0435\u043a \u0432 \u0441\u043f\u0438\u0441\u043a\u0435", "Select a track first");
            case "Audio output is unavailable" -> HudStyle.tr("\u0410\u0443\u0434\u0438\u043e\u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u043e \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u0432\u044b\u0432\u043e\u0434 \u0437\u0432\u0443\u043a\u0430.", "Audio output is unavailable. Check your output device.");
            case "Unsupported or damaged audio file" -> HudStyle.tr("\u0424\u0430\u0439\u043b \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0451\u043d \u0438\u043b\u0438 \u0435\u0433\u043e \u0430\u0443\u0434\u0438\u043e\u0444\u043e\u0440\u043c\u0430\u0442 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f", "Unsupported or damaged audio file");
            case "Could not read or play this file" -> HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0438\u043b\u0438 \u0432\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u0435\u0441\u0442\u0438 \u0444\u0430\u0439\u043b", "Could not read or play this file");
            case "Selected file does not match the original track" -> HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d \u0434\u0440\u0443\u0433\u043e\u0439 \u0442\u0440\u0435\u043a. \u041d\u0430\u0439\u0434\u0438 \u0438\u0441\u0445\u043e\u0434\u043d\u044b\u0439 \u0444\u0430\u0439\u043b \u043f\u043e\u0441\u043b\u0435 \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0435\u043d\u0438\u044f.", "This is a different track. Locate the moved original file.");
            case "File chooser unavailable; paste a file or folder path" -> HudStyle.tr("\u0412\u044b\u0431\u043e\u0440 \u0444\u0430\u0439\u043b\u043e\u0432 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u2014 \u0432\u0441\u0442\u0430\u0432\u044c \u043f\u0443\u0442\u044c \u0432 \u043f\u043e\u043b\u0435", "File chooser unavailable; paste a file or folder path");
            case "Music library could not be read; existing file was preserved" -> HudStyle.tr("\u0411\u0438\u0431\u043b\u0438\u043e\u0442\u0435\u043a\u0430 \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u0430. \u0418\u0441\u0445\u043e\u0434\u043d\u044b\u0439 \u0444\u0430\u0439\u043b \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0431\u0435\u0437 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u0439.", "Music library could not be read; existing file was preserved");
            default -> notice.replace(" \u00b7 skipped: ", HudStyle.tr(" \u00b7 \u043f\u0440\u043e\u043f\u0443\u0449\u0435\u043d\u043e: ", " \u00b7 skipped: ")).replace("Restored: ", HudStyle.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e: ", "Restored: ")).replace(" \u00b7 missing: ", HudStyle.tr(" \u00b7 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e: ", " \u00b7 missing: "));
        };
    }

    private static void field(GuiGraphicsExtractor g, GuiTextField field, int x, int y, int width, int mx, int my, float delta) {
        field.setX(x + 7);
        field.setY(y);
        field.setWidth(Math.max(1, width - 14));
        field.setHeight(24);
        UiDraw.rounded(g, x, y, width, 24, 5, ThemeColors.surface(-14671833));
        field.render(g, mx, my, delta);
    }

    private void select(LocalMusicLibrary.Track track) {
        this.selected = track.id();
        this.title.setValue(track.title());
        this.title.setFocused(false);
        this.inputError = "";
    }

    private Path enteredPath() {
        String value = this.path.getValue().strip();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(HudStyle.tr("\u0423\u043a\u0430\u0436\u0438 \u043f\u0443\u0442\u044c \u043a \u0444\u0430\u0439\u043b\u0443 \u0438\u043b\u0438 \u043f\u0430\u043f\u043a\u0435", "Enter a file or folder path"));
        }
        return Path.of(value, new String[0]).toAbsolutePath().normalize();
    }

    private void importPath() {
        try {
            this.inputError = "";
            this.music.importPaths(List.of(this.enteredPath()));
        }
        catch (IllegalArgumentException invalid) {
            this.inputError = HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u043f\u0443\u0442\u044c \u043a \u0444\u0430\u0439\u043b\u0443 \u0438\u043b\u0438 \u043f\u0430\u043f\u043a\u0435", "Check the file or folder path");
        }
    }

    private void repairPath() {
        try {
            this.inputError = "";
            Path file = this.enteredPath();
            String track = this.selected;
            this.music.run(() -> {
                if (Files.isDirectory(file, new LinkOption[0])) {
                    this.music.library().relinkFolder(file);
                } else {
                    this.music.library().relink(track, file);
                }
            });
        }
        catch (IllegalArgumentException invalid) {
            this.inputError = HudStyle.tr("\u0412\u044b\u0431\u0435\u0440\u0438 \u0442\u0440\u0435\u043a \u0438 \u043f\u0440\u043e\u0432\u0435\u0440\u044c \u043f\u0443\u0442\u044c", "Select a track and check the path");
        }
    }

    private void rename() {
        if (!this.selected.isEmpty()) {
            this.inputError = "";
            this.music.rename(this.selected, this.title.getValue());
        }
    }

    private void play(LocalMusicLibrary.Track track) {
        if (this.music.missing(track.id())) {
            this.select(track);
            this.inputError = HudStyle.tr("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438 \u0441\u0441\u044b\u043b\u043a\u0443 \u043d\u0430 \u0444\u0430\u0439\u043b", "Relink this file first");
            return;
        }
        MediaService service = BloodHelperClient.media();
        BloodHelperClient.config().music.preferredSource = "bloodhelper:local";
        BloodHelperClient.config().music.enabled = true;
        ConfigStore.requestSave();
        service.setPreferredSource("bloodhelper:local");
        service.setEnabled(true);
        this.music.play(track.id());
        this.minecraft.gui.setScreen((Screen)new MusicPlayerScreen(this));
    }

    public void onFilesDrop(List<Path> files) {
        this.inputError = "";
        this.music.importPaths(files);
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent event, boolean doubled) {
        boolean titleHit;
        boolean pathHit = this.path.mouseClicked(event, doubled);
        boolean bl = titleHit = !this.selected.isEmpty() && this.title.mouseClicked(event, doubled);
        if (pathHit) {
            this.title.setFocused(false);
        }
        if (titleHit) {
            this.path.setFocused(false);
        }
        return pathHit || titleHit || super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent event, double dx, double dy) {
        return this.path.mouseDragged(event, dx, dy) || this.title.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent event) {
        return this.path.mouseReleased(event) || this.title.mouseReleased(event) || super.panelMouseReleased(event);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            if (this.title.isFocused()) {
                this.rename();
            } else if (this.path.isFocused()) {
                this.importPath();
            }
            return true;
        }
        return this.path.keyPressed(event) || this.title.keyPressed(event) || super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return this.path.charTyped(event) || this.title.charTyped(event) || super.charTyped(event);
    }

    @Override
    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        if (y >= (double)this.listTop && y < (double)this.listBottom) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 37.0)), 0, this.maximumScroll);
            return true;
        }
        return false;
    }
}

