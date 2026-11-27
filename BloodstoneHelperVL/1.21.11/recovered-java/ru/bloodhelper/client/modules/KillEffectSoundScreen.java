/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.KillEffectSoundCatalog;
import ru.bloodhelper.client.modules.LegendarySoundVolume;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class KillEffectSoundScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 21, "");
    private List<KillEffectSoundCatalog.Entry> filtered = KillEffectSoundCatalog.entries();
    private final List<Knob> knobs = new ArrayList<Knob>();
    private Knob dragging;

    public KillEffectSoundScreen(class_437 parent) {
        super(parent, "Kill effects");
        this.search.setMaxLength(80);
        this.search.setTextSize(11.0f);
        this.search.setResponder(value -> {
            String query = value.strip().toLowerCase(Locale.ROOT);
            this.filtered = KillEffectSoundCatalog.entries().stream().filter(e -> e.matches(query)).toList();
            this.scroll = 0;
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u041a\u0438\u043b\u043b-\u044d\u0444\u0444\u0435\u043a\u0442\u044b \u00b7 \u043f\u043e \u043e\u0442\u0434\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u0438", "Kill effects \u00b7 individual volume"), mx, my);
        UiDraw.rounded(g, this.left + 14, this.bodyTop, this.panelWidth - 28, 23, 6, ThemeColors.surface(-14211282));
        this.search.setX(this.left + 21);
        this.search.setY(this.bodyTop + 1);
        this.search.setWidth(this.panelWidth - 42);
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u044d\u0444\u0444\u0435\u043a\u0442\u0430", "Search effect"));
        this.search.render(g, mx, my, delta);
        this.bodyTop += 31;
        this.knobs.clear();
        this.scrollbar(g, this.filtered.size() * 34);
        ClientConfig.LegendarySoundSettings settings = BloodHelperClient.config().legendarySounds;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int y;
            for (int i = Math.max(0, this.scroll / 34); i < this.filtered.size() && (y = this.bodyTop + i * 34 - this.scroll) < this.bodyBottom; ++i) {
                KillEffectSoundCatalog.Entry effect = this.filtered.get(i);
                double gain = settings.killEffectVolume(effect.id());
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 29, 6, ThemeColors.surface(-1339677139));
                this.text(g, CustomFont.regular().ellipsize(HudStyle.tr(effect.ru(), effect.en()), 11.0f, (float)this.panelWidth * 0.47f - 24.0f), this.left + 23, y + 9, -1381908);
                int x = this.left + (int)((double)this.panelWidth * 0.51);
                int width = Math.max(20, this.left + this.panelWidth - 70 - x);
                UiDraw.rounded(g, x, y + 13, width, 3, 1, ThemeColors.surface(-11184801));
                UiDraw.rounded(g, x, y + 13, (int)Math.round((double)width * gain), 3, 1, ThemeColors.accent());
                UiDraw.rounded(g, x + (int)Math.round((double)width * gain) - 4, y + 10, 8, 9, 4, ThemeColors.text(-1381908));
                this.text(g, Math.round(gain * 100.0) + "%", this.left + this.panelWidth - 59, y + 9, -1381908);
                this.knobs.add(new Knob(effect.id(), x, y, width, Math.max(y, this.bodyTop), Math.min(y + 29, this.bodyBottom)));
            }
            if (this.filtered.isEmpty()) {
                this.text(g, HudStyle.tr("\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e", "No matches"), this.left + 20, this.bodyTop + 12, -7105633);
            }
        }
        finally {
            g.method_44380();
        }
        this.footer(g, HudStyle.tr("\u041d\u041b\u041e \u0434\u0435\u043b\u044f\u0442 \u0441\u0442\u0430\u0440\u044b\u0439 \u0437\u0432\u0443\u043a: \u0434\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442 \u043c\u0435\u043d\u044c\u0448\u0438\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u00b7 0% \u2014 \u0431\u0435\u0437 \u0437\u0432\u0443\u043a\u0430", "UFOs share a legacy sound: the lower level wins \u00b7 0% mutes"));
    }

    private void change(double x) {
        if (this.dragging == null) {
            return;
        }
        ClientConfig.LegendarySoundSettings p = BloodHelperClient.config().legendarySounds;
        p.killEffectsMode = "SELECTED";
        p.killEffects.put(this.dragging.id(), Math.clamp((x - (double)this.dragging.x()) / (double)this.dragging.width(), 0.0, 1.0));
        LegendarySoundVolume.settingsChanged();
    }

    @Override
    protected boolean panelMouseClicked(class_11909 e, boolean doubled) {
        if (this.search.mouseClicked(e, doubled)) {
            return true;
        }
        if (e.method_74245() == 0) {
            for (Knob knob : this.knobs) {
                if (!(e.comp_4798() >= (double)(knob.x() - 5)) || !(e.comp_4798() <= (double)(knob.x() + knob.width() + 5)) || !(e.comp_4799() >= (double)knob.top()) || !(e.comp_4799() < (double)knob.bottom())) continue;
                this.dragging = knob;
                this.change(e.comp_4798());
                return true;
            }
        }
        return super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 e, double dx, double dy) {
        if (this.dragging != null) {
            this.change(e.comp_4798());
            return true;
        }
        return this.search.mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 e) {
        if (this.dragging != null) {
            this.dragging = null;
            BloodHelperClient.saveConfig();
            return true;
        }
        return this.search.mouseReleased(e) || super.panelMouseReleased(e);
    }

    public void method_25432() {
        if (this.dragging != null) {
            this.dragging = null;
            BloodHelperClient.saveConfig();
        }
    }

    public boolean method_25404(class_11908 e) {
        return this.search.keyPressed(e) || super.method_25404(e);
    }

    public boolean method_25400(class_11905 e) {
        return this.search.charTyped(e) || super.method_25400(e);
    }

    private record Knob(String id, int x, int y, int width, int top, int bottom) {
    }
}

