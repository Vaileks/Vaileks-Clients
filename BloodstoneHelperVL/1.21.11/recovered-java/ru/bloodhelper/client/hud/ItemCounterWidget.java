/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_7923;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.InventorySnapshot;

public final class ItemCounterWidget {
    private static List<String> cachedSelection = List.of();
    private static List<Descriptor> cachedDescriptors = List.of();
    private static String cachedLanguage;
    private static Object cachedTranslations;
    private static final int[] PREVIEW_COUNTS;
    private static final int HEADER_HEIGHT = 21;

    private ItemCounterWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig c, boolean preview) {
        int contentWidth;
        int cellWidth;
        ClientConfig.ItemCounterSettings settings = c.itemCounter;
        InventorySnapshot.Snapshot snapshot = preview ? null : InventorySnapshot.capture();
        ArrayList<Entry> entries = new ArrayList<Entry>(8);
        for (Descriptor descriptor : ItemCounterWidget.descriptors(c)) {
            int count;
            if (entries.size() == 8) break;
            int n = count = preview ? PREVIEW_COUNTS[entries.size()] : snapshot.count(descriptor.id());
            if (settings.hideZero && count == 0) continue;
            class_1799 icon = preview ? descriptor.defaultIcon() : snapshot.icons().getOrDefault(descriptor.id(), descriptor.defaultIcon());
            entries.add(new Entry(descriptor.id(), icon, descriptor.name(), count));
        }
        if ("SEPARATE".equals(settings.layout)) {
            ItemCounterWidget.renderSeparate(g, c, entries);
            return;
        }
        if (entries.isEmpty() && !preview) {
            return;
        }
        boolean vertical = "VERTICAL".equals(settings.orientation);
        int n = cellWidth = settings.showNames ? 86 : 43;
        int rowHeight = vertical ? 25 : (settings.showNames ? 57 : 42);
        String title = HudStyle.tr(c, "\u0421\u0447\u0451\u0442\u0447\u0438\u043a \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "Item Counter");
        int n2 = vertical ? (settings.showNames ? 174 : 90) : (contentWidth = Math.max(1, entries.size()) * cellWidth);
        int width = c.hudTitle("itemCounter") ? Math.max(Math.max(113, HudStyle.strongWidth(title) + 32), contentWidth) : (entries.isEmpty() ? Math.max(113, contentWidth) : contentWidth);
        int headerHeight = c.hudTitle("itemCounter") ? 21 : 0;
        int height = headerHeight + Math.max(1, vertical ? entries.size() : 1) * rowHeight;
        HudRenderer.begin(g, "itemCounter", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (settings.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("itemCounter")) {
                HudStyle.divider(g, 6, 19, width - 12);
                UiIcons.draw(g, "modules", 5, 4, 12, -4802625);
                HudStyle.strong(g, title, 25, 3, -921101, settings.shadow);
            }
            if (entries.isEmpty()) {
                HudStyle.label(g, HudStyle.fitLabel(HudStyle.tr("\u0412\u044b\u0431\u0435\u0440\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", "Select items"), width - 12), 6, headerHeight + 6, -4802625, settings.shadow);
            }
            for (int i = 0; i < entries.size(); ++i) {
                if (vertical) {
                    int y = headerHeight + i * rowHeight;
                    if (c.hudTitle("itemCounter") && i > 0) {
                        HudStyle.divider(g, 7, y - 1, width - 14);
                    }
                    ItemCounterWidget.renderRow(g, c, (Entry)entries.get(i), y, width);
                    continue;
                }
                int x = i * width / entries.size();
                int actualWidth = (i + 1) * width / entries.size() - x;
                if (c.hudTitle("itemCounter") && i > 0) {
                    HudStyle.separator(g, x, headerHeight + 3, rowHeight - 8);
                }
                ItemCounterWidget.renderCell(g, c, (Entry)entries.get(i), x, headerHeight, actualWidth);
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static List<Descriptor> descriptors(ClientConfig config) {
        Object translations = UiLanguage.cacheToken();
        List<String> selection = config.itemCounter.items;
        if (translations == cachedTranslations && Objects.equals(config.language, cachedLanguage) && cachedSelection.equals(selection)) {
            return cachedDescriptors;
        }
        ArrayList<Descriptor> descriptors = new ArrayList<Descriptor>();
        for (String raw : new LinkedHashSet<String>(selection)) {
            class_1799 defaultIcon;
            class_2960 id = raw == null ? null : class_2960.method_12829((String)raw);
            if (id == null || !class_7923.field_41178.method_10250(id) || (defaultIcon = new class_1799((class_1935)class_7923.field_41178.method_63535(id))).method_7960()) continue;
            descriptors.add(new Descriptor(id.toString(), defaultIcon, UiLanguage.text(defaultIcon.method_7964())));
        }
        cachedSelection = new ArrayList<String>(selection);
        cachedLanguage = config.language;
        cachedTranslations = translations;
        cachedDescriptors = List.copyOf(descriptors);
        return cachedDescriptors;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderSeparate(class_332 g, ClientConfig c, List<Entry> entries) {
        ClientConfig.ItemCounterSettings settings = c.itemCounter;
        int width = settings.showNames ? 174 : 43;
        int height = settings.showNames ? 28 : 42;
        for (Entry entry : entries) {
            HudRenderer.begin(g, "itemCounter." + entry.id(), settings.partPosition(entry.id()), "LEFT", false, width, height);
            try {
                if (settings.background) {
                    HudStyle.panel(g, width, height, c);
                }
                if (settings.showNames) {
                    ItemCounterWidget.renderRow(g, c, entry, 2, width);
                    continue;
                }
                ItemCounterWidget.renderCell(g, c, entry, 0, 0, width);
            }
            finally {
                g.method_51448().popMatrix();
            }
        }
    }

    private static void renderRow(class_332 g, ClientConfig c, Entry item, int y, int width) {
        ClientConfig.ItemCounterSettings settings = c.itemCounter;
        g.method_51427(item.icon(), 5, y + 2);
        String count = HudStyle.fitStrong(Integer.toString(item.count()), width - 36);
        int countX = width - 7 - HudStyle.strongWidth(count);
        if (settings.showNames) {
            HudStyle.text(g, HudStyle.fit(item.name(), countX - 32), 27, y + 3, -4802625, settings.shadow);
        }
        HudStyle.strong(g, count, countX, y + 3, ItemCounterWidget.color(item, settings), settings.shadow);
    }

    private static void renderCell(class_332 g, ClientConfig c, Entry item, int x, int y, int width) {
        ClientConfig.ItemCounterSettings settings = c.itemCounter;
        g.method_51427(item.icon(), x + (width - 16) / 2, y + 3);
        if (settings.showNames) {
            String name = HudStyle.fit(item.name(), width - 12);
            HudStyle.text(g, name, x + (width - HudStyle.width(name)) / 2, y + 23, -4802625, settings.shadow);
        }
        String count = HudStyle.fitStrong(Integer.toString(item.count()), width - 6);
        HudStyle.strong(g, count, x + (width - HudStyle.strongWidth(count)) / 2, y + (settings.showNames ? 38 : 23), ItemCounterWidget.color(item, settings), settings.shadow);
    }

    private static int color(Entry entry, ClientConfig.ItemCounterSettings settings) {
        return entry.count() <= settings.lowCount ? -39568 : -921101;
    }

    static {
        PREVIEW_COUNTS = new int[]{2, 16, 24, 64, 6, 12, 8, 32};
    }

    private record Descriptor(String id, class_1799 defaultIcon, String name) {
    }

    private record Entry(String id, class_1799 icon, String name, int count) {
    }
}

