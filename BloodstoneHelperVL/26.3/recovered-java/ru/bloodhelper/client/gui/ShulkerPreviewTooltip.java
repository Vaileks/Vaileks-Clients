/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.modules.ShulkerPreview;
import ru.bloodhelper.client.modules.ShulkerPreviewLayout;

public final class ShulkerPreviewTooltip
implements ClientTooltipComponent {
    private final ShulkerPreview.Payload payload;

    public ShulkerPreviewTooltip(ShulkerPreview.Payload payload) {
        this.payload = payload;
    }

    public int getWidth(Font font) {
        return this.payload.layout().width();
    }

    public int getHeight(Font font) {
        return this.payload.layout().height() + 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)x, (float)y);
        graphics.pose().scale(this.payload.layout().scale());
        try {
            UiDraw.border(graphics, 0, 0, 170, 62, 4, ThemeColors.surface(-14276302), ThemeColors.line());
            int slotColor = ThemeColors.surface(-15724009);
            for (int slot = 0; slot < 27; ++slot) {
                int sx = ShulkerPreviewLayout.slotX(slot);
                int sy = ShulkerPreviewLayout.slotY(slot);
                UiDraw.rounded(graphics, sx, sy, 17, 17, 2, slotColor);
                ItemStack item = this.payload.slots().get(slot);
                if (item.isEmpty()) continue;
                graphics.fakeItem(item, sx, sy, slot);
                graphics.itemDecorations(font, item, sx, sy);
            }
        }
        finally {
            graphics.pose().popMatrix();
        }
    }
}

