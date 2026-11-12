/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_303
 *  net.minecraft.class_303$class_7590
 *  net.minecraft.class_310
 *  net.minecraft.class_338
 *  net.minecraft.class_634
 *  net.minecraft.class_7469
 *  net.minecraft.class_7591
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.bloodhelper.client.mixin;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_338;
import net.minecraft.class_634;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.compat.ExternalChatPatches;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.ChatMessageGroups;
import ru.bloodhelper.client.modules.ChatNicknameRefresh;
import ru.bloodhelper.client.modules.ChatTools;

@Mixin(value={class_338.class})
public abstract class ChatToolsMixin
implements ChatNicknameRefresh {
    @Shadow
    @Final
    private List<class_303> field_2061;
    @Shadow
    @Final
    private List<class_303.class_7590> field_2064;
    @Shadow
    private int field_2066;
    @Unique
    private final ChatMessageGroups bloodhelper$groups = new ChatMessageGroups(2000);
    @Unique
    private class_2561 bloodhelper$original;
    @Unique
    private class_2561 bloodhelper$prepared;
    @Unique
    private Object bloodhelper$connection;
    @Unique
    private long bloodhelper$nicknameGeneration = -1L;

    @Shadow
    protected abstract void method_44813();

    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="HEAD")})
    private void bloodhelper$captureOriginal(class_2561 content, class_7469 signature, class_7591 tag, CallbackInfo ci) {
        this.bloodhelper$original = content;
        this.bloodhelper$prepared = null;
        class_634 connection = class_310.method_1551().method_1562();
        if (connection != this.bloodhelper$connection) {
            this.bloodhelper$groups.clear();
            this.bloodhelper$connection = connection;
        }
    }

    @ModifyArgs(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_303;<init>(ILnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"))
    private void bloodhelper$displayMessage(Args arguments) {
        class_2561 display = ChatTools.receiveForDisplay((class_2561)arguments.get(1), (class_7469)arguments.get(2), arguments.get(3));
        ClientConfig.ChatToolsSettings settings = BloodHelperClient.config().chatTools;
        if (ExternalChatPatches.loaded() || !settings.enabled || !settings.groupDuplicates || this.bloodhelper$connection == null) {
            this.bloodhelper$groups.clear();
            arguments.set(1, (Object)display);
            return;
        }
        class_303 incoming = new class_303(((Integer)arguments.get(0)).intValue(), display, (class_7469)arguments.get(2), (class_7591)arguments.get(3));
        ChatMessageGroups.Result result = this.bloodhelper$groups.accept(this.bloodhelper$original, incoming, this.field_2061.isEmpty() ? null : this.field_2061.getFirst(), true);
        if (result.replacesPrevious()) {
            this.field_2061.removeFirst();
            int removed = 0;
            if (!this.field_2064.isEmpty()) {
                this.field_2064.removeFirst();
                ++removed;
                while (!this.field_2064.isEmpty() && !this.field_2064.getFirst().comp_898()) {
                    this.field_2064.removeFirst();
                    ++removed;
                }
            }
            this.field_2066 = Math.max(0, this.field_2066 - removed);
        }
        arguments.set(1, (Object)result.message().comp_893());
        this.bloodhelper$prepared = result.message().comp_893();
    }

    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="TAIL")})
    private void bloodhelper$bindStoredMessage(class_2561 content, class_7469 signature, class_7591 tag, CallbackInfo ci) {
        if (this.bloodhelper$prepared != null && !this.field_2061.isEmpty()) {
            this.bloodhelper$groups.rebind(this.bloodhelper$prepared, this.field_2061.getFirst());
        }
        this.bloodhelper$prepared = null;
    }

    @Inject(method={"method_44812"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$deleteMessage(class_7469 signature, CallbackInfo callback) {
        ChatTools.delete(signature);
        if (this.bloodhelper$groups.delete(signature, this.field_2061)) {
            this.method_44813();
            callback.cancel();
        }
    }

    @Inject(method={"method_1808"}, at={@At(value="HEAD")})
    private void bloodhelper$clearMessages(boolean clearSentHistory, CallbackInfo callback) {
        if (!clearSentHistory || !ExternalChatPatches.loaded() || ExternalChatPatches.option("vanillaClearing", false)) {
            ChatTools.clear();
        }
        this.bloodhelper$groups.clear();
    }

    @Override
    public void bloodhelper$refreshNicknameLinks() {
        long generation = ChatTools.nicknameGeneration();
        if (generation == this.bloodhelper$nicknameGeneration) {
            return;
        }
        this.bloodhelper$nicknameGeneration = generation;
        boolean changed = false;
        for (int i = 0; i < this.field_2061.size(); ++i) {
            class_303 old = this.field_2061.get(i);
            class_2561 next = ChatTools.nicknameLinks(old.comp_893());
            if (next == old.comp_893()) continue;
            class_303 replacement = new class_303(old.comp_892(), next, old.comp_915(), old.comp_894());
            this.field_2061.set(i, replacement);
            this.bloodhelper$groups.rebind(old.comp_893(), replacement);
            changed = true;
        }
        if (changed) {
            this.method_44813();
        }
    }
}

