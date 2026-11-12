/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.community.CommunityPanels;

@Mixin(value={class_442.class})
public abstract class CommunityTitleMixin
extends class_437 {
    protected CommunityTitleMixin(class_2561 title) {
        super(title);
    }

    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        return CommunityPanels.scroll(x, y, vertical, this.field_22789, this.field_22790) || super.method_25401(x, y, horizontal, vertical);
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void bloodhelper$community(class_332 g, int mx, int my, float delta, CallbackInfo ci) {
        class_1041 window = class_310.method_1551().method_22683();
        CommunityPanels.title(g, window.method_4486(), window.method_4502());
    }
}

