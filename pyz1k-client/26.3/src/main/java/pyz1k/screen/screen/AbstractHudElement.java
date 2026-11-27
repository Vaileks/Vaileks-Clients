/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.screen.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pyz1k.Pyz1k;
import pyz1k.module.setting.Setting;
import pyz1k.screen.screen.HudElement;

public abstract class AbstractHudElement
implements HudElement {
    private final String id;
    private final String displayName;
    private final List<Setting> settings = new ArrayList<Setting>();
    private float x;
    private float y;
    private float width;
    private float height;

    protected AbstractHudElement(String id, String displayName, float defaultX, float defaultY, float defaultWidth, float defaultHeight) {
        this.id = id;
        this.displayName = displayName;
        this.x = defaultX;
        this.y = defaultY;
        this.width = defaultWidth;
        this.height = defaultHeight;
    }

    protected final <T extends Setting> T addSetting(T setting) {
        this.settings.add(setting);
        return setting;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public void setPosition(float x, float y) {
        if (Float.isFinite(x) && Float.isFinite(y) && (!(Math.abs(this.x - x) <= 0.001f) || !(Math.abs(this.y - y) <= 0.001f))) {
            this.x = x;
            this.y = y;
            Pyz1k.getInstance().markConfigDirty();
        }
    }

    protected final void setBounds(float width, float height) {
        this.width = Math.max(0.0f, width);
        this.height = Math.max(0.0f, height);
    }

    @Override
    public float getWidth() {
        return this.width;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public List<Setting> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }
}

