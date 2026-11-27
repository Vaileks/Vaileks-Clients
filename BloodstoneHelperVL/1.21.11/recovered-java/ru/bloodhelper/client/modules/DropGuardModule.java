/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_11910
 *  net.minecraft.class_1703
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_7923
 *  net.minecraft.class_9280
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_7923;
import net.minecraft.class_9280;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DropGuardConfirmation;
import ru.bloodhelper.client.modules.DropGuardInput;
import ru.bloodhelper.client.modules.DropGuardScreen;

public final class DropGuardModule {
    private static final DropGuardConfirmation<class_1799> confirmation = new DropGuardConfirmation<class_1799>(class_1799::method_7973, class_1799::method_7972);
    private static final DropGuardInput input = new DropGuardInput();
    private static Object observedPlayer;
    private static Object observedWorld;
    private static Object observedScreen;
    private static Object observedMenu;
    private static Object observedSettings;
    private static int observedSlot;
    private static String observedKey;
    private static long lastNotice;

    private DropGuardModule() {
    }

    private static boolean active(class_310 mc) {
        return BloodHelperClient.config().dropGuard.enabled && mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_5805() && !BloodHelperClient.config().dropGuard.protectedItems.isEmpty();
    }

    public static void tick(class_310 mc) {
        if (!DropGuardModule.active(mc)) {
            DropGuardModule.reset();
            return;
        }
        DropGuardModule.observeContext(mc);
        confirmation.expire(System.nanoTime());
    }

    private static void observeContext(class_310 mc) {
        DropGuardSettings settings = BloodHelperClient.config().dropGuard;
        String key = mc.field_1690.field_1869.method_1428();
        int slot = mc.field_1724.method_31548().method_67532();
        if (observedPlayer != mc.field_1724 || observedWorld != mc.field_1687 || observedScreen != mc.field_1755 || observedMenu != mc.field_1724.field_7512 || observedSettings != settings || observedSlot != slot || !Objects.equals(key, observedKey)) {
            confirmation.reset();
            observedPlayer = mc.field_1724;
            observedWorld = mc.field_1687;
            observedScreen = mc.field_1755;
            observedMenu = mc.field_1724.field_7512;
            observedSettings = settings;
            observedSlot = slot;
            observedKey = key;
        }
    }

    public static void reset() {
        confirmation.reset();
        input.reset();
        observedSettings = null;
        observedMenu = null;
        observedScreen = null;
        observedWorld = null;
        observedPlayer = null;
        observedSlot = -1;
        observedKey = null;
        lastNotice = Long.MIN_VALUE;
    }

    public static void keyInput(class_310 mc, long window, int action, class_11908 event) {
        if (!DropGuardModule.active(mc) || window != mc.method_22683().method_4490() || !mc.field_1690.field_1869.method_1417(event)) {
            return;
        }
        input.event((long)event.comp_4795() << 32 ^ (long)event.comp_4796() & 0xFFFFFFFFL, action);
    }

    public static void mouseInput(class_310 mc, long window, class_11910 info, int action) {
        if (!DropGuardModule.active(mc) || window != mc.method_22683().method_4490()) {
            return;
        }
        if (action == 0 || mc.field_1755 instanceof class_465 && (info.comp_4801() == 0 || info.comp_4801() == 1) || mc.field_1690.field_1869.method_1433(new class_11909(0.0, 0.0, info))) {
            input.event(Long.MIN_VALUE + (long)info.comp_4801(), action);
        }
    }

    public static boolean blockSelectedDrop(class_310 mc, boolean wholeStack) {
        if (!DropGuardModule.active(mc)) {
            return false;
        }
        return DropGuardModule.block(mc, mc.field_1724.method_6047(), mc.field_1724.method_31548().method_67532(), wholeStack ? 1 : 0, null);
    }

    public static boolean blockContainerDrop(class_310 mc, class_1703 menu, class_1735 slot, int slotId, int button, class_1713 type) {
        if (!DropGuardModule.active(mc)) {
            return false;
        }
        if (type == class_1713.field_7795 && slot != null && slot.method_7681()) {
            return DropGuardModule.block(mc, slot.method_7677(), slotId, 10 + button, menu);
        }
        if (!(slotId != -999 || type != class_1713.field_7790 && type != class_1713.field_7795 || menu.method_34255().method_7960())) {
            return DropGuardModule.block(mc, menu.method_34255(), -999, 20 + button, menu);
        }
        return false;
    }

    private static boolean block(class_310 mc, class_1799 stack, int slot, int operation, Object menu) {
        DropGuardModule.observeContext(mc);
        if (!DropGuardModule.isProtected(stack)) {
            confirmation.reset();
            return false;
        }
        long now = System.nanoTime();
        Context context = new Context(mc.field_1724, mc.field_1687, mc.field_1755, menu, BloodHelperClient.config().dropGuard, slot, operation);
        if (confirmation.confirm(context, stack, input.token(), now)) {
            return false;
        }
        if (lastNotice == Long.MIN_VALUE || now - lastNotice > 700000000L) {
            String action = slot == -999 ? HudStyle.tr("\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u0435 \u0449\u0435\u043b\u0447\u043e\u043a", "Click again") : HudStyle.tr("\u041d\u0430\u0436\u043c\u0438\u0442\u0435 ", "Press ") + mc.field_1690.field_1869.method_16007().getString() + HudStyle.tr(" \u0435\u0449\u0451 \u0440\u0430\u0437", " again");
            mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)(HudStyle.tr("Drop Guard: \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0437\u0430\u0449\u0438\u0449\u0451\u043d. ", "Drop Guard: item protected. ") + action + HudStyle.tr(" \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0435 2 \u0441 \u0434\u043b\u044f \u0432\u044b\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u043d\u0438\u044f.", " within 2s to drop."))), true);
            lastNotice = now;
        }
        return true;
    }

    public static boolean isProtected(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        DropGuardSettings.Entry entry = DropGuardModule.capture(stack);
        return BloodHelperClient.config().dropGuard.protectedItems.stream().anyMatch(e -> e.key().equals(entry.key()));
    }

    public static DropGuardSettings.Entry capture(class_1799 stack) {
        class_2561 customName = (class_2561)stack.method_58694(class_9334.field_49631);
        class_2960 model = (class_2960)stack.method_58694(class_9334.field_54199);
        class_9280 customModel = (class_9280)stack.method_58694(class_9334.field_49637);
        return new DropGuardSettings.Entry(class_7923.field_41178.method_10221((Object)stack.method_7909()).toString(), DropGuardSettings.clean(UiLanguage.text(stack.method_7964()), 160), DropGuardSettings.clean(customName == null ? "" : customName.getString(), 512), model == null ? "" : model.toString(), customModel == null ? "" : DropGuardModule.digest(customModel.toString()));
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean add(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        DropGuardSettings settings = BloodHelperClient.config().dropGuard;
        DropGuardSettings.Entry entry = DropGuardModule.capture(stack);
        if (settings.protectedItems.size() >= 128 || settings.protectedItems.stream().anyMatch(e -> e.key().equals(entry.key()))) {
            return false;
        }
        settings.protectedItems.add(entry);
        confirmation.reset();
        BloodHelperClient.saveConfig();
        return true;
    }

    public static void openEditor(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new DropGuardScreen(parent));
    }

    static {
        observedSlot = -1;
        lastNotice = Long.MIN_VALUE;
    }

    private record Context(Object player, Object world, Object screen, Object menu, Object settings, int slot, int operation) {
    }
}

