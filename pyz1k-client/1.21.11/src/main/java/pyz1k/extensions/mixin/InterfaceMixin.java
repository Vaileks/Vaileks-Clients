package pyz1k.extensions.mixin;
import java.util.*;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.event.impl.EventScreenRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=pyz1k.module.impl.visuals.Interface.class,remap=false)
public abstract class InterfaceMixin {
    @Inject(method="onRenderHud",at=@At("HEAD"),cancellable=true,remap=false)
    private void vaileks$hideHud(EventScreenRender event,CallbackInfo ci){if(ScreenshotMode.active())ci.cancel();}
    @Inject(method="getVisibleElements",at=@At("RETURN"),cancellable=true,remap=false)
    private void vaileks$hud(CallbackInfoReturnable<Set<String>> cir){
        if(FeatureModules.blood==null||!FeatureModules.blood.isEnable())return;
        Set<String> original=cir.getReturnValue();
        if(original.contains("BloodSwordCooldown"))return;
        Set<String> visible=new LinkedHashSet<>(original);
        visible.add("BloodSwordCooldown");cir.setReturnValue(visible);
    }
}
