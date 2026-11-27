package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.DroppedItemFilter;

@Mixin(value={EntityRenderer.class})
public abstract class DroppedItemFilterMixin {
    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideSelectedDrop(Entity entity, Frustum frustum, double x, double y, double z, float partialTick, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity item;
        if (entity instanceof ItemEntity && DroppedItemFilter.hides((item = (ItemEntity)entity).getItem())) {
            cir.setReturnValue((Object)false);
        }
    }
}

