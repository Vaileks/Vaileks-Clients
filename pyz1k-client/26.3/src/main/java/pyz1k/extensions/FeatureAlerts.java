/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 */
package pyz1k.extensions;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import pyz1k.extensions.ClientFeatureLogic;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.extensions.SkinCatalog;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;

public final class FeatureAlerts {
    private static final ArrayDeque<Notice> notices = new ArrayDeque();
    private static Set<String> low = Set.of();
    private static int ticks;
    private static long lastSound;

    public static void reset() {
        low = Set.of();
        notices.clear();
        ticks = 0;
        lastSound = 0L;
    }

    public static void notice(String title, Component detail, ItemStack icon, boolean sound) {
        while (notices.size() >= 4) {
            notices.removeFirst();
        }
        long now = System.currentTimeMillis();
        notices.addLast(new Notice(title, (Component)detail.copy(), icon.copy(), now + 6000L));
        if (sound && now - lastSound >= 2000L) {
            lastSound = now;
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.NOTE_BLOCK_PLING, (float)1.2f));
        }
    }

    public static void tick() {
        if(FeatureModules.durability==null||!FeatureModules.durability.isEnable()){if(!low.isEmpty())low=Set.of();return;}
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            FeatureAlerts.reset();
            return;
        }
        if (++ticks % 4 != 0) {
            return;
        }
        FeatureModules.Durability durability = FeatureModules.durability;
        if (durability != null && durability.isEnable()) {
            HashSet<String> now = new HashSet<String>();
            FeatureAlerts.checkDurability("main", mc.player.getMainHandItem(), now, durability);
            FeatureAlerts.checkDurability("off", mc.player.getOffhandItem(), now, durability);
            if (durability.armor.isValue()) {
                for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                    FeatureAlerts.checkDurability(slot.getName(), mc.player.getItemBySlot(slot), now, durability);
                }
            }
            low = now;
        } else {
            low = Set.of();
        }
    }

    private static void checkDurability(String slot, ItemStack stack, Set<String> now, FeatureModules.Durability module) {
        if (!stack.isDamageableItem() || !ClientFeatureLogic.lowDurability(stack.getMaxDamage(), stack.getDamageValue(), Math.round(((Float)module.threshold.getValue()).floatValue()))) {
            return;
        }
        String key = slot + ":" + FeatureAlerts.signature(stack);
        now.add(key);
        if (!low.contains(key)) {
            FeatureAlerts.notice("Низкая прочность: " + Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) + " / " + stack.getMaxDamage(), stack.getHoverName(), stack, module.sound.isValue());
        }
    }

    private static String signature(ItemStack stack) {
        return String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem())) + "|" + String.valueOf(stack.get(DataComponents.ITEM_MODEL)) + "|" + String.valueOf(stack.get(DataComponents.CUSTOM_MODEL_DATA)) + "|" + stack.getHoverName().getString();
    }

    public static void render(GuiGraphicsExtractor ctx) {
        if(notices.isEmpty())return;
        if (ScreenshotMode.active() || Minecraft.getInstance().gui.hud.isHidden()) {
            return;
        }
        long now = System.currentTimeMillis();
        notices.removeIf(n -> n.until < now);
        Minecraft mc = Minecraft.getInstance();
        int x = Math.max(4, mc.getWindow().getGuiScaledWidth() - 250);
        int y = 38;
        for (Notice n2 : notices) {
            HudVisualStyle.drawPanel(ctx, x, y, 242.0f, 39.0f, 1.0f);
            RenderUtil.drawText(ctx, n2.title, (float)(x + 29), (float)(y + 6), ClientColors.HUD_TEXT, GFonts.MEDIUM, 7);
            List lines = mc.font.split((FormattedText)n2.detail, 204);
            if (!lines.isEmpty()) {
                ctx.text(mc.font, (FormattedCharSequence)lines.getFirst(), x + 29, y + 21, ClientColors.HUD_SECONDARY_TEXT, false);
            }
            int iy = y + 11;
            SkinCatalog.drawOriginal(() -> ctx.item(n2.icon, x + 7, iy));
            y += 44;
        }
    }

    private record Notice(String title, Component detail, ItemStack icon, long until) {
    }
}

