/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pyz1k.module.setting.AbstractSetting;
import pyz1k.module.setting.SettingType;

public final class SelectSetting
extends AbstractSetting<String> {
    private final List<String> options;
    private int index;

    public SelectSetting(String name, String defaultValue, List<String> options) {
        this(name, null, defaultValue, options);
    }

    public SelectSetting(String name, String description, String defaultValue, List<String> options) {
        super(name, description, defaultValue);
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("SelectSetting options must not be empty");
        }
        this.options = new ArrayList<String>(options);
        this.index = Math.max(0, this.options.indexOf(defaultValue));
        this.setValue(this.options.get(this.index));
    }

    @Override
    public SettingType getType() {
        return SettingType.SELECT;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    @Override
    public String getValue() {
        return (String)super.getValue();
    }

    @Override
    public void setValue(String value) {
        int foundIndex = this.options.indexOf(value);
        if (foundIndex >= 0) {
            this.index = foundIndex;
            super.setValue(this.options.get(this.index));
        }
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        if (index >= 0 && index < this.options.size()) {
            this.index = index;
            super.setValue(this.options.get(this.index));
        }
    }

    public void next() {
        this.setIndex((this.index + 1) % this.options.size());
    }

    public void previous() {
        this.setIndex((this.index - 1 + this.options.size()) % this.options.size());
    }
}

