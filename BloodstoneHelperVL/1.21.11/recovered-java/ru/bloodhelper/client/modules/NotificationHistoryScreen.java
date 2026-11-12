/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class NotificationHistoryScreen
extends ModuleDetailScreen {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault());

    public NotificationHistoryScreen(class_437 parent) {
        super(parent, "Notifications");
        NotificationInbox.markRead();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", "Notification history"), mx, my);
        this.button(g, this.left + 14, this.bodyTop, 135, HudStyle.tr("\u0422\u0438\u0448\u0438\u043d\u0430 \u043d\u0430 15 \u043c\u0438\u043d", "Snooze 15 min"), mx, my, () -> NotificationInbox.snooze(15));
        this.button(g, this.left + 157, this.bodyTop, 135, HudStyle.tr("\u0422\u0438\u0448\u0438\u043d\u0430 \u043d\u0430 \u0447\u0430\u0441", "Snooze 1 hour"), mx, my, () -> NotificationInbox.snooze(60));
        this.button(g, this.left + 300, this.bodyTop, 135, HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0441\u043d\u043e\u0432\u0430", "Resume alerts"), mx, my, () -> NotificationInbox.snooze(0));
        this.bodyTop += 30;
        this.text(g, (String)(NotificationInbox.snoozed() ? HudStyle.tr("\u0411\u0435\u0437 \u0437\u0432\u0443\u043a\u0430 \u0438 \u043f\u043b\u0430\u0448\u0435\u043a \u0434\u043e ", "Sound and cards paused until ") + TIME.format(Instant.ofEpochMilli(NotificationInbox.snoozedUntil())) : HudStyle.tr("\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u044b", "Notifications enabled")), this.left + 14, this.bodyTop, -7105633);
        this.bodyTop += 24;
        List<NotificationInbox.Entry> entries = NotificationInbox.entries();
        int total = 0;
        for (NotificationInbox.Entry e : entries) {
            total += 42 + CustomFont.regular().wrap(e.body(), 11.0f, this.panelWidth - 42).size() * 14;
        }
        this.scrollbar(g, total);
        int y = this.bodyTop - this.scroll;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            for (NotificationInbox.Entry e : entries) {
                List<String> lines = CustomFont.regular().wrap(e.body(), 11.0f, this.panelWidth - 42);
                int height = 42 + lines.size() * 14;
                if (y + height >= this.bodyTop && y < this.bodyBottom) {
                    this.text(g, TIME.format(Instant.ofEpochMilli(e.time())) + " \u00b7 " + e.source(), this.left + 14, y, -7105633);
                    this.text(g, CustomFont.regular().ellipsize(e.title(), 11.0f, this.panelWidth - 36), this.left + 14, y + 16, -1381908);
                    int lineY = y + 32;
                    for (String line : lines) {
                        this.text(g, line, this.left + 14, lineY, -1381908);
                        lineY += 14;
                    }
                }
                if ((y += height) < this.bodyBottom) continue;
                break;
            }
        }
        finally {
            g.method_44380();
        }
        if (entries.isEmpty()) {
            this.text(g, HudStyle.tr("\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", "No notifications yet"), this.left + 14, this.bodyTop + 10, -7105633);
        }
        this.footer(g, HudStyle.tr("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0435 200 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f \u043d\u0430 \u044d\u0442\u043e\u043c \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u0435", "Last 200 notifications are stored on this device"));
    }
}

