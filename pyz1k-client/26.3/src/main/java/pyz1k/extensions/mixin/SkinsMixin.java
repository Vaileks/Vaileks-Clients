package pyz1k.extensions.mixin;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pyz1k.extensions.SkinCatalog;

@Mixin(value={ItemModelResolver.class})
public abstract class SkinsMixin {
    @ModifyVariable(method={"appendItemLayers"}, at={@At(value="HEAD")}, argsOnly=true)
    private ItemStack vaileks$skin(ItemStack stack, ItemStackRenderState state, ItemStack original, ItemDisplayContext context, Level world, ItemOwner holder, int seed) {
        var mirror=pyz1k.extensions.LootGloow.oldMirror(stack);
        return mirror!=stack?mirror:SkinCatalog.displayCopy(stack,context,holder);
    }
}

