/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class WaypointLabelLayout {
    private WaypointLabelLayout() {
    }

    public static Rect place(double anchorX, double anchorY, int w, int h, int sw, int sh, List<Rect> occupied) {
        if (!Double.isFinite(anchorX) || !Double.isFinite(anchorY) || w <= 0 || h <= 0 || w > sw - 8 || h > sh - 8) {
            return null;
        }
        int idealX = (int)Math.round(anchorX - (double)w / 2.0);
        int idealY = (int)Math.round(anchorY - (double)h - 9.0);
        ArrayList<Integer> xs = new ArrayList<Integer>();
        ArrayList<Integer> ys = new ArrayList<Integer>();
        xs.add(idealX);
        ys.add(idealY);
        ys.add((int)Math.round(anchorY + 9.0));
        for (Rect r : occupied) {
            xs.add(r.x - w - 4);
            xs.add(r.x + r.width + 4);
            ys.add(r.y - h - 4);
            ys.add(r.y + r.height + 4);
        }
        Rect best = null;
        double score = Double.POSITIVE_INFINITY;
        Iterator iterator = xs.iterator();
        while (iterator.hasNext()) {
            int x = (Integer)iterator.next();
            Iterator iterator2 = ys.iterator();
            while (iterator2.hasNext()) {
                double dy;
                double dx;
                double cost;
                int y = (Integer)iterator2.next();
                Rect candidate = new Rect(Math.clamp((long)x, 4, sw - w - 4), Math.clamp((long)y, 4, sh - h - 4), w, h);
                if (occupied.stream().anyMatch(candidate::overlaps) || !((cost = (dx = (double)(candidate.x - idealX)) * dx + (dy = (double)(candidate.y - idealY)) * dy) < score)) continue;
                score = cost;
                best = candidate;
            }
        }
        return best;
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean overlaps(Rect other) {
            return this.x < other.x + other.width + 3 && this.x + this.width + 3 > other.x && this.y < other.y + other.height + 3 && this.y + this.height + 3 > other.y;
        }
    }
}

