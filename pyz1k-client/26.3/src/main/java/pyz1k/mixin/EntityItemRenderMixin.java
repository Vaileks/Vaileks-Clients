/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.Pyz1k;
import pyz1k.itemrender.ItemRenderManager;
import pyz1k.itemrender.ItemRenderRule;

@Mixin(value={Entity.class})
public abstract class EntityItemRenderMixin {
    @Inject(method={"isCurrentlyGlowing"}, at={@At(value="RETURN")}, cancellable=true)
    private void pyz1k$itemGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (!(self instanceof ItemEntity)) {
            return;
        }
        ItemEntity itemEntity = (ItemEntity)self;
        ItemRenderRule rule = this.pyz1k$glowRule(itemEntity);
        if (rule != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"getTeamColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$itemGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity)(Object)this;
        if (!(self instanceof ItemEntity)) {
            return;
        }
        ItemEntity itemEntity = (ItemEntity)self;
        ItemRenderRule rule = this.pyz1k$glowRule(itemEntity);
        if (rule != null) {
            cir.setReturnValue((rule.glowColor & 0xFFFFFF));
        }
    }

    private ItemRenderRule pyz1k$glowRule(ItemEntity entity) {
        Pyz1k pyz1k = Pyz1k.getInstance();
        if (pyz1k == null) {
            return null;
        }
        ItemRenderManager manager = ItemRenderManager.getInstance();
        return manager == null ? null : manager.getGlowRule(entity);
    }
}

