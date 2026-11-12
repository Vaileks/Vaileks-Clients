package pyz1k.extensions.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.module.base.Module;
import pyz1k.extensions.FunctionDescriptions;
@Mixin(value=Module.class,remap=false)
public abstract class ModuleDescriptionMixin {
 @Inject(method="getDesc",at=@At("RETURN"),cancellable=true,remap=false)
 private void help$description(CallbackInfoReturnable<String> out){out.setReturnValue(FunctionDescriptions.get(((Module)(Object)this).getName(),out.getReturnValue()));}
}
