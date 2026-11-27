package ru.bloodhelper.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.visuals.ZoneTracker;

@Mixin(value={ClientLevel.class})
public class LegendaryZoneMixin {
    @Inject(method={"setServerVerifiedBlockState"}, at={@At(value="HEAD")})
    private void bloodhelper$blockChanged(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        ZoneTracker.onBlock(pos, state);
    }
}

