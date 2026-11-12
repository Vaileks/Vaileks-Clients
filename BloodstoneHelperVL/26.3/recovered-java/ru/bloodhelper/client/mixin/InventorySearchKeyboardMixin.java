/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.input.CharacterEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.InventoryProfiles;
import ru.bloodhelper.client.modules.InventorySearch;

@Mixin(value={KeyboardHandler.class})
public abstract class InventorySearchKeyboardMixin {
    @Inject(method={"charTyped"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$searchCharacter(long window, CharacterEvent event, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().handle() && (InventoryProfiles.character(event) || InventorySearch.character(event))) {
            ci.cancel();
        }
    }
}

