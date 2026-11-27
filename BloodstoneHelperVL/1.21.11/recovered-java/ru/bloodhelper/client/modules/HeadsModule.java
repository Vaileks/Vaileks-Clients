/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_11719$class_11881
 *  net.minecraft.class_11724
 *  net.minecraft.class_11885
 *  net.minecraft.class_1299
 *  net.minecraft.class_2561
 *  net.minecraft.class_2568
 *  net.minecraft.class_2568$class_10611
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 *  net.minecraft.class_634
 *  net.minecraft.class_640
 *  net.minecraft.class_9296
 */
package ru.bloodhelper.client.modules;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11719;
import net.minecraft.class_11724;
import net.minecraft.class_11885;
import net.minecraft.class_1299;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_9296;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.HeadsSettings;
import ru.bloodhelper.client.modules.StreamerMode;

public final class HeadsModule {
    private static final boolean EXTERNAL_CHAT_HEADS = FabricLoader.getInstance().isModLoaded("chat_heads") || FabricLoader.getInstance().isModLoaded("chatheads");

    private HeadsModule() {
    }

    public static boolean externalChatHeads() {
        return EXTERNAL_CHAT_HEADS;
    }

    public static boolean containsHead(class_2561 message) {
        return message != null && message.method_27658((style, text) -> style.method_27708() instanceof class_11719.class_11881 ? Optional.of(Boolean.TRUE) : Optional.empty(), class_2583.field_24360).orElse(false) != false;
    }

    public static class_2561 withoutOwnHead(class_2561 original) {
        List parts = original.method_10855();
        return parts.size() == 3 && "bloodhelper:heads".equals(((class_2561)parts.getFirst()).method_10866().method_10955()) ? (class_2561)parts.get(2) : original;
    }

    public static class_2561 decorate(class_2561 original) {
        return HeadsModule.decorate(original, original);
    }

    public static class_2561 decorate(class_2561 original, class_2561 identitySource) {
        class_2561 previousHead = identitySource.method_10855().size() == 3 && "bloodhelper:heads".equals(((class_2561)identitySource.method_10855().getFirst()).method_10866().method_10955()) ? (class_2561)identitySource.method_10855().getFirst() : null;
        original = HeadsModule.withoutOwnHead(original);
        identitySource = HeadsModule.withoutOwnHead(identitySource);
        HeadsSettings cfg = BloodHelperClient.config().heads;
        if (!cfg.enabled || !cfg.chat || EXTERNAL_CHAT_HEADS || StreamerMode.hideChat() || StreamerMode.hideNickname() || HeadsModule.containsHead(original)) {
            return original;
        }
        if (previousHead != null) {
            return class_2561.method_43473().method_10852(previousHead).method_27693(" ").method_10852(original);
        }
        class_634 connection = class_310.method_1551().method_1562();
        if (connection == null) {
            return original;
        }
        class_640[] sender = new class_640[]{null};
        int[] offset = new int[]{0};
        identitySource.method_27658((style, text) -> {
            if (offset[0] > 160) {
                return Optional.of(Boolean.TRUE);
            }
            offset[0] = offset[0] + text.length();
            class_2568 patt0$temp = style.method_10969();
            if (patt0$temp instanceof class_2568.class_10611) {
                class_2568.class_10611 entity = (class_2568.class_10611)patt0$temp;
                if (entity.comp_3508().field_24351 == class_1299.field_6097) {
                    sender[0] = connection.method_2871(entity.comp_3508().field_24352);
                }
            }
            return sender[0] == null ? Optional.empty() : Optional.of(Boolean.TRUE);
        }, class_2583.field_24360);
        if (sender[0] == null) {
            String prefix = identitySource.method_10858(160);
            for (class_640 info : connection.method_2880()) {
                String name = info.method_2966().name();
                if (!prefix.matches("^\\s*(?:<|\\[)?" + Pattern.quote(name) + "(?:>|\\])?\\s*[:\u00bb>].*") && !prefix.startsWith("<" + name + "> ") && !prefix.startsWith("[" + name + "] ")) continue;
                sender[0] = info;
                break;
            }
        }
        if (sender[0] == null) {
            return original;
        }
        class_5250 head = class_2561.method_74062((class_11724)new class_11885(class_9296.method_73307((GameProfile)sender[0].method_2966()), true)).method_27694(style -> style.method_10975("bloodhelper:heads"));
        return class_2561.method_43473().method_10852((class_2561)head).method_27693(" ").method_10852(original);
    }

    public static boolean tabHeads(boolean alreadyVisible) {
        if (alreadyVisible) {
            return true;
        }
        HeadsSettings cfg = BloodHelperClient.config().heads;
        if (!cfg.enabled || !cfg.tab || StreamerMode.hideNickname() || StreamerMode.hideNametags()) {
            return false;
        }
        class_634 connection = class_310.method_1551().method_1562();
        if (connection == null) {
            return false;
        }
        for (class_640 info : connection.method_2880()) {
            if (!HeadsModule.containsHead(info.method_2971())) continue;
            return false;
        }
        return true;
    }
}

