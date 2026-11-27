/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.SubmitNodeCollection
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.config.NameVisibilitySettings;
import ru.bloodhelper.client.cosmetic.CosmeticNameplates;
import ru.bloodhelper.client.modules.BetterNameVisibilityModule;

@Mixin(value={SubmitNodeCollection.class})
public abstract class BetterNameStyleMixin {
    @ModifyArgs(method={"submitNameTag"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollection;nameTag(Lorg/joml/Matrix4f;FFLnet/minecraft/util/FormattedCharSequence;IIILnet/minecraft/client/gui/Font$DisplayMode;)Lnet/minecraft/client/renderer/feature/TextFeatureRenderer$Submit;")})
    private void bloodhelper$nameStyle(Args args) {
        if (!BetterNameVisibilityModule.active()) {
            return;
        }
        NameVisibilitySettings settings = BetterNameVisibilityModule.settings();
        args.set(5, (Object)settings.foreground());
        args.set(6, (Object)settings.background());
        if (settings.fullbright) {
            args.set(4, (Object)0xF000F0);
        }
        if (settings.disableTeamColors) {
            args.set(3, (Object)CosmeticNameplates.recolor(CosmeticNameplates.recolor(BetterNameVisibilityModule.withoutTeamColors((FormattedCharSequence)args.get(3)))));
        }
        if (settings.distantScale != 0.0) {
            args.set(0, (Object)BetterNameVisibilityModule.scalePose((Matrix4fc)((Matrix4f)args.get(0)), settings.distantScale));
        }
    }
}

