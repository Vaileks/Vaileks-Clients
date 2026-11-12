/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.policy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Predicate;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.policy.ModulePolicy;
import ru.bloodhelper.client.policy.ServerModuleRule;
import ru.bloodhelper.client.vl.VLRestrictions;

public final class RuntimeConfig {
    private RuntimeConfig() {
    }

    public static ClientConfig restrict(ClientConfig saved, ModulePolicy policy, boolean fallback) {
        ClientConfig view = new ClientConfig();
        RuntimeConfig.refresh(view, saved, policy, fallback);
        return view;
    }

    public static void refresh(ClientConfig view, ClientConfig saved, ModulePolicy policy, boolean fallback) {
        RuntimeConfig.refresh(view, saved, policy, fallback, null, Map.of());
        VLRestrictions.enforce(view);
    }

    public static void refresh(ClientConfig view, ClientConfig saved, ModulePolicy policy, boolean fallback, String serverAddress, Map<String, ServerModuleRule> serverRules) {
        RuntimeConfig.refresh(view, saved, policy, fallback, serverAddress, serverRules, id -> true, false);
        VLRestrictions.enforce(view);
    }

    public static void refresh(ClientConfig view, ClientConfig saved, ModulePolicy policy, boolean fallback, String serverAddress, Map<String, ServerModuleRule> serverRules, Predicate<String> ownerAllows, boolean clientDisabled) {
        RuntimeConfig.copyFields(saved, view);
        view.serverSafeMode = saved.serverSafeMode || fallback || policy != null && policy.safeMode();
        try {
            for (String id : ModulePolicy.MODULES) {
                boolean allowed;
                boolean bl = policy == null ? !view.serverSafeMode || !ModulePolicy.SAFE_BLOCKS.contains(id) : (allowed = policy.allows(id, saved.serverSafeMode));
                if (allowed &= ServerModuleRule.allows(serverRules, id, serverAddress) && !clientDisabled && ownerAllows.test(id)) continue;
                String path = ModulePolicy.settingPath(id);
                if (path.contains(".")) {
                    RuntimeConfig.disablePath(view, path.split("\\."), 0);
                    continue;
                }
                Field field = ClientConfig.class.getField(id);
                if (field.getType() == Boolean.TYPE) {
                    field.setBoolean(view, false);
                    continue;
                }
                Object settings = RuntimeConfig.shallowCopy(field.get(saved));
                settings.getClass().getField("enabled").setBoolean(settings, false);
                field.set(view, settings);
            }
        }
        catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Module catalog/config mismatch", error);
        }
        if (clientDisabled) {
            view.reglassEnabled = false;
            view.reglassFullGui = false;
            view.glassBlur = 0.0;
            view.surfaceStyle = "MATTE";
            view.hudMenuEffects = false;
            view.hudEditor = RuntimeConfig.shallowCopy(saved.hudEditor);
            view.hudEditor.showChatEditor = false;
            view.hudEditor.chatEditingEnabled = false;
            view.combatTimeline = RuntimeConfig.shallowCopy(saved.combatTimeline);
            view.combatTimeline.enabled = false;
        }
        view.screenshotAutoCopy = false;
        VLRestrictions.enforce(view);
    }

    private static void disablePath(Object owner, String[] path, int index) throws ReflectiveOperationException {
        Field field = owner.getClass().getField(path[index]);
        if (index == path.length - 1) {
            field.setBoolean(owner, false);
            return;
        }
        Object copy = RuntimeConfig.shallowCopy(field.get(owner));
        field.set(owner, copy);
        RuntimeConfig.disablePath(copy, path, index + 1);
    }

    private static void copyFields(Object source, Object target) {
        try {
            for (Field field : source.getClass().getFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
                field.set(target, field.get(source));
            }
        }
        catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot refresh runtime settings", error);
        }
    }

    private static <T> T shallowCopy(T source) {
        try {
            Object copy = source.getClass().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            RuntimeConfig.copyFields(source, copy);
            return (T)copy;
        }
        catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot create runtime settings", error);
        }
    }
}

