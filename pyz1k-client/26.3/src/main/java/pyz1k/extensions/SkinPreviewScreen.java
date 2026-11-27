/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.client.renderer.entity.state.ItemEntityRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package pyz1k.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import pyz1k.extensions.SkinCatalog;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;

public final class SkinPreviewScreen
extends Screen {
    private final Screen parent;
    private final SkinCatalog.Skin skin;
    private final Item target;
    private float yaw = 25.0f;
    private float pitch = -15.0f;
    private float zoom = 1.0f;
    private boolean rotating;
    private int x;
    private int y;
    private int w;
    private int h;

    public SkinPreviewScreen(Screen parent, Item target, SkinCatalog.Skin skin) {
        super((Component)Component.literal((String)"ItemSkin · 3D"));
        this.parent = parent;
        this.target = target;
        this.skin = skin;
    }

    protected void init() {
        this.w = Math.min(450, this.width - 16);
        this.h = Math.min(340, this.height - 16);
        this.x = (this.width - this.w) / 2;
        this.y = (this.height - this.h) / 2;
    }

    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        HudVisualStyle.drawMenuPanel(ctx, this.x, this.y, this.w, this.h, this.w, 1.0f);
        RenderUtil.drawText(ctx, "ItemSkin · 3D", (float)(this.x + 15), (float)(this.y + 12), ClientColors.TEXT_PRIMARY, GFonts.MEDIUM, 10);
        ItemStack stack = this.skin.preview(this.target);
        ItemEntityRenderState state = new ItemEntityRenderState();
        state.entityType = net.minecraft.world.entity.EntityTypes.ITEM;
        state.boundingBoxHeight = 1.0f;
        state.boundingBoxWidth = 1.0f;
        state.lightCoords = 0xF000F0;
        state.count = 1;
        SkinCatalog.drawOriginal(() -> Minecraft.getInstance().getItemModelResolver().updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, (Level)Minecraft.getInstance().level, null, 0));
        AABB box = state.item.getModelBoundingBox();
        float dx = (float)(box.maxX - box.minX);
        float dy = (float)(box.maxY - box.minY);
        float dz = (float)(box.maxZ - box.minZ);
        float diameter = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (!Float.isFinite(diameter) || diameter < 0.01f) {
            diameter = 1.0f;
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI).rotateX((float)Math.toRadians(this.pitch)).rotateY((float)Math.toRadians(this.yaw));
        Vector3f center = new Vector3f((float)(box.minX + box.maxX) * 0.5f, dy * 0.5f + 0.1625f, (float)(box.minZ + box.maxZ) * 0.5f).rotate((Quaternionfc)rotation).negate();
        ctx.entity((EntityRenderState)state, (float)Math.min(this.w - 30, this.h - 100) * 0.75f * this.zoom / diameter, center, rotation, null, this.x + 15, this.y + 35, this.x + this.w - 15, this.y + this.h - 65);
        RenderUtil.drawText(ctx, "ЛКМ: вращение · Колесо: масштаб · ПКМ: сброс", (float)(this.x + 15), (float)(this.y + this.h - 58), ClientColors.CATEGORY_INACTIVE, GFonts.MEDIUM, 7);
        ctx.text(this.font, this.font.plainSubstrByWidth(stack.getHoverName().getString(), this.w - 30), this.x + 15, this.y + this.h - 44, ClientColors.TEXT_PRIMARY, false);
        RenderUtil.drawRoundRect(ctx, (float)(this.x + 15), (float)(this.y + this.h - 27), 100.0f, 19.0f, 4.0f, ClientColors.CATEGORY_ACCENT);
        RenderUtil.drawText(ctx, "Применить", (float)(this.x + 25), (float)(this.y + this.h - 21), ClientColors.TEXT_PRIMARY, GFonts.MEDIUM, 8);
        RenderUtil.drawRoundRect(ctx, (float)(this.x + this.w - 90), (float)(this.y + this.h - 27), 75.0f, 19.0f, 4.0f, ClientColors.SETTINGS_BACKGROUND);
        RenderUtil.drawText(ctx, "Назад", (float)(this.x + this.w - 78), (float)(this.y + this.h - 21), ClientColors.TEXT_PRIMARY, GFonts.MEDIUM, 8);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) == 1) {
            this.yaw = 25.0f;
            this.pitch = -15.0f;
            this.zoom = 1.0f;
            return true;
        }
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) == 0) {
            if (click.y() >= (double)(this.y + this.h - 27) && click.y() <= (double)(this.y + this.h - 8)) {
                if (click.x() >= (double)(this.x + 15) && click.x() <= (double)(this.x + 115)) {
                    SkinCatalog.select(this.target, this.skin);
                    this.onClose();
                    return true;
                }
                if (click.x() >= (double)(this.x + this.w - 90) && click.x() <= (double)(this.x + this.w - 15)) {
                    this.onClose();
                    return true;
                }
            }
            this.rotating = click.x() > (double)(this.x + 15) && click.x() < (double)(this.x + this.w - 15) && click.y() > (double)(this.y + 35) && click.y() < (double)(this.y + this.h - 65);
            return this.rotating;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (this.rotating) {
            this.yaw += (float)dx;
            this.pitch = Math.clamp(this.pitch + (float)dy, -85.0f, 85.0f);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        this.rotating = false;
        return true;
    }

    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        this.zoom = Math.clamp(this.zoom + (float)vertical * 0.1f, 0.4f, 2.5f);
        return true;
    }

    public boolean keyPressed(KeyEvent key) {
        if (pyz1k.compat.LegacyInput.fromNativeKey(key.key()) == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(key);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

