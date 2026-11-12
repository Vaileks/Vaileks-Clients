package pyz1k.extensions.pvp.mixin;
import pyz1k.extensions.pvp.Core;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EnderPearlEntity.class)
abstract class PearlMixin {
 @Inject(method="tick",at=@At("HEAD")) private void pvp$track(CallbackInfo ci){Core.pearlTick((EnderPearlEntity)(Object)this);}
 @Inject(method="onCollision",at=@At("HEAD")) private void pvp$land(HitResult hit,CallbackInfo ci){Core.pearlLand((EnderPearlEntity)(Object)this);}
}

