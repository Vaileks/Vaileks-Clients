/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemLore
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.AbilityGuideSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.modules.AbilityGuideScreen;

public final class AbilityGuideModule {
    private static KeyMapping key;

    private AbilityGuideModule() {
    }

    public static void register(KeyMapping.Category category) {
        key = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.ability_guide", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
    }

    public static void tick(Minecraft client) {
        boolean pressed = false;
        if (key != null) {
            while (key.consumeClick()) {
                pressed = true;
            }
        }
        if (pressed && client.gui.screen() == null && client.player != null && BloodHelperClient.config().abilityGuide.enabled) {
            AbilityGuideModule.openScreen(null);
        }
    }

    public static void openScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new AbilityGuideScreen(parent, AbilityGuideModule.captureHeld()));
    }

    public static String keyName() {
        return key == null ? "None" : key.getTranslatedKeyMessage().getString();
    }

    public static AbilityGuideSettings.Entry captureHeld() {
        Minecraft client = Minecraft.getInstance();
        if (!BloodHelperClient.config().abilityGuide.enabled || client.player == null) {
            return null;
        }
        ItemStack item = client.player.getMainHandItem();
        if (!AbilityGuideModule.eligible(item)) {
            item = client.player.getOffhandItem();
        }
        if (!AbilityGuideModule.eligible(item)) {
            return null;
        }
        AbilityGuideSettings.Entry entry = new AbilityGuideSettings.Entry();
        entry.name = AbilityGuideSettings.clean(UiLanguage.text(item.getHoverName()), 128);
        entry.itemId = BuiltInRegistries.ITEM.getKey((Object)item.getItem()).toString();
        Identifier model = (Identifier)item.get(DataComponents.ITEM_MODEL);
        CustomData custom = (CustomData)item.get(DataComponents.CUSTOM_DATA);
        String type = custom == null ? "" : custom.copyTag().getCompoundOrEmpty("PublicBukkitValues").getStringOr("ulegendary:weapon_type", "");
        String source = client.getCurrentServer() == null ? "local" : client.getCurrentServer().ip.strip().toLowerCase(Locale.ROOT);
        entry.id = AbilityGuideModule.identity(source, entry.itemId, type.isEmpty() ? String.valueOf(model) : type, item.getHoverName().getString());
        ItemLore lore = (ItemLore)item.get(DataComponents.LORE);
        if (lore != null) {
            for (Component line : lore.lines().stream().limit(64L).toList()) {
                entry.description.add(AbilityGuideSettings.clean(UiLanguage.text(line), 512));
            }
        }
        AbilityGuideSettings.Entry saved = BloodHelperClient.config().abilityGuide.remember(entry);
        BloodHelperClient.saveConfig();
        return saved;
    }

    private static boolean eligible(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }
        ItemLore lore = (ItemLore)item.get(DataComponents.LORE);
        Identifier model = (Identifier)item.get(DataComponents.ITEM_MODEL);
        return lore != null && !lore.lines().isEmpty() || model != null && model.getNamespace().equals("bloodstone");
    }

    static String identity(String server, String itemId, String model, String name) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((server + "\u0000" + itemId + "\u0000" + model + "\u0000" + name).getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError((Object)impossible);
        }
    }
}

