package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={AbstractContainerScreen.class})
public abstract class ReGlassContainerMixin {
    @Inject(method={"extractSlot"}, at={@At(value="HEAD")})
    private void bloodhelper$slot(GuiGraphicsExtractor g, Slot slot, int offsetX, int offsetY, CallbackInfo ci) {
        ReGlassGui.slot(g, slot);
    }

    @ModifyArg(method={"extractLabels"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V")}, index=4)
    private int bloodhelper$label(int color) {
        return ReGlassGui.labelColor(color);
    }
}

