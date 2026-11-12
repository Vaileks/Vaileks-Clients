/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.class_11909
 *  net.minecraft.class_1703
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_481
 *  net.minecraft.class_746
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.class_11909;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_746;
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
    private static class_437 selecting;
    private static final InventoryUiDrag drag;
    private static class_437 draggingScreen;
    private static long lastNotice;

    private SlotLockModule() {
    }

    public static int inventorySlot(class_1735 slot) {
        class_746 player;
        if (slot instanceof CreativeSlotTargetAccessor) {
            CreativeSlotTargetAccessor wrapped = (CreativeSlotTargetAccessor)slot;
            slot = wrapped.bloodhelper$target();
        }
        if ((player = class_310.method_1551().field_1724) == null || slot == null || slot.field_7871 != player.method_31548()) {
            return -1;
        }
        int index = slot.method_34266();
        return SlotLockSettings.valid(index) ? index : -1;
    }

    public static boolean locked(class_1735 slot) {
        return BloodHelperClient.config().slotLocks.locked(SlotLockModule.inventorySlot(slot));
    }

    public static boolean selectedLocked() {
        class_310 mc = class_310.method_1551();
        return mc.field_1724 != null && BloodHelperClient.config().slotLocks.locked(mc.field_1724.method_31548().method_67532());
    }

    public static boolean block(class_1703 menu, class_1735 slot, int button, class_1713 type) {
        class_437 class_4372;
        class_310 mc = class_310.method_1551();
        SlotLockSettings settings = BloodHelperClient.config().slotLocks;
        if (!settings.enabled || settings.slots.isEmpty() || mc.field_1724 == null) {
            return false;
        }
        boolean blocked = SlotLockModule.locked(slot);
        if (type == class_1713.field_7791) {
            blocked |= settings.locked(button);
        }
        if (type == class_1713.field_7793 && !menu.method_34255().method_7960()) {
            for (class_1735 candidate : menu.field_7761) {
                if (!SlotLockModule.locked(candidate) || !candidate.method_7681() || !class_1799.method_31577((class_1799)candidate.method_7677(), (class_1799)menu.method_34255())) continue;
                blocked = true;
                break;
            }
        }
        if ((class_4372 = mc.field_1755) instanceof class_481) {
            class_481 creative = (class_481)class_4372;
            if (type == class_1713.field_7794 && slot != null && slot == ((CreativeDestroySlotAccessor)creative).bloodhelper$destroySlot()) {
                blocked |= settings.slots.stream().anyMatch(i -> !mc.field_1724.method_31548().method_5438(i.intValue()).method_7960());
            }
        }
        if (blocked) {
            SlotLockModule.notice();
        }
        return blocked;
    }

    public static void guardSwapKey() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || !SlotLockModule.selectedLocked() && !BloodHelperClient.config().slotLocks.locked(40)) {
            return;
        }
        while (mc.field_1690.field_1831.method_1436()) {
            SlotLockModule.notice();
        }
    }

    public static void notice() {
        class_746 player = class_310.method_1551().field_1724;
        long now = System.nanoTime();
        if (player != null && (lastNotice == 0L || now - lastNotice > 900000000L)) {
            player.method_7353((class_2561)class_2561.method_43470((String)HudStyle.tr("\u0421\u043b\u043e\u0442 \u0437\u0430\u0449\u0438\u0449\u0451\u043d \u00b7 Alt + \u041b\u041a\u041c \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435 \u0441\u043d\u0438\u043c\u0430\u0435\u0442 \u0437\u0430\u043c\u043e\u043a", "Slot locked \u00b7 Alt + left click in inventory to unlock")), true);
            lastNotice = now;
        }
    }

    public static void attach(class_437 screen) {
        if (!(screen instanceof class_465)) {
            return;
        }
        class_465 container = (class_465)screen;
        ScreenMouseEvents.allowMouseClick((class_437)screen).register((owner, event) -> {
            if (SlotLockModule.isDragHandle(container, event)) {
                int[] b = SlotLockModule.hintBounds(container);
                drag.begin(BloodHelperClient.config().inventoryUi.slotLocks, event.comp_4798(), event.comp_4799(), b[0], b[1], b[2], b[3]);
                draggingScreen = screen;
                return false;
            }
            return !SlotLockModule.select(container, event);
        });
        ScreenMouseEvents.allowMouseDrag((class_437)screen).register((owner, event, dx, dy) -> {
            if (draggingScreen == screen && drag.move(event.comp_4798(), event.comp_4799(), screen.field_22789, screen.field_22790)) {
                return false;
            }
            return selecting != screen;
        });
        ScreenMouseEvents.allowMouseRelease((class_437)screen).register((owner, event) -> {
            if (draggingScreen == screen && event.method_74245() == 0 && drag.end()) {
                draggingScreen = null;
                BloodHelperClient.saveConfig();
                return false;
            }
            if (selecting == screen && event.method_74245() == 0) {
                selecting = null;
                return false;
            }
            return true;
        });
        ScreenEvents.remove((class_437)screen).register(owner -> {
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
        ScreenEvents.afterRender((class_437)screen).register((owner, graphics, x, y, delta) -> SlotLockModule.hint(container, graphics));
    }

    public static boolean select(class_465<?> screen, class_11909 event) {
        if (!BloodHelperClient.config().slotLocks.enabled || event.method_74245() != 0 || !event.method_74238()) {
            return false;
        }
        ContainerBoundsAccessor bounds = (ContainerBoundsAccessor)screen;
        double x = event.comp_4798() - (double)bounds.bloodhelper$left();
        double y = event.comp_4799() - (double)bounds.bloodhelper$top();
        for (class_1735 slot : screen.method_17577().field_7761) {
            if (!slot.method_7682() || !(x >= (double)slot.field_7873) || !(x < (double)(slot.field_7873 + 16)) || !(y >= (double)slot.field_7872) || !(y < (double)(slot.field_7872 + 16))) continue;
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

    public static void renderSlot(class_332 g, class_1735 slot) {
        if (!slot.method_7682() || !SlotLockModule.locked(slot)) {
            return;
        }
        int x = slot.field_7873;
        int y = slot.field_7872;
        int gold = -12688;
        g.method_25294(x, y, x + 16, y + 1, gold);
        g.method_25294(x, y + 15, x + 16, y + 16, gold);
        g.method_25294(x, y, x + 1, y + 16, gold);
        g.method_25294(x + 15, y, x + 16, y + 16, gold);
        g.method_25294(x + 10, y + 1, x + 16, y + 8, -450747864);
        g.method_25294(x + 12, y + 2, x + 15, y + 3, gold);
        g.method_25294(x + 11, y + 3, x + 12, y + 5, gold);
        g.method_25294(x + 14, y + 3, x + 15, y + 5, gold);
        g.method_25294(x + 11, y + 5, x + 15, y + 8, gold);
    }

    private static void hint(class_465<?> screen, class_332 g) {
        if (!BloodHelperClient.config().slotLocks.enabled || !BloodHelperClient.config().slotLocks.showHint) {
            return;
        }
        g.method_71048();
        String text = HudStyle.tr("Alt + \u041b\u041a\u041c \u043f\u043e \u0441\u043b\u043e\u0442\u0443 \u2014 \u0437\u0430\u043c\u043e\u043a", "Alt + left click a slot to lock");
        int[] b = SlotLockModule.hintBounds(screen);
        int x = b[0];
        int y = b[1];
        LiquidGlass.surfaceMenu(g, x, y, b[2], b[3], 4, ThemeColors.surface(-568780515), BloodHelperClient.config());
        CustomFont.regular().draw(g, text, x + 7, y + 3, 9.0f, -12688);
        for (int d = 3; d <= 9; d += 3) {
            g.method_25294(x + 2, y + d, x + 3, y + d + 1, -5597847);
            g.method_25294(x + b[2] - 3, y + d, x + b[2] - 2, y + d + 1, -5597847);
        }
    }

    public static int[] tooltipHintBounds(class_437 screen) {
        int[] nArray;
        if (BloodHelperClient.config().slotLocks.enabled && BloodHelperClient.config().slotLocks.showHint && screen instanceof class_465) {
            class_465 container = (class_465)screen;
            nArray = SlotLockModule.hintBounds(container);
        } else {
            nArray = null;
        }
        return nArray;
    }

    private static int[] hintBounds(class_465<?> screen) {
        ContainerBoundsAccessor b = (ContainerBoundsAccessor)screen;
        int width = (int)Math.ceil(CustomFont.regular().width(HudStyle.tr("Alt + \u041b\u041a\u041c \u043f\u043e \u0441\u043b\u043e\u0442\u0443 \u2014 \u0437\u0430\u043c\u043e\u043a", "Alt + left click a slot to lock"), 9.0f)) + 14;
        InventoryUiSettings.Position p = BloodHelperClient.config().inventoryUi.slotLocks;
        return new int[]{p.x(b.bloodhelper$left() - 3, screen.field_22789, width), p.y(b.bloodhelper$top() + b.bloodhelper$height() + 3, screen.field_22790, 15), width, 15};
    }

    public static boolean isDragging(class_437 screen) {
        return draggingScreen == screen && drag.active();
    }

    public static boolean isDragHandle(class_465<?> screen, class_11909 event) {
        if (!BloodHelperClient.config().slotLocks.enabled || !BloodHelperClient.config().slotLocks.showHint || event.method_74245() != 0) {
            return false;
        }
        int[] b = SlotLockModule.hintBounds(screen);
        return event.comp_4798() >= (double)b[0] && event.comp_4798() < (double)(b[0] + b[2]) && event.comp_4799() >= (double)b[1] && event.comp_4799() < (double)(b[1] + b[3]) && (event.comp_4798() < (double)(b[0] + 5) || event.comp_4798() >= (double)(b[0] + b[2] - 5));
    }

    static {
        drag = new InventoryUiDrag();
    }
}

