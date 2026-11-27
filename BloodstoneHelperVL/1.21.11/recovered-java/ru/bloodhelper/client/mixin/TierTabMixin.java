/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_2561
 *  net.minecraft.class_355
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.CosmeticTabCache;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.HeadsModule;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.policy.ClientControlPolicy;
import ru.bloodhelper.client.tiers.TierDisplay;

@Mixin(value={class_355.class})
public class TierTabMixin {
    @Unique
    private final CosmeticTabCache bloodhelper$painted = new CosmeticTabCache();

    @ModifyExpressionValue(method={"method_1919"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2535;method_10771()Z")})
    private boolean bloodhelper$heads(boolean alreadyVisible) {
        return HeadsModule.tabHeads(alreadyVisible);
    }

    @Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$tiers(class_640 info, CallbackInfoReturnable<class_2561> cir) {
        GameProfile profile;
        Optional<NicknamePalette> color;
        class_2561 display = (class_2561)cir.getReturnValue();
        if (ClientControlPolicy.enabled() && (color = CosmeticNames.palette((profile = info.method_2966()).id(), profile.name(), StreamerMode.hideNickname() || StreamerMode.hideNametags())).isPresent()) {
            display = this.bloodhelper$painted.color(info, display, profile.name(), color.get(), System.nanoTime() / 1000000L);
        }
        cir.setReturnValue((Object)TierDisplay.decorate(info.method_2966().name(), display, true));
    }
}

