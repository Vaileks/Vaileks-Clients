/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_342
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_342;
import net.minecraft.class_408;
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

@Mixin(value={class_408.class})
public abstract class ChatDraftsMixin {
    @Shadow
    protected class_342 field_2382;
    @Shadow
    protected String field_18973;
    @Shadow
    protected boolean field_62013;
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

    @Inject(method={"method_25426()V"}, at={@At(value="TAIL")})
    private void bloodhelper$restoreDraft(CallbackInfo callback) {
        if (this.bloodhelper$draftInitialized) {
            if (this.bloodhelper$closedDraft != null && ChatDraftsModule.active() && this.bloodhelper$draftConnection == class_310.method_1551().method_1562() && this.bloodhelper$draftSettings == BloodHelperClient.config().chatDrafts && this.bloodhelper$closedDraft.text().equals(this.field_2382.method_1882())) {
                this.field_2382.method_1875(this.bloodhelper$closedDraft.cursor());
                this.field_2382.method_1884(this.bloodhelper$closedDraft.anchor());
            }
            this.bloodhelper$closedDraft = null;
            return;
        }
        this.bloodhelper$draftInitialized = true;
        this.bloodhelper$commandEntry = this.field_18973 != null && this.field_18973.startsWith("/");
        this.bloodhelper$draftConnection = class_310.method_1551().method_1562();
        this.bloodhelper$draftSettings = BloodHelperClient.config().chatDrafts;
        ChatDraftState.Draft draft = ChatDraftsModule.restore(this.field_18973);
        if (draft != null) {
            this.field_2382.method_1852(draft.text());
            this.field_2382.method_1875(draft.cursor());
            this.field_2382.method_1884(draft.anchor());
            this.field_62013 = false;
        }
    }

    @Inject(method={"method_25410"}, at={@At(value="HEAD")})
    private void bloodhelper$rememberSelection(int width, int height, CallbackInfo callback) {
        this.bloodhelper$resizeDraft = ChatDraftsModule.active() && this.field_2382 != null ? new ChatDraftState.Draft(this.field_2382.method_1882(), this.field_2382.method_1881(), ((ChatDraftEditBoxAccessor)this.field_2382).bloodhelper$draftSelectionAnchor()) : null;
    }

    @Inject(method={"method_25410"}, at={@At(value="TAIL")})
    private void bloodhelper$restoreSelection(int width, int height, CallbackInfo callback) {
        if (this.bloodhelper$resizeDraft == null) {
            return;
        }
        this.field_2382.method_1875(this.bloodhelper$resizeDraft.cursor());
        this.field_2382.method_1884(this.bloodhelper$resizeDraft.anchor());
        this.bloodhelper$resizeDraft = null;
    }

    @Inject(method={"method_25432"}, at={@At(value="HEAD")})
    private void bloodhelper$saveDraft(CallbackInfo callback) {
        if (this.field_2382 == null || this.bloodhelper$draftSettings != BloodHelperClient.config().chatDrafts) {
            return;
        }
        this.bloodhelper$closedDraft = ChatDraftsModule.active() ? new ChatDraftState.Draft(this.field_2382.method_1882(), this.field_2382.method_1881(), ((ChatDraftEditBoxAccessor)this.field_2382).bloodhelper$draftSelectionAnchor()) : null;
        ChatDraftsModule.closed(this.bloodhelper$draftConnection, this.field_2382.method_1882(), this.field_2382.method_1881(), ((ChatDraftEditBoxAccessor)this.field_2382).bloodhelper$draftSelectionAnchor(), this.bloodhelper$sent, this.bloodhelper$commandEntry);
    }

    @Inject(method={"method_44056"}, at={@At(value="HEAD")})
    private void bloodhelper$sentDraft(String message, boolean addToHistory, CallbackInfo callback) {
        this.bloodhelper$sent = true;
    }

    @Inject(method={"method_73218"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$avoidDuplicateNativeDraft(CallbackInfoReturnable<Boolean> callback) {
        if (ChatDraftsModule.active()) {
            callback.setReturnValue((Object)true);
        }
    }
}

