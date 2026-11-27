/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner
 *  org.joml.Vector2i
 *  org.joml.Vector2ic
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.inventory.TooltipPlacement;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={DefaultTooltipPositioner.class})
public abstract class SlotHintTooltipMixin {
    @Inject(method={"positionTooltip(IIIIII)Lorg/joml/Vector2ic;"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$avoidLockHint(int screenWidth, int screenHeight, int mouseX, int mouseY, int width, int height, CallbackInfoReturnable<Vector2ic> callback) {
        int[] hint = SlotLockModule.tooltipHintBounds(Minecraft.getInstance().gui.screen());
        if (hint == null) {
            return;
        }
        Vector2ic original = (Vector2ic)callback.getReturnValue();
        TooltipPlacement.Point point = TooltipPlacement.avoid(original.x(), original.y(), width, height, screenWidth, screenHeight, mouseX, mouseY, hint[0], hint[1], hint[2], hint[3]);
        if (point.x() != original.x() || point.y() != original.y()) {
            callback.setReturnValue((Object)new Vector2i(point.x(), point.y()));
        }
    }
}

