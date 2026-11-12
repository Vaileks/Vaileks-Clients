package pyz1k.mixin;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pyz1k.cosmetics.AvatarAttackState;
@Mixin(AvatarRenderState.class)
public class AvatarAttackMixin implements AvatarAttackState {
 @Unique private float pyz1k$attackTime;
 @Unique private HumanoidArm pyz1k$attackArm = HumanoidArm.RIGHT;
 public float pyz1k$attackTime() { return pyz1k$attackTime; }
 public HumanoidArm pyz1k$attackArm() { return pyz1k$attackArm; }
 public void pyz1k$setAttack(float time, HumanoidArm arm) { pyz1k$attackTime=time; pyz1k$attackArm=arm; }
}
