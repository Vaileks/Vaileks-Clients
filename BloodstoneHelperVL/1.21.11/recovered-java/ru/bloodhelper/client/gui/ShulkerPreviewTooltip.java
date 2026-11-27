/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5684
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_1799;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5684;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.modules.ShulkerPreview;
import ru.bloodhelper.client.modules.ShulkerPreviewLayout;

public final class ShulkerPreviewTooltip
implements class_5684 {
    private final ShulkerPreview.Payload payload;

    public ShulkerPreviewTooltip(ShulkerPreview.Payload payload) {
        this.payload = payload;
    }

    public int method_32664(class_327 font) {
        return this.payload.layout().width();
    }

    public int method_32661(class_327 font) {
        return this.payload.layout().height() + 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_32666(class_327 font, int x, int y, int width, int height, class_332 graphics) {
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(this.payload.layout().scale());
        try {
            UiDraw.border(graphics, 0, 0, 170, 62, 4, ThemeColors.surface(-14276302), ThemeColors.line());
            int slotColor = ThemeColors.surface(-15724009);
            for (int slot = 0; slot < 27; ++slot) {
                int sx = ShulkerPreviewLayout.slotX(slot);
                int sy = ShulkerPreviewLayout.slotY(slot);
                UiDraw.rounded(graphics, sx, sy, 17, 17, 2, slotColor);
                class_1799 item = this.payload.slots().get(slot);
                if (item.method_7960()) continue;
                graphics.method_55231(item, sx, sy, slot);
                graphics.method_51431(font, item, sx, sy);
            }
        }
        finally {
            graphics.method_51448().popMatrix();
        }
    }
}

