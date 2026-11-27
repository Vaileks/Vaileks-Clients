package ru.bloodhelper.client.mixin;

import net.minecraft.class_11683;
import net.minecraft.class_11954;
import net.minecraft.class_2586;
import net.minecraft.class_2625;
import net.minecraft.class_824;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={class_824.class})
public abstract class OptimizerSignMixin {
    @Inject(method={"method_74348"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$distantSigns(class_2586 entity, float partial, class_11683.class_11792 overlay, CallbackInfoReturnable<class_11954> cir) {
        if (entity instanceof class_2625 && OptimizerModule.hideSign(entity.method_11016())) {
            cir.setReturnValue(null);
        }
    }
}

