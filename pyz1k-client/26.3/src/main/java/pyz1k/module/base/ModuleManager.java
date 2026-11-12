/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.base;

import java.util.ArrayList;
import java.util.List;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.module.impl.visuals.ItemCooldownsModule;
import pyz1k.module.impl.visuals.KillEffects;
import pyz1k.module.impl.visuals.Menu;
import pyz1k.module.impl.visuals.PotionList;
import pyz1k.module.impl.visuals.PvPCooldown;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<Module>();

    public ModuleManager() {
        this.modules.add(new Menu());
        this.modules.add(new Interface());
        this.modules.add(new ItemCooldownsModule());
        this.modules.add(new PvPCooldown());
        this.modules.add(new PotionList());
        this.modules.add(new KillEffects());
        for (Module module : this.modules) {
            ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
            if (info == null || !info.enabledByDefault() || module.isEnable()) continue;
            module.toggle();
        }
    }

    public List<Module> getModules(Category moduleCategory) {
        return this.modules.stream().filter(module -> module.getCategory() == moduleCategory).toList();
    }

    public <T extends Module> T getModule(Class<T> tClass) {
        return (T)((Module)this.modules.stream().filter(module -> module.getClass() == tClass).findFirst().orElse(null));
    }

    public List<Module> getModules() {
        return this.modules;
    }
}

