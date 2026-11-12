/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
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
extends class_437
implements BackgroundMenuScreen {
    protected static final float PANEL_SCALE = 0.7f;
    protected static final int TEXT = -1381908;
    protected static final int MUTED = -7105633;
    protected final class_437 parent;
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

    protected ModuleDetailScreen(class_437 parent, String title) {
        super((class_2561)class_2561.method_43470((String)title));
        this.parent = parent;
    }

    public void method_25420(class_332 g, int mx, int my, float delta) {
        if (this.field_22787.field_1687 == null) {
            this.method_57728(g, delta);
        }
        MenuBackdrop.render(g);
        this.field_22787.field_1705.method_72738();
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void method_25394(class_332 g, int mx, int my, float delta) {
        double appear = Math.clamp(this.appearance(System.nanoTime()), 0.0, 1.0);
        this.drawnScale = (float)((double)0.7f * (0.94 + 0.06 * appear));
        this.drawnLift = (float)(12.0 * (1.0 - appear));
        g.method_25294(0, 0, this.field_22789, this.field_22790, UiDraw.alpha(592139, (int)(144.0 * appear)));
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate((float)this.field_22789 * 0.5f, (float)this.field_22790 * 0.5f + this.drawnLift);
            g.method_51448().scale(this.drawnScale, this.drawnScale);
            g.method_51448().translate((float)(-this.field_22789) * 0.5f, (float)(-this.field_22790) * 0.5f);
            this.renderPanel(g, (int)Math.floor(this.panelX(mx)), (int)Math.floor(this.panelY(my)), delta);
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    protected abstract void renderPanel(class_332 var1, int var2, int var3, float var4);

    protected int preferredPanelWidth() {
        return 560;
    }

    protected int preferredPanelHeight() {
        return 410;
    }

    protected final void layoutFrame() {
        this.panelWidth = Math.max(1, Math.min(this.preferredPanelWidth(), (int)((float)(this.field_22789 - 20) / 0.7f)));
        this.panelHeight = Math.max(1, Math.min(this.preferredPanelHeight(), (int)((float)(this.field_22790 - 20) / 0.7f)));
        this.left = (this.field_22789 - this.panelWidth) / 2;
        this.top = (this.field_22790 - this.panelHeight) / 2;
        this.bodyTop = this.top + 48;
        this.bodyBottom = this.top + this.panelHeight - 26;
        this.actions.clear();
        this.scrollBar.hide();
    }

    protected static int listPanelHeight(int header, int rows, int rowHeight, int emptyHeight) {
        return Math.min(410, header + 26 + Math.max(emptyHeight, Math.min(256, rows) * rowHeight));
    }

    protected void frame(class_332 g, String title, int mx, int my) {
        this.layoutFrame();
        LiquidGlass.surfaceMenu(g, this.left, this.top, this.panelWidth, this.panelHeight, 12, ThemeColors.surface(-99018467), BloodHelperClient.config());
        if (!"OUTLINE".equals(BloodHelperClient.config().surfaceStyle)) {
            UiDraw.outline(g, this.left, this.top, this.panelWidth, this.panelHeight, 12, ThemeColors.surface(-13355972));
        }
        g.method_25294(this.left + 14, this.top + 37, this.left + this.panelWidth - 14, this.top + 38, ThemeColors.surface(-13619144));
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(title, 14.0f, Math.max(0, this.panelWidth - 112)), this.left + 14, this.top + 12, 14.0f, -1381908);
        this.button(g, this.left + this.panelWidth - 76, this.top + 10, 62, HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my, this::method_25419);
    }

    protected void button(class_332 g, int x, int y, int w, String text, int mx, int my, Runnable callback) {
        Action action = new Action(x, y, w, 21, callback);
        this.actions.add(action);
        boolean hovered = action.contains(mx, my);
        UiDraw.rounded(g, x, y, w, 21, 6, ThemeColors.surface(hovered ? -11451061 : -13421766));
        UiDraw.outline(g, x, y, w, 21, 6, hovered ? UiDraw.alpha(BloodHelperClient.config().accent, 180) : ThemeColors.surface(-11184801));
        CustomFont font = CustomFont.medium();
        String label = font.ellipsize(text, 10.0f, Math.max(0, w - 10));
        font.draw(g, label, (float)x + ((float)w - font.width(label, 10.0f)) / 2.0f, y + 5, 10.0f, -1381908);
    }

    protected void text(class_332 g, String text, int x, int y, int color) {
        CustomFont.regular().draw(g, text, x, y, 11.0f, color);
    }

    protected void footer(class_332 g, String label) {
        g.method_25294(this.left + 1, this.bodyBottom + 6, this.left + this.panelWidth - 1, this.bodyBottom + 7, ThemeColors.line());
        CustomFont.regular().draw(g, CustomFont.regular().ellipsize(label, 9.0f, Math.max(0, this.panelWidth - 28)), this.left + 14, this.top + this.panelHeight - 15, 9.0f, -7105633);
    }

    protected void scrollbar(class_332 g, int contentHeight) {
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
        return (double)this.field_22789 * 0.5 + (x - (double)this.field_22789 * 0.5) / (double)this.drawnScale;
    }

    private double panelY(double y) {
        return (double)this.field_22790 * 0.5 + (y - (double)this.field_22790 * 0.5 - (double)this.drawnLift) / (double)this.drawnScale;
    }

    private class_11909 panelEvent(class_11909 event) {
        return new class_11909(this.panelX(event.comp_4798()), this.panelY(event.comp_4799()), event.comp_4800());
    }

    public final boolean method_25402(class_11909 event, boolean doubled) {
        class_11909 local = this.panelEvent(event);
        if (event.method_74245() == 0 && this.scrollBar.press(local.comp_4798(), local.comp_4799())) {
            this.scroll = (int)Math.round(this.scrollBar.value());
            return true;
        }
        return this.panelMouseClicked(local, doubled);
    }

    public final boolean method_25403(class_11909 event, double dx, double dy) {
        class_11909 local = this.panelEvent(event);
        if (this.scrollBar.drag(local.comp_4799())) {
            this.scroll = (int)Math.round(this.scrollBar.value());
            return true;
        }
        return this.panelMouseDragged(local, dx / (double)this.drawnScale, dy / (double)this.drawnScale);
    }

    public final boolean method_25406(class_11909 event) {
        if (this.scrollBar.release()) {
            return true;
        }
        return this.panelMouseReleased(this.panelEvent(event));
    }

    public final boolean method_25401(double x, double y, double horizontal, double vertical) {
        return this.panelMouseScrolled(this.panelX(x), this.panelY(y), horizontal, vertical);
    }

    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        return super.method_25403(event, dx, dy);
    }

    protected boolean panelMouseReleased(class_11909 event) {
        return super.method_25406(event);
    }

    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        if (event.method_74245() == 0) {
            for (Action action : List.copyOf(this.actions)) {
                if (!action.contains(event.comp_4798(), event.comp_4799())) continue;
                action.click.run();
                return true;
            }
        }
        return super.method_25402(event, doubled);
    }

    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= (double)this.left && x < (double)(this.left + this.panelWidth) && y >= (double)this.bodyTop && y < (double)this.bodyBottom) {
            this.scroll = Math.clamp((long)(this.scroll - (int)Math.round(vertical * 28.0)), 0, this.maximumScroll);
            return true;
        }
        return super.method_25401(x, y, horizontal, vertical);
    }

    protected record Action(int x, int y, int width, int height, Runnable click) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.width) && my >= (double)this.y && my < (double)(this.y + this.height);
        }
    }
}

