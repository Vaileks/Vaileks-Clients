/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomModelData
 */
package ru.bloodhelper.client.modules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DropGuardConfirmation;
import ru.bloodhelper.client.modules.DropGuardInput;
import ru.bloodhelper.client.modules.DropGuardScreen;

public final class DropGuardModule {
    private static final DropGuardConfirmation<ItemStack> confirmation = new DropGuardConfirmation<ItemStack>(ItemStack::matches, ItemStack::copy);
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

    private static boolean active(Minecraft mc) {
        return BloodHelperClient.config().dropGuard.enabled && mc.player != null && mc.level != null && mc.player.isAlive() && !BloodHelperClient.config().dropGuard.protectedItems.isEmpty();
    }

    public static void tick(Minecraft mc) {
        if (!DropGuardModule.active(mc)) {
            DropGuardModule.reset();
            return;
        }
        DropGuardModule.observeContext(mc);
        confirmation.expire(System.nanoTime());
    }

    private static void observeContext(Minecraft mc) {
        DropGuardSettings settings = BloodHelperClient.config().dropGuard;
        String key = mc.options.keyDrop.saveString();
        int slot = mc.player.getInventory().getSelectedSlot();
        if (observedPlayer != mc.player || observedWorld != mc.level || observedScreen != mc.gui.screen() || observedMenu != mc.player.containerMenu || observedSettings != settings || observedSlot != slot || !Objects.equals(key, observedKey)) {
            confirmation.reset();
            observedPlayer = mc.player;
            observedWorld = mc.level;
            observedScreen = mc.gui.screen();
            observedMenu = mc.player.containerMenu;
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

    public static void keyInput(Minecraft mc, long window, int action, KeyEvent event) {
        if (!DropGuardModule.active(mc) || window != mc.getWindow().handle() || !mc.options.keyDrop.matches(event)) {
            return;
        }
        input.event((long)event.key() << 32 ^ (long)event.keycode() & 0xFFFFFFFFL, action);
    }

    public static void mouseInput(Minecraft mc, long window, MouseButtonInfo info, int action) {
        if (!DropGuardModule.active(mc) || window != mc.getWindow().handle()) {
            return;
        }
        if (action == 0 || mc.gui.screen() instanceof AbstractContainerScreen && (info.button() == 1 || info.button() == 3) || mc.options.keyDrop.matchesMouse(new MouseButtonEvent(0.0, 0.0, info))) {
            input.event(Long.MIN_VALUE + (long)info.button(), action);
        }
    }

    public static boolean blockSelectedDrop(Minecraft mc, boolean wholeStack) {
        if (!DropGuardModule.active(mc)) {
            return false;
        }
        return DropGuardModule.block(mc, mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot(), wholeStack ? 1 : 0, null);
    }

    public static boolean blockContainerDrop(Minecraft mc, AbstractContainerMenu menu, Slot slot, int slotId, int button, ContainerInput type) {
        if (!DropGuardModule.active(mc)) {
            return false;
        }
        if (type == ContainerInput.THROW && slot != null && slot.hasItem()) {
            return DropGuardModule.block(mc, slot.getItem(), slotId, 10 + button, menu);
        }
        if (!(slotId != -999 || type != ContainerInput.PICKUP && type != ContainerInput.THROW || menu.getCarried().isEmpty())) {
            return DropGuardModule.block(mc, menu.getCarried(), -999, 20 + button, menu);
        }
        return false;
    }

    private static boolean block(Minecraft mc, ItemStack stack, int slot, int operation, Object menu) {
        DropGuardModule.observeContext(mc);
        if (!DropGuardModule.isProtected(stack)) {
            confirmation.reset();
            return false;
        }
        long now = System.nanoTime();
        Context context = new Context(mc.player, mc.level, mc.gui.screen(), menu, BloodHelperClient.config().dropGuard, slot, operation);
        if (confirmation.confirm(context, stack, input.token(), now)) {
            return false;
        }
        if (lastNotice == Long.MIN_VALUE || now - lastNotice > 700000000L) {
            String action = slot == -999 ? HudStyle.tr("\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u0435 \u0449\u0435\u043b\u0447\u043e\u043a", "Click again") : HudStyle.tr("\u041d\u0430\u0436\u043c\u0438\u0442\u0435 ", "Press ") + mc.options.keyDrop.getTranslatedKeyMessage().getString() + HudStyle.tr(" \u0435\u0449\u0451 \u0440\u0430\u0437", " again");
            mc.player.sendOverlayMessage((Component)Component.literal((String)(HudStyle.tr("Drop Guard: \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0437\u0430\u0449\u0438\u0449\u0451\u043d. ", "Drop Guard: item protected. ") + action + HudStyle.tr(" \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0435 2 \u0441 \u0434\u043b\u044f \u0432\u044b\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u043d\u0438\u044f.", " within 2s to drop."))));
            lastNotice = now;
        }
        return true;
    }

    public static boolean isProtected(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        DropGuardSettings.Entry entry = DropGuardModule.capture(stack);
        return BloodHelperClient.config().dropGuard.protectedItems.stream().anyMatch(e -> e.key().equals(entry.key()));
    }

    public static DropGuardSettings.Entry capture(ItemStack stack) {
        Component customName = (Component)stack.get(DataComponents.CUSTOM_NAME);
        Identifier model = (Identifier)stack.get(DataComponents.ITEM_MODEL);
        CustomModelData customModel = (CustomModelData)stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return new DropGuardSettings.Entry(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString(), DropGuardSettings.clean(UiLanguage.text(stack.getHoverName()), 160), DropGuardSettings.clean(customName == null ? "" : customName.getString(), 512), model == null ? "" : model.toString(), customModel == null ? "" : DropGuardModule.digest(customModel.toString()));
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean add(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
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

    public static void openEditor(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new DropGuardScreen(parent));
    }

    static {
        observedSlot = -1;
        lastNotice = Long.MIN_VALUE;
    }

    private record Context(Object player, Object world, Object screen, Object menu, Object settings, int slot, int operation) {
    }
}

