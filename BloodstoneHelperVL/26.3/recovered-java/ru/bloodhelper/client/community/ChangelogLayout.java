/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.community;

public final class ChangelogLayout {
    private ChangelogLayout() {
    }

    public static Box box(int width, int height, int lines) {
        if (width < 400 || height < 180) {
            return new Box(10, 10, 0, 0, 0);
        }
        int w = Math.min(220, (width - 200) / 2 - 20);
        int maxHeight = Math.min(176, height - 20);
        int rows = Math.max(1, Math.min(Math.max(0, lines), (maxHeight - 36) / 10));
        return new Box(10, 10, w, Math.min(maxHeight, 36 + rows * 10), rows);
    }

    public record Box(int x, int y, int width, int height, int rows) {
        public boolean contains(double x, double y) {
            return this.width > 0 && x >= (double)this.x && x < (double)(this.x + this.width) && y >= (double)this.y && y < (double)(this.y + this.height);
        }
    }
}

