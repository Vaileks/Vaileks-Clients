/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.ChatScreen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.mixin.ChatDraftEditBoxAccessor;
import ru.bloodhelper.client.modules.ChatDraftState;
import ru.bloodhelper.client.modules.ChatDraftsModule;

@Mixin(value={ChatScreen.class})
public abstract class ChatDraftsMixin {
    @Shadow
    protected EditBox input;
    @Shadow
    protected String initial;
    @Shadow
    protected boolean isDraft;
    @Unique
    private boolean bloodhelper$draftInitialized;
    @Unique
    private boolean bloodhelper$commandEntry;
    @Unique
    private Object bloodhelper$draftConnection;
    @Unique
    private Object bloodhelper$draftSettings;
    @Unique
    private boolean bloodhelper$sent;
    @Unique
    private ChatDraftState.Draft bloodhelper$resizeDraft;
    @Unique
    private ChatDraftState.Draft bloodhelper$closedDraft;

    @Inject(method={"init()V"}, at={@At(value="TAIL")})
    private void bloodhelper$restoreDraft(CallbackInfo callback) {
        if (this.bloodhelper$draftInitialized) {
            if (this.bloodhelper$closedDraft != null && ChatDraftsModule.active() && this.bloodhelper$draftConnection == Minecraft.getInstance().getConnection() && this.bloodhelper$draftSettings == BloodHelperClient.config().chatDrafts && this.bloodhelper$closedDraft.text().equals(this.input.getValue())) {
                this.input.setCursorPosition(this.bloodhelper$closedDraft.cursor());
                this.input.setHighlightPos(this.bloodhelper$closedDraft.anchor());
            }
            this.bloodhelper$closedDraft = null;
            return;
        }
        this.bloodhelper$draftInitialized = true;
        this.bloodhelper$commandEntry = this.initial != null && this.initial.startsWith("/");
        this.bloodhelper$draftConnection = Minecraft.getInstance().getConnection();
        this.bloodhelper$draftSettings = BloodHelperClient.config().chatDrafts;
        ChatDraftState.Draft draft = ChatDraftsModule.restore(this.initial);
        if (draft != null) {
            this.input.setValue(draft.text());
            this.input.setCursorPosition(draft.cursor());
            this.input.setHighlightPos(draft.anchor());
            this.isDraft = false;
        }
    }

    @Inject(method={"resize"}, at={@At(value="HEAD")})
    private void bloodhelper$rememberSelection(int width, int height, CallbackInfo callback) {
        this.bloodhelper$resizeDraft = ChatDraftsModule.active() && this.input != null ? new ChatDraftState.Draft(this.input.getValue(), this.input.getCursorPosition(), ((ChatDraftEditBoxAccessor)this.input).bloodhelper$draftSelectionAnchor()) : null;
    }

    @Inject(method={"resize"}, at={@At(value="TAIL")})
    private void bloodhelper$restoreSelection(int width, int height, CallbackInfo callback) {
        if (this.bloodhelper$resizeDraft == null) {
            return;
        }
        this.input.setCursorPosition(this.bloodhelper$resizeDraft.cursor());
        this.input.setHighlightPos(this.bloodhelper$resizeDraft.anchor());
        this.bloodhelper$resizeDraft = null;
    }

    @Inject(method={"removed"}, at={@At(value="HEAD")})
    private void bloodhelper$saveDraft(CallbackInfo callback) {
        if (this.input == null || this.bloodhelper$draftSettings != BloodHelperClient.config().chatDrafts) {
            return;
        }
        this.bloodhelper$closedDraft = ChatDraftsModule.active() ? new ChatDraftState.Draft(this.input.getValue(), this.input.getCursorPosition(), ((ChatDraftEditBoxAccessor)this.input).bloodhelper$draftSelectionAnchor()) : null;
        ChatDraftsModule.closed(this.bloodhelper$draftConnection, this.input.getValue(), this.input.getCursorPosition(), ((ChatDraftEditBoxAccessor)this.input).bloodhelper$draftSelectionAnchor(), this.bloodhelper$sent, this.bloodhelper$commandEntry);
    }

    @Inject(method={"handleChatInput"}, at={@At(value="HEAD")})
    private void bloodhelper$sentDraft(String message, boolean addToHistory, CallbackInfo callback) {
        this.bloodhelper$sent = true;
    }

    @Inject(method={"shouldDiscardDraft"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$avoidDuplicateNativeDraft(CallbackInfoReturnable<Boolean> callback) {
        if (ChatDraftsModule.active()) {
            callback.setReturnValue((Object)true);
        }
    }
}

