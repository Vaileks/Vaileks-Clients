/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11905;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.InventoryProfiles;
import ru.bloodhelper.client.modules.InventorySearch;

@Mixin(value={class_309.class})
public abstract class InventorySearchKeyboardMixin {
    @Inject(method={"method_1457"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$searchCharacter(long window, class_11905 event, CallbackInfo ci) {
        if (window == class_310.method_1551().method_22683().method_4490() && (InventoryProfiles.character(event) || InventorySearch.character(event))) {
            ci.cancel();
        }
    }
}

