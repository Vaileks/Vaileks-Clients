/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import pyz1k.module.setting.AbstractSetting;
import pyz1k.module.setting.SettingType;

public final class MultiSelectSetting
extends AbstractSetting<Set<String>> {
    private final List<String> options;
    private final Set<String> selected = new LinkedHashSet<String>();

    public MultiSelectSetting(String name, List<String> options, Set<String> defaultSelected) {
        this(name, null, options, defaultSelected);
    }

    public MultiSelectSetting(String name, String description, List<String> options, Set<String> defaultSelected) {
        super(name, description, Collections.emptySet());
        if (options != null && !options.isEmpty()) {
            this.options = new ArrayList<String>(options);
            if (defaultSelected != null) {
                for (String value : defaultSelected) {
                    if (!this.options.contains(value)) continue;
                    this.selected.add(value);
                }
            }
        } else {
            throw new IllegalArgumentException("MultiSelectSetting options must not be empty");
        }
        this.syncValue();
    }

    @Override
    public SettingType getType() {
        return SettingType.MULTI_SELECT;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    public Set<String> getSelectedValues() {
        return Collections.unmodifiableSet(this.selected);
    }

    public boolean isSelected(String option) {
        return this.selected.contains(option);
    }

    @Override
    public void setValue(Set<String> value) {
        this.selected.clear();
        if (value != null) {
            for (String option : value) {
                if (!this.options.contains(option)) continue;
                this.selected.add(option);
            }
        }
        this.syncValue();
    }

    public void toggle(String option) {
        if (this.options.contains(option)) {
            if (!this.selected.remove(option)) {
                this.selected.add(option);
            }
            this.syncValue();
        }
    }

    private void syncValue() {
        super.setValue(Collections.unmodifiableSet(new LinkedHashSet<String>(this.selected)));
    }

    @Override
    protected Set<String> copyValue(Set<String> value) {
        return value == null ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<String>(value));
    }
}

