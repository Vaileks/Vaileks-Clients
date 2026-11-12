/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import ru.bloodhelper.client.hud.WatermarkText;

public final class WatermarkLayout {
    public static final int HEIGHT = 26;
    public static final int GAP = 6;
    public static final int TEXT_X = 28;
    public static final int RIGHT_PAD = 10;
    public static final int GROUP_PAD = 4;
    public static final int GROUP_ROW_GAP = 4;

    private WatermarkLayout() {
    }

    public static Layout create(List<WatermarkText.Part> parts, int maxWidth, ToIntFunction<String> measure) {
        return WatermarkLayout.create(parts, maxWidth, false, measure);
    }

    public static Layout create(List<WatermarkText.Part> parts, int maxWidth, boolean separate, ToIntFunction<String> measure) {
        if (parts.isEmpty()) {
            return new Layout(List.of(), 0, 0, separate);
        }
        int padding = separate ? 0 : 4;
        int gap = separate ? 6 : 0;
        int rowGap = separate ? 6 : 4;
        int limit = Math.max(38 + measure.applyAsInt("\u2026"), maxWidth - padding * 2);
        ArrayList<Cell> cells = new ArrayList<Cell>();
        int x = 0;
        int y = 0;
        int width = 0;
        for (WatermarkText.Part part : parts) {
            int cap = Math.min(limit, switch (part.kind()) {
                case WatermarkText.Kind.BRAND -> 150;
                case WatermarkText.Kind.SERVER -> 172;
                case WatermarkText.Kind.COORDINATES -> 300;
                default -> 158;
            });
            String text = WatermarkLayout.fit(part.text(), cap - 28 - 10, measure);
            String reserve = switch (part.kind()) {
                case WatermarkText.Kind.FPS -> "999 FPS";
                case WatermarkText.Kind.PING -> "999 Ping";
                case WatermarkText.Kind.CLOCK -> "00:00";
                default -> "";
            };
            int cellWidth = Math.min(cap, 38 + Math.max(measure.applyAsInt(text), measure.applyAsInt(reserve)));
            if (x > 0 && x + cellWidth > limit) {
                x = 0;
                y += 26 + rowGap;
            }
            cells.add(new Cell(part.kind(), text, x + padding, y + padding, cellWidth));
            width = Math.max(width, x + cellWidth);
            x += cellWidth + gap;
        }
        return new Layout(List.copyOf(cells), width + padding * 2, y + 26 + padding * 2, separate);
    }

    private static String fit(String text, int width, ToIntFunction<String> measure) {
        if (measure.applyAsInt(text) <= width) {
            return text;
        }
        int end = text.length();
        while (end > 0) {
            String shortened = text.substring(0, end = text.offsetByCodePoints(end, -1)) + "\u2026";
            if (measure.applyAsInt(shortened) > width) continue;
            return shortened;
        }
        return "\u2026";
    }

    public record Layout(List<Cell> cells, int width, int height, boolean separate) {
    }

    public record Cell(WatermarkText.Kind kind, String text, int x, int y, int width) {
    }
}

