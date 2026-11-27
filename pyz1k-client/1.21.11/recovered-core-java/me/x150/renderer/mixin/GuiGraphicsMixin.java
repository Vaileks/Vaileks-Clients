/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package me.x150.renderer.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import me.x150.renderer.render.GuiScissorTracker;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_332.class})
public abstract class GuiGraphicsMixin
implements GuiScissorTracker {
    @Unique
    private static final class_8030 PYZ1K_HIDDEN_SCISSOR = new class_8030(0, 0, 1, 1);
    @Shadow
    @Final
    private Matrix3x2fStack field_44657;
    @Unique
    private final Deque<class_8030> pyz1k$scissors = new ArrayDeque<class_8030>();

    @Inject(method={"method_44379"}, at={@At(value="HEAD")})
    private void pyz1k$pushScissor(int x1, int y1, int x2, int y2, CallbackInfo ci) {
        class_8030 area = new class_8030(x1, y1, Math.max(0, x2 - x1), Math.max(0, y2 - y1)).method_71523((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)this.field_44657));
        class_8030 parent = this.pyz1k$scissors.peek();
        class_8030 clipped = parent == null ? area : parent.method_49701(area);
        this.pyz1k$scissors.push(GuiGraphicsMixin.pyz1k$nonEmptyScissor(GuiGraphicsMixin.pyz1k$clipToFramebuffer(clipped)));
    }

    @Inject(method={"method_44380"}, at={@At(value="HEAD")})
    private void pyz1k$popScissor(CallbackInfo ci) {
        if (!this.pyz1k$scissors.isEmpty()) {
            this.pyz1k$scissors.pop();
        }
    }

    @Override
    public class_8030 pyz1k$currentScissor() {
        return this.pyz1k$scissors.peek();
    }

    @Unique
    private static class_8030 pyz1k$nonEmptyScissor(class_8030 area) {
        return area != null && area.comp_1196() > 0 && area.comp_1197() > 0 ? area : PYZ1K_HIDDEN_SCISSOR;
    }

    @Unique
    private static class_8030 pyz1k$clipToFramebuffer(class_8030 area) {
        if (area == null) {
            return null;
        }
        class_310 minecraft = class_310.method_1551();
        if (minecraft != null && minecraft.method_22683() != null) {
            class_8030 framebuffer = new class_8030(0, 0, Math.max(1, minecraft.method_22683().method_4489()), Math.max(1, minecraft.method_22683().method_4506()));
            return framebuffer.method_49701(area);
        }
        return area;
    }
}

