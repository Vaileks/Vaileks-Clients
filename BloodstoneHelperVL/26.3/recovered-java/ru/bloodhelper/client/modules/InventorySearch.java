/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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

    public static void attach(Screen screen) {
        State s;
        if (!BloodHelperClient.config().inventorySearch || !(screen instanceof AbstractContainerScreen)) {
            return;
        }
        active = s = active != null && InventorySearch.active.screen == screen ? active : new State(screen);
        ScreenEvents.afterExtract((Screen)screen).register((owner, g, x, y, delta) -> InventorySearch.render(s, g, x, y));
        ScreenEvents.remove((Screen)screen).register(owner -> {
            if (active == s) {
                active = null;
            }
        });
        ScreenKeyboardEvents.allowKeyPress((Screen)screen).register((owner, e) -> !InventorySearch.key(s, e));
        ScreenMouseEvents.allowMouseClick((Screen)screen).register((owner, e) -> {
            if (!InventorySearch.usable(s) || !s.visible) {
                return true;
            }
            boolean hit = s.field.mouseClicked(e, false);
            if (!hit) {
                s.field.setFocused(false);
            }
            return !hit;
        });
        ScreenMouseEvents.allowMouseDrag((Screen)screen).register((owner, e, dx, dy) -> !InventorySearch.usable(s) || !s.visible || !s.field.mouseDragged(e, dx, dy));
        ScreenMouseEvents.allowMouseRelease((Screen)screen).register((owner, e) -> !InventorySearch.usable(s) || !s.visible || !s.field.mouseReleased(e));
    }

    private static boolean usable(State s) {
        return s != null && BloodHelperClient.config().inventorySearch && Minecraft.getInstance().gui.screen() == s.screen;
    }

    private static boolean key(State s, KeyEvent e) {
        if (!InventorySearch.usable(s)) {
            return false;
        }
        if (e.key() == 9 && (e.modifiers() & 0xC0) != 0) {
            s.visible = true;
            s.field.setFocused(true);
            return true;
        }
        if (!s.visible) {
            return false;
        }
        if (e.key() == 41) {
            if (!s.field.getValue().isEmpty()) {
                s.field.setValue("");
            } else {
                s.visible = false;
                s.field.setFocused(false);
            }
            return true;
        }
        if (s.field.isFocused()) {
            if (e.key() == 40 || e.key() == 88) {
                s.field.setFocused(false);
            } else {
                s.field.keyPressed(e);
            }
            return true;
        }
        return false;
    }

    public static boolean character(CharacterEvent event) {
        return InventorySearch.usable(active) && InventorySearch.active.visible && InventorySearch.active.field.isFocused() && InventorySearch.active.field.charTyped(event);
    }

    private static void render(State s, GuiGraphicsExtractor g, int mx, int my) {
        if (!InventorySearch.usable(s) || !s.visible) {
            return;
        }
        ContainerBoundsAccessor bounds = (ContainerBoundsAccessor)s.screen;
        int w = Math.min(220, Math.max(80, g.guiWidth() - 12));
        int x = Math.clamp((long)bounds.bloodhelper$left(), 6, Math.max(6, g.guiWidth() - w - 6));
        int above = bounds.bloodhelper$top() - 27;
        int below = bounds.bloodhelper$top() + bounds.bloodhelper$height() + 5;
        int y = above >= 6 ? above : Math.min(g.guiHeight() - 24, below);
        UiDraw.border(g, x, y, w, 22, 5, -300739301, -9601901);
        s.field.setX(x + 6);
        s.field.setY(y + 3);
        s.field.setWidth(w - 12);
        s.field.setHeight(17);
        s.field.setHint(HudStyle.tr("\u041d\u0430\u0439\u0442\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u00b7 Esc \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Find item \u00b7 Esc to clear"));
        s.field.render(g, mx, my, 0.0f);
    }

    public static void slot(GuiGraphicsExtractor g, Slot slot) {
        State s = active;
        if (!(InventorySearch.usable(s) && s.visible && !s.field.getValue().isBlank() && slot.isActive() && slot.hasItem())) {
            return;
        }
        Object translations = UiLanguage.cacheToken();
        if (s.translations != translations) {
            s.cache.clear();
            s.translations = translations;
        }
        ItemStack stack = slot.getItem();
        Cached cached = s.cache.get(slot.index);
        if (cached == null || !ItemStack.isSameItemSameComponents((ItemStack)cached.stack, (ItemStack)stack)) {
            StringBuilder text = new StringBuilder(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString());
            Minecraft mc = Minecraft.getInstance();
            for (Component line : stack.getTooltipLines(Item.TooltipContext.of((Level)mc.level), (Player)mc.player, (TooltipFlag)TooltipFlag.NORMAL)) {
                text.append(' ').append(line.getString());
            }
            cached = new Cached(stack.copy(), text.toString(), s.field.getValue(), ItemSearchQuery.matches(text.toString(), s.field.getValue()));
            s.cache.put(slot.index, cached);
        }
        if (!cached.query.equals(s.field.getValue())) {
            cached = new Cached(cached.stack, cached.text, s.field.getValue(), ItemSearchQuery.matches(cached.text, s.field.getValue()));
            s.cache.put(slot.index, cached);
        }
        if (!cached.matches) {
            g.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, -1342177280);
        } else {
            UiDraw.outline(g, slot.x - 1, slot.y - 1, 18, 18, 2, -7675712);
        }
    }

    private static final class State {
        final Screen screen;
        final GuiTextField field = new GuiTextField(0, 0, 180, 18, "");
        final Map<Integer, Cached> cache = new HashMap<Integer, Cached>();
        boolean visible;
        Object translations;

        State(Screen screen) {
            this.screen = screen;
            this.field.setMaxLength(80);
            this.field.setTextSize(10.0f);
        }
    }

    private record Cached(ItemStack stack, String text, String query, boolean matches) {
    }
}

