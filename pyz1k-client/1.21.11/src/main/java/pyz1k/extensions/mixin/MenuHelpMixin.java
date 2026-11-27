package pyz1k.extensions.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.extensions.FeatureHelpScreen;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
@Mixin(value=MenuScreen.class,remap=false)
public abstract class MenuHelpMixin {
 @Inject(method="method_25394",at=@At("TAIL"),remap=false)
 private void help$hint(DrawContext ctx,int mx,int my,float delta,CallbackInfo ci){RenderUtil.drawText(ctx,"F1 — подробное описание функций",8,MinecraftClient.getInstance().getWindow().getScaledHeight()-13,0xffdce5f1,GFonts.MEDIUM,7);}
 @Inject(method="method_25404",at=@At("HEAD"),cancellable=true,remap=false)
 private void help$key(KeyInput key,CallbackInfoReturnable<Boolean> out){if(key.key()==290){MinecraftClient.getInstance().setScreen(new FeatureHelpScreen((Screen)(Object)this));out.setReturnValue(true);}}
}
