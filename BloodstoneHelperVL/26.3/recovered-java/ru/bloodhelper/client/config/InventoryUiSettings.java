/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class InventoryUiSettings {
    public Position layoutsButton = new Position();
    public Position layoutsPanel = new Position();
    public Position slotLocks = new Position();

    public void sanitize() {
        if (this.layoutsButton == null) {
            this.layoutsButton = new Position();
        }
        if (this.layoutsPanel == null) {
            this.layoutsPanel = new Position();
        }
        if (this.slotLocks == null) {
            this.slotLocks = new Position();
        }
        this.layoutsButton.sanitize();
        this.layoutsPanel.sanitize();
        this.slotLocks.sanitize();
    }

    public static final class Position {
        public boolean moved;
        public double x;
        public double y;

        public int x(int fallback, int screen, int width) {
            return Position.coordinate(this.moved, this.x, fallback, screen, width);
        }

        public int y(int fallback, int screen, int height) {
            return Position.coordinate(this.moved, this.y, fallback, screen, height);
        }

        private static int coordinate(boolean moved, double fraction, int fallback, int screen, int size) {
            int room = Math.max(0, screen - size - 4);
            return 2 + (moved ? (int)Math.round(Math.clamp(Double.isFinite(fraction) ? fraction : 0.0, 0.0, 1.0) * (double)room) : Math.clamp((long)(fallback - 2), 0, room));
        }

        public void move(double left, double top, int screenWidth, int screenHeight, int width, int height) {
            this.moved = true;
            this.x = Math.clamp((left - 2.0) / (double)Math.max(1, screenWidth - width - 4), 0.0, 1.0);
            this.y = Math.clamp((top - 2.0) / (double)Math.max(1, screenHeight - height - 4), 0.0, 1.0);
        }

        void sanitize() {
            this.x = Double.isFinite(this.x) ? Math.clamp(this.x, 0.0, 1.0) : 0.0;
            this.y = Double.isFinite(this.y) ? Math.clamp(this.y, 0.0, 1.0) : 0.0;
        }
    }
}

