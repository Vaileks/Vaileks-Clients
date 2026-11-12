/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class ReGlassGuiRules {
    private ReGlassGuiRules() {
    }

    public static boolean hasGuiGlyph(String text) {
        return text != null && text.codePoints().anyMatch(cp -> cp >= 57344 && cp <= 63743 || cp >= 983040 && cp <= 1048573 || cp >= 0x100000 && cp <= 1114109);
    }

    public static boolean sprite(String p) {
        return p.equals("widget/button") || p.startsWith("widget/button_") || p.startsWith("widget/tab") || p.startsWith("widget/scroller") || p.startsWith("widget/checkbox") || p.startsWith("widget/locked_button") || p.startsWith("widget/unlocked_button") || p.startsWith("widget/cross_button") || p.startsWith("widget/page_forward") || p.startsWith("widget/page_backward") || p.startsWith("widget/slider") || p.startsWith("widget/text_field") || p.equals("hud/hotbar") || p.startsWith("hud/hotbar_offhand") || p.equals("tooltip/background") || p.startsWith("container/inventory/effect_background") || p.startsWith("container/creative_inventory/tab_") || p.equals("recipe_book/tab") || p.equals("recipe_book/tab_selected") || p.startsWith("recipe_book/slot_") || p.startsWith("container/beacon/button") || p.startsWith("container/anvil/text_field") || p.startsWith("container/enchanting_table/enchantment_slot") || p.startsWith("container/stonecutter/recipe") || p.startsWith("container/loom/pattern");
    }

    public static boolean outline(String p) {
        return p.equals("hud/hotbar_selection") || p.equals("tooltip/frame") || p.equals("widget/slot_frame");
    }

    public static boolean container(String p, int w, int h) {
        return w >= 100 && h >= 54 && (p.startsWith("textures/gui/container/") || p.equals("textures/gui/recipe_book.png"));
    }

    public static boolean background(int w, int h, int color) {
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >>> 24;
        return w >= 30 && h >= 8 && a >= 24 && a <= 224 && Math.max(r, Math.max(g, b)) <= 32 && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) <= 5;
    }
}

