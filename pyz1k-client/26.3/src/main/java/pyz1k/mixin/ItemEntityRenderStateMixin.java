/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.state.ItemEntityRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package pyz1k.mixin;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pyz1k.itemrender.ItemRenderState;

@Mixin(value={ItemEntityRenderState.class})
public class ItemEntityRenderStateMixin
implements ItemRenderState {
    @Unique
    private float pyz1k$groundScale = 1.0f;

    @Override
    public void pyz1k$setGroundScale(float scale) {
        this.pyz1k$groundScale = scale;
    }

    @Override
    public float pyz1k$getGroundScale() {
        return this.pyz1k$groundScale;
    }
}

