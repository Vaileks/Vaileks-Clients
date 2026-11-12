/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1058
 *  net.minecraft.class_11659
 *  net.minecraft.class_11683$class_11792
 *  net.minecraft.class_1921
 *  net.minecraft.class_310
 *  net.minecraft.class_3879
 *  net.minecraft.class_4587
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package pyz1k.mixin;

import net.minecraft.class_10055;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_11683;
import net.minecraft.class_1921;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.cosmetics.KirbyMesh;

@Mixin(value={class_922.class})
public abstract class ChickenPlayerRenderMixin {
    @Redirect(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"), require=0)
    private void pyz1k$renderChicken(class_11659 collector, class_3879<?> model, Object state, class_4587 pose, class_1921 renderType, int light, int overlay, int color, class_1058 sprite, int outlineColor, class_11683.class_11792 crumbling) {
        class_310 minecraft = class_310.method_1551();
        if (state instanceof class_10055) {
            class_10055 avatar = (class_10055)state;
            if (minecraft.field_1724 != null && avatar.field_53528 == minecraft.field_1724.method_5628()) {
                CosmeticsManager cosmetics = CosmeticsManager.getInstance();
                if (cosmetics.isKirbySelected() && KirbyMesh.isAvailable()) {
                    KirbyMesh.submit(avatar, pose, collector, light, overlay);
                    return;
                }
                if (cosmetics.isChickenSelected() && ChickenMesh.isAvailable()) {
                    ChickenMesh.submit(avatar, pose, collector, light, overlay);
                    return;
                }
            }
        }
        collector.method_73490(model, state, pose, renderType, light, overlay, color, sprite, outlineColor, crumbling);
    }
}

