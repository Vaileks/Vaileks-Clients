/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.class_2561
 *  net.minecraft.class_437
 *  net.minecraft.class_634
 */
package ru.bloodhelper.client.tiers;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_634;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.tiers.TierLookupScreen;

public final class TierLookupCommand {
    private static String pendingName;

    private TierLookupCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal((String)"checktiers").executes(context -> {
            ((FabricClientCommandSource)context.getSource()).sendFeedback((class_2561)class_2561.method_43470((String)HudStyle.tr("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: /checktiers <\u043d\u0438\u043a>", "Usage: /checktiers <name>")));
            return 1;
        })).then(ClientCommandManager.argument((String)"nickname", (ArgumentType)StringArgumentType.word()).suggests((context, builder) -> {
            String prefix = builder.getRemainingLowerCase();
            class_634 connection = ((FabricClientCommandSource)context.getSource()).getClient().method_1562();
            if (connection != null) {
                connection.method_2880().stream().limit(256L).map(player -> player.method_2966().name()).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix)).forEach(arg_0 -> ((SuggestionsBuilder)builder).suggest(arg_0));
            }
            return builder.buildFuture();
        }).executes(context -> {
            String name = StringArgumentType.getString((CommandContext)context, (String)"nickname");
            if (!name.matches("[A-Za-z0-9_]{1,16}")) {
                ((FabricClientCommandSource)context.getSource()).sendError((class_2561)class_2561.method_43470((String)HudStyle.tr("\u041d\u0438\u043a: 1\u201316 \u043b\u0430\u0442\u0438\u043d\u0441\u043a\u0438\u0445 \u0431\u0443\u043a\u0432, \u0446\u0438\u0444\u0440 \u0438\u043b\u0438 _.", "Use 1\u201316 letters, numbers or _ for a player name.")));
                return 0;
            }
            if (!BloodHelperClient.config().serverAllowsModule("otherTiers")) {
                ((FabricClientCommandSource)context.getSource()).sendError((class_2561)class_2561.method_43470((String)HudStyle.tr("\u041f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0442\u0438\u0440\u043e\u0432 \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d \u043f\u0440\u0430\u0432\u0438\u043b\u0430\u043c\u0438 \u043c\u043e\u0434\u0430.", "Tier lookups are disabled by the module policy.")));
                return 0;
            }
            pendingName = name;
            return 1;
        }))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingName == null) {
                return;
            }
            String name = pendingName;
            pendingName = null;
            if (client.field_1724 != null && BloodHelperClient.config().serverAllowsModule("otherTiers")) {
                client.method_1507((class_437)new TierLookupScreen(name));
            }
        });
    }
}

