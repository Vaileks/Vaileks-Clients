/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.ItemActivation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.player.ItemActivation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.modules.HudItemControlsModule;
import ru.bloodhelper.client.modules.TotemDurationState;

@Mixin(value={ItemActivation.class})
abstract class HudItemControlsActivationMixin
implements TotemDurationState {
    @Shadow
    private int ticks;
    @Unique
    private int bloodhelper$duration = 40;

    HudItemControlsActivationMixin() {
    }

    @Inject(method={"activate"}, at={@At(value="TAIL")})
    private void bloodhelper$activate(ItemStack item, RandomSource random, CallbackInfo ci) {
        HudItemControlsSettings.Totem s = HudItemControlsModule.settings().totem;
        this.ticks = this.bloodhelper$duration = s.enabled && item.is((Object)Items.TOTEM_OF_UNDYING) ? s.durationTicks : 40;
    }

    @Override
    public int bloodhelper$duration() {
        return this.bloodhelper$duration;
    }
}

