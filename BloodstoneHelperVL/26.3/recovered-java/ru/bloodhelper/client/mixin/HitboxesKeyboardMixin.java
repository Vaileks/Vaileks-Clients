/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.gui.components.debug.DebugScreenEntries
 *  net.minecraft.client.gui.components.debug.DebugScreenEntryList
 *  net.minecraft.client.gui.components.debug.DebugScreenEntryStatus
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.VisualHitboxes;

@Mixin(value={KeyboardHandler.class})
abstract class HitboxesKeyboardMixin {
    HitboxesKeyboardMixin() {
    }

    @WrapOperation(method={"handleDebugKeys"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;toggleStatus(Lnet/minecraft/resources/Identifier;)Z")})
    private boolean bloodhelper$toggleHitboxes(DebugScreenEntryList entries, Identifier entry, Operation<Boolean> original) {
        if (DebugScreenEntries.ENTITY_HITBOXES.equals((Object)entry) && BloodHelperClient.config().hitboxes.enabled) {
            entries.setStatus(entry, DebugScreenEntryStatus.NEVER);
            return VisualHitboxes.toggleVisibility();
        }
        return (Boolean)original.call(new Object[]{entries, entry});
    }
}

