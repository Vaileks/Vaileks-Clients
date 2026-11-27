package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractContainerScreen.class})
abstract class SlotMixin {
    SlotMixin() {
    }

    @Inject(method={"extractSlot"}, at={@At(value="TAIL")})
    private void pvp$slot(GuiGraphicsExtractor c, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        Core.inventorySlot(c, slot);
    }
}

