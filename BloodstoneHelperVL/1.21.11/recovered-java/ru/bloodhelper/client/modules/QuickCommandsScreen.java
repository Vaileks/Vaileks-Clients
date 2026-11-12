/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_634
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_634;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.QuickCommandsSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.QuickCommandWheelState;
import ru.bloodhelper.client.modules.StreamerMode;

public final class QuickCommandsScreen
extends class_437 {
    private static final class_2960 SECTOR = class_2960.method_60655((String)"bloodhelper", (String)"textures/gui/quick_sector.png");
    private static final class_2960 EDGE = class_2960.method_60655((String)"bloodhelper", (String)"textures/gui/quick_sector_edge.png");
    private static final int[] DX = new int[]{0, 1, 0, -1};
    private static final int[] DY = new int[]{-1, 0, 1, 0};
    private final QuickCommandWheelState state;
    private final class_634 connection;
    private final float[] hover = new float[4];
    private final long openedAt;
    private long previousFrame = this.openedAt = System.nanoTime();
    private long closedAt;

    public QuickCommandsScreen(QuickCommandsSettings settings) {
        super((class_2561)class_2561.method_43470((String)"Quick Commands"));
        this.state = new QuickCommandWheelState(settings);
        this.connection = class_310.method_1551().method_1562();
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25420(class_332 g, int mx, int my, float delta) {
    }

    private float baseScale() {
        return Math.min(1.1f, Math.max(0.3f, ((float)Math.min(this.field_22789, this.field_22790) - 44.0f) / 280.0f));
    }

    private float progress(long now) {
        if (!BloodHelperClient.config().animations) {
            return this.closedAt == 0L ? 1.0f : 0.0f;
        }
        double t = this.closedAt == 0L ? Math.clamp((double)(now - this.openedAt) / 1.6E8, 0.0, 1.0) : 1.0 - Math.clamp((double)(now - this.closedAt) / 9.5E7, 0.0, 1.0);
        return (float)(1.0 - Math.pow(1.0 - t, 3.0));
    }

    public void method_25393() {
        if (this.field_22787.field_1724 == null || this.field_22787.method_1562() != this.connection || !BloodHelperClient.config().quickCommands.enabled) {
            this.closeNow();
        } else if (this.closedAt != 0L && this.progress(System.nanoTime()) <= 0.0f) {
            this.closeNow();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        double dt = Math.clamp((double)(now - this.previousFrame) / 1.0E9, 0.0, 0.05);
        this.previousFrame = now;
        float progress = this.progress(now);
        float base = this.baseScale();
        float scale = base * (0.91f + 0.09f * progress);
        int active = this.closedAt == 0L ? this.sector(mouseX, mouseY) : -1;
        g.method_25294(0, 0, this.field_22789, this.field_22790, UiDraw.alpha(526604, Math.round(90.0f * progress)));
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f);
            g.method_51448().scale(scale, scale);
            int backdrop = UiDraw.alpha(ThemeColors.surface(-15395557), Math.round(180.0f * progress));
            g.method_51448().pushMatrix();
            try {
                g.method_51448().scale(2.015873f, 2.015873f);
                LiquidGlass.surface(g, -63, -63, 126, 126, 63, backdrop, BloodHelperClient.config());
            }
            finally {
                g.method_51448().popMatrix();
            }
            for (int i = 0; i < 4; ++i) {
                boolean enabled = this.state.action(i) != null;
                float target = active == i && enabled ? 1.0f : 0.0f;
                int n = i;
                this.hover[n] = this.hover[n] + (target - this.hover[i]) * (float)(BloodHelperClient.config().animations ? 1.0 - Math.exp(-18.0 * dt) : 1.0);
                float lift = this.hover[i] * 3.0f;
                g.method_51448().pushMatrix();
                try {
                    g.method_51448().translate((float)DX[i] * lift, (float)DY[i] * lift);
                    g.method_51448().rotate((float)((double)i * Math.PI / 2.0));
                    int fill = UiDraw.blend(ThemeColors.surface(-12303280), ThemeColors.accent(), (double)this.hover[i] * 0.48);
                    QuickCommandsScreen.blit(g, SECTOR, UiDraw.alpha(fill, Math.round((float)(enabled ? 225 : 115) * progress)));
                    int edge = UiDraw.blend(ThemeColors.surface(-5197639), ThemeColors.accent(), this.hover[i]);
                    QuickCommandsScreen.blit(g, EDGE, UiDraw.alpha(edge, Math.round((80.0f + 140.0f * this.hover[i]) * progress)));
                }
                finally {
                    g.method_51448().popMatrix();
                }
                float x = (float)DX[i] * (84.0f + lift);
                float y = (float)DY[i] * (84.0f + lift);
                String label = StreamerMode.hideChat() ? HudStyle.tr("\u0421\u043b\u043e\u0442 ", "Slot ") + (i + 1) : (enabled ? this.state.label(i) : HudStyle.tr("\u041f\u0443\u0441\u0442\u043e", "Empty"));
                float available = i % 2 == 0 ? 142.0f : 72.0f;
                label = CustomFont.medium().ellipsize(label, 12.0f, available);
                QuickCommandsScreen.centered(g, label, x, y - 9.0f, 12.0f, UiDraw.alpha(enabled ? -921099 : -7763567, Math.round(255.0f * progress)));
                String type = enabled ? (this.state.action(i).command() ? HudStyle.tr("\u041a\u041e\u041c\u0410\u041d\u0414\u0410", "COMMAND") : HudStyle.tr("\u0412 \u0427\u0410\u0422", "CHAT")) : "\u2014";
                QuickCommandsScreen.centered(g, type, x, y + 8.0f, 8.0f, UiDraw.alpha(ThemeColors.text(-5592401), Math.round(210.0f * progress)));
            }
            boolean central = Math.hypot(((float)mouseX - (float)this.field_22789 / 2.0f) / base, ((float)mouseY - (float)this.field_22790 / 2.0f) / base) < 40.0;
            UiDraw.outline(g, -37, -37, 74, 74, 37, UiDraw.alpha(ThemeColors.accent(), Math.round((float)(central ? 160 : 60) * progress)));
            QuickCommandsScreen.centered(g, "\u00d7", 0.0f, -18.0f, 23.0f, UiDraw.alpha(-1644820, Math.round(255.0f * progress)));
            QuickCommandsScreen.centered(g, HudStyle.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), 0.0f, 9.0f, 10.0f, UiDraw.alpha(-5526604, Math.round(255.0f * progress)));
        }
        finally {
            g.method_51448().popMatrix();
        }
        QuickCommandsScreen.centered(g, HudStyle.tr("\u041b\u041a\u041c \u2014 \u0432\u044b\u0431\u0440\u0430\u0442\u044c  \u00b7  Esc \u2014 \u0437\u0430\u043a\u0440\u044b\u0442\u044c", "Click to choose  \u00b7  Esc to close"), (float)this.field_22789 / 2.0f, Math.min((float)(this.field_22790 - 15), (float)this.field_22790 / 2.0f + 137.0f * base), 10.0f, UiDraw.alpha(-2039578, Math.round(220.0f * progress)));
    }

    private static void blit(class_332 g, class_2960 texture, int color) {
        g.method_25293(class_10799.field_56883, texture, -128, -128, 0.0f, 0.0f, 256, 256, 512, 512, 512, 512, color);
    }

    private static void centered(class_332 g, String label, float x, float y, float size, int color) {
        CustomFont.medium().draw(g, label, x - CustomFont.medium().width(label, size) / 2.0f, y, size, color);
    }

    private int sector(double x, double y) {
        float scale = this.baseScale() * (0.91f + 0.09f * this.progress(System.nanoTime()));
        return QuickCommandWheelState.sector((x - (double)this.field_22789 / 2.0) / (double)scale, (y - (double)this.field_22790 / 2.0) / (double)scale);
    }

    public boolean method_25402(class_11909 event, boolean doubled) {
        if (this.closedAt != 0L) {
            return true;
        }
        if (event.method_74245() == 1 || event.method_74245() == 2) {
            this.method_25419();
            return true;
        }
        if (event.method_74245() != 0) {
            return true;
        }
        int selected = this.sector(event.comp_4798(), event.comp_4799());
        if (selected < 0) {
            this.method_25419();
            return true;
        }
        if (!BloodHelperClient.config().quickCommands.enabled || this.field_22787.method_1562() != this.connection) {
            this.method_25419();
            return true;
        }
        this.state.select(selected).ifPresent(action -> {
            if (action.command()) {
                this.connection.method_45730(action.value());
            } else {
                this.connection.method_45729(action.value());
            }
            this.beginClose();
        });
        return true;
    }

    public boolean method_25404(class_11908 event) {
        if (event.comp_4795() == 256) {
            this.method_25419();
            return true;
        }
        return true;
    }

    public void method_25419() {
        this.state.cancel();
        this.beginClose();
    }

    private void beginClose() {
        if (this.closedAt == 0L) {
            this.closedAt = System.nanoTime();
        }
    }

    private void closeNow() {
        this.state.cancel();
        if (this.field_22787.field_1755 == this) {
            this.field_22787.method_1507(null);
        }
    }
}

