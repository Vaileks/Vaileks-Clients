package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
abstract class StackMixin {
    StackMixin() {
    }

    @Inject(method={"finishUsingItem"}, at={@At(value="HEAD")})
    private void pvp$consume(Level w, LivingEntity user, CallbackInfoReturnable<ItemStack> ci) {
        if (Core.recording() && w.isClientSide() && user == Minecraft.getInstance().player) {
            Core.used((ItemStack)(Object)this, true);
        }
    }
}

