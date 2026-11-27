/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.toasts.SystemToast
 *  net.minecraft.client.gui.components.toasts.SystemToast$SystemToastId
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.gui.components.toasts.ToastManager
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.assistant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.assistant.AssistantAssets;
import ru.bloodhelper.client.assistant.AssistantService;

public final class AssistantNotice {
    private static final SystemToast.SystemToastId ID = new SystemToast.SystemToastId(14000L);
    private static boolean active;
    private static boolean pending;

    private AssistantNotice() {
    }

    public static void tick(Minecraft client, boolean enabled, boolean russian) {
        Object body;
        if (!enabled) {
            active = false;
            pending = false;
            return;
        }
        if (!active) {
            active = true;
            pending = true;
        }
        AssistantService.Snapshot state = AssistantService.get().snapshot();
        if (!pending || state.stage() == AssistantService.Stage.CHECKING) {
            return;
        }
        pending = false;
        if (state.stage() == AssistantService.Stage.UNSUPPORTED) {
            body = russian ? "\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u0418\u0418 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 Windows x64 \u0438 Linux x64. \u0412\u0441\u0442\u0440\u043e\u0435\u043d\u043d\u044b\u0439 \u0441\u043f\u0440\u0430\u0432\u043e\u0447\u043d\u0438\u043a \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0431\u0435\u0437 \u043c\u043e\u0434\u0435\u043b\u0438." : "Local AI requires Windows x64 or Linux x64. Built-in help works without the model.";
        } else {
            body = state.installed() ? (russian ? "\u041c\u043e\u0434\u0435\u043b\u044c \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430. " : "Model installed. ") : (russian ? "\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b \u0441\u043a\u0430\u0447\u0430\u0439 \u043c\u043e\u0434\u0435\u043b\u044c \u0447\u0435\u0440\u0435\u0437 \u00ab\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0418\u0418\u00bb \u0432 \u043f\u0430\u043d\u0435\u043b\u0438. " : "Download the model with Install AI in the panel. ");
            body = (String)body + AssistantAssets.requirements(russian);
            body = (String)body + (russian ? " \u0417\u0430\u043a\u0440\u044b\u0442\u0438\u0435 \u043c\u0435\u043d\u044e \u043d\u0435 \u043f\u0440\u0435\u0440\u044b\u0432\u0430\u0435\u0442 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0443. \u041c\u043e\u0434\u0435\u043b\u044c \u043e\u0441\u0442\u0430\u0451\u0442\u0441\u044f \u0432 \u041e\u0417\u0423 \u0434\u043e \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u043c\u043e\u0434\u0443\u043b\u044f." : " Closing the menu keeps downloads running. The model stays in RAM until you disable the module.");
        }
        SystemToast.forceHide((ToastManager)client.gui.toastManager(), (SystemToast.SystemToastId)ID);
        client.gui.toastManager().addToast((Toast)new SystemToast(ID, (Component)Component.literal((String)(russian ? "\u0418\u0418-\u043f\u043e\u043c\u043e\u0449\u043d\u0438\u043a BloodHelper" : "BloodHelper AI assistant")), (Component)Component.literal((String)body)));
    }
}

