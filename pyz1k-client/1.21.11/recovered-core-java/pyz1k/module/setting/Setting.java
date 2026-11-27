/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.setting;

import pyz1k.module.bind.BindData;
import pyz1k.module.setting.SettingType;

public interface Setting {
    public String getName();

    default public String getDescription() {
        return null;
    }

    public SettingType getType();

    public BindData getBind();

    default public boolean isVisible() {
        return true;
    }
}

