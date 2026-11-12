/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package ru.bloodhelper.client.access;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.access.ClientControlService;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.StreamerSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.policy.ClientControlPolicy;

public final class ControlStatusScreen
extends ModuleDetailScreen {
    public ControlStatusScreen() {
        super(null, HudStyle.tr("\u0421\u0442\u0430\u0442\u0443\u0441 BloodHelper", "BloodHelper status"));
    }

    @Override
    protected int preferredPanelWidth() {
        return 570;
    }

    @Override
    protected int preferredPanelHeight() {
        return 300;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderPanel(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        this.frame(graphics, HudStyle.tr("\u0421\u0442\u0430\u0442\u0443\u0441 BloodHelper", "BloodHelper status"), mx, my);
        int x = this.left + 18;
        int y = this.bodyTop + 8;
        int w = this.panelWidth - 36;
        StreamerSettings privacy = ConfigStore.get().streamer;
        String uid = privacy.enabled && privacy.hideNickname ? "\u2014" : StaffAccess.uid();
        CustomFont.medium().draw(graphics, "UID: " + (uid.isEmpty() ? "\u2026" : uid), x, y, 16.0f, -1381908);
        y += 30;
        for (String line : ControlStatusScreen.wrap(ClientControlPolicy.status(!"en".equals(BloodHelperClient.config().language)), w)) {
            this.text(graphics, line, x, y, -7105633);
            y += 17;
        }
        y += 10;
        for (String line : ControlStatusScreen.wrap(HudStyle.tr("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u044b. \u0414\u043e\u0441\u0442\u0443\u043f \u043f\u0440\u043e\u0432\u0435\u0440\u044f\u0435\u0442\u0441\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438; \u043f\u043e\u0441\u043b\u0435 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0438\u044f \u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u0441\u043d\u043e\u0432\u0430 \u0441\u0442\u0430\u043d\u0443\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b.", "Your settings are preserved. Access is checked automatically; features return after the owner restores access."), w)) {
            this.text(graphics, line, x, y, -7105633);
            y += 17;
        }
        int half = (w - 8) / 2;
        this.button(graphics, x, y += 18, half, HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u0441\u0435\u0439\u0447\u0430\u0441", "Check now"), mx, my, ClientControlService::retry);
        this.button(graphics, x + half + 8, y, half, HudStyle.tr("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c UID", "Copy UID"), mx, my, () -> {
            if (!StaffAccess.uid().isEmpty()) {
                this.minecraft.keyboardHandler.setClipboard(StaffAccess.uid());
            }
        });
        this.footer(graphics, HudStyle.tr("Minecraft \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u0435\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u00b7 Esc \u2014 \u0437\u0430\u043a\u0440\u044b\u0442\u044c", "Minecraft keeps running \u00b7 Esc to close"));
    }
}

