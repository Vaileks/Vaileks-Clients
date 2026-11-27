package pyz1k.extensions.pvp.mixin;
import pyz1k.extensions.pvp.Core;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HandledScreen.class)
abstract class SlotMixin {
 @Inject(method="drawSlot",at=@At("TAIL")) private void pvp$slot(DrawContext c,Slot slot,int mouseX,int mouseY,CallbackInfo ci){Core.inventorySlot(c,slot);}
}

