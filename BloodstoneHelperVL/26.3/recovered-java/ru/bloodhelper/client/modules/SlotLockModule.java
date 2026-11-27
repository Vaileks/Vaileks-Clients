/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.InventoryUiSettings;
import ru.bloodhelper.client.config.SlotLockSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.inventory.InventoryUiDrag;
import ru.bloodhelper.client.mixin.ContainerBoundsAccessor;
import ru.bloodhelper.client.mixin.CreativeDestroySlotAccessor;
import ru.bloodhelper.client.mixin.CreativeSlotTargetAccessor;

public final class SlotLockModule {
    private static Screen selecting;
    private static final InventoryUiDrag drag;
    private static Screen draggingScreen;
    private static long lastNotice;

    private SlotLockModule() {
    }

    public static int inventorySlot(Slot slot) {
        LocalPlayer player;
        if (slot instanceof CreativeSlotTargetAccessor) {
            CreativeSlotTargetAccessor wrapped = (CreativeSlotTargetAccessor)slot;
            slot = wrapped.bloodhelper$target();
        }
        if ((player = Minecraft.getInstance().player) == null || slot == null || slot.container != player.getInventory()) {
            return -1;
        }
        int index = slot.getContainerSlot();
        return SlotLockSettings.valid(index) ? index : -1;
    }

    public static boolean locked(Slot slot) {
        return BloodHelperClient.config().slotLocks.locked(SlotLockModule.inventorySlot(slot));
    }

    public static boolean selectedLocked() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && BloodHelperClient.config().slotLocks.locked(mc.player.getInventory().getSelectedSlot());
    }

    public static boolean block(AbstractContainerMenu menu, Slot slot, int button, ContainerInput type) {
        Screen screen;
        Minecraft mc = Minecraft.getInstance();
        SlotLockSettings settings = BloodHelperClient.config().slotLocks;
        if (!settings.enabled || settings.slots.isEmpty() || mc.player == null) {
            return false;
        }
        boolean blocked = SlotLockModule.locked(slot);
        if (type == ContainerInput.SWAP) {
            blocked |= settings.locked(button);
        }
        if (type == ContainerInput.PICKUP_ALL && !menu.getCarried().isEmpty()) {
            for (Slot candidate : menu.slots) {
                if (!SlotLockModule.locked(candidate) || !candidate.hasItem() || !ItemStack.isSameItemSameComponents((ItemStack)candidate.getItem(), (ItemStack)menu.getCarried())) continue;
                blocked = true;
                break;
            }
        }
        if ((screen = mc.gui.screen()) instanceof CreativeModeInventoryScreen) {
            CreativeModeInventoryScreen creative = (CreativeModeInventoryScreen)screen;
            if (type == ContainerInput.QUICK_MOVE && slot != null && slot == ((CreativeDestroySlotAccessor)creative).bloodhelper$destroySlot()) {
                blocked |= settings.slots.stream().anyMatch(i -> !mc.player.getInventory().getItem(i.intValue()).isEmpty());
            }
        }
        if (blocked) {
            SlotLockModule.notice();
        }
        return blocked;
    }

    public static void guardSwapKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !SlotLockModule.selectedLocked() && !BloodHelperClient.config().slotLocks.locked(40)) {
            return;
        }
        while (mc.options.keySwapOffhand.consumeClick()) {
            SlotLockModule.notice();
        }
    }

    public static void notice() {
        LocalPlayer player = Minecraft.getInstance().player;
        long now = System.nanoTime();
        if (player != null && (lastNotice == 0L || now - lastNotice > 900000000L)) {
            player.sendOverlayMessage((Component)Component.literal((String)HudStyle.tr("\u0421\u043b\u043e\u0442 \u0437\u0430\u0449\u0438\u0449\u0451\u043d \u00b7 Alt + \u041b\u041a\u041c \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435 \u0441\u043d\u0438\u043c\u0430\u0435\u0442 \u0437\u0430\u043c\u043e\u043a", "Slot locked \u00b7 Alt + left click in inventory to unlock")));
            lastNotice = now;
        }
    }

    public static void attach(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen)) {
            return;
        }
        AbstractContainerScreen container = (AbstractContainerScreen)screen;
        ScreenMouseEvents.allowMouseClick((Screen)screen).register((owner, event) -> {
            if (SlotLockModule.isDragHandle(container, event)) {
                int[] b = SlotLockModule.hintBounds(container);
                drag.begin(BloodHelperClient.config().inventoryUi.slotLocks, event.x(), event.y(), b[0], b[1], b[2], b[3]);
                draggingScreen = screen;
                return false;
            }
            return !SlotLockModule.select(container, event);
        });
        ScreenMouseEvents.allowMouseDrag((Screen)screen).register((owner, event, dx, dy) -> {
            if (draggingScreen == screen && drag.move(event.x(), event.y(), screen.width, screen.height)) {
                return false;
            }
            return selecting != screen;
        });
        ScreenMouseEvents.allowMouseRelease((Screen)screen).register((owner, event) -> {
            if (draggingScreen == screen && event.button() == 1 && drag.end()) {
                draggingScreen = null;
                BloodHelperClient.saveConfig();
                return false;
            }
            if (selecting == screen && event.button() == 1) {
                selecting = null;
                return false;
            }
            return true;
        });
        ScreenEvents.remove((Screen)screen).register(owner -> {
            if (selecting == screen) {
                selecting = null;
            }
            if (draggingScreen == screen) {
                if (drag.end()) {
                    BloodHelperClient.saveConfig();
                }
                draggingScreen = null;
            }
        });
        ScreenEvents.afterExtract((Screen)screen).register((owner, graphics, x, y, delta) -> SlotLockModule.hint(container, graphics));
    }

    public static boolean select(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!BloodHelperClient.config().slotLocks.enabled || event.button() != 1 || !event.hasAltDown()) {
            return false;
        }
        ContainerBoundsAccessor bounds = (ContainerBoundsAccessor)screen;
        double x = event.x() - (double)bounds.bloodhelper$left();
        double y = event.y() - (double)bounds.bloodhelper$top();
        for (Slot slot : screen.getMenu().slots) {
            if (!slot.isActive() || !(x >= (double)slot.x) || !(x < (double)(slot.x + 16)) || !(y >= (double)slot.y) || !(y < (double)(slot.y + 16))) continue;
            int index = SlotLockModule.inventorySlot(slot);
            if (index < 0) {
                return false;
            }
            BloodHelperClient.config().slotLocks.toggle(index);
            BloodHelperClient.saveConfig();
            selecting = screen;
            return true;
        }
        return false;
    }

    public static void renderSlot(GuiGraphicsExtractor g, Slot slot) {
        if (!slot.isActive() || !SlotLockModule.locked(slot)) {
            return;
        }
        int x = slot.x;
        int y = slot.y;
        int gold = -12688;
        g.fill(x, y, x + 16, y + 1, gold);
        g.fill(x, y + 15, x + 16, y + 16, gold);
        g.fill(x, y, x + 1, y + 16, gold);
        g.fill(x + 15, y, x + 16, y + 16, gold);
        g.fill(x + 10, y + 1, x + 16, y + 8, -450747864);
        g.fill(x + 12, y + 2, x + 15, y + 3, gold);
        g.fill(x + 11, y + 3, x + 12, y + 5, gold);
        g.fill(x + 14, y + 3, x + 15, y + 5, gold);
        g.fill(x + 11, y + 5, x + 15, y + 8, gold);
    }

    private static void hint(AbstractContainerScreen<?> screen, GuiGraphicsExtractor g) {
        if (!BloodHelperClient.config().slotLocks.enabled || !BloodHelperClient.config().slotLocks.showHint) {
            return;
        }
        g.nextStratum();
        String text = HudStyle.tr("Alt + \u041b\u041a\u041c \u043f\u043e \u0441\u043b\u043e\u0442\u0443 \u2014 \u0437\u0430\u043c\u043e\u043a", "Alt + left click a slot to lock");
        int[] b = SlotLockModule.hintBounds(screen);
        int x = b[0];
        int y = b[1];
        LiquidGlass.surfaceMenu(g, x, y, b[2], b[3], 4, ThemeColors.surface(-568780515), BloodHelperClient.config());
        CustomFont.regular().draw(g, text, x + 7, y + 3, 9.0f, -12688);
        for (int d = 3; d <= 9; d += 3) {
            g.fill(x + 2, y + d, x + 3, y + d + 1, -5597847);
            g.fill(x + b[2] - 3, y + d, x + b[2] - 2, y + d + 1, -5597847);
        }
    }

    public static int[] tooltipHintBounds(Screen screen) {
        int[] nArray;
        if (BloodHelperClient.config().slotLocks.enabled && BloodHelperClient.config().slotLocks.showHint && screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen container = (AbstractContainerScreen)screen;
            nArray = SlotLockModule.hintBounds(container);
        } else {
            nArray = null;
        }
        return nArray;
    }

    private static int[] hintBounds(AbstractContainerScreen<?> screen) {
        ContainerBoundsAccessor b = (ContainerBoundsAccessor)screen;
        int width = (int)Math.ceil(CustomFont.regular().width(HudStyle.tr("Alt + \u041b\u041a\u041c \u043f\u043e \u0441\u043b\u043e\u0442\u0443 \u2014 \u0437\u0430\u043c\u043e\u043a", "Alt + left click a slot to lock"), 9.0f)) + 14;
        InventoryUiSettings.Position p = BloodHelperClient.config().inventoryUi.slotLocks;
        return new int[]{p.x(b.bloodhelper$left() - 3, screen.width, width), p.y(b.bloodhelper$top() + b.bloodhelper$height() + 3, screen.height, 15), width, 15};
    }

    public static boolean isDragging(Screen screen) {
        return draggingScreen == screen && drag.active();
    }

    public static boolean isDragHandle(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!BloodHelperClient.config().slotLocks.enabled || !BloodHelperClient.config().slotLocks.showHint || event.button() != 1) {
            return false;
        }
        int[] b = SlotLockModule.hintBounds(screen);
        return event.x() >= (double)b[0] && event.x() < (double)(b[0] + b[2]) && event.y() >= (double)b[1] && event.y() < (double)(b[1] + b[3]) && (event.x() < (double)(b[0] + 5) || event.x() >= (double)(b[0] + b[2] - 5));
    }

    static {
        drag = new InventoryUiDrag();
    }
}

