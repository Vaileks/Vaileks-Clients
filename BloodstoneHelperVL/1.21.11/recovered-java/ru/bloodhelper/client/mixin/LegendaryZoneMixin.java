package ru.bloodhelper.client.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.visuals.ZoneTracker;

@Mixin(value={class_638.class})
public class LegendaryZoneMixin {
    @Inject(method={"method_41928"}, at={@At(value="HEAD")})
    private void bloodhelper$blockChanged(class_2338 pos, class_2680 state, int flags, CallbackInfo ci) {
        ZoneTracker.onBlock(pos, state);
    }
}

