/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.community;

import java.util.List;
import java.util.Objects;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.community.ChangelogLayout;
import ru.bloodhelper.client.community.CommunityFeed;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.hud.NotificationInbox;

public final class CommunityPanels {
    private static CommunityFeed.Panel cachedPanel;
    private static int cachedWidth;
    private static int offset;
    private static List<String> lines;
    private static ChangelogLayout.Box box;

    private CommunityPanels() {
    }

    private static void prepare(CommunityFeed.Panel panel, int width, int height) {
        ChangelogLayout.Box available = ChangelogLayout.box(width, height, Integer.MAX_VALUE);
        if (!Objects.equals(panel, cachedPanel) || cachedWidth != available.width()) {
            cachedPanel = panel;
            cachedWidth = available.width();
            offset = 0;
            lines = available.width() == 0 ? List.of() : CustomFont.regular().wrap(panel.body().replaceAll("\n[ \t]*\n+", "\n"), 7.5f, available.width() - 20);
        }
        box = ChangelogLayout.box(width, height, lines.size());
        offset = Math.clamp((long)offset, 0, Math.max(0, lines.size() - box.rows()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void title(class_332 g, int width, int height) {
        CommunityFeed.Panel panel = CommunityFeed.mainMenu();
        if (panel == null) {
            box = new ChangelogLayout.Box(10, 10, 0, 0, 0);
            return;
        }
        CommunityPanels.prepare(panel, width, height);
        if (box.width() == 0) {
            return;
        }
        LiquidGlass.surfaceMenu(g, box.x(), box.y(), box.width(), box.height(), 6, -434101717, BloodHelperClient.config());
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(panel.title(), 9.0f, box.width() - 18), box.x() + 8, box.y() + 7, 9.0f, -921101);
        g.method_44379(box.x() + 6, box.y() + 25, box.x() + box.width() - 6, box.y() + box.height() - 6);
        try {
            for (int row = 0; row < box.rows() && offset + row < lines.size(); ++row) {
                CustomFont.regular().draw(g, lines.get(offset + row), box.x() + 8, box.y() + 26 + row * 10, 7.5f, -2960421);
            }
        }
        finally {
            g.method_44380();
        }
        if (lines.size() > box.rows()) {
            int track = box.height() - 32;
            int thumb = Math.max(9, track * box.rows() / lines.size());
            int y = box.y() + 26 + (track - thumb) * offset / Math.max(1, lines.size() - box.rows());
            g.method_25294(box.x() + box.width() - 4, y, box.x() + box.width() - 3, y + thumb, 1719633041);
        }
    }

    public static boolean scroll(double x, double y, double amount, int width, int height) {
        CommunityFeed.Panel panel = CommunityFeed.mainMenu();
        if (panel == null || !Double.isFinite(amount) || amount == 0.0) {
            return false;
        }
        CommunityPanels.prepare(panel, width, height);
        if (!box.contains(x, y) || lines.size() <= box.rows()) {
            return false;
        }
        offset = Math.clamp((long)(offset + (amount < 0.0 ? 3 : -3)), 0, lines.size() - box.rows());
        return true;
    }

    public static void menu(class_332 g, int width) {
        if (NotificationInbox.snoozed()) {
            return;
        }
        List<CommunityFeed.Message> messages = CommunityFeed.messages();
        if (messages.isEmpty()) {
            return;
        }
        CommunityFeed.Message message = messages.getLast();
        LiquidGlass.surfaceMenu(g, 0, -69, width, 59, 10, -316661205, BloodHelperClient.config());
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(message.title(), 12.0f, width - 32), 16.0f, -59.0f, 12.0f, -921101);
        List<String> lines = CustomFont.regular().wrap(message.body(), 11.0f, width - 32);
        for (int i = 0; i < Math.min(2, lines.size()); ++i) {
            CustomFont.regular().draw(g, CustomFont.regular().ellipsize(lines.get(i), 11.0f, width - 32), 16.0f, -40 + i * 13, 11.0f, -2960421);
        }
    }

    public static boolean menuVisible() {
        return !NotificationInbox.snoozed() && !CommunityFeed.messages().isEmpty();
    }

    static {
        lines = List.of();
        box = new ChangelogLayout.Box(10, 10, 0, 0, 0);
    }
}

