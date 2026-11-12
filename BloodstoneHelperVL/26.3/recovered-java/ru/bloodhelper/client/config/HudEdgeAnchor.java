/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.Objects;

public final class HudEdgeAnchor {
    public double sourceX;
    public double sourceY;
    public double sourceScale;
    public String sourceAlignment = "LEFT";
    public boolean sourceBottom;
    public String horizontal = "LEFT";
    public String vertical = "TOP";
    public double offsetX;
    public double offsetY;

    public HudEdgeAnchor copy() {
        HudEdgeAnchor c = new HudEdgeAnchor();
        c.sourceX = this.sourceX;
        c.sourceY = this.sourceY;
        c.sourceScale = this.sourceScale;
        c.sourceAlignment = this.sourceAlignment;
        c.sourceBottom = this.sourceBottom;
        c.horizontal = this.horizontal;
        c.vertical = this.vertical;
        c.offsetX = this.offsetX;
        c.offsetY = this.offsetY;
        return c;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object other) {
        if (!(other instanceof HudEdgeAnchor)) return false;
        HudEdgeAnchor a = (HudEdgeAnchor)other;
        if (Double.compare(this.sourceX, a.sourceX) != 0) return false;
        if (Double.compare(this.sourceY, a.sourceY) != 0) return false;
        if (Double.compare(this.sourceScale, a.sourceScale) != 0) return false;
        if (!Objects.equals(this.sourceAlignment, a.sourceAlignment)) return false;
        if (this.sourceBottom != a.sourceBottom) return false;
        if (!Objects.equals(this.horizontal, a.horizontal)) return false;
        if (!Objects.equals(this.vertical, a.vertical)) return false;
        if (Double.compare(this.offsetX, a.offsetX) != 0) return false;
        if (Double.compare(this.offsetY, a.offsetY) != 0) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.sourceX, this.sourceY, this.sourceScale, this.sourceAlignment, this.sourceBottom, this.horizontal, this.vertical, this.offsetX, this.offsetY);
    }

    public boolean valid() {
        return !(!Double.isFinite(this.sourceX) || !Double.isFinite(this.sourceY) || !Double.isFinite(this.sourceScale) || !(this.sourceScale > 0.0) || !Double.isFinite(this.offsetX) || !Double.isFinite(this.offsetY) || !(Math.abs(this.offsetX) <= 32768.0) || !(Math.abs(this.offsetY) <= 32768.0) || !"LEFT".equals(this.horizontal) && !"CENTER".equals(this.horizontal) && !"RIGHT".equals(this.horizontal) || !"TOP".equals(this.vertical) && !"BOTTOM".equals(this.vertical) || !"LEFT".equals(this.sourceAlignment) && !"CENTER".equals(this.sourceAlignment) && !"RIGHT".equals(this.sourceAlignment));
    }
}

