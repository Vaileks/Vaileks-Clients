/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.inventory;

public final class TooltipPlacement {
    private TooltipPlacement() {
    }

    public static Point avoid(int x, int y, int width, int height, int screenWidth, int screenHeight, int mouseX, int mouseY, int hintX, int hintY, int hintWidth, int hintHeight) {
        int maxX = Math.max(12, screenWidth - width - 12);
        int maxY = Math.max(12, screenHeight - height - 12);
        if (TooltipPlacement.overlap((x = Math.clamp((long)x, 12, maxX)) - 12, (y = Math.clamp((long)y, 12, maxY)) - 12, width + 24, height + 24, hintX, hintY, hintWidth, hintHeight) == 0L) {
            return new Point(x, y);
        }
        int[] xs = new int[]{x, hintX - width - 16, hintX + hintWidth + 16, 12, maxX};
        int[] ys = new int[]{y, hintY - height - 16, hintY + hintHeight + 16, 12, maxY};
        Point best = new Point(x, y);
        long bestOverlap = Long.MAX_VALUE;
        long bestScore = Long.MAX_VALUE;
        for (int cx : xs) {
            for (int cy : ys) {
                int px = Math.clamp((long)cx, 12, maxX);
                int py = Math.clamp((long)cy, 12, maxY);
                long area = TooltipPlacement.overlap(px - 12, py - 12, width + 24, height + 24, hintX, hintY, hintWidth, hintHeight);
                long cursor = TooltipPlacement.overlap(px - 12, py - 12, width + 24, height + 24, mouseX - 2, mouseY - 2, 5, 5);
                long distance = (long)(px - x) * (long)(px - x) + (long)(py - y) * (long)(py - y);
                long score = distance + (cursor == 0L ? 0L : 576L);
                if (area >= bestOverlap && (area != bestOverlap || score >= bestScore)) continue;
                best = new Point(px, py);
                bestOverlap = area;
                bestScore = score;
            }
        }
        return best;
    }

    private static long overlap(int x, int y, int w, int h, int bx, int by, int bw, int bh) {
        return (long)Math.max(0, Math.min(x + w, bx + bw) - Math.max(x, bx)) * (long)Math.max(0, Math.min(y + h, by + bh) - Math.max(y, by));
    }

    public record Point(int x, int y) {
    }
}

