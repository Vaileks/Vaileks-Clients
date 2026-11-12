package pyz1k.extensions.mixin;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.item.*;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import pyz1k.extensions.SkinCatalog;
@Mixin(ItemModelManager.class)
public abstract class SkinsMixin {
    @ModifyVariable(method="update",at=@At("HEAD"),argsOnly=true)
    private ItemStack vaileks$skin(ItemStack stack,ItemRenderState state,ItemStack original,ItemDisplayContext context,World world,HeldItemContext holder,int seed){
        var mirror=pyz1k.extensions.LootGloow.oldMirror(stack);
        return mirror!=stack?mirror:SkinCatalog.displayCopy(stack,context,holder);
    }
}
