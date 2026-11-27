package pyz1k.cosmetics;
import net.minecraft.world.entity.HumanoidArm;
public interface AvatarAttackState {
 float pyz1k$attackTime();
 HumanoidArm pyz1k$attackArm();
 void pyz1k$setAttack(float time, HumanoidArm arm);
}
