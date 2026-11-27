/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import java.util.ArrayList;
import java.util.List;

public final class HudSnap {
    private HudSnap() {
    }

    public static Result move(Box group, double dx, double dy, List<Box> neighbors, int width, int height, boolean snapping, int gap) {
        Candidate bestY;
        double space;
        double space2;
        double x = HudSnap.clamp(group.x + dx, 0.0, Math.max(0.0, (double)width - group.width));
        double y = HudSnap.clamp(group.y + dy, 0.0, Math.max(0.0, (double)height - group.height));
        if (!snapping) {
            return new Result(x - group.x, y - group.y, List.of());
        }
        ArrayList<Candidate> gx = new ArrayList<Candidate>();
        ArrayList<Candidate> gy = new ArrayList<Candidate>();
        HudSnap.screenCandidates(gx, width, group.width, height, true, gap);
        HudSnap.screenCandidates(gy, height, group.height, width, false, gap);
        for (Box b : neighbors) {
            HudSnap.align(gx, b.x, b.width, group.width, b.y, b.bottom(), y, y + group.height, true);
            HudSnap.align(gy, b.y, b.height, group.height, b.x, b.right(), x, x + group.width, false);
            if (HudSnap.overlap(y, y + group.height, b.y, b.bottom())) {
                gx.add(new Candidate(b.right() + (double)gap, new Guide(true, b.right() + (double)gap / 2.0, Math.min(y, b.y), Math.max(y + group.height, b.bottom()), gap)));
                gx.add(new Candidate(b.x - (double)gap - group.width, new Guide(true, b.x - (double)gap / 2.0, Math.min(y, b.y), Math.max(y + group.height, b.bottom()), gap)));
            }
            if (!HudSnap.overlap(x, x + group.width, b.x, b.right())) continue;
            gy.add(new Candidate(b.bottom() + (double)gap, new Guide(false, b.bottom() + (double)gap / 2.0, Math.min(x, b.x), Math.max(x + group.width, b.right()), gap)));
            gy.add(new Candidate(b.y - (double)gap - group.height, new Guide(false, b.y - (double)gap / 2.0, Math.min(x, b.x), Math.max(x + group.width, b.right()), gap)));
        }
        Box left = null;
        Box right = null;
        Box top = null;
        Box bottom = null;
        for (Box b : neighbors) {
            if (HudSnap.overlap(y, y + group.height, b.y, b.bottom())) {
                if (b.right() <= x + group.width / 2.0 && (left == null || b.right() > left.right())) {
                    left = b;
                }
                if (b.x >= x + group.width / 2.0 && (right == null || b.x < right.x)) {
                    right = b;
                }
            }
            if (!HudSnap.overlap(x, x + group.width, b.x, b.right())) continue;
            if (b.bottom() <= y + group.height / 2.0 && (top == null || b.bottom() > top.bottom())) {
                top = b;
            }
            if (!(b.y >= y + group.height / 2.0) || bottom != null && !(b.y < bottom.y)) continue;
            bottom = b;
        }
        if (left != null && right != null && (space2 = right.x - left.right() - group.width) >= 0.0) {
            gx.add(new Candidate(left.right() + space2 / 2.0, new Guide(true, left.right() + space2 / 4.0, y, y + group.height, space2 / 2.0)));
        }
        if (top != null && bottom != null && (space = bottom.y - top.bottom() - group.height) >= 0.0) {
            gy.add(new Candidate(top.bottom() + space / 2.0, new Guide(false, top.bottom() + space / 4.0, x, x + group.width, space / 2.0)));
        }
        ArrayList<Guide> guides = new ArrayList<Guide>(2);
        Candidate bestX = HudSnap.nearest(gx, x, (double)width - group.width);
        if (bestX != null) {
            x = bestX.origin;
            guides.add(bestX.guide);
        }
        if ((bestY = HudSnap.nearest(gy, y, (double)height - group.height)) != null) {
            y = bestY.origin;
            guides.add(bestY.guide);
        }
        return new Result(x - group.x, y - group.y, List.copyOf(guides));
    }

    private static void screenCandidates(List<Candidate> out, int size, double ownSize, int otherSize, boolean vertical, int gap) {
        for (double start : new double[]{0.0, gap, ((double)size - ownSize) / 2.0, (double)(size - gap) - ownSize, (double)size - ownSize}) {
            double at = start == ((double)size - ownSize) / 2.0 ? (double)size / 2.0 : (start >= (double)size / 2.0 ? start + ownSize : start);
            out.add(new Candidate(start, new Guide(vertical, at, 0.0, otherSize, -1.0)));
        }
    }

    private static void align(List<Candidate> out, double start, double size, double ownSize, double from, double to, double ownFrom, double ownTo, boolean vertical) {
        for (double point : new double[]{start, start + size / 2.0, start + size}) {
            for (double offset : new double[]{0.0, ownSize / 2.0, ownSize}) {
                out.add(new Candidate(point - offset, new Guide(vertical, point, Math.min(from, ownFrom) - 5.0, Math.max(to, ownTo) + 5.0, -1.0)));
            }
        }
    }

    private static Candidate nearest(List<Candidate> candidates, double value, double max) {
        Candidate result = null;
        double distance = 4.5;
        for (Candidate candidate : candidates) {
            double next = Math.abs(candidate.origin - value);
            if (!(candidate.origin >= 0.0) || !(candidate.origin <= Math.max(0.0, max)) || !(next < distance)) continue;
            result = candidate;
            distance = next;
        }
        return result;
    }

    private static boolean overlap(double a, double b, double c, double d) {
        return a < d && c < b;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Box(double x, double y, double width, double height) {
        public double right() {
            return this.x + this.width;
        }

        public double bottom() {
            return this.y + this.height;
        }
    }

    public record Result(double dx, double dy, List<Guide> guides) {
    }

    private record Candidate(double origin, Guide guide) {
    }

    public record Guide(boolean vertical, double coordinate, double from, double to, double gap) {
    }
}

