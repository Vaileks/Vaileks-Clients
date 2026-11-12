/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class ScrollBarDrag {
    private double x;
    private double y;
    private double width;
    private double height;
    private double thumb;
    private double value;
    private double maximum;
    private double grab;
    private boolean visible;
    private boolean active;

    public void configure(double x, double y, double width, double height, double minThumb, double value, double maximum) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = Math.max(1.0, height);
        this.maximum = Math.max(0.0, maximum);
        this.value = Math.clamp(value, 0.0, this.maximum);
        this.thumb = Math.min(this.height, Math.max(minThumb, this.height * this.height / (this.height + this.maximum)));
        boolean bl = this.visible = this.maximum > 0.0 && this.height > this.thumb;
        if (!this.visible) {
            this.active = false;
        }
    }

    public void hide() {
        this.visible = false;
    }

    public double thumbTop() {
        return this.y + (this.height - this.thumb) * this.value / Math.max(1.0, this.maximum);
    }

    public double thumbHeight() {
        return this.thumb;
    }

    public double value() {
        return this.value;
    }

    public double maximum() {
        return this.maximum;
    }

    public boolean active() {
        return this.active;
    }

    public boolean press(double mx, double my) {
        if (!this.visible || mx < this.x - 5.0 || mx > this.x + this.width + 5.0 || my < this.y || my > this.y + this.height) {
            return false;
        }
        double top = this.thumbTop();
        this.grab = my >= top && my <= top + this.thumb ? my - top : this.thumb / 2.0;
        this.active = true;
        this.drag(my);
        return true;
    }

    public boolean drag(double my) {
        if (!this.active || !this.visible) {
            this.active = false;
            return false;
        }
        this.value = Math.clamp((my - this.y - this.grab) / Math.max(1.0, this.height - this.thumb) * this.maximum, 0.0, this.maximum);
        return true;
    }

    public boolean release() {
        boolean was = this.active;
        this.active = false;
        return was;
    }
}

