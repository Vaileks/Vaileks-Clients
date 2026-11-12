/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import java.util.ArrayList;
import java.util.List;
import ru.bloodhelper.client.gui.ModuleCategory;

public final class ModuleGroupLayout {
    public static final int HEADER_HEIGHT = 26;
    public static final int CARD_GAP = 8;
    public static final int GROUP_GAP = 16;

    private ModuleGroupLayout() {
    }

    public static <T> Layout<T> create(List<Entry<T>> entries, int top, int columnWidth, int columnGap) {
        ArrayList<Header> headers = new ArrayList<Header>();
        ArrayList positions = new ArrayList();
        int bottom = top;
        for (ModuleCategory category : ModuleCategory.values()) {
            List<Entry> group = entries.stream().filter(entry -> entry.category() == category).toList();
            if (group.isEmpty()) continue;
            int headerY = headers.isEmpty() ? top : bottom + 16;
            headers.add(new Header(category, headerY, group.size()));
            int[] columns = new int[]{headerY + 26, headerY + 26};
            for (int i = 0; i < group.size(); ++i) {
                Entry entry2 = group.get(i);
                int column = i % 2;
                positions.add(new Position(entry2.item(), column * (columnWidth + columnGap), columns[column]));
                int n = column;
                columns[n] = columns[n] + (Math.max(1, entry2.height()) + 8);
            }
            bottom = Math.max(columns[0], columns[1]) - 8;
        }
        return new Layout(List.copyOf(headers), List.copyOf(positions), bottom);
    }

    public record Header(ModuleCategory category, int y, int count) {
    }

    public record Entry<T>(ModuleCategory category, T item, int height) {
    }

    public record Position<T>(T item, int x, int y) {
    }

    public record Layout<T>(List<Header> headers, List<Position<T>> positions, int bottom) {
    }
}

