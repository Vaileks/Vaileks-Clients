package pyz1k.extensions.mixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.extensions.LootGloow;
@Mixin(Entity.class)
public abstract class LootEntityMixin {
 @Inject(method="isGlowing",at=@At("RETURN"),cancellable=true)
 private void lootGloow$glow(CallbackInfoReturnable<Boolean> out){if((Object)this instanceof ItemEntity e&&LootGloow.glow(e))out.setReturnValue(true);}
 @Inject(method="getTeamColorValue",at=@At("HEAD"),cancellable=true)
 private void lootGloow$color(CallbackInfoReturnable<Integer> out){if((Object)this instanceof ItemEntity e&&LootGloow.glow(e))out.setReturnValue(LootGloow.color(e)&0xffffff);}
}
