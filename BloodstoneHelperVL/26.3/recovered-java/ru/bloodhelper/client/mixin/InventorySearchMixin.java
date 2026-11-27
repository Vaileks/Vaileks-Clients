package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.InventorySearch;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={AbstractContainerScreen.class})
public abstract class InventorySearchMixin {
    @Inject(method={"extractSlot"}, at={@At(value="TAIL")})
    private void bloodhelper$searchSlot(GuiGraphicsExtractor g, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        InventorySearch.slot(g, slot);
        SlotLockModule.renderSlot(g, slot);
    }
}

