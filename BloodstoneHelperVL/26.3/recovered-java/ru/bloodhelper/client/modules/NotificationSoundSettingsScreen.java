/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.NotificationSounds;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class NotificationSoundSettingsScreen
extends ModuleDetailScreen {
    public NotificationSoundSettingsScreen(Screen parent) {
        super(parent, HudStyle.tr("\u0417\u0432\u0443\u043a\u0438 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", "Notification sounds"));
    }

    @Override
    protected int preferredPanelWidth() {
        return 600;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0417\u0432\u0443\u043a\u0438 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", "Notification sounds"), mx, my);
        NotificationSoundSettings settings = BloodHelperClient.config().notificationSounds;
        this.button(g, this.left + 14, this.bodyTop, 116, settings.enabled ? HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Enabled") : HudStyle.tr("\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Disabled"), mx, my, () -> {
            settings.enabled = !settings.enabled;
            NotificationSoundSettingsScreen.save();
        });
        this.text(g, CustomFont.regular().ellipsize(HudStyle.tr("\u041e\u0431\u0449\u0430\u044f \u0433\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c \u0438\u0433\u0440\u044b \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f", "Game master volume stays unchanged"), 11.0f, this.panelWidth - 158), this.left + 143, this.bodyTop + 6, -7105633);
        this.bodyTop += 32;
        this.scrollbar(g, NotificationSoundSettings.Event.values().length * 68);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int index = 0;
            for (NotificationSoundSettings.Event event : NotificationSoundSettings.Event.values()) {
                int y;
                if ((y = this.bodyTop + index++ * 68 - this.scroll) + 64 < this.bodyTop || y >= this.bodyBottom) continue;
                NotificationSoundSettings.Choice choice = settings.choice(event);
                this.text(g, CustomFont.medium().ellipsize(NotificationSoundSettingsScreen.eventName(event), 11.0f, this.panelWidth - 30), this.left + 15, y + 3, -1381908);
                if (y + 22 < this.bodyTop || y + 43 > this.bodyBottom) continue;
                int usable = this.panelWidth - 28;
                int soundWidth = Math.max(30, (usable - 174) / 2);
                int muteWidth = Math.max(30, usable - soundWidth - 174);
                this.button(g, this.left + 14, y + 22, soundWidth, NotificationSoundSettingsScreen.soundName(choice.sound), mx, my, () -> {
                    choice.sound = NotificationSoundSettings.SOUNDS.get((NotificationSoundSettings.SOUNDS.indexOf(choice.sound) + 1) % NotificationSoundSettings.SOUNDS.size());
                    NotificationSoundSettingsScreen.save();
                });
                int x = this.left + 20 + soundWidth;
                this.button(g, x, y + 22, muteWidth, choice.silent ? HudStyle.tr("\u0411\u0435\u0437 \u0437\u0432\u0443\u043a\u0430", "Silent") : HudStyle.tr("\u0421\u043e \u0437\u0432\u0443\u043a\u043e\u043c", "Sound on"), mx, my, () -> {
                    choice.silent = !choice.silent;
                    NotificationSoundSettingsScreen.save();
                });
                this.button(g, x += muteWidth + 6, y + 22, 25, "\u2212", mx, my, () -> {
                    choice.volume = Math.max(0, choice.volume - 5);
                    NotificationSoundSettingsScreen.save();
                });
                this.text(g, choice.volume + "%", x + 31, y + 28, -7105633);
                this.button(g, x + 69, y + 22, 25, "+", mx, my, () -> {
                    choice.volume = Math.min(100, choice.volume + 5);
                    NotificationSoundSettingsScreen.save();
                });
                this.button(g, x + 100, y + 22, 56, HudStyle.tr("\u25b6 \u0422\u0435\u0441\u0442", "\u25b6 Test"), mx, my, () -> NotificationSounds.preview(this.minecraft, event));
            }
        }
        finally {
            g.disableScissor();
        }
        this.footer(g, HudStyle.tr("\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c 0% \u0438 \u00ab\u0411\u0435\u0437 \u0437\u0432\u0443\u043a\u0430\u00bb \u0432\u044b\u043a\u043b\u044e\u0447\u0430\u044e\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0435 \u0441\u043e\u0431\u044b\u0442\u0438\u0435", "0% volume and Silent mute the selected event"));
    }

    public static String eventName(NotificationSoundSettings.Event event) {
        return switch (event) {
            default -> throw new MatchException(null, null);
            case NotificationSoundSettings.Event.INVENTORY -> HudStyle.tr("\u041e\u0431\u0449\u0435\u0435 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435 / \u043f\u0440\u0438\u043c\u0435\u0440", "General notification / example");
            case NotificationSoundSettings.Event.ARMOR -> HudStyle.tr("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u0440\u043e\u043d\u0438", "Armor durability");
            case NotificationSoundSettings.Event.ITEM -> HudStyle.tr("\u041c\u0430\u043b\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "Low item count");
            case NotificationSoundSettings.Event.EFFECT -> HudStyle.tr("\u042d\u0444\u0444\u0435\u043a\u0442\u044b", "Potion effects");
            case NotificationSoundSettings.Event.REMINDER -> HudStyle.tr("\u041d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435 \u0441\u0442\u0430\u0444\u0444\u0430", "Staff reminder");
            case NotificationSoundSettings.Event.COMMAND_RESULT -> HudStyle.tr("\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442 \u043a\u043e\u043c\u0430\u043d\u0434\u044b", "Command result");
            case NotificationSoundSettings.Event.PLAYER_RETURN -> HudStyle.tr("\u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u0438\u0435 \u0438\u0433\u0440\u043e\u043a\u0430", "Player return");
            case NotificationSoundSettings.Event.CHAT_MENTION -> HudStyle.tr("\u0423\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442\u0435", "Chat mention");
            case NotificationSoundSettings.Event.CHAT_VIOLATION -> HudStyle.tr("\u0421\u043e\u0432\u043f\u0430\u0434\u0435\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442\u0435 \u0441\u0442\u0430\u0444\u0444\u0430", "Staff chat match");
            case NotificationSoundSettings.Event.COOLDOWN_READY -> HudStyle.tr("\u041f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430", "Cooldown ready");
            case NotificationSoundSettings.Event.STAFF_CHECK -> HudStyle.tr("\u0421\u043e\u0431\u044b\u0442\u0438\u0435 \u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0438", "Player check event");
        };
    }

    private static String soundName(String id) {
        int index = NotificationSoundSettings.SOUNDS.indexOf(id);
        return switch (index) {
            case 1 -> HudStyle.tr("\u041a\u043e\u0440\u043e\u0442\u043a\u0430\u044f \u043d\u043e\u0442\u0430", "Pling");
            case 2 -> HudStyle.tr("\u041a\u043e\u043b\u043e\u043a\u043e\u043b\u044c\u0447\u0438\u043a", "Bell");
            case 3 -> HudStyle.tr("\u041e\u043f\u044b\u0442", "Experience");
            case 4 -> HudStyle.tr("\u0410\u043c\u0435\u0442\u0438\u0441\u0442", "Amethyst");
            case 5 -> HudStyle.tr("\u041f\u0435\u0440\u0435\u0437\u0432\u043e\u043d", "Chime");
            case 6 -> HudStyle.tr("\u041d\u043e\u0432\u044b\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c", "Level up");
            case 7 -> HudStyle.tr("\u0410\u0440\u0444\u0430", "Harp");
            default -> HudStyle.tr("\u0429\u0435\u043b\u0447\u043e\u043a", "Click");
        };
    }

    private static void save() {
        BloodHelperClient.saveConfig();
    }
}

