/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Base64;
import java.util.List;
import ru.bloodhelper.client.config.CrosshairSettings;

public final class CrosshairPattern {
    public static final List<String> PRESETS = List.of("PLUS", "DOT", "RING", "RING_DOT", "SQUARE", "DIAMOND", "X", "T", "BRACKETS", "CROSS_DOT", "CHEVRON", "STAR", "HEART", "SMILE");

    private CrosshairPattern() {
    }

    public static Mask create(CrosshairSettings s) {
        int n = s.mode.equals("CUSTOM") ? s.gridSize : 65;
        boolean[] bits = new boolean[n * n];
        if (s.mode.equals("CUSTOM")) {
            for (int y = 0; y < n; ++y) {
                for (int x = 0; x < n; ++x) {
                    bits[y * n + x] = (s.pixels[y] & 1 << x) != 0;
                }
            }
            return new Mask(n, bits);
        }
        String preset = s.mode.equals("SIMPLE") ? "PLUS" : s.preset;
        int center = n / 2;
        double radius = Math.max(2, s.size);
        double half = (double)s.thickness / 2.0;
        for (int y = 0; y < n; ++y) {
            for (int x = 0; x < n; ++x) {
                int dx = x - center;
                int dy = y - center;
                int ax = Math.abs(dx);
                int ay = Math.abs(dy);
                boolean horizontal = dy >= -(s.thickness / 2) && dy < (s.thickness + 1) / 2;
                boolean vertical = dx >= -(s.thickness / 2) && dx < (s.thickness + 1) / 2;
                boolean arms = horizontal && ax > s.gap && ax <= s.gap + s.size || vertical && ay > s.gap && ay <= s.gap + s.size;
                boolean centerDot = dx >= -(s.dotSize / 2) && dx < (s.dotSize + 1) / 2 && dy >= -(s.dotSize / 2) && dy < (s.dotSize + 1) / 2;
                double distance = Math.hypot(dx, dy);
                boolean ring = Math.abs(distance - radius) < Math.max(0.65, half);
                boolean pixel = switch (preset) {
                    case "DOT" -> centerDot;
                    case "RING" -> ring;
                    case "RING_DOT" -> {
                        if (ring || centerDot) {
                            yield true;
                        }
                        yield false;
                    }
                    case "SQUARE" -> {
                        if ((double)Math.max(ax, ay) <= radius && (double)Math.max(ax, ay) > radius - (double)s.thickness) {
                            yield true;
                        }
                        yield false;
                    }
                    case "DIAMOND" -> {
                        if ((double)(ax + ay) <= radius && (double)(ax + ay) > radius - (double)s.thickness) {
                            yield true;
                        }
                        yield false;
                    }
                    case "X" -> {
                        if ((double)ax <= radius && (double)ay <= radius && (double)Math.abs(ax - ay) < Math.max(1.0, half) && Math.max(ax, ay) > s.gap) {
                            yield true;
                        }
                        yield false;
                    }
                    case "T" -> {
                        if (arms && (dy >= 0 || horizontal)) {
                            yield true;
                        }
                        yield false;
                    }
                    case "BRACKETS" -> {
                        if ((double)ax <= radius && (double)ay <= radius && ((double)ax > radius - (double)s.thickness || (double)ay > radius - (double)s.thickness && (double)ax > radius / 2.0)) {
                            yield true;
                        }
                        yield false;
                    }
                    case "CROSS_DOT" -> {
                        if (arms || centerDot) {
                            yield true;
                        }
                        yield false;
                    }
                    case "CHEVRON" -> {
                        if ((double)ay <= radius && dy <= 0 && (double)Math.abs(ax + dy) < Math.max(1.0, half)) {
                            yield true;
                        }
                        yield false;
                    }
                    case "STAR" -> {
                        if ((horizontal || vertical || (double)Math.abs(ax - ay) < Math.max(1.0, half)) && distance <= radius) {
                            yield true;
                        }
                        yield false;
                    }
                    case "HEART" -> CrosshairPattern.heart((double)dx / radius, (double)dy / radius);
                    case "SMILE" -> {
                        if (ring || dy == -((int)(radius / 3.0)) && ax == (int)(radius / 3.0) || dy > 0 && distance < radius * 0.7 && distance > radius * 0.45) {
                            yield true;
                        }
                        yield false;
                    }
                    default -> arms;
                };
                bits[y * n + x] = pixel || s.dot && centerDot;
            }
        }
        return new Mask(n, bits);
    }

    private static boolean heart(double x, double y) {
        double yy = -y + 0.15;
        double value = x * x + yy * yy - 1.0;
        return value * value * value - x * x * yy * yy * yy <= 0.0;
    }

    public static Mask outline(Mask source, int radius) {
        int x;
        int y;
        int n = source.size + radius * 2;
        boolean[] out = new boolean[n * n];
        if (radius == 0) {
            return new Mask(n, out);
        }
        for (y = 0; y < source.size; ++y) {
            for (x = 0; x < source.size; ++x) {
                if (!source.at(x, y)) continue;
                for (int dy = -radius; dy <= radius; ++dy) {
                    for (int dx = -radius; dx <= radius; ++dx) {
                        out[(y + radius + dy) * n + x + radius + dx] = true;
                    }
                }
            }
        }
        for (y = 0; y < source.size; ++y) {
            for (x = 0; x < source.size; ++x) {
                if (!source.at(x, y)) continue;
                out[(y + radius) * n + x + radius] = false;
            }
        }
        return new Mask(n, out);
    }

    public static void editCurrent(CrosshairSettings s) {
        Mask old = CrosshairPattern.create(s);
        int n = 31;
        int[] rows = new int[n];
        for (int y = 0; y < n; ++y) {
            for (int x = 0; x < n; ++x) {
                if (!old.at(x + (old.size - n) / 2, y + (old.size - n) / 2)) continue;
                int n2 = y;
                rows[n2] = rows[n2] | 1 << x;
            }
        }
        s.gridSize = n;
        s.pixels = rows;
        s.mode = "CUSTOM";
    }

    public static void resize(CrosshairSettings s, int size) {
        int n = Math.clamp((long)size, 3, 31);
        int[] rows = new int[n];
        int shift = (n - s.gridSize) / 2;
        for (int y = 0; y < s.gridSize; ++y) {
            for (int x = 0; x < s.gridSize; ++x) {
                if ((s.pixels[y] & 1 << x) == 0 || x + shift < 0 || y + shift < 0 || x + shift >= n || y + shift >= n) continue;
                int n2 = y + shift;
                rows[n2] = rows[n2] | 1 << x + shift;
            }
        }
        s.gridSize = n;
        s.pixels = rows;
    }

    public static String encode(CrosshairSettings s) {
        int size;
        Mask m = CrosshairPattern.create(s);
        int n = m.size;
        int trim = 0;
        while (n - 2 * trim > 3) {
            boolean empty = true;
            int end = n - 1 - trim;
            for (int i = trim; i <= end; ++i) {
                if (!m.at(i, trim) && !m.at(i, end) && !m.at(trim, i) && !m.at(end, i)) continue;
                empty = false;
                break;
            }
            if (!empty) break;
            ++trim;
        }
        if ((size = n - 2 * trim) > 31) {
            throw new IllegalArgumentException("Crosshair too large for a 31 x 31 drawing");
        }
        byte[] data = new byte[1 + (size * size + 7) / 8];
        data[0] = (byte)size;
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                if (!m.at(x + trim, y + trim)) continue;
                int bit = y * size + x;
                int n2 = 1 + bit / 8;
                data[n2] = (byte)(data[n2] | (byte)(1 << bit % 8));
            }
        }
        return "BHCH-1-" + Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static void decode(String code, CrosshairSettings target) {
        if (code == null || code.length() > 256 || !code.strip().matches("BHCH-1-[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Expected BHCH-1 crosshair code");
        }
        byte[] data = Base64.getUrlDecoder().decode(code.strip().substring(7));
        if (data.length < 2) {
            throw new IllegalArgumentException("Truncated crosshair code");
        }
        int n = Byte.toUnsignedInt(data[0]);
        if (n < 3 || n > 31 || data.length != 1 + (n * n + 7) / 8) {
            throw new IllegalArgumentException("Invalid crosshair dimensions");
        }
        int padding = (8 - n * n % 8) % 8;
        if (padding > 0 && Byte.toUnsignedInt(data[data.length - 1]) >>> 8 - padding != 0) {
            throw new IllegalArgumentException("Invalid trailing pixels");
        }
        int[] rows = new int[n];
        for (int y = 0; y < n; ++y) {
            for (int x = 0; x < n; ++x) {
                int bit = y * n + x;
                if ((data[1 + bit / 8] & 1 << bit % 8) == 0) continue;
                int n2 = y;
                rows[n2] = rows[n2] | 1 << x;
            }
        }
        target.gridSize = n;
        target.pixels = rows;
        target.mode = "CUSTOM";
    }

    public record Mask(int size, boolean[] pixels) {
        public boolean at(int x, int y) {
            return x >= 0 && y >= 0 && x < this.size && y < this.size && this.pixels[y * this.size + x];
        }
    }
}

