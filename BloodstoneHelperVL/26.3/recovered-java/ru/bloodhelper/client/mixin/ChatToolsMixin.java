/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.client.multiplayer.chat.GuiMessageSource
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
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

@Mixin(value={ChatComponent.class})
public abstract class ChatToolsMixin
implements ChatNicknameRefresh {
    @Shadow
    @Final
    private List<GuiMessage> allMessages;
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;
    @Shadow
    private int chatScrollbarPos;
    @Unique
    private final ChatMessageGroups bloodhelper$groups = new ChatMessageGroups(2000);
    @Unique
    private Component bloodhelper$original;
    @Unique
    private Component bloodhelper$prepared;
    @Unique
    private Object bloodhelper$connection;
    @Unique
    private long bloodhelper$nicknameGeneration = -1L;

    @Shadow
    protected abstract void refreshTrimmedMessages();

    @Inject(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="HEAD")})
    private void bloodhelper$captureOriginal(Component content, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        this.bloodhelper$original = content;
        this.bloodhelper$prepared = null;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != this.bloodhelper$connection) {
            this.bloodhelper$groups.clear();
            this.bloodhelper$connection = connection;
        }
    }

    @ModifyArgs(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")})
    private void bloodhelper$displayMessage(Args arguments) {
        Component display = ChatTools.receiveForDisplay((Component)arguments.get(1), (MessageSignature)arguments.get(2), arguments.get(4));
        ClientConfig.ChatToolsSettings settings = BloodHelperClient.config().chatTools;
        if (ExternalChatPatches.loaded() || !settings.enabled || !settings.groupDuplicates || this.bloodhelper$connection == null) {
            this.bloodhelper$groups.clear();
            arguments.set(1, (Object)display);
            return;
        }
        GuiMessage incoming = new GuiMessage(((Integer)arguments.get(0)).intValue(), display, (MessageSignature)arguments.get(2), (GuiMessageSource)arguments.get(3), (GuiMessageTag)arguments.get(4));
        ChatMessageGroups.Result result = this.bloodhelper$groups.accept(this.bloodhelper$original, incoming, this.allMessages.isEmpty() ? null : this.allMessages.getFirst(), true);
        if (result.replacesPrevious()) {
            this.allMessages.removeFirst();
            int removed = 0;
            if (!this.trimmedMessages.isEmpty()) {
                this.trimmedMessages.removeFirst();
                ++removed;
                while (!this.trimmedMessages.isEmpty() && !this.trimmedMessages.getFirst().endOfEntry()) {
                    this.trimmedMessages.removeFirst();
                    ++removed;
                }
            }
            this.chatScrollbarPos = Math.max(0, this.chatScrollbarPos - removed);
        }
        arguments.set(1, (Object)result.message().content());
        this.bloodhelper$prepared = result.message().content();
    }

    @Inject(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="TAIL")})
    private void bloodhelper$bindStoredMessage(Component content, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        if (this.bloodhelper$prepared != null && !this.allMessages.isEmpty()) {
            this.bloodhelper$groups.rebind(this.bloodhelper$prepared, this.allMessages.getFirst());
        }
        this.bloodhelper$prepared = null;
    }

    @Inject(method={"deleteMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$deleteMessage(MessageSignature signature, CallbackInfo callback) {
        ChatTools.delete(signature);
        if (this.bloodhelper$groups.delete(signature, this.allMessages)) {
            this.refreshTrimmedMessages();
            callback.cancel();
        }
    }

    @Inject(method={"clearMessages"}, at={@At(value="HEAD")})
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
        for (int i = 0; i < this.allMessages.size(); ++i) {
            GuiMessage old = this.allMessages.get(i);
            Component next = ChatTools.nicknameLinks(old.content());
            if (next == old.content()) continue;
            GuiMessage replacement = new GuiMessage(old.addedTime(), next, old.signature(), old.source(), old.tag());
            this.allMessages.set(i, replacement);
            this.bloodhelper$groups.rebind(old.content(), replacement);
            changed = true;
        }
        if (changed) {
            this.refreshTrimmedMessages();
        }
    }
}

