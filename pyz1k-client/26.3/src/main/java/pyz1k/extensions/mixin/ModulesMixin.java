/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.extensions.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.FeatureModules;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleManager;

@Mixin(value={ModuleManager.class}, remap=false)
public abstract class ModulesMixin {
    @Shadow
    @Final
    private List<Module> modules;

    @Inject(method={"<init>"}, at={@At(value="TAIL")}, remap=false)
    private void vaileks$register(CallbackInfo ci) {
        FeatureModules.register(this.modules);
    }
}

