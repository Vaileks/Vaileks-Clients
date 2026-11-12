/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.inventory;

public final class InventoryOverlayPlacement {
    private InventoryOverlayPlacement() {
    }

    public static Point avoidEffects(int x, int y, int w, int h, int screenW, int screenH, int left, int top, int bodyW, int bodyH, int count) {
        int effectH;
        int maxX = Math.max(2, screenW - w - 2);
        int maxY = Math.max(2, screenH - h - 2);
        x = Math.clamp((long)x, 2, maxX);
        y = Math.clamp((long)y, 2, maxY);
        int effectX = left + bodyW + 2;
        int room = screenW - effectX;
        if (count <= 0 || room < 32) {
            return new Point(x, y);
        }
        Rect wanted = new Rect(x, y, w, h);
        Rect effects = new Rect(effectX - 3, top - 3, (room >= 120 ? room - 7 : 32) + 6, (effectH = count <= 5 ? count * 33 : 164) + 6);
        if (wanted.overlap(effects) == 0L) {
            return new Point(x, y);
        }
        Rect body = new Rect(left - 3, top - 30, bodyW + 6, bodyH + 60);
        int[] xs = new int[]{x, effects.x - w - 5, effects.x + effects.w + 5, left - w - 6, left + bodyW + 6, 2, maxX};
        int[] ys = new int[]{y, effects.y + effects.h + 5, effects.y - h - 5, top + bodyH + 32, top - h - 32, 2, maxY};
        Point best = new Point(x, y);
        long bestEffects = Long.MAX_VALUE;
        long bestBody = Long.MAX_VALUE;
        long bestDistance = Long.MAX_VALUE;
        for (int candidateY : ys) {
            for (int candidateX : xs) {
                int px = Math.clamp((long)candidateX, 2, maxX);
                int py = Math.clamp((long)candidateY, 2, maxY);
                Rect candidate = new Rect(px, py, w, h);
                long effectOverlap = candidate.overlap(effects);
                long bodyOverlap = candidate.overlap(body);
                long distance = (long)(px - x) * (long)(px - x) + (long)(py - y) * (long)(py - y);
                if (effectOverlap >= bestEffects && (effectOverlap != bestEffects || bodyOverlap >= bestBody && (bodyOverlap != bestBody || distance >= bestDistance))) continue;
                best = new Point(px, py);
                bestEffects = effectOverlap;
                bestBody = bodyOverlap;
                bestDistance = distance;
            }
        }
        return best;
    }

    public record Point(int x, int y) {
    }

    private record Rect(int x, int y, int w, int h) {
        long overlap(Rect other) {
            return (long)Math.max(0, Math.min(this.x + this.w, other.x + other.w) - Math.max(this.x, other.x)) * (long)Math.max(0, Math.min(this.y + this.h, other.y + other.h) - Math.max(this.y, other.y));
        }
    }
}

