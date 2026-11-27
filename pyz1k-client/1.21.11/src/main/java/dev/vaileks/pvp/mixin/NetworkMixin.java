package pyz1k.extensions.pvp.mixin;
import pyz1k.extensions.pvp.Core;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayNetworkHandler.class)
abstract class NetworkMixin {
 @Inject(method="onEntityStatus",at=@At("TAIL")) private void pvp$status(EntityStatusS2CPacket p,CallbackInfo ci){Core.status(p);}
 @Inject(method="onEntityDamage",at=@At("TAIL")) private void pvp$damage(EntityDamageS2CPacket p,CallbackInfo ci){Core.damage(p);}
}

