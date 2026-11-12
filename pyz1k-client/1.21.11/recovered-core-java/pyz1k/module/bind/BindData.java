/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.bind;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Generated;
import pyz1k.Pyz1k;
import pyz1k.module.bind.BindMode;

public final class BindData {
    private BindMode mode = BindMode.TOGGLE;
    private final LinkedHashSet<Integer> keys = new LinkedHashSet();
    private boolean active;
    private boolean onBindEnable = true;
    private boolean showInBinds = true;

    public void setMode(BindMode mode) {
        this.mode = mode == null ? BindMode.TOGGLE : mode;
        Pyz1k.getInstance().markConfigDirty();
    }

    public Set<Integer> getKeys() {
        return Collections.unmodifiableSet(this.keys);
    }

    public void setKeys(Collection<Integer> keys) {
        this.keys.clear();
        if (keys != null) {
            this.keys.addAll(keys);
        }
        Pyz1k.getInstance().markConfigDirty();
    }

    public void clearKeys() {
        this.keys.clear();
        Pyz1k.getInstance().markConfigDirty();
    }

    public boolean hasKeys() {
        return !this.keys.isEmpty();
    }

    public void setActive(boolean active) {
        this.active = active;
        Pyz1k.getInstance().markConfigDirty();
    }

    public void setOnBindEnable(boolean onBindEnable) {
        this.onBindEnable = onBindEnable;
        Pyz1k.getInstance().markConfigDirty();
    }

    public void setShowInBinds(boolean showInBinds) {
        this.showInBinds = showInBinds;
        Pyz1k.getInstance().markConfigDirty();
    }

    @Generated
    public BindMode getMode() {
        return this.mode;
    }

    @Generated
    public boolean isActive() {
        return this.active;
    }

    @Generated
    public boolean isOnBindEnable() {
        return this.onBindEnable;
    }

    @Generated
    public boolean isShowInBinds() {
        return this.showInBinds;
    }
}

