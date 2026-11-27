/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.resources.server.ServerPackManager
 *  net.minecraft.client.resources.server.ServerPackManager$PackPromptStatus
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.UUID;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.resourcepack.PackAcceptanceScope;

@Mixin(value={ServerPackManager.class})
public abstract class BloodstonePackPromptMixin {
    @ModifyExpressionValue(method={"pushPack(Ljava/util/UUID;Ljava/net/URL;Lcom/google/common/hash/HashCode;)V", "pushLocalPack(Ljava/util/UUID;Ljava/nio/file/Path;)V", "pushNewPack(Ljava/util/UUID;Lnet/minecraft/client/resources/server/ServerPackManager$ServerPackData;)V"}, at={@At(value="FIELD", target="Lnet/minecraft/client/resources/server/ServerPackManager;packPromptStatus:Lnet/minecraft/client/resources/server/ServerPackManager$PackPromptStatus;")})
    private ServerPackManager.PackPromptStatus bloodhelper$acceptTrustedPack(ServerPackManager.PackPromptStatus original, UUID id) {
        return PackAcceptanceScope.matches(id) ? ServerPackManager.PackPromptStatus.ALLOWED : original;
    }
}

