/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_481
 *  net.minecraft.class_490
 *  net.minecraft.class_6903
 *  net.minecraft.class_746
 */
package ru.bloodhelper.client.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_6903;
import net.minecraft.class_746;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.InventoryUiSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ReGlassCompat;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.inventory.InventoryLayoutPlan;
import ru.bloodhelper.client.inventory.InventoryLayoutStore;
import ru.bloodhelper.client.inventory.InventoryOverlayPlacement;
import ru.bloodhelper.client.inventory.InventoryUiDrag;
import ru.bloodhelper.client.mixin.ContainerBoundsAccessor;
import ru.bloodhelper.client.modules.SlotLockModule;

public final class InventoryProfiles {
    private static State active;
    private static Job job;

    private InventoryProfiles() {
    }

    public static void attach(class_437 screen) {
        State s;
        if (!BloodHelperClient.config().inventoryProfiles || !(screen instanceof class_490) && !(screen instanceof class_481)) {
            return;
        }
        class_465 container = (class_465)screen;
        active = s = active != null && InventoryProfiles.active.screen == screen ? active : new State(container);
        ScreenEvents.afterRender((class_437)screen).register((owner, g, x, y, delta) -> InventoryProfiles.render(s, g, x, y));
        ScreenEvents.remove((class_437)screen).register(owner -> {
            if (s.drag.end()) {
                BloodHelperClient.saveConfig();
            }
            if (active == s) {
                InventoryProfiles.reset();
            }
        });
        ScreenMouseEvents.allowMouseClick((class_437)screen).register((owner, event) -> !InventoryProfiles.click(s, event));
        ScreenMouseEvents.allowMouseScroll((class_437)screen).register((owner, x, y, horizontal, vertical) -> {
            if (!(InventoryProfiles.usable(s) && s.open && InventoryProfiles.panel(s).has(x, y))) {
                return true;
            }
            if (s.preview == null) {
                s.offset = Math.clamp((long)(s.offset - (int)Math.signum(vertical)), 0, Math.max(0, InventoryLayoutStore.profiles().size() - InventoryProfiles.rows(s)));
            }
            return false;
        });
        ScreenMouseEvents.allowMouseDrag((class_437)screen).register((owner, event, dx, dy) -> {
            if (SlotLockModule.isDragging(screen)) {
                return true;
            }
            if (s.drag.move(event.comp_4798(), event.comp_4799(), screen.field_22789, screen.field_22790)) {
                return false;
            }
            return !InventoryProfiles.usable(s) || !s.open || !s.name.mouseDragged(event, dx, dy) && !InventoryProfiles.panel(s).has(event.comp_4798(), event.comp_4799());
        });
        ScreenMouseEvents.allowMouseRelease((class_437)screen).register((owner, event) -> {
            if (SlotLockModule.isDragging(screen)) {
                return true;
            }
            if (event.method_74245() == 0 && s.drag.end()) {
                BloodHelperClient.saveConfig();
                return false;
            }
            return !InventoryProfiles.usable(s) || !s.open || !s.name.mouseReleased(event) && !InventoryProfiles.panel(s).has(event.comp_4798(), event.comp_4799());
        });
        ScreenKeyboardEvents.allowKeyPress((class_437)screen).register((owner, event) -> !InventoryProfiles.key(s, event));
    }

    private static boolean usable(State s) {
        return s != null && BloodHelperClient.config().inventoryProfiles && class_310.method_1551().field_1755 == s.screen && class_310.method_1551().field_1724 != null;
    }

    private static Box toggle(State s) {
        ContainerBoundsAccessor b = (ContainerBoundsAccessor)s.screen;
        int x = b.bloodhelper$left() + b.bloodhelper$width() + 5;
        int y = b.bloodhelper$top();
        if (x + 83 > s.screen.field_22789 - 3) {
            x = b.bloodhelper$left() + b.bloodhelper$width() - 83;
            y -= 20;
        }
        InventoryUiSettings.Position position = BloodHelperClient.config().inventoryUi.layoutsButton;
        return InventoryProfiles.avoidEffects(s, new Box(position.x(x, s.screen.field_22789, 83), position.y(y, s.screen.field_22790, 18), 83, 18));
    }

    private static Box panel(State s) {
        ContainerBoundsAccessor b = (ContainerBoundsAccessor)s.screen;
        int w = Math.min(190, s.screen.field_22789 - 8);
        int h = Math.min(244, s.screen.field_22790 - 8);
        int x = b.bloodhelper$left() + b.bloodhelper$width() + 5;
        if (x + w > s.screen.field_22789 - 4) {
            x = b.bloodhelper$left() - w - 5;
        }
        if (x < 4) {
            x = (s.screen.field_22789 - w) / 2;
        }
        InventoryUiSettings.Position position = BloodHelperClient.config().inventoryUi.layoutsPanel;
        return InventoryProfiles.avoidEffects(s, new Box(position.x(x, s.screen.field_22789, w), position.y(b.bloodhelper$top(), s.screen.field_22790, h), w, h));
    }

    private static Box avoidEffects(State s, Box box) {
        class_310 mc = class_310.method_1551();
        int effects = mc.field_1724 == null ? 0 : mc.field_1724.method_6026().size();
        ContainerBoundsAccessor b = (ContainerBoundsAccessor)s.screen;
        InventoryOverlayPlacement.Point placed = InventoryOverlayPlacement.avoidEffects(box.x, box.y, box.w, box.h, s.screen.field_22789, s.screen.field_22790, b.bloodhelper$left(), b.bloodhelper$top(), b.bloodhelper$width(), b.bloodhelper$height(), effects);
        return new Box(placed.x(), placed.y(), box.w, box.h);
    }

    private static int rows(State s) {
        return Math.max(1, (InventoryProfiles.panel((State)s).h - 134) / 22);
    }

    private static void text(class_332 g, String text, int x, int y, float size, int width, int color) {
        CustomFont font = CustomFont.regular();
        font.draw(g, font.ellipsize(text, size, Math.max(0, width)), x, y, size, color);
    }

    private static void button(class_332 g, Box b, String label, int mx, int my) {
        int accent = BloodHelperClient.config().accent;
        InventoryProfiles.surface(g, b, 4, b.has(mx, my) ? -13028806 : -216327388, accent);
        InventoryProfiles.text(g, label, b.x + 6, b.y + 5, 9.0f, b.w - 12, -725006);
    }

    private static void surface(class_332 g, Box b, int radius, int fill, int edge) {
        ClientConfig config = BloodHelperClient.config();
        LiquidGlass.surfaceMenu(g, b.x, b.y, b.w, b.h, radius, ThemeColors.surface(fill), config);
        if (ReGlassCompat.installed() && "LIQUID_GLASS".equals(config.surfaceStyle) && b.h > 100) {
            UiDraw.roundedSolid(g, b.x, b.y, b.w, b.h, radius, ThemeColors.surface(-2145377237));
        }
        if (!"OUTLINE".equals(config.surfaceStyle)) {
            UiDraw.outline(g, b.x, b.y, b.w, b.h, radius, UiDraw.alpha(edge, 160));
        }
    }

    private static void render(State s, class_332 g, int mx, int my) {
        if (!InventoryProfiles.usable(s)) {
            return;
        }
        g.method_71048();
        if (!s.open) {
            InventoryProfiles.button(g, InventoryProfiles.toggle(s), HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438", "Layouts"), mx, my);
            InventoryProfiles.grips(g, InventoryProfiles.toggle(s));
            return;
        }
        Box b = InventoryProfiles.panel(s);
        InventoryProfiles.surface(g, b, 6, -15395555, BloodHelperClient.config().accent);
        InventoryProfiles.grips(g, b);
        InventoryProfiles.text(g, HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f", "Inventory layouts"), b.x + 8, b.y + 8, 10.0f, b.w - 31, -1);
        InventoryProfiles.text(g, "\u00d7", b.x + b.w - 17, b.y + 6, 13.0f, 13, -2242351);
        if (!InventoryLayoutStore.ready()) {
            InventoryProfiles.text(g, HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043f\u0440\u043e\u0444\u0438\u043b\u0435\u0439\u2026", "Loading profiles\u2026"), b.x + 8, b.y + 33, 9.0f, b.w - 16, -4738110);
            return;
        }
        if (!InventoryLayoutStore.error().isEmpty()) {
            InventoryProfiles.text(g, HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u043f\u0440\u043e\u0444\u0438\u043b\u0438", "Could not read profiles"), b.x + 8, b.y + 33, 9.0f, b.w - 16, -18785);
            InventoryProfiles.text(g, InventoryLayoutStore.error(), b.x + 8, b.y + 48, 9.0f, b.w - 16, -4738110);
            return;
        }
        if (s.preview != null) {
            InventoryProfiles.renderPreview(s, g, b, mx, my);
        } else {
            InventoryProfiles.text(g, HudStyle.tr("\u041b\u041a\u041c \u2014 \u043f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c \u00b7 \u041f\u041a\u041c \u2014 \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440", "Left: apply \u00b7 Right: preview"), b.x + 8, b.y + 26, 8.0f, b.w - 16, -4738110);
            List<InventoryLayoutStore.Profile> profiles = InventoryLayoutStore.profiles();
            int rows = InventoryProfiles.rows(s);
            s.offset = Math.clamp((long)s.offset, 0, Math.max(0, profiles.size() - rows));
            for (int i = 0; i < rows && i + s.offset < profiles.size(); ++i) {
                InventoryProfiles.button(g, new Box(b.x + 8, b.y + 43 + i * 22, b.w - 16, 20), profiles.get(i + s.offset).name(), mx, my);
            }
            if (profiles.isEmpty()) {
                InventoryProfiles.text(g, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438 \u043f\u0435\u0440\u0432\u0443\u044e \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0443 \u043d\u0438\u0436\u0435", "Save your first layout below"), b.x + 8, b.y + 53, 9.0f, b.w - 16, -4738110);
            }
            if (profiles.size() > rows) {
                InventoryProfiles.text(g, s.offset + 1 + "\u2013" + Math.min(profiles.size(), s.offset + rows) + " / " + profiles.size() + HudStyle.tr(" \u00b7 \u043a\u043e\u043b\u0435\u0441\u043e \u043c\u044b\u0448\u0438", " \u00b7 scroll"), b.x + 8, b.y + b.h - 87, 8.0f, b.w - 16, -4738110);
            }
            s.name.setX(b.x + 11);
            s.name.setY(b.y + b.h - 69);
            s.name.setWidth(b.w - 22);
            s.name.setHeight(17);
            s.name.setHint(HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043d\u043e\u0432\u043e\u0439 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438", "New layout name"));
            InventoryProfiles.surface(g, new Box(b.x + 8, b.y + b.h - 72, b.w - 16, 23), 3, -14408915, -11251617);
            s.name.render(g, mx, my, 0.0f);
            String saveLabel = InventoryLayoutStore.named(s.name.getValue()) == null ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0443\u044e", "Save current layout") : (s.confirmSave.equals(s.name.getValue().strip()) ? HudStyle.tr("\u0415\u0449\u0451 \u0440\u0430\u0437 \u2014 \u0437\u0430\u043c\u0435\u043d\u0438\u0442\u044c", "Click again to replace") : HudStyle.tr("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0443", "Update layout"));
            InventoryProfiles.button(g, new Box(b.x + 8, b.y + b.h - 44, b.w - 16, 20), InventoryLayoutStore.busy() ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435\u2026", "Saving\u2026") : saveLabel, mx, my);
        }
        String status = job != null && InventoryProfiles.job.owner == s ? HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u044b\u0432\u0430\u044e: ", "Arranging: ") + InventoryProfiles.job.index + "/" + InventoryProfiles.job.swaps.size() : (InventoryLayoutStore.busy() ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435\u2026", "Saving\u2026") : s.message);
        InventoryProfiles.text(g, status, b.x + 8, b.y + b.h - 14, 8.0f, b.w - 16, -2764579);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderPreview(State s, class_332 g, Box b, int mx, int my) {
        InventoryProfiles.button(g, new Box(b.x + 8, b.y + 23, 48, 18), HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my);
        InventoryProfiles.text(g, s.preview.name(), b.x + 62, b.y + 28, 9.0f, b.w - 70, -1515541);
        int cell = Math.min(18, Math.min((b.w - 16) / 9, Math.max(8, (b.h - 119) / 5)));
        int left = b.x + (b.w - 9 * cell) / 2;
        int top = b.y + 51;
        String hover = "";
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = row == 3 ? col : 9 + row * 9 + col;
                int x = left + col * cell;
                int y = top + row * cell + (row == 3 ? 5 : 0);
                g.method_25294(x, y, x + cell - 1, y + cell - 1, -13290945);
                class_1799 item = s.previewItems[index];
                if (!item.method_7960()) {
                    g.method_51448().pushMatrix();
                    g.method_51448().translate((float)x, (float)y);
                    float scale = (float)(cell - 1) / 16.0f;
                    g.method_51448().scale(scale, scale);
                    try {
                        g.method_51427(item, 0, 0);
                    }
                    finally {
                        g.method_51448().popMatrix();
                    }
                }
                if (item.method_7960() || !new Box(x, y, cell, cell).has(mx, my)) continue;
                hover = item.method_7964().getString();
            }
        }
        for (int col = 0; col < 5; ++col) {
            class_1799 item;
            int index = col == 4 ? 40 : 39 - col;
            int x = left + col * cell;
            int y = top + cell * 4 + 9;
            g.method_25294(x, y, x + cell - 1, y + cell - 1, -11976630);
            class_1799 class_17992 = item = s.previewItems.length > index ? s.previewItems[index] : class_1799.field_8037;
            if (!item.method_7960()) {
                g.method_51448().pushMatrix();
                g.method_51448().translate((float)x, (float)y);
                g.method_51448().scale((float)(cell - 1) / 16.0f, (float)(cell - 1) / 16.0f);
                try {
                    g.method_51427(item, 0, 0);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            if (!new Box(x, y, cell, cell).has(mx, my)) continue;
            hover = !item.method_7960() ? item.method_7964().getString() : (col == 4 ? HudStyle.tr("\u0412\u0442\u043e\u0440\u0430\u044f \u0440\u0443\u043a\u0430", "Offhand") : HudStyle.tr("\u0411\u0440\u043e\u043d\u044f", "Armor"));
        }
        InventoryProfiles.text(g, s.validPreview ? (hover.isEmpty() ? (s.preview.slots().size() == 36 ? HudStyle.tr("\u0421\u0442\u0430\u0440\u0430\u044f \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u00b7 \u044d\u043a\u0438\u043f\u0438\u0440\u043e\u0432\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f", "Legacy layout \u00b7 equipment kept") : HudStyle.tr("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c, \u0431\u0440\u043e\u043d\u044f \u0438 \u0432\u0442\u043e\u0440\u0430\u044f \u0440\u0443\u043a\u0430", "Inventory, armor and offhand")) : hover) : HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 \u044d\u0442\u043e\u0439 \u0432\u0435\u0440\u0441\u0438\u0438", "Item unavailable in this version"), b.x + 8, top + cell * 5 + 13, 8.0f, b.w - 16, -4738110);
        InventoryProfiles.button(g, new Box(b.x + 8, b.y + b.h - 44, b.w - 91, 20), HudStyle.tr("\u041f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c", "Apply"), mx, my);
        InventoryProfiles.button(g, new Box(b.x + b.w - 77, b.y + b.h - 44, 69, 20), s.confirmDelete.equals(s.preview.id()) ? HudStyle.tr("\u0422\u043e\u0447\u043d\u043e?", "Confirm?") : HudStyle.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c", "Delete"), mx, my);
    }

    private static boolean click(State s, class_11909 e) {
        boolean edge;
        if (!InventoryProfiles.usable(s)) {
            return false;
        }
        if (SlotLockModule.isDragHandle(s.screen, e)) {
            return false;
        }
        Box handle = s.open ? InventoryProfiles.panel(s) : InventoryProfiles.toggle(s);
        boolean bl = edge = handle.has(e.comp_4798(), e.comp_4799()) && (e.comp_4798() < (double)(handle.x + 5) || e.comp_4798() >= (double)(handle.x + handle.w - 5) || s.open && (e.comp_4799() >= (double)(handle.y + handle.h - 4) || e.comp_4799() < (double)(handle.y + 22) && e.comp_4798() < (double)(handle.x + handle.w - 24)));
        if (e.method_74245() == 0 && edge) {
            InventoryProfiles.stop(HudStyle.tr("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e", "Stopped"));
            s.name.setFocused(false);
            s.drag.begin(s.open ? BloodHelperClient.config().inventoryUi.layoutsPanel : BloodHelperClient.config().inventoryUi.layoutsButton, e.comp_4798(), e.comp_4799(), handle.x, handle.y, handle.w, handle.h);
            return true;
        }
        if (!s.open) {
            if (e.method_74245() == 0 && InventoryProfiles.toggle(s).has(e.comp_4798(), e.comp_4799())) {
                s.open = true;
                return true;
            }
            return false;
        }
        Box b = InventoryProfiles.panel(s);
        if (!b.has(e.comp_4798(), e.comp_4799())) {
            s.name.setFocused(false);
            InventoryProfiles.stop(HudStyle.tr("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e", "Stopped"));
            return false;
        }
        InventoryProfiles.stop(HudStyle.tr("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e", "Stopped"));
        if (new Box(b.x + b.w - 24, b.y, 24, 22).has(e.comp_4798(), e.comp_4799())) {
            s.open = false;
            s.name.setFocused(false);
            return true;
        }
        if (!InventoryLayoutStore.ready() || !InventoryLayoutStore.error().isEmpty() || InventoryLayoutStore.busy()) {
            return true;
        }
        if (s.preview != null) {
            if (e.method_74245() != 0) {
                return true;
            }
            if (new Box(b.x + 8, b.y + 23, 48, 18).has(e.comp_4798(), e.comp_4799())) {
                s.preview = null;
                s.confirmDelete = "";
                return true;
            }
            if (new Box(b.x + 8, b.y + b.h - 44, b.w - 91, 20).has(e.comp_4798(), e.comp_4799())) {
                InventoryProfiles.start(s, s.preview);
            }
            if (new Box(b.x + b.w - 77, b.y + b.h - 44, 69, 20).has(e.comp_4798(), e.comp_4799())) {
                if (!s.confirmDelete.equals(s.preview.id())) {
                    s.confirmDelete = s.preview.id();
                } else {
                    InventoryLayoutStore.delete(s.preview, ok -> {
                        String string = s.message = ok != false ? HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u0443\u0434\u0430\u043b\u0435\u043d\u0430", "Layout deleted") : HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Could not save");
                        if (ok.booleanValue()) {
                            s.preview = null;
                        }
                        s.confirmDelete = "";
                    });
                }
            }
            return true;
        }
        if (s.name.mouseClicked(e, false)) {
            return true;
        }
        s.name.setFocused(false);
        if (new Box(b.x + 8, b.y + b.h - 44, b.w - 16, 20).has(e.comp_4798(), e.comp_4799())) {
            if (e.method_74245() == 0) {
                InventoryProfiles.save(s);
            }
            return true;
        }
        List<InventoryLayoutStore.Profile> profiles = InventoryLayoutStore.profiles();
        for (int i = 0; i < InventoryProfiles.rows(s) && i + s.offset < profiles.size(); ++i) {
            if (!new Box(b.x + 8, b.y + 43 + i * 22, b.w - 16, 20).has(e.comp_4798(), e.comp_4799())) continue;
            InventoryLayoutStore.Profile p = profiles.get(i + s.offset);
            if (e.method_74245() == 1) {
                InventoryProfiles.preview(s, p);
            } else if (e.method_74245() == 0) {
                InventoryProfiles.start(s, p);
            }
            return true;
        }
        return true;
    }

    private static boolean key(State s, class_11908 e) {
        if (!InventoryProfiles.usable(s)) {
            return false;
        }
        if (job != null) {
            InventoryProfiles.stop(HudStyle.tr("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e \u043a\u043b\u0430\u0432\u0438\u0448\u0435\u0439", "Stopped by key press"));
        }
        if (!s.open) {
            return false;
        }
        if (e.comp_4795() == 256) {
            if (s.preview != null) {
                s.preview = null;
                s.confirmDelete = "";
            } else {
                s.open = false;
                s.name.setFocused(false);
            }
            return true;
        }
        if (s.preview == null && s.name.isFocused()) {
            if (e.comp_4795() == 257 || e.comp_4795() == 335) {
                InventoryProfiles.save(s);
            } else {
                s.name.keyPressed(e);
            }
            return true;
        }
        return false;
    }

    public static boolean character(class_11905 e) {
        return InventoryProfiles.usable(active) && InventoryProfiles.active.open && InventoryProfiles.active.preview == null && InventoryProfiles.active.name.isFocused() && InventoryProfiles.active.name.charTyped(e);
    }

    private static class_1799[] snapshot() {
        class_746 player = class_310.method_1551().field_1724;
        class_1799[] result = new class_1799[41];
        for (int i = 0; i < result.length; ++i) {
            result[i] = player.method_31548().method_5438(i).method_7972();
        }
        return result;
    }

    private static boolean[] locks() {
        boolean[] locks = new boolean[41];
        for (int i = 0; i < locks.length; ++i) {
            locks[i] = BloodHelperClient.config().slotLocks.locked(i);
        }
        return locks;
    }

    private static void save(State s) {
        String name;
        if (InventoryLayoutStore.busy()) {
            return;
        }
        try {
            name = InventoryLayoutStore.validName(s.name.getValue());
        }
        catch (IllegalArgumentException e) {
            s.message = HudStyle.tr("\u0412\u0432\u0435\u0434\u0438 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438", "Enter a layout name");
            return;
        }
        if (InventoryLayoutStore.named(name) != null && !s.confirmSave.equals(name)) {
            s.confirmSave = name;
            return;
        }
        if (InventoryLayoutStore.named(name) == null && InventoryLayoutStore.profiles().size() >= 32) {
            s.message = HudStyle.tr("\u041c\u0430\u043a\u0441\u0438\u043c\u0443\u043c 32 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438", "Maximum 32 layouts");
            return;
        }
        if (!s.screen.method_17577().method_34255().method_7960()) {
            s.message = HudStyle.tr("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u043f\u043e\u043b\u043e\u0436\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0441 \u043a\u0443\u0440\u0441\u043e\u0440\u0430", "Put down the cursor item first");
            return;
        }
        try {
            class_6903 ops = class_310.method_1551().field_1687.method_30349().method_57093((DynamicOps)JsonOps.INSTANCE);
            ArrayList<JsonElement> slots = new ArrayList<JsonElement>();
            for (class_1799 item : InventoryProfiles.snapshot()) {
                slots.add((JsonElement)(item.method_7960() ? JsonNull.INSTANCE : (JsonElement)class_1799.field_24671.encodeStart((DynamicOps)ops, (Object)item.method_46651(1)).getOrThrow()));
            }
            InventoryLayoutStore.save(name, slots, ok -> {
                s.message = ok != false ? HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430", "Layout saved") : HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Could not save");
                s.confirmSave = "";
            });
        }
        catch (RuntimeException e) {
            s.message = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", "Could not save items");
        }
    }

    private static class_1799[] decode(InventoryLayoutStore.Profile profile) {
        class_6903 ops = class_310.method_1551().field_1687.method_30349().method_57093((DynamicOps)JsonOps.INSTANCE);
        class_1799[] items = new class_1799[profile.slots().size()];
        for (int i = 0; i < items.length; ++i) {
            items[i] = profile.slots().get(i).isJsonNull() ? class_1799.field_8037 : (class_1799)class_1799.field_24671.parse((DynamicOps)ops, (Object)profile.slots().get(i)).getOrThrow();
        }
        return items;
    }

    private static void preview(State s, InventoryLayoutStore.Profile p) {
        s.preview = p;
        s.confirmDelete = "";
        s.name.setFocused(false);
        try {
            s.previewItems = InventoryProfiles.decode(p);
            s.validPreview = true;
        }
        catch (RuntimeException e) {
            s.previewItems = new class_1799[41];
            Arrays.fill(s.previewItems, class_1799.field_8037);
            s.validPreview = false;
        }
    }

    private static Map<Integer, class_1735> slots(State s) {
        HashMap<Integer, class_1735> result = new HashMap<Integer, class_1735>();
        for (class_1735 slot : s.screen.method_17577().field_7761) {
            int index = SlotLockModule.inventorySlot(slot);
            if (index < 0 || index >= 41 || !slot.method_7682()) continue;
            result.put(index, slot);
        }
        return result;
    }

    private static void start(State s, InventoryLayoutStore.Profile profile) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1761 == null || mc.field_1724.method_7325() || !s.screen.method_17577().method_34255().method_7960()) {
            s.message = HudStyle.tr("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u043f\u043e\u043b\u043e\u0436\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0441 \u043a\u0443\u0440\u0441\u043e\u0440\u0430", "Put down the cursor item first");
            return;
        }
        if (InventoryProfiles.slots(s).size() != 41) {
            s.message = HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439 \u0432\u043a\u043b\u0430\u0434\u043a\u0443 \u043b\u0438\u0447\u043d\u043e\u0433\u043e \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f", "Open the player inventory tab");
            return;
        }
        try {
            class_1799[] current = InventoryProfiles.snapshot();
            boolean[] locks = InventoryProfiles.locks();
            Map<Integer, class_1735> available = InventoryProfiles.slots(s);
            boolean[] protectedSlots = (boolean[])locks.clone();
            for (int i = 0; i < 41; ++i) {
                int n = i;
                protectedSlots[n] = protectedSlots[n] | !available.get(i).method_7674((class_1657)mc.field_1724);
            }
            InventoryLayoutPlan.Plan plan = InventoryLayoutPlan.create(current, InventoryProfiles.decode(profile), protectedSlots, (index, item) -> item.method_7960() || ((class_1735)available.get(index)).method_7680(item) && item.method_7947() <= ((class_1735)available.get(index)).method_7676(item));
            if (plan.needsBuffer()) {
                s.message = HudStyle.tr("\u0420\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u0443\u0439 \u0445\u043e\u0442\u044f \u0431\u044b 1 \u0441\u043b\u043e\u0442 \u0445\u043e\u0442\u0431\u0430\u0440\u0430", "Unlock at least one hotbar slot");
                return;
            }
            s.name.setFocused(false);
            if (plan.swaps().isEmpty()) {
                s.message = InventoryProfiles.result(plan.missing());
                return;
            }
            job = new Job(s, plan, current, locks);
        }
        catch (RuntimeException e) {
            s.message = HudStyle.tr("\u041f\u0440\u043e\u0444\u0438\u043b\u044c \u043d\u0435\u0441\u043e\u0432\u043c\u0435\u0441\u0442\u0438\u043c \u0441 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430\u043c\u0438", "Profile contains unavailable items");
        }
    }

    private static String result(int missing) {
        return missing == 0 ? HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u043e \u00b7 \u0437\u0430\u0449\u0438\u0449\u0451\u043d\u043d\u044b\u0435 \u0441\u043b\u043e\u0442\u044b \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u044b", "Done \u00b7 locked slots preserved") : HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u043e \u00b7 \u043d\u0435 \u0432\u044b\u043f\u043e\u043b\u043d\u0435\u043d\u043e \u043f\u043e\u0437\u0438\u0446\u0438\u0439: ", "Done \u00b7 unfilled positions: ") + missing;
    }

    public static void tick(class_310 mc) {
        Job j = job;
        if (j == null) {
            return;
        }
        if (!InventoryProfiles.usable(j.owner) || mc.field_1761 == null || mc.field_1724.method_7325() || !j.owner.screen.method_17577().method_34255().method_7960() || !Arrays.equals(j.locks, InventoryProfiles.locks())) {
            InventoryProfiles.stop(HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430", "Layout stopped"));
            return;
        }
        class_1799[] now = InventoryProfiles.snapshot();
        for (int i = 0; i < 41; ++i) {
            if (class_1799.method_7973((class_1799)now[i], (class_1799)j.expected[i])) continue;
            InventoryProfiles.stop(HudStyle.tr("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u0438\u0437\u043c\u0435\u043d\u0438\u043b\u0441\u044f \u00b7 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e", "Inventory changed \u00b7 stopped"));
            return;
        }
        if (j.wait-- > 0) {
            return;
        }
        if (j.index == j.swaps.size()) {
            j.owner.message = InventoryProfiles.result(j.missing);
            job = null;
            return;
        }
        InventoryLayoutPlan.Swap swap = j.swaps.get(j.index);
        Map<Integer, class_1735> slots = InventoryProfiles.slots(j.owner);
        class_1735 source = slots.get(swap.slot());
        class_1735 hotbar = slots.get(swap.hotbar());
        class_1799 a = now[swap.slot()];
        class_1799 b = now[swap.hotbar()];
        if (!(slots.size() == 41 && source != null && hotbar != null && source.method_7674((class_1657)mc.field_1724) && hotbar.method_7674((class_1657)mc.field_1724) && (b.method_7960() || source.method_7680(b) && b.method_7947() <= source.method_7676(b)) && (a.method_7960() || hotbar.method_7680(a) && a.method_7947() <= hotbar.method_7676(a)))) {
            InventoryProfiles.stop(HudStyle.tr("\u042d\u0442\u043e\u0442 \u0441\u043b\u043e\u0442 \u043d\u0435\u043b\u044c\u0437\u044f \u043f\u0435\u0440\u0435\u0441\u0442\u0430\u0432\u0438\u0442\u044c", "Cannot move this slot"));
            return;
        }
        ((ContainerBoundsAccessor)j.owner.screen).bloodhelper$clickSlot(source, source.field_7874, swap.hotbar(), class_1713.field_7791);
        j.expected[swap.slot()] = b;
        j.expected[swap.hotbar()] = a;
        ++j.index;
        j.wait = 2;
    }

    private static void stop(String reason) {
        if (job != null) {
            InventoryProfiles.job.owner.message = reason;
            job = null;
        }
    }

    private static void grips(class_332 g, Box b) {
        for (int d = -3; d <= 3; d += 3) {
            int y = b.y + b.h / 2 + d;
            g.method_25294(b.x + 2, y, b.x + 3, y + 1, -7505016);
            g.method_25294(b.x + b.w - 3, y, b.x + b.w - 2, y + 1, -7505016);
        }
    }

    public static void reset() {
        InventoryProfiles.stop(HudStyle.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430", "Layout stopped"));
        active = null;
    }

    private static final class State {
        final class_465<?> screen;
        final GuiTextField name = new GuiTextField(0, 0, 170, 17, "");
        final InventoryUiDrag drag = new InventoryUiDrag();
        boolean open;
        boolean validPreview;
        int offset;
        InventoryLayoutStore.Profile preview;
        class_1799[] previewItems;
        String message = "";
        String confirmSave = "";
        String confirmDelete = "";

        State(class_465<?> screen) {
            this.screen = screen;
            this.name.setMaxLength(32);
            this.name.setTextSize(10.0f);
        }
    }

    private record Box(int x, int y, int w, int h) {
        boolean has(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private static final class Job {
        final State owner;
        final List<InventoryLayoutPlan.Swap> swaps;
        final class_1799[] expected;
        final boolean[] locks;
        final int missing;
        int index;
        int wait;

        Job(State owner, InventoryLayoutPlan.Plan plan, class_1799[] expected, boolean[] locks) {
            this.owner = owner;
            this.swaps = plan.swaps();
            this.missing = plan.missing();
            this.expected = expected;
            this.locks = locks;
        }
    }
}

