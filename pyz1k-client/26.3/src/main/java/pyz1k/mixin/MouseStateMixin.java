package pyz1k.mixin;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.compat.LegacyInput;
/** MouseHandler's gameplay pressed flags are not reliable while a GUI handles input. */
@Mixin(MouseHandler.class)
public abstract class MouseStateMixin {
 @Inject(method="onButton",at=@At("HEAD"))
 private void pyz1k$button(long window,MouseButtonInfo button,int action,CallbackInfo ci){if(window!=0&&window==net.minecraft.client.Minecraft.getInstance().getWindow().handle())LegacyInput.mouseButton(button.button(),action);}
}
