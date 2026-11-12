/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.util.Util
 *  org.lwjgl.sdl.SDLMisc
 */
package ru.bloodhelper.client.modules;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLMisc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DeathScreenshotFiles;
import ru.bloodhelper.client.modules.DeathScreenshotModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class DeathScreenshotGallery
extends ModuleDetailScreen {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm:ss").withZone(ZoneId.systemDefault());
    private List<DeathScreenshotFiles.Entry> entries = List.of();
    private boolean loading;
    private boolean failed;
    private int generation;

    public DeathScreenshotGallery(Screen parent) {
        super(parent, HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438", "Death Screenshots"));
    }

    @Override
    protected int preferredPanelWidth() {
        return 460;
    }

    @Override
    protected int preferredPanelHeight() {
        return DeathScreenshotGallery.listPanelHeight(80, this.loading || this.failed ? 0 : this.entries.size(), 42, 64);
    }

    protected void init() {
        this.reload();
    }

    public void removed() {
        ++this.generation;
    }

    private void reload() {
        int token = ++this.generation;
        this.loading = true;
        this.failed = false;
        Minecraft client = this.minecraft;
        Path directory = client.gameDirectory.toPath();
        int limit = BloodHelperClient.config().deathScreenshot.galleryLimit;
        Util.ioPool().execute(() -> {
            List<Object> found = List.of();
            boolean error = false;
            try {
                found = DeathScreenshotFiles.list(directory, limit);
            }
            catch (Exception ignored) {
                error = true;
            }
            List<Object> result = found;
            boolean didFail = error;
            client.execute(() -> {
                if (this.generation != token) {
                    return;
                }
                this.entries = result;
                this.failed = didFail;
                this.loading = false;
                this.scroll = 0;
            });
        });
    }

    private void openFolder() {
        Path directory = this.minecraft.gameDirectory.toPath();
        Util.ioPool().execute(() -> {
            try {
                SDLMisc.SDL_OpenURL((CharSequence)DeathScreenshotFiles.ensureDirectory(directory).toUri().toString());
            }
            catch (Exception ignored) {
                this.minecraft.execute(() -> {
                    this.failed = true;
                });
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        this.frame(g, HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438", "Death Screenshots"), mouseX, mouseY);
        this.button(g, this.left + 14, this.top + 47, 104, HudStyle.tr("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c", "Refresh"), mouseX, mouseY, this::reload);
        this.button(g, this.left + 125, this.top + 47, 126, HudStyle.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Open folder"), mouseX, mouseY, this::openFolder);
        this.bodyTop = this.top + 80;
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (this.loading || this.failed || this.entries.isEmpty()) {
                String message = this.loading ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0433\u0430\u043b\u0435\u0440\u0435\u0438\u2026", "Loading the gallery\u2026") : (this.failed ? HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u043f\u0430\u043f\u043a\u0443 \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u043e\u0432.", "Could not read the screenshot folder.") : HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u043e\u0432 \u043f\u043e\u043a\u0430 \u043d\u0435\u0442. \u0412\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c: \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f \u0441\u043c\u0435\u0440\u0442\u044c \u0431\u0443\u0434\u0435\u0442 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430 \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438.", "No screenshots yet. Enable the module to capture your next death automatically."));
                int y = this.bodyTop + 14;
                for (String line : DeathScreenshotGallery.wrap(message, this.panelWidth - 40)) {
                    this.text(g, line, this.left + 20, y, -7105633);
                    y += 16;
                }
                this.maximumScroll = 0;
            } else {
                for (int i = 0; i < this.entries.size(); ++i) {
                    DeathScreenshotFiles.Entry entry = this.entries.get(i);
                    int y = this.bodyTop + i * 42 - this.scroll;
                    if (y + 36 < this.bodyTop || y > this.bodyBottom) continue;
                    UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 36, 6, ThemeColors.surface(-14342869));
                    String name = DATE.format(entry.time());
                    CustomFont.medium().draw(g, CustomFont.medium().ellipsize(name, 11.0f, Math.max(0, this.panelWidth - 136)), this.left + 24, y + 12, 11.0f, -1381908);
                    if (y < this.bodyTop || y + 36 > this.bodyBottom) continue;
                    this.button(g, this.left + this.panelWidth - 98, y + 7, 74, HudStyle.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c", "View"), mouseX, mouseY, () -> DeathScreenshotModule.openScreenshot(this, entry.filename()));
                }
            }
        }
        finally {
            g.disableScissor();
        }
        this.scrollbar(g, this.loading || this.entries.isEmpty() ? 0 : this.entries.size() * 42);
        this.footer(g, HudStyle.tr("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0435 " + BloodHelperClient.config().deathScreenshot.galleryLimit + " \u00b7 \u0424\u0430\u0439\u043b\u044b \u043d\u0435 \u0443\u0434\u0430\u043b\u044f\u044e\u0442\u0441\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438", "Latest " + BloodHelperClient.config().deathScreenshot.galleryLimit + " \u00b7 Files are never deleted automatically"));
    }
}

