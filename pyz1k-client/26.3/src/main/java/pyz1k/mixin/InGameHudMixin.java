/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.Pyz1k;
import pyz1k.event.impl.EventScreenRender;
import pyz1k.module.impl.visuals.PotionList;
import pyz1k.screen.menu.MenuCloseAnimation;

@Mixin(value={Hud.class})
public class InGameHudMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="RETURN")})
    private void onRenderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Pyz1k pyz1k = Pyz1k.getInstance();
        if (pyz1k != null) {
            pyz1k.getEventBus().post((Object)new EventScreenRender(context, tickCounter, EventScreenRender.Stage.HUD));
            MenuCloseAnimation.render(context);
        }
    }

    @Inject(method={"extractEffects"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$hideVanillaPotionEffects(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
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

