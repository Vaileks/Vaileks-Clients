package pyz1k.extensions.mixin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pyz1k.module.base.Module;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.*;
import pyz1k.extensions.UnifiedMenu;
import pyz1k.extensions.pvp.UnifiedFeaturePanel;
@Mixin(value=ModuleGridSection.class,remap=false)
public abstract class UnifiedGridMixin implements UnifiedMenu.Section {
 @Shadow private Module settingsModule;
 @Shadow public abstract void closeSettings();
 @Unique private final UnifiedFeaturePanel unified$panel=new UnifiedFeaturePanel();
 public UnifiedFeaturePanel pyz1k$panel(){return unified$panel;}
 @Unique private boolean unified$active(){return settingsModule!=null&&UnifiedFeaturePanel.supports(settingsModule);}
 @Inject(method="openSettings",at=@At("TAIL"),remap=false)
 private void unified$open(Module m,CallbackInfo ci){if(unified$active())unified$panel.select(m);}
 @Inject(method="closeSettings",at=@At("HEAD"),remap=false)
 private void unified$close(CallbackInfo ci){unified$panel.release();unified$panel.select(null);}
 @Inject(method="render",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$render(GuiGraphicsExtractor c,UiAnimationEngine a,float x,float y,float w,float h,int mx,int my,float alpha,CategoryBarSection.TopTab tab,CallbackInfo ci){if(unified$active()){unified$panel.render(c,x,y,w,h,mx,my,alpha,this::closeSettings);ci.cancel();}}
 @Inject(method="handleClick",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$click(MouseButtonEvent click,float x,float y,float w,float h,CategoryBarSection.TopTab tab,CallbackInfoReturnable<Boolean> out){if(unified$active())out.setReturnValue(unified$panel.click(click.x(),click.y(),pyz1k.compat.LegacyInput.fromNativeMouse(click.button())));}
 @Inject(method="handleScroll",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$scroll(double mx,double my,double amount,float x,float y,float w,float h,CategoryBarSection.TopTab tab,CallbackInfoReturnable<Boolean> out){if(unified$active())out.setReturnValue(unified$panel.scroll(mx,my,amount));}
 @Inject(method="handleKeyPressed",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$key(KeyEvent key,CallbackInfoReturnable<Boolean> out){if(unified$active()){var mc=Minecraft.getInstance();boolean ctrl=(key.modifiers()&6)!=0;if(unified$panel.key(pyz1k.compat.LegacyInput.fromNativeKey(key.key()),ctrl,ctrl&&pyz1k.compat.LegacyInput.fromNativeKey(key.key())==86?mc.keyboardHandler.getClipboard():""))out.setReturnValue(true);}}
 @Inject(method="handleMouseReleased",at=@At("HEAD"),cancellable=true,remap=false)
 private void unified$release(int button,CallbackInfoReturnable<Boolean> out){if(unified$active()&&unified$panel.release())out.setReturnValue(true);}
}
