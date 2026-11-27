/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.gui.ShulkerPreviewTooltip;
import ru.bloodhelper.client.modules.ShulkerPreview;

@Mixin(value={ClientTooltipComponent.class})
public interface ShulkerPreviewComponentMixin {
    @Inject(method={"create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void bloodhelper$createShulkerPreview(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> result) {
        if (data instanceof ShulkerPreview.Payload) {
            ShulkerPreview.Payload payload = (ShulkerPreview.Payload)data;
            result.setReturnValue((Object)new ShulkerPreviewTooltip(payload));
        }
    }
}

