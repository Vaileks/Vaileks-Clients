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
package ru.bloodhelper.client.discord;

import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.DiscordPresenceSettings;
import ru.bloodhelper.client.discord.DiscordPresenceModule;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class DiscordPresenceScreen
extends ModuleDetailScreen {
    private final GuiTextField customText = DiscordPresenceScreen.field(128);
    private final List<GuiTextField> fields = List.of(this.customText);

    public DiscordPresenceScreen(class_437 parent) {
        super(parent, "Discord Rich Presence");
        DiscordPresenceSettings settings = ConfigStore.get().discordPresence;
        this.customText.setValue(settings.customText);
    }

    private static GuiTextField field(int limit) {
        GuiTextField result = new GuiTextField(0, 0, 100, 24, "");
        result.setMaxLength(limit);
        result.setTextSize(11.0f);
        return result;
    }

    private boolean privateMode() {
        return BloodHelperClient.config().streamer.enabled;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, "Discord Rich Presence", mx, my);
        if (this.privateMode()) {
            this.fields.forEach(field -> field.setFocused(false));
            int y = this.bodyTop + 10;
            for (String line : DiscordPresenceScreen.wrap(HudStyle.tr("Streamer Mode: \u043f\u0440\u0438\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u0435 \u0438 \u0442\u0435\u043a\u0441\u0442\u044b \u0441\u043a\u0440\u044b\u0442\u044b. \u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c \u0441\u0442\u0440\u0438\u043c\u0435\u0440\u0430 \u0434\u043b\u044f \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438.", "Streamer Mode: presence and texts are hidden. Turn it off to edit these settings."), this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, y, -7105633);
                y += 15;
            }
            this.maximumScroll = 0;
            this.footer(g, DiscordPresenceModule.statusText());
            return;
        }
        String explanation = HudStyle.tr("\u0412 Discord \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u044e\u0442\u0441\u044f \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 BloodHelper \u0438 \u0433\u043e\u043b\u043e\u0432\u0430 \u0438\u0433\u0440\u043e\u043a\u0430. \u041f\u0435\u0440\u0432\u0430\u044f \u0441\u0442\u0440\u043e\u043a\u0430 \u2014 \u0441\u0435\u0440\u0432\u0435\u0440 \u0438\u043b\u0438 \u0441\u0432\u043e\u0439 \u0442\u0435\u043a\u0441\u0442; \u0432\u0442\u043e\u0440\u0430\u044f \u2014 \u043d\u0438\u043a, UID, \u0432\u0435\u0440\u0441\u0438\u044f \u0438 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u0442\u0440\u0435\u043a. \u0412\u0440\u0435\u043c\u044f \u0438\u0433\u0440\u044b \u0441\u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044f \u0441 \u0432\u0445\u043e\u0434\u0430 \u0432 \u043c\u0438\u0440. \u041d\u0438\u043a \u0432\u043c\u0435\u0441\u0442\u0435 \u0441 UID, \u043c\u0443\u0437\u044b\u043a\u0443 \u0438 \u0442\u0430\u0439\u043c\u0435\u0440 \u043c\u043e\u0436\u043d\u043e \u0441\u043a\u0440\u044b\u0442\u044c \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u043c\u043e\u0434\u0443\u043b\u044f. \u041e\u0442\u043a\u0440\u043e\u0439\u0442\u0435 Discord \u043d\u0430 \u043a\u043e\u043c\u043f\u044c\u044e\u0442\u0435\u0440\u0435. \u041a\u0430\u0440\u0442\u043e\u0447\u043a\u0430 \u043e\u0441\u0442\u0430\u0451\u0442\u0441\u044f \u0438 \u0432 \u0433\u043b\u0430\u0432\u043d\u043e\u043c \u043c\u0435\u043d\u044e. Streamer Mode \u0441\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0432\u0441\u044e \u043a\u0430\u0440\u0442\u043e\u0447\u043a\u0443.", "Discord shows the BloodHelper name and player head. The first line shows your server or custom text; the second shows your nickname, UID, version and optional song. Session time starts when you join a world. Nickname together with UID, music and the timer can be hidden in the module settings. Open desktop Discord. Presence remains in the main menu. Streamer Mode hides the entire card.");
        List<String> help = DiscordPresenceScreen.wrap(explanation, this.panelWidth - 40);
        int contentHeight = 112 + help.size() * 15;
        this.maximumScroll = Math.max(0, contentHeight - (this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        int y = this.bodyTop - this.scroll;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            this.input(g, this.customText, HudStyle.tr("\u0421\u0432\u043e\u0439 \u0442\u0435\u043a\u0441\u0442", "Custom text"), y + 4, mx, my, delta);
            int textY = y + 60;
            for (String line : help) {
                this.text(g, line, this.left + 20, textY, -7105633);
                textY += 15;
            }
            int buttonY = textY + 10;
            if (buttonY >= this.bodyTop && buttonY + 21 <= this.bodyBottom) {
                this.button(g, this.left + 20, buttonY, Math.min(160, this.panelWidth - 40), HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Save"), mx, my, this::save);
            }
        }
        finally {
            g.method_44380();
        }
        this.scrollbar(g, contentHeight);
        this.footer(g, HudStyle.tr("Enter \u2014 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u00b7 Esc \u2014 \u043e\u0442\u043c\u0435\u043d\u0438\u0442\u044c \u00b7 ", "Enter: save \u00b7 Esc: cancel \u00b7 ") + DiscordPresenceModule.statusText());
    }

    private void input(class_332 g, GuiTextField field, String label, int y, int mx, int my, float delta) {
        this.text(g, CustomFont.regular().ellipsize(label, 11.0f, this.panelWidth - 40), this.left + 20, y, -7105633);
        UiDraw.border(g, this.left + 20, y + 16, this.panelWidth - 40, 27, 5, ThemeColors.surface(-15395556), field.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-7829352));
        field.setX(this.left + 27);
        field.setY(y + 17);
        field.setWidth(Math.max(1, this.panelWidth - 54));
        field.render(g, mx, my, delta);
    }

    private void save() {
        if (this.privateMode()) {
            return;
        }
        DiscordPresenceSettings settings = ConfigStore.get().discordPresence;
        settings.customText = this.customText.getValue();
        settings.sanitize();
        BloodHelperClient.saveConfig();
        this.method_25419();
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        this.fields.forEach(field -> field.setFocused(false));
        if (!this.privateMode() && event.comp_4799() >= (double)this.bodyTop && event.comp_4799() < (double)this.bodyBottom) {
            for (GuiTextField field2 : this.fields) {
                if (!field2.mouseClicked(event, doubled)) continue;
                return true;
            }
        }
        return super.panelMouseClicked(event, doubled);
    }

    public boolean method_25404(class_11908 event) {
        if (this.privateMode()) {
            return super.method_25404(event);
        }
        if (event.comp_4795() == 257 || event.comp_4795() == 335) {
            this.save();
            return true;
        }
        if (event.comp_4795() == 258) {
            int current = -1;
            for (int i = 0; i < this.fields.size(); ++i) {
                if (!this.fields.get(i).isFocused()) continue;
                current = i;
            }
            this.fields.forEach(field -> field.setFocused(false));
            GuiTextField next = this.fields.get(Math.floorMod(current + (event.method_74239() ? -1 : 1), this.fields.size()));
            next.setFocused(true);
            if (next.getY() < this.bodyTop || next.getY() + 24 > this.bodyBottom) {
                this.scroll = 0;
            }
            return true;
        }
        for (GuiTextField field2 : this.fields) {
            if (!field2.keyPressed(event)) continue;
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        if (this.privateMode()) {
            return false;
        }
        for (GuiTextField field : this.fields) {
            if (!field.charTyped(event)) continue;
            return true;
        }
        return super.method_25400(event);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        if (!this.privateMode()) {
            for (GuiTextField field : this.fields) {
                if (!field.mouseDragged(event, dx, dy)) continue;
                return true;
            }
        }
        return super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 event) {
        for (GuiTextField field : this.fields) {
            if (!field.mouseReleased(event)) continue;
            return true;
        }
        return super.panelMouseReleased(event);
    }
}

