/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.extensions.mixin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.event.impl.EventScreenRender;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.module.impl.visuals.Interface;

@Mixin(value={Interface.class}, remap=false)
public abstract class InterfaceMixin {
    @Inject(method={"onRenderHud"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void vaileks$hideHud(EventScreenRender event, CallbackInfo ci) {
        if (ScreenshotMode.active()) {
            ci.cancel();
        }
    }

    @Inject(method={"getVisibleElements"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private void vaileks$hud(CallbackInfoReturnable<Set<String>> cir) {
        if(FeatureModules.blood==null||!FeatureModules.blood.isEnable())return;
        Set<String> original=cir.getReturnValue();
        if(original.contains("BloodSwordCooldown"))return;
        Set<String> visible=new LinkedHashSet<>(original);
        visible.add("BloodSwordCooldown");cir.setReturnValue(visible);
    }
}
