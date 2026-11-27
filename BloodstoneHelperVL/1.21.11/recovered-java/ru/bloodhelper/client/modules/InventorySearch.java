/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1836
 *  net.minecraft.class_1937
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_7923;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.mixin.ContainerBoundsAccessor;
import ru.bloodhelper.client.modules.ItemSearchQuery;

public final class InventorySearch {
    private static State active;

    private InventorySearch() {
    }

    public static void attach(class_437 screen) {
        State s;
        if (!BloodHelperClient.config().inventorySearch || !(screen instanceof class_465)) {
            return;
        }
        active = s = active != null && InventorySearch.active.screen == screen ? active : new State(screen);
        ScreenEvents.afterRender((class_437)screen).register((owner, g, x, y, delta) -> InventorySearch.render(s, g, x, y));
        ScreenEvents.remove((class_437)screen).register(owner -> {
            if (active == s) {
                active = null;
            }
        });
        ScreenKeyboardEvents.allowKeyPress((class_437)screen).register((owner, e) -> !InventorySearch.key(s, e));
        ScreenMouseEvents.allowMouseClick((class_437)screen).register((owner, e) -> {
            if (!InventorySearch.usable(s) || !s.visible) {
                return true;
            }
            boolean hit = s.field.mouseClicked(e, false);
            if (!hit) {
                s.field.setFocused(false);
            }
            return !hit;
        });
        ScreenMouseEvents.allowMouseDrag((class_437)screen).register((owner, e, dx, dy) -> !InventorySearch.usable(s) || !s.visible || !s.field.mouseDragged(e, dx, dy));
        ScreenMouseEvents.allowMouseRelease((class_437)screen).register((owner, e) -> !InventorySearch.usable(s) || !s.visible || !s.field.mouseReleased(e));
    }

    private static boolean usable(State s) {
        return s != null && BloodHelperClient.config().inventorySearch && class_310.method_1551().field_1755 == s.screen;
    }

    private static boolean key(State s, class_11908 e) {
        if (!InventorySearch.usable(s)) {
            return false;
        }
        if (e.comp_4795() == 70 && (e.comp_4797() & 2) != 0) {
            s.visible = true;
            s.field.setFocused(true);
            return true;
        }
        if (!s.visible) {
            return false;
        }
        if (e.comp_4795() == 256) {
            if (!s.field.getValue().isEmpty()) {
                s.field.setValue("");
            } else {
                s.visible = false;
                s.field.setFocused(false);
            }
            return true;
        }
        if (s.field.isFocused()) {
            if (e.comp_4795() == 257 || e.comp_4795() == 335) {
                s.field.setFocused(false);
            } else {
                s.field.keyPressed(e);
            }
            return true;
        }
        return false;
    }

    public static boolean character(class_11905 event) {
        return InventorySearch.usable(active) && InventorySearch.active.visible && InventorySearch.active.field.isFocused() && InventorySearch.active.field.charTyped(event);
    }

    private static void render(State s, class_332 g, int mx, int my) {
        if (!InventorySearch.usable(s) || !s.visible) {
            return;
        }
        ContainerBoundsAccessor bounds = (ContainerBoundsAccessor)s.screen;
        int w = Math.min(220, Math.max(80, g.method_51421() - 12));
        int x = Math.clamp((long)bounds.bloodhelper$left(), 6, Math.max(6, g.method_51421() - w - 6));
        int above = bounds.bloodhelper$top() - 27;
        int below = bounds.bloodhelper$top() + bounds.bloodhelper$height() + 5;
        int y = above >= 6 ? above : Math.min(g.method_51443() - 24, below);
        UiDraw.border(g, x, y, w, 22, 5, -300739301, -9601901);
        s.field.setX(x + 6);
        s.field.setY(y + 3);
        s.field.setWidth(w - 12);
        s.field.setHeight(17);
        s.field.setHint(HudStyle.tr("\u041d\u0430\u0439\u0442\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u00b7 Esc \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Find item \u00b7 Esc to clear"));
        s.field.render(g, mx, my, 0.0f);
    }

    public static void slot(class_332 g, class_1735 slot) {
        State s = active;
        if (!(InventorySearch.usable(s) && s.visible && !s.field.getValue().isBlank() && slot.method_7682() && slot.method_7681())) {
            return;
        }
        Object translations = UiLanguage.cacheToken();
        if (s.translations != translations) {
            s.cache.clear();
            s.translations = translations;
        }
        class_1799 stack = slot.method_7677();
        Cached cached = s.cache.get(slot.field_7874);
        if (cached == null || !class_1799.method_31577((class_1799)cached.stack, (class_1799)stack)) {
            StringBuilder text = new StringBuilder(class_7923.field_41178.method_10221((Object)stack.method_7909()).toString());
            class_310 mc = class_310.method_1551();
            for (class_2561 line : stack.method_7950(class_1792.class_9635.method_59528((class_1937)mc.field_1687), (class_1657)mc.field_1724, (class_1836)class_1836.field_41070)) {
                text.append(' ').append(line.getString());
            }
            cached = new Cached(stack.method_7972(), text.toString(), s.field.getValue(), ItemSearchQuery.matches(text.toString(), s.field.getValue()));
            s.cache.put(slot.field_7874, cached);
        }
        if (!cached.query.equals(s.field.getValue())) {
            cached = new Cached(cached.stack, cached.text, s.field.getValue(), ItemSearchQuery.matches(cached.text, s.field.getValue()));
            s.cache.put(slot.field_7874, cached);
        }
        if (!cached.matches) {
            g.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, -1342177280);
        } else {
            UiDraw.outline(g, slot.field_7873 - 1, slot.field_7872 - 1, 18, 18, 2, -7675712);
        }
    }

    private static final class State {
        final class_437 screen;
        final GuiTextField field = new GuiTextField(0, 0, 180, 18, "");
        final Map<Integer, Cached> cache = new HashMap<Integer, Cached>();
        boolean visible;
        Object translations;

        State(class_437 screen) {
            this.screen = screen;
            this.field.setMaxLength(80);
            this.field.setTextSize(10.0f);
        }
    }

    private record Cached(class_1799 stack, String text, String query, boolean matches) {
    }
}

