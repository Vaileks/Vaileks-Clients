/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Util
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ImageClipboard;
import ru.bloodhelper.client.modules.ScreenshotActions;
import ru.bloodhelper.client.modules.ScreenshotImages;

public final class ScreenshotEditorScreen
extends Screen
implements BackgroundMenuScreen {
    private final Screen parent;
    private final Path original;
    private final Identifier texture = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("screenshot_editor/" + String.valueOf(UUID.randomUUID())));
    private ScreenshotImages.Tool tool;
    private BufferedImage image;
    private BufferedImage undo;
    private boolean busy;
    private boolean ready;
    private boolean dragging;
    private int generation;
    private int ix;
    private int iy;
    private int iw;
    private int ih;
    private int tw;
    private int th;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private String status = "";

    public ScreenshotEditorScreen(Screen parent, Path file, ScreenshotImages.Tool tool) {
        super((Component)Component.literal((String)HudStyle.tr("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u0430", "Screenshot editor")));
        this.parent = parent;
        this.original = file;
        this.tool = tool;
    }

    protected void init() {
        String[] labels = new String[]{HudStyle.tr("\u041e\u0431\u0440\u0435\u0437\u0430\u0442\u044c", "Crop"), HudStyle.tr("\u0417\u0430\u043c\u0430\u0437\u0430\u0442\u044c", "Redact"), HudStyle.tr("\u0421\u0442\u0440\u0435\u043b\u043a\u0430", "Arrow"), HudStyle.tr("\u041e\u0442\u043c\u0435\u043d\u0438\u0442\u044c", "Undo"), HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Save"), HudStyle.tr("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c", "Copy"), HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back")};
        int bw = Math.max(32, Math.min(94, (this.width - 24) / 7));
        int x = (this.width - bw * 7) / 2;
        for (int i = 0; i < labels.length; ++i) {
            int action = i;
            this.addRenderableWidget((GuiEventListener)new EditorButton(this, x + i * bw, 27, bw - 3, 20, labels[i], action));
        }
        if (this.image == null && !this.busy) {
            this.load();
        }
    }

    private void load() {
        this.busy = true;
        this.status = HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430\u2026", "Loading\u2026");
        int token = ++this.generation;
        Minecraft client = this.minecraft;
        Util.ioPool().execute(() -> {
            try {
                this.publish(token, ScreenshotImages.read(ScreenshotActions.allowed(this.original)), null);
            }
            catch (Exception failure) {
                client.execute(() -> {
                    if (token == this.generation) {
                        this.busy = false;
                        this.status = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Could not open image");
                    }
                });
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void publish(int token, BufferedImage result, BufferedImage previous) throws Exception {
        double scale = Math.min(1.0, Math.min(1600.0 / (double)result.getWidth(), 1000.0 / (double)result.getHeight()));
        BufferedImage preview = new BufferedImage(Math.max(1, (int)((double)result.getWidth() * scale)), Math.max(1, (int)((double)result.getHeight() * scale)), 2);
        Graphics2D graphics = preview.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(result, 0, 0, preview.getWidth(), preview.getHeight(), null);
        }
        finally {
            graphics.dispose();
        }
        NativeImage pixels = NativeImage.read((InputStream)new ByteArrayInputStream(ScreenshotImages.png(preview)));
        this.minecraft.execute(() -> {
            if (token != this.generation) {
                pixels.close();
                return;
            }
            try {
                this.tw = pixels.getWidth();
                this.th = pixels.getHeight();
                this.minecraft.getTextureManager().register(this.texture, (AbstractTexture)new DynamicTexture(() -> "BloodHelper screenshot editor", pixels));
                this.image = result;
                this.undo = previous;
                this.ready = true;
                this.busy = false;
                this.status = "";
            }
            catch (RuntimeException failure) {
                pixels.close();
                this.busy = false;
                this.status = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Could not display image");
            }
        });
    }

    private void action(int action) {
        if (action == 6) {
            this.onClose();
            return;
        }
        if (this.busy) {
            return;
        }
        if (action < 3) {
            this.tool = ScreenshotImages.Tool.values()[action];
            return;
        }
        if (this.image == null) {
            return;
        }
        if (action == 3) {
            if (this.undo != null) {
                BufferedImage previous = this.undo;
                this.update(() -> previous, false);
            }
            return;
        }
        BufferedImage snapshot = this.image;
        this.busy = true;
        this.status = action == 4 ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u2026", "Saving\u2026") : HudStyle.tr("\u041a\u043e\u043f\u0438\u0440\u0443\u044e\u2026", "Copying\u2026");
        int token = this.generation;
        Util.ioPool().execute(() -> {
            String message;
            try {
                if (action == 4) {
                    Path file = ScreenshotImages.saveCopy(ScreenshotActions.allowed(this.original), snapshot);
                    ScreenshotActions.saved(file);
                    message = HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430 \u043e\u0442\u0434\u0435\u043b\u044c\u043d\u0430\u044f \u043a\u043e\u043f\u0438\u044f", "Saved a separate copy");
                } else {
                    ImageClipboard.copy(snapshot);
                    message = HudStyle.tr("\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u0432 \u0431\u0443\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430", "Image copied to clipboard");
                }
            }
            catch (Exception failure) {
                message = HudStyle.tr("\u041e\u043f\u0435\u0440\u0430\u0446\u0438\u044f \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c", "Operation failed");
            }
            String result = message;
            this.minecraft.execute(() -> {
                if (token == this.generation) {
                    this.busy = false;
                    this.status = result;
                }
            });
        });
    }

    private void update(Supplier<BufferedImage> operation, boolean retainUndo) {
        this.busy = true;
        int token = ++this.generation;
        BufferedImage previous = retainUndo && (long)this.image.getWidth() * (long)this.image.getHeight() <= 0x800000L ? this.image : null;
        Util.ioPool().execute(() -> {
            try {
                this.publish(token, (BufferedImage)operation.get(), previous);
            }
            catch (Exception failure) {
                this.minecraft.execute(() -> {
                    if (token == this.generation) {
                        this.busy = false;
                        this.status = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0438\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Could not edit image");
                    }
                });
            }
        });
    }

    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, this.width, this.height, -15921388);
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.centered(g, this.title.getString(), 8, 12, -986378);
        if (this.ready) {
            float scale = Math.min(((float)this.width - 24.0f) / (float)this.tw, Math.max(1.0f, (float)this.height - 99.0f) / (float)this.th);
            this.iw = Math.max(1, (int)((float)this.tw * scale));
            this.ih = Math.max(1, (int)((float)this.th * scale));
            this.ix = (this.width - this.iw) / 2;
            this.iy = 54 + (this.height - 99 - this.ih) / 2;
            g.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.ix, this.iy, 0.0f, 0.0f, this.iw, this.ih, this.tw, this.th, this.tw, this.th);
            if (this.dragging) {
                int x = this.screenX(this.startX);
                int y = this.screenY(this.startY);
                int ex = this.screenX(this.endX);
                int ey = this.screenY(this.endY);
                int l = Math.min(x, ex);
                int t = Math.min(y, ey);
                int r = Math.max(x, ex);
                int b = Math.max(y, ey);
                int color = this.tool == ScreenshotImages.Tool.REDACT ? -585886180 : 1149952192;
                g.fill(l, t, r + 1, b + 1, color);
                g.outline(l, t, Math.max(1, r - l + 1), Math.max(1, b - t + 1), -7675712);
            }
        }
        String hint = !this.status.isEmpty() ? this.status : (this.tool == ScreenshotImages.Tool.CROP ? HudStyle.tr("\u0412\u044b\u0434\u0435\u043b\u0438 \u043e\u0431\u043b\u0430\u0441\u0442\u044c \u0434\u043b\u044f \u043e\u0431\u0440\u0435\u0437\u043a\u0438", "Drag to crop") : (this.tool == ScreenshotImages.Tool.REDACT ? HudStyle.tr("\u0412\u044b\u0434\u0435\u043b\u0438 \u043e\u0431\u043b\u0430\u0441\u0442\u044c: \u043e\u043d\u0430 \u0431\u0443\u0434\u0435\u0442 \u0437\u0430\u043a\u0440\u0430\u0448\u0435\u043d\u0430", "Drag to cover an area") : HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0434\u0438 \u0441\u0442\u0440\u0435\u043b\u043a\u0443 \u043e\u0442 \u043d\u0430\u0447\u0430\u043b\u0430 \u043a \u043a\u043e\u043d\u0446\u0443", "Drag from arrow tail to tip")));
        this.centered(g, hint, this.height - 30, 10, -2038550);
        if (this.image != null) {
            this.centered(g, this.image.getWidth() + " \u00d7 " + this.image.getHeight(), this.height - 16, 9, -6445643);
        }
        super.extractRenderState(g, mx, my, delta);
    }

    private void centered(GuiGraphicsExtractor g, String text, int y, int size, int color) {
        CustomFont f = CustomFont.regular();
        String visible = f.ellipsize(text, size, Math.max(1, this.width - 20));
        f.drawRaw(g, visible, ((float)this.width - f.width(visible, size)) / 2.0f, y, size, color);
    }

    private int pixelX(double x) {
        return Math.clamp((long)((int)((x - (double)this.ix) * (double)this.image.getWidth() / (double)this.iw)), 0, this.image.getWidth() - 1);
    }

    private int pixelY(double y) {
        return Math.clamp((long)((int)((y - (double)this.iy) * (double)this.image.getHeight() / (double)this.ih)), 0, this.image.getHeight() - 1);
    }

    private int screenX(int x) {
        return this.ix + (int)((long)x * (long)this.iw / (long)this.image.getWidth());
    }

    private int screenY(int y) {
        return this.iy + (int)((long)y * (long)this.ih / (long)this.image.getHeight());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }
        if (!this.busy && this.ready && event.button() == 1 && event.x() >= (double)this.ix && event.x() < (double)(this.ix + this.iw) && event.y() >= (double)this.iy && event.y() < (double)(this.iy + this.ih)) {
            this.dragging = true;
            this.startX = this.endX = this.pixelX(event.x());
            this.startY = this.endY = this.pixelY(event.y());
            return true;
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging) {
            this.endX = this.pixelX(event.x());
            this.endY = this.pixelY(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging && event.button() == 1) {
            this.dragging = false;
            this.endX = this.pixelX(event.x());
            this.endY = this.pixelY(event.y());
            if (Math.abs(this.endX - this.startX) + Math.abs(this.endY - this.startY) >= 3) {
                BufferedImage source = this.image;
                ScreenshotImages.Tool selected = this.tool;
                int x1 = this.startX;
                int y1 = this.startY;
                int x2 = this.endX;
                int y2 = this.endY;
                this.update(() -> ScreenshotImages.edit(source, selected, x1, y1, x2, y2), true);
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 29 && event.hasControlDown()) {
            this.action(3);
            return true;
        }
        return super.keyPressed(event);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public void removed() {
        ++this.generation;
        this.dragging = false;
        if (this.ready) {
            this.minecraft.getTextureManager().release(this.texture);
        }
        this.ready = false;
        this.image = null;
        this.undo = null;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private final class EditorButton
    extends Button {
        private final int actionId;
        final /* synthetic */ ScreenshotEditorScreen this$0;

        EditorButton(ScreenshotEditorScreen screenshotEditorScreen, int x, int y, int w, int h, String label, int actionId) {
            ScreenshotEditorScreen screenshotEditorScreen2 = screenshotEditorScreen;
            Objects.requireNonNull(screenshotEditorScreen2);
            this.this$0 = screenshotEditorScreen2;
            super(x, y, w, h, (Component)Component.literal((String)label), b -> screenshotEditorScreen.action(actionId), DEFAULT_NARRATION);
            this.actionId = actionId;
        }

        protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
            boolean selected;
            boolean bl = selected = this.actionId < 3 && this.this$0.tool.ordinal() == this.actionId;
            int fill = selected ? -14138305 : (this.isHoveredOrFocused() ? -13617341 : -14999511);
            UiDraw.border(g, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 5, fill, selected ? -7675712 : -12037277);
            CustomFont f = CustomFont.medium();
            String label = f.ellipsize(this.getMessage().getString(), 10.0f, this.getWidth() - 8);
            f.drawRaw(g, label, (float)this.getX() + ((float)this.getWidth() - f.width(label, 10.0f)) / 2.0f, this.getY() + 4, 10.0f, this.this$0.busy && this.actionId != 6 ? -7761507 : -1840657);
        }
    }
}

