package pyz1k.extensions.pvp.mixin;
import pyz1k.extensions.pvp.Core;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
abstract class StackMixin {
 @Inject(method="finishUsing",at=@At("HEAD")) private void pvp$consume(World w,LivingEntity user,CallbackInfoReturnable<ItemStack> ci){if(Core.recording()&&w.isClient()&&user==MinecraftClient.getInstance().player)Core.used(((ItemStack)(Object)this),true);}
}

