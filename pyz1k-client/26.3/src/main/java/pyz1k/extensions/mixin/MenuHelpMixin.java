package pyz1k.extensions.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.extensions.FeatureHelpScreen;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
@Mixin(value=MenuScreen.class,remap=false)
public abstract class MenuHelpMixin {
 @Inject(method="extractRenderState",at=@At("TAIL"),remap=false)
 private void help$hint(GuiGraphicsExtractor ctx,int mx,int my,float delta,CallbackInfo ci){RenderUtil.drawText(ctx,"F1 — подробное описание функций",8,Minecraft.getInstance().getWindow().getGuiScaledHeight()-13,0xffdce5f1,GFonts.MEDIUM,7);}
 @Inject(method="keyPressed",at=@At("HEAD"),cancellable=true,remap=false)
 private void help$key(KeyEvent key,CallbackInfoReturnable<Boolean> out){if(pyz1k.compat.LegacyInput.fromNativeKey(key.key())==290){Minecraft.getInstance().gui.setScreen(new FeatureHelpScreen((Screen)(Object)this));out.setReturnValue(true);}}
}
