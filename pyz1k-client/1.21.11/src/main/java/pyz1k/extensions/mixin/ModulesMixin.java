package pyz1k.extensions.mixin;
import java.util.List;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleManager;
import pyz1k.extensions.FeatureModules;

@Mixin(value=ModuleManager.class,remap=false)
public abstract class ModulesMixin {
    @Shadow @Final private List<Module> modules;
    @Inject(method="<init>",at=@At("TAIL"),remap=false)
    private void vaileks$register(CallbackInfo ci){FeatureModules.register(modules);}
}
