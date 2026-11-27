package pyz1k.extensions.mixin;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.menu.sections.ModuleGridSection;
import pyz1k.extensions.UnifiedMenu;
@Mixin(value=MenuScreen.class,remap=false)
public abstract class UnifiedInputMixin {
 @Shadow @Final private ModuleGridSection moduleGridSection;
 @Shadow private MouseButtonEvent toLogicalClick(MouseButtonEvent click){throw new AssertionError();}
 @Inject(method="charTyped",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$char(CharacterEvent input,CallbackInfoReturnable<Boolean> out){if(moduleGridSection.isSettingsOpen()&&((UnifiedMenu.Section)(Object)moduleGridSection).pyz1k$panel().character(input.codepoint()))out.setReturnValue(true);}
 @Inject(method="mouseDragged",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$drag(MouseButtonEvent click,double dx,double dy,CallbackInfoReturnable<Boolean> out){if(moduleGridSection.isSettingsOpen()){MouseButtonEvent logical=toLogicalClick(click);if(((UnifiedMenu.Section)(Object)moduleGridSection).pyz1k$panel().drag(logical.x(),logical.y()))out.setReturnValue(true);}}
}
