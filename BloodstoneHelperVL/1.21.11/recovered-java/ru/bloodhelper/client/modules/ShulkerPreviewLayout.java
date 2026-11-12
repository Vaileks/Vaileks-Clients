/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import ru.bloodhelper.client.config.ShulkerPreviewSettings;

public record ShulkerPreviewLayout(float scale, int width, int height) {
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final int SLOTS = 27;
    public static final int CELL = 18;
    public static final int PADDING = 4;
    public static final int WIDTH = 170;
    public static final int HEIGHT = 62;

    public static ShulkerPreviewLayout fit(double requested, int screenWidth, int screenHeight) {
        double maxWidth = Math.max(1, screenWidth - 16);
        double maxHeight = Math.max(1, screenHeight - 48);
        double bound = Math.min(ShulkerPreviewSettings.boundedScale(requested), Math.min(maxWidth / 170.0, maxHeight / 62.0));
        float scale = (float)bound;
        if ((double)scale > bound) {
            scale = Math.nextDown(scale);
        }
        return new ShulkerPreviewLayout(scale, (int)Math.ceil(170.0 * (double)scale), (int)Math.ceil(62.0 * (double)scale));
    }

    public static int slotX(int slot) {
        ShulkerPreviewLayout.check(slot);
        return 4 + slot % 9 * 18;
    }

    public static int slotY(int slot) {
        ShulkerPreviewLayout.check(slot);
        return 4 + slot / 9 * 18;
    }

    private static void check(int slot) {
        if (slot < 0 || slot >= 27) {
            throw new IndexOutOfBoundsException(slot);
        }
    }
}

