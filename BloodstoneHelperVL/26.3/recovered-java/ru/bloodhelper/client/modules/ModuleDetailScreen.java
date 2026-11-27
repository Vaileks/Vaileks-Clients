/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.MenuBackdrop;
import ru.bloodhelper.client.gui.ScrollBarDrag;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;

public abstract class ModuleDetailScreen
extends Screen
implements BackgroundMenuScreen {
    protected static final float PANEL_SCALE = 0.7f;
    protected static final int TEXT = -1381908;
    protected static final int MUTED = -7105633;
    protected final Screen parent;
    protected int left;
    protected int top;
    protected int panelWidth;
    protected int panelHeight;
    protected int bodyTop;
    protected int bodyBottom;
    protected int scroll;
    protected int maximumScroll;
    private final ScrollBarDrag scrollBar = new ScrollBarDrag();
    private float drawnScale = 0.7f;
    private float drawnLift;
    protected final List<Action> actions = new ArrayList<Action>();

    protected double appearance(long now) {
        return 1.0;
    }

    protected ModuleDetailScreen(Screen parent, String title) {
        super((Component)Component.literal((String)title));
        this.parent = parent;
    }

    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        if (this.minecraft.level == null) {
            this.extractPanorama(g, delta);
        }
        MenuBackdrop.render(g);
        this.minecraft.gui.hud.extractDeferredSubtitles();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        double appear = Math.clamp(this.appearance(System.nanoTime()), 0.0, 1.0);
        this.drawnScale = (float)((double)0.7f * (0.94 + 0.06 * appear));
        this.drawnLift = (float)(12.0 * (1.0 - appear));
        g.fill(0, 0, this.width, this.height, UiDraw.alpha(592139, (int)(144.0 * appear)));
        g.pose().pushMatrix();
        try {
            g.pose().translate((float)this.width * 0.5f, (float)this.height * 0.5f + this.drawnLift);
            g.pose().scale(this.drawnScale, this.drawnScale);
            g.pose().translate((float)(-this.width) * 0.5f, (float)(-this.height) * 0.5f);
            this.renderPanel(g, (int)Math.floor(this.panelX(mx)), (int)Math.floor(this.panelY(my)), delta);
        }
        finally {
            g.pose().popMatrix();
        }
    }

    protected abstract void renderPanel(GuiGraphicsExtractor var1, int var2, int var3, float var4);

    protected int preferredPanelWidth() {
        return 560;
    }

    protected int preferredPanelHeight() {
        return 410;
    }

    protected final void layoutFrame() {
        this.panelWidth = Math.max(1, Math.min(this.preferredPanelWidth(), (int)((float)(this.width - 20) / 0.7f)));
        this.panelHeight = Math.max(1, Math.min(this.preferredPanelHeight(), (int)((float)(this.height - 20) / 0.7f)));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.bodyTop = this.top + 48;
        this.bodyBottom = this.top + this.panelHeight - 26;
        this.actions.clear();
        this.scrollBar.hide();
    }

    protected static int listPanelHeight(int header, int rows, int rowHeight, int emptyHeight) {
        return Math.min(410, header + 26 + Math.max(emptyHeight, Math.min(256, rows) * rowHeight));
    }

    protected void frame(GuiGraphicsExtractor g, String title, int mx, int my) {
        this.layoutFrame();
        LiquidGlass.surfaceMenu(g, this.left, this.top, this.panelWidth, this.panelHeight, 12, ThemeColors.surface(-99018467), BloodHelperClient.config());
        if (!"OUTLINE".equals(BloodHelperClient.config().surfaceStyle)) {
            UiDraw.outline(g, this.left, this.top, this.panelWidth, this.panelHeight, 12, ThemeColors.surface(-13355972));
        }
        g.fill(this.left + 14, this.top + 37, this.left + this.panelWidth - 14, this.top + 38, ThemeColors.surface(-13619144));
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(title, 14.0f, Math.max(0, this.panelWidth - 112)), this.left + 14, this.top + 12, 14.0f, -1381908);
        this.button(g, this.left + this.panelWidth - 76, this.top + 10, 62, HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my, this::onClose);
    }

    protected void button(GuiGraphicsExtractor g, int x, int y, int w, String text, int mx, int my, Runnable callback) {
        Action action = new Action(x, y, w, 21, callback);
        this.actions.add(action);
        boolean hovered = action.contains(mx, my);
        UiDraw.rounded(g, x, y, w, 21, 6, ThemeColors.surface(hovered ? -11451061 : -13421766));
        UiDraw.outline(g, x, y, w, 21, 6, hovered ? UiDraw.alpha(BloodHelperClient.config().accent, 180) : ThemeColors.surface(-11184801));
        CustomFont font = CustomFont.medium();
        String label = font.ellipsize(text, 10.0f, Math.max(0, w - 10));
        font.draw(g, label, (float)x + ((float)w - font.width(label, 10.0f)) / 2.0f, y + 5, 10.0f, -1381908);
    }

    protected void text(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        CustomFont.regular().draw(g, text, x, y, 11.0f, color);
    }

    protected void footer(GuiGraphicsExtractor g, String label) {
        g.fill(this.left + 1, this.bodyBottom + 6, this.left + this.panelWidth - 1, this.bodyBottom + 7, ThemeColors.line());
        CustomFont.regular().draw(g, CustomFont.regular().ellipsize(label, 9.0f, Math.max(0, this.panelWidth - 28)), this.left + 14, this.top + this.panelHeight - 15, 9.0f, -7105633);
    }

    protected void scrollbar(GuiGraphicsExtractor g, int contentHeight) {
        int available = Math.max(1, this.bodyBottom - this.bodyTop);
        this.maximumScroll = Math.max(0, contentHeight - available);
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        this.scrollBar.configure(this.left + this.panelWidth - 7, this.bodyTop, 2.0, available, 16.0, this.scroll, this.maximumScroll);
        if (this.maximumScroll == 0) {
            return;
        }
        int barHeight = Math.max(16, available * available / Math.max(1, contentHeight));
        int position = this.bodyTop + (available - barHeight) * this.scroll / Math.max(1, this.maximumScroll);
        UiDraw.rounded(g, this.left + this.panelWidth - 7, position, 2, barHeight, 1, -10197905);
    }

    protected static List<String> wrap(String value, float maxWidth) {
        return CustomFont.regular().wrap(value, 11.0f, Math.max(1.0f, maxWidth));
    }

    private double panelX(double x) {
        return (double)this.width * 0.5 + (x - (double)this.width * 0.5) / (double)this.drawnScale;
    }

    private double panelY(double y) {
        return (double)this.height * 0.5 + (y - (double)this.height * 0.5 - (double)this.drawnLift) / (double)this.drawnScale;
    }

    private MouseButtonEvent panelEvent(MouseButtonEvent event) {
        return new MouseButtonEvent(this.panelX(event.x()), this.panelY(event.y()), event.buttonInfo());
    }

    public final boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        MouseButtonEvent local = this.panelEvent(event);
        if (event.button() == 1 && this.scrollBar.press(local.x(), local.y())) {
            this.scroll = (int)Math.round(this.scrollBar.value());
            return true;
        }
        return this.panelMouseClicked(local, doubled);
    }

    public final boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        MouseButtonEvent local = this.panelEvent(event);
        if (this.scrollBar.drag(local.y())) {
            this.scroll = (int)Math.round(this.scrollBar.value());
            return true;
        }
        return this.panelMouseDragged(local, dx / (double)this.drawnScale, dy / (double)this.drawnScale);
    }

    public final boolean mouseReleased(MouseButtonEvent event) {
        if (this.scrollBar.release()) {
            return true;
        }
        return this.panelMouseReleased(this.panelEvent(event));
    }

    public final boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return this.panelMouseScrolled(this.panelX(x), this.panelY(y), horizontal, vertical);
    }

    protected boolean panelMouseDragged(MouseButtonEvent event, double dx, double dy) {
        return super.mouseDragged(event, dx, dy);
    }

    protected boolean panelMouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(event);
    }

    protected boolean panelMouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 1) {
            for (Action action : List.copyOf(this.actions)) {
                if (!action.contains(event.x(), event.y())) continue;
                action.click.run();
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= (double)this.left && x < (double)(this.left + this.panelWidth) && y >= (double)this.bodyTop && y < (double)this.bodyBottom) {
            this.scroll = Math.clamp((long)(this.scroll - (int)Math.round(vertical * 28.0)), 0, this.maximumScroll);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    protected record Action(int x, int y, int width, int height, Runnable click) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.width) && my >= (double)this.y && my < (double)(this.y + this.height);
        }
    }
}

