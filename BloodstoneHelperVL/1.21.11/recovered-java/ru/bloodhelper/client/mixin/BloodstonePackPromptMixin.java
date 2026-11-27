/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_9044
 *  net.minecraft.class_9044$class_9047
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.UUID;
import net.minecraft.class_9044;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.resourcepack.PackAcceptanceScope;

@Mixin(value={class_9044.class})
public abstract class BloodstonePackPromptMixin {
    @ModifyExpressionValue(method={"method_55558(Ljava/util/UUID;Ljava/net/URL;Lcom/google/common/hash/HashCode;)V", "method_55559(Ljava/util/UUID;Ljava/nio/file/Path;)V", "method_55557(Ljava/util/UUID;Lnet/minecraft/class_9044$class_9049;)V"}, at={@At(value="FIELD", target="Lnet/minecraft/class_9044;field_47633:Lnet/minecraft/class_9044$class_9047;")})
    private class_9044.class_9047 bloodhelper$acceptTrustedPack(class_9044.class_9047 original, UUID id) {
        return PackAcceptanceScope.matches(id) ? class_9044.class_9047.field_47648 : original;
    }
}

