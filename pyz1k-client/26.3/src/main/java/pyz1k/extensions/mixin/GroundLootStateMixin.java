package pyz1k.extensions.mixin;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.*;
import pyz1k.extensions.GroundLootState;
@Mixin(ItemEntityRenderState.class)
public class GroundLootStateMixin implements GroundLootState {
 @Unique private float lootGloow$scale=1;
 public float lootGloow$getScale(){return lootGloow$scale;}
 public void lootGloow$setScale(float value){lootGloow$scale=value;}
}
