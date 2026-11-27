package pyz1k.extensions.pvp.mixin;
import pyz1k.extensions.pvp.Core;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
abstract class HudMixin {
 @Inject(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",at=@At("HEAD"),cancellable=true) private void pvp$scoreboard(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(Core.hideVanilla("scoreboard"))ci.cancel();}
 @Inject(method="renderBossBarHud",at=@At("HEAD"),cancellable=true) private void pvp$bossbar(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(Core.hideVanilla("bossbar"))ci.cancel();}
 @Inject(method="renderStatusEffectOverlay",at=@At("HEAD"),cancellable=true) private void pvp$effects(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(Core.hideVanilla("effects"))ci.cancel();}
 @Inject(method="renderHeldItemTooltip",at=@At("HEAD"),cancellable=true) private void pvp$item(DrawContext c,CallbackInfo ci){if(Core.hideVanilla("item"))ci.cancel();}

 @Inject(method="render",at=@At("TAIL")) private void pvp$render(DrawContext c,RenderTickCounter t,CallbackInfo ci){Core.render(c);}
 @Inject(method="renderHotbarItem",at=@At("TAIL")) private void pvp$slot(DrawContext c,int x,int y,RenderTickCounter t,PlayerEntity p,ItemStack s,int seed,CallbackInfo ci){Core.hotbarSlot(c,s,x,y);}
}

