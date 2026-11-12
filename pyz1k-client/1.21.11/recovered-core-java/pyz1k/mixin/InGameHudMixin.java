/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.Pyz1k;
import pyz1k.event.impl.EventScreenRender;
import pyz1k.module.impl.visuals.PotionList;
import pyz1k.screen.menu.MenuCloseAnimation;

@Mixin(value={class_329.class})
public class InGameHudMixin {
    @Inject(method={"method_1753"}, at={@At(value="RETURN")})
    private void onRenderHud(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        Pyz1k pyz1k = Pyz1k.getInstance();
        if (pyz1k != null) {
            pyz1k.getEventBus().post((Object)new EventScreenRender(context, tickCounter, EventScreenRender.Stage.HUD));
            MenuCloseAnimation.render(context);
        }
    }

    @Inject(method={"method_1765"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$hideVanillaPotionEffects(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (InGameHudMixin.pyz1k$isPotionListEnabled()) {
            ci.cancel();
        }
    }

    private static boolean pyz1k$isPotionListEnabled() {
        Pyz1k pyz1k = Pyz1k.getInstance();
        if (pyz1k == null || pyz1k.getModuleManager() == null) {
            return false;
        }
        PotionList potionList = pyz1k.getModuleManager().getModule(PotionList.class);
        return potionList != null && potionList.isEnable();
    }
}

