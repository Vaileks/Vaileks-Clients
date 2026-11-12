/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  net.minecraft.class_7923
 *  net.minecraft.class_9279
 *  net.minecraft.class_9290
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.AbilityGuideSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.modules.AbilityGuideScreen;

public final class AbilityGuideModule {
    private static class_304 key;

    private AbilityGuideModule() {
    }

    public static void register(class_304.class_11900 category) {
        key = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.ability_guide", class_3675.class_307.field_1668, class_3675.field_16237.method_1444(), category));
    }

    public static void tick(class_310 client) {
        boolean pressed = false;
        if (key != null) {
            while (key.method_1436()) {
                pressed = true;
            }
        }
        if (pressed && client.field_1755 == null && client.field_1724 != null && BloodHelperClient.config().abilityGuide.enabled) {
            AbilityGuideModule.openScreen(null);
        }
    }

    public static void openScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new AbilityGuideScreen(parent, AbilityGuideModule.captureHeld()));
    }

    public static String keyName() {
        return key == null ? "None" : key.method_16007().getString();
    }

    public static AbilityGuideSettings.Entry captureHeld() {
        class_310 client = class_310.method_1551();
        if (!BloodHelperClient.config().abilityGuide.enabled || client.field_1724 == null) {
            return null;
        }
        class_1799 item = client.field_1724.method_6047();
        if (!AbilityGuideModule.eligible(item)) {
            item = client.field_1724.method_6079();
        }
        if (!AbilityGuideModule.eligible(item)) {
            return null;
        }
        AbilityGuideSettings.Entry entry = new AbilityGuideSettings.Entry();
        entry.name = AbilityGuideSettings.clean(UiLanguage.text(item.method_7964()), 128);
        entry.itemId = class_7923.field_41178.method_10221((Object)item.method_7909()).toString();
        class_2960 model = (class_2960)item.method_58694(class_9334.field_54199);
        class_9279 custom = (class_9279)item.method_58694(class_9334.field_49628);
        String type = custom == null ? "" : custom.method_57461().method_68568("PublicBukkitValues").method_68564("ulegendary:weapon_type", "");
        String source = client.method_1558() == null ? "local" : client.method_1558().field_3761.strip().toLowerCase(Locale.ROOT);
        entry.id = AbilityGuideModule.identity(source, entry.itemId, type.isEmpty() ? String.valueOf(model) : type, item.method_7964().getString());
        class_9290 lore = (class_9290)item.method_58694(class_9334.field_49632);
        if (lore != null) {
            for (class_2561 line : lore.comp_2400().stream().limit(64L).toList()) {
                entry.description.add(AbilityGuideSettings.clean(UiLanguage.text(line), 512));
            }
        }
        AbilityGuideSettings.Entry saved = BloodHelperClient.config().abilityGuide.remember(entry);
        BloodHelperClient.saveConfig();
        return saved;
    }

    private static boolean eligible(class_1799 item) {
        if (item.method_7960()) {
            return false;
        }
        class_9290 lore = (class_9290)item.method_58694(class_9334.field_49632);
        class_2960 model = (class_2960)item.method_58694(class_9334.field_54199);
        return lore != null && !lore.comp_2400().isEmpty() || model != null && model.method_12836().equals("bloodstone");
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

