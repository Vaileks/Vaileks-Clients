/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.setting;

import java.util.function.BooleanSupplier;
import pyz1k.Pyz1k;
import pyz1k.module.bind.BindData;
import pyz1k.module.setting.Setting;

public abstract class AbstractSetting<T>
implements Setting {
    private final String name;
    private final String description;
    private T value;
    private final BindData bind = new BindData();
    private T bindOnValue;
    private T bindOffValue;
    private BooleanSupplier visibleCondition = () -> true;

    protected AbstractSetting(String name, T defaultValue) {
        this(name, null, defaultValue);
    }

    protected AbstractSetting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.bindOnValue = this.copyValue(defaultValue);
        this.bindOffValue = this.copyValue(defaultValue);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    protected T copyValue(T value) {
        return value;
    }

    public T getBindOnValue() {
        return this.bindOnValue;
    }

    public T getBindOffValue() {
        return this.bindOffValue;
    }

    public void setBindOnValue(T bindOnValue) {
        this.bindOnValue = this.copyValue(bindOnValue);
        Pyz1k.getInstance().markConfigDirty();
    }

    public void setBindOffValue(T bindOffValue) {
        this.bindOffValue = this.copyValue(bindOffValue);
        Pyz1k.getInstance().markConfigDirty();
    }

    public void setValue(T value) {
        this.value = this.copyValue(value);
        Pyz1k.getInstance().markConfigDirty();
    }

    public void setBindValues(T bindOnValue, T bindOffValue) {
        this.setBindOnValue(bindOnValue);
        this.setBindOffValue(bindOffValue);
    }

    public void captureBindOnFromCurrent() {
        this.setBindOnValue(this.getValue());
    }

    public void captureBindOffFromCurrent() {
        this.setBindOffValue(this.getValue());
    }

    public void applyBindState(boolean active) {
        T targetValue;
        this.bind.setActive(active);
        T t = targetValue = active ? this.bindOnValue : this.bindOffValue;
        if (targetValue != null) {
            this.setValue(this.copyValue(targetValue));
        }
    }

    public <S extends AbstractSetting<T>> S visibleWhen(BooleanSupplier condition) {
        this.visibleCondition = condition == null ? () -> true : condition;
        return (S)this;
    }

    @Override
    public boolean isVisible() {
        try {
            return this.visibleCondition == null || this.visibleCondition.getAsBoolean();
        }
        catch (Exception ignored) {
            return true;
        }
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public BindData getBind() {
        return this.bind;
    }
}

