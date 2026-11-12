/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.inventory;

import ru.bloodhelper.client.config.InventoryUiSettings;

public final class InventoryUiDrag {
    private InventoryUiSettings.Position position;
    private double offsetX;
    private double offsetY;
    private int width;
    private int height;

    public boolean active() {
        return this.position != null;
    }

    public void begin(InventoryUiSettings.Position target, double mouseX, double mouseY, int x, int y, int w, int h) {
        this.position = target;
        this.offsetX = mouseX - (double)x;
        this.offsetY = mouseY - (double)y;
        this.width = w;
        this.height = h;
    }

    public boolean move(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (!this.active()) {
            return false;
        }
        this.position.move(mouseX - this.offsetX, mouseY - this.offsetY, screenWidth, screenHeight, this.width, this.height);
        return true;
    }

    public boolean end() {
        boolean was = this.active();
        this.position = null;
        return was;
    }
}

