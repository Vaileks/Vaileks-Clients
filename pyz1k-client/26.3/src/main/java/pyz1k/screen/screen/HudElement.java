/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 */
package pyz1k.screen.screen;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import pyz1k.module.setting.Setting;

public interface HudElement {
    public String getId();

    public String getDisplayName();

    public float getX();

    public float getY();

    public void setPosition(float var1, float var2);

    default public float getVisualX() {
        return this.getX();
    }

    default public float getVisualY() {
        return this.getY();
    }

    default public void setVisualPosition(float x, float y) {
        this.setPosition(x, y);
    }

    public float getWidth();

    public float getHeight();

    public List<Setting> getSettings();

    public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4);

    default public void setActive(boolean active) {
    }

    default public boolean shouldRenderWhenInactive() {
        return false;
    }

    default public boolean canInteractInEditor(Minecraft mc) {
        return mc != null && mc.gui.screen() instanceof ChatScreen;
    }

    default public boolean allowHorizontalDrag() {
        return true;
    }

    default public boolean allowVerticalDrag() {
        return true;
    }

    default public boolean handleLeftClick(Minecraft mc, float mouseX, float mouseY, int screenWidth, int screenHeight) {
        return false;
    }
}

