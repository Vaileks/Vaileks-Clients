package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.circles.BloodstoneCircles;

@Mixin(value={PlayerTabOverlay.class})
public abstract class CirclesTabMixin {
    @Inject(method={"getNameForDisplay"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$circles(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue((Object)BloodstoneCircles.rewrite((Component)cir.getReturnValue(), true));
    }
}

