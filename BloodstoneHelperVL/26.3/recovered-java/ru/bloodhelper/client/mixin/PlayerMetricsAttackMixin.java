package ru.bloodhelper.client.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.skins.VeritiAnimation;

@Mixin(value={MultiPlayerGameMode.class})
public class PlayerMetricsAttackMixin {
    @Inject(method={"attack"}, at={@At(value="HEAD")})
    private void bloodhelper$attack(Player player, Entity target, CallbackInfo ci) {
        VeritiAnimation.attack(target);
        PlayerMetrics.attack(target);
    }
}

