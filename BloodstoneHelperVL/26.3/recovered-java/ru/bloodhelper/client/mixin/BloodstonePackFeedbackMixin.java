/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.server.DownloadedPackSource
 *  net.minecraft.client.resources.server.PackLoadFeedback
 *  net.minecraft.client.resources.server.PackLoadFeedback$FinalResult
 *  net.minecraft.client.resources.server.PackLoadFeedback$Update
 *  net.minecraft.client.resources.server.ServerPackManager$PackPromptStatus
 *  net.minecraft.network.Connection
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.resourcepack.BloodstoneResourcePack;

@Mixin(value={DownloadedPackSource.class})
public abstract class BloodstonePackFeedbackMixin {
    @Shadow
    PackLoadFeedback packFeedback;

    @Inject(method={"configureForServerControl"}, at={@At(value="TAIL")})
    private void bloodhelper$observe(final Connection connection, ServerPackManager.PackPromptStatus prompt, CallbackInfo ci) {
        final PackLoadFeedback vanilla = this.packFeedback;
        this.packFeedback = new PackLoadFeedback(){
            {
                Objects.requireNonNull(this$0);
            }

            public void reportUpdate(UUID id, PackLoadFeedback.Update update) {
                vanilla.reportUpdate(id, update);
            }

            public void reportFinalResult(UUID id, PackLoadFeedback.FinalResult result) {
                vanilla.reportFinalResult(id, result);
                BloodstoneResourcePack.feedback(connection, id, result);
            }
        };
    }

    @Inject(method={"cleanupAfterDisconnect"}, at={@At(value="HEAD")})
    private void bloodhelper$disconnected(CallbackInfo ci) {
        BloodstoneResourcePack.disconnected();
    }
}

