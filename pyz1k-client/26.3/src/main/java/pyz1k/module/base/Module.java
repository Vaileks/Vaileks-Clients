/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import pyz1k.Pyz1k;
import pyz1k.module.base.Category;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.bind.BindData;
import pyz1k.module.impl.visuals.Menu;
import pyz1k.module.setting.Setting;
import pyz1k.utilites.IMinecraft;

public class Module
implements IMinecraft {
    private final String name;
    private final String desc;
    private final Category category;
    private final List<Setting> settings = new ArrayList<Setting>();
    private final BindData bind = new BindData();
    private int key;
    private boolean enable;

    public Module() {
        ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        Objects.requireNonNull(moduleInfo, "ModuleInfo not found for " + this.getClass().getSimpleName());
        this.name = moduleInfo.name();
        this.desc = moduleInfo.desc();
        this.category = moduleInfo.category();
    }

    public void onEnable() {
        Pyz1k.getInstance().getEventBus().register((Object)this);
    }

    public void onDisable() {
        Pyz1k.getInstance().getEventBus().unregister((Object)this);
    }

    public void toggle() {
        this.setEnable(!this.enable);
    }

    public void setEnable(boolean enable) {
        if (this.enable != enable) {
            this.enable = enable;
            if (enable) {
                this.onEnable();
            } else {
                this.onDisable();
            }
            Pyz1k pyz1k = Pyz1k.getInstance();
            pyz1k.markConfigDirty();
            if (!enable && this instanceof Menu) {
                String loaded = pyz1k.getConfigManager().getLoadedConfigName();
                pyz1k.getConfigManager().saveConfig(loaded == null || loaded.isBlank() ? "auto" : loaded);
            }
        }
    }

    public void setKey(int key) {
        if (this.key != key) {
            this.key = key;
            Pyz1k.getInstance().markConfigDirty();
        }
    }

    protected <T extends Setting> T registerSetting(T setting) {
        this.settings.add(setting);
        return setting;
    }

    protected final void clearBindKeys() {
        this.bind.clearKeys();
    }

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }

    public boolean hasSettings() {
        return !this.settings.isEmpty();
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public Category getCategory() {
        return this.category;
    }

    public BindData getBind() {
        return this.bind;
    }

    public int getKey() {
        return this.key;
    }

    public boolean isEnable() {
        return this.enable;
    }
}

