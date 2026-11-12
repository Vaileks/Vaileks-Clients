/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2663
 *  net.minecraft.class_2718
 *  net.minecraft.class_2749
 *  net.minecraft.class_2783
 *  net.minecraft.class_310
 *  net.minecraft.class_5892
 *  net.minecraft.class_634
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2663;
import net.minecraft.class_2718;
import net.minecraft.class_2749;
import net.minecraft.class_2783;
import net.minecraft.class_310;
import net.minecraft.class_5892;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.CombatTimelineModule;
import ru.bloodhelper.client.modules.DeathRecapModule;
import ru.bloodhelper.client.modules.DeathScreenshotModule;

@Mixin(value={class_634.class})
public abstract class DeathRecapMixin {
    @Inject(method={"method_34075"}, at={@At(value="TAIL")})
    private void bloodhelper$ownDeathMessage(class_5892 packet, CallbackInfo callback) {
        DeathScreenshotModule.tick(class_310.method_1551());
        DeathRecapModule.receiveDeathMessage(packet.comp_2275(), packet.comp_2276());
    }

    @Inject(method={"method_11122"}, at={@At(value="TAIL")})
    private void bloodhelper$observeDeathHealth(class_2749 packet, CallbackInfo callback) {
        ClientConfig settings = BloodHelperClient.config();
        if (settings.deathScreenshot.enabled || settings.deathRecap.enabled) {
            DeathRecapModule.tick(class_310.method_1551());
        }
    }

    @Inject(method={"method_11148"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnTotem(class_2663 packet, CallbackInfo callback) {
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled) {
            CombatTimelineModule.receiveStatus(class_310.method_1551(), packet);
        }
    }

    @Inject(method={"method_11084"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnEffect(class_2783 packet, CallbackInfo callback) {
        class_310 client = class_310.method_1551();
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled && client.field_1724 != null && packet.method_11943() == client.field_1724.method_5628()) {
            CombatTimelineModule.effectsChanged(client, false);
        }
    }

    @Inject(method={"method_11119"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnEffectRemoval(class_2718 packet, CallbackInfo callback) {
        class_310 client = class_310.method_1551();
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled && client.field_1724 != null && packet.comp_2175() == client.field_1724.method_5628()) {
            CombatTimelineModule.effectsChanged(client, true);
        }
    }
}

