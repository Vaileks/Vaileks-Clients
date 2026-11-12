/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.extensions;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import pyz1k.extensions.BloodCooldownProfiles;
import pyz1k.extensions.BloodSwordCooldownTracker;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.extensions.SkinCatalog;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.AbstractHudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;

public final class BloodSwordHud
extends AbstractHudElement {
    public BloodSwordHud() {
        super("blood_sword_cooldown", "BloodSwordCooldown", 8.0f, 134.0f, 240.0f, 55.0f);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, int width, int height, float delta) {
        FeatureModules.BloodSwordCooldown module = FeatureModules.blood;
        if (module == null || ScreenshotMode.active()) {
            return;
        }
        List<BloodSwordCooldownTracker.Timer> timers = BloodSwordCooldownTracker.active();
        boolean editor = this.canInteractInEditor(Minecraft.getInstance());
        if (!(!timers.isEmpty() || editor || module.showIdle.isValue() && BloodSwordCooldownTracker.present())) {
            this.setBounds(0.0f, 0.0f);
            return;
        }
        BloodCooldownProfiles.Mode mode = BloodCooldownProfiles.mode(module.mode.getValue());
        int count = Math.min(timers.size(), 6);
        int body = Math.max(count, 1) * 26;
        int footer = mode.serverOnly() ? 12 : 24;
        float h = 24 + body + footer;
        float scale = ((Float)module.scale.getValue()).floatValue();
        this.setBounds(240.0f * scale, h * scale);
        Matrix3x2fStack matrix = ctx.pose();
        matrix.pushMatrix();
        matrix.translate(this.getX(), this.getY());
        matrix.scale(scale, scale);
        HudVisualStyle.drawPanel(ctx, 0.0f, 0.0f, 240.0f, h, 1.0f);
        RenderUtil.drawText(ctx, "BloodSword · " + mode.label(), 8.0f, 7.0f, ClientColors.HUD_TEXT, GFonts.MEDIUM, 8);
        if (count == 0) {
            RenderUtil.drawText(ctx, editor ? "Превью · серверные таймеры" : "Нет полученных активных таймеров", 8.0f, 30.0f, ClientColors.HUD_SECONDARY_TEXT, GFonts.MEDIUM, 7);
        }
        for (int i = 0; i < count; ++i) {
            BloodSwordCooldownTracker.Timer timer = timers.get(i);
            int y = 24 + i * 26;
            int iy = y + 2;
            SkinCatalog.drawOriginal(() -> ctx.item(timer.stack(), 7, iy));
            RenderUtil.drawText(ctx, timer.label(), 29.0f, (float)(y + 2), ClientColors.HUD_TEXT, GFonts.MEDIUM, 7);
            String time = BloodCooldownProfiles.time(timer.remaining(), module.precise.isValue());
            RenderUtil.drawText(ctx, time, 231.0f - RenderUtil.getTextWidth(time, GFonts.MEDIUM, 8), (float)(y + 2), timer.form().color, GFonts.MEDIUM, 8);
            RenderUtil.drawRoundRect(ctx, 29.0f, (float)(y + 15), 202.0f, 3.0f, 1.0f, ClientColors.SETTINGS_BACKGROUND);
            RenderUtil.drawRoundRect(ctx, 29.0f, (float)(y + 15), 202.0f * timer.progress(), 3.0f, 1.0f, timer.form().color);
        }
        int fy = 24 + body;
        String info = mode.serverOnly() ? "Только данные сервера" : "Пресет КД: булава " + BloodCooldownProfiles.time(mode.seconds("cooldown"), false) + " · гарпун " + BloodCooldownProfiles.time(mode.seconds("bow_cooldown"), false) + " · тяга " + BloodCooldownProfiles.time(mode.seconds("bow_pull_cooldown"), false);
        RenderUtil.drawText(ctx, info, 8.0f, (float)fy, ClientColors.HUD_SECONDARY_TEXT, GFonts.MEDIUM, 6);
        if (!mode.serverOnly()) {
            RenderUtil.drawText(ctx, "Форма: булава " + BloodCooldownProfiles.time(mode.seconds("mace_duration"), false) + " · гарпун " + BloodCooldownProfiles.time(mode.seconds("bow_duration"), false) + " · переход " + BloodCooldownProfiles.time(mode.seconds("transition_duration"), true), 8.0f, (float)(fy + 11), ClientColors.HUD_SECONDARY_TEXT, GFonts.MEDIUM, 6);
        }
        matrix.popMatrix();
    }
}

