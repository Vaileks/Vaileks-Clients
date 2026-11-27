/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.class_2561
 *  net.minecraft.class_2588
 *  net.minecraft.class_310
 *  net.minecraft.class_5218
 *  net.minecraft.class_7417
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_5218;
import net.minecraft.class_7417;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.hud.FfaKillReceipt;
import ru.bloodhelper.client.hud.KillMessageTemplate;
import ru.bloodhelper.client.hud.MessageDeliveryGuard;
import ru.bloodhelper.client.hud.PerformanceTracker;
import ru.bloodhelper.client.hud.SessionState;

public final class SessionTracker {
    private static final SessionState STATE = new SessionState();
    private static boolean registered;
    private static final MessageDeliveryGuard DELIVERIES;
    private static String deliveryContext;
    private static String templateText;
    private static KillMessageTemplate template;
    private static final Set<String> DIRECT;
    private static final Set<String> ASSISTED;

    private SessionTracker() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) {
                return;
            }
            class_310 mc = class_310.method_1551();
            ClientConfig config = BloodHelperClient.config();
            if (mc.field_1724 == null || mc.field_1687 == null || !config.session.enabled || mc.method_1493()) {
                return;
            }
            SessionTracker.tick(mc, config);
            if (!DELIVERIES.accept(message)) {
                return;
            }
            long now = System.nanoTime();
            if ("FFA".equals(config.session.mode)) {
                FfaKillReceipt.parse(message.getString()).ifPresent(receipt -> STATE.confirmFfa((FfaKillReceipt)receipt, true, false, now));
            } else {
                Optional<KillMessageTemplate.Kill> kill = SessionTracker.translatedKill(message);
                if (kill.isPresent() && mc.method_1562().method_2874(kill.get().victim()) == null) {
                    kill = Optional.empty();
                }
                if (kill.isEmpty()) {
                    kill = template.parse(message.getString());
                }
                kill.ifPresent(value -> STATE.confirm((KillMessageTemplate.Kill)value, true, false, now));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            STATE.disconnect(BloodHelperClient.config().session.resetOnDisconnect);
            DELIVERIES.clear();
            deliveryContext = "";
            PerformanceTracker.reset();
        });
    }

    public static void tick(class_310 mc, ClientConfig config) {
        String identity;
        String context;
        if (mc.field_1724 == null || mc.field_1687 == null || mc.method_1562() == null) {
            STATE.disconnect(false);
            return;
        }
        if (!config.session.killMessageTemplate.equals(templateText)) {
            templateText = config.session.killMessageTemplate;
            template = KillMessageTemplate.compile(templateText);
        }
        if (!(context = (identity = SessionTracker.identity(mc)) + "|" + config.session.mode).equals(deliveryContext)) {
            DELIVERIES.clear();
            deliveryContext = context;
        }
        STATE.tick(identity, mc.field_1724.method_7334().name(), config.session.mode, mc.field_1724.method_5805(), config.session.enabled, mc.method_1493(), System.nanoTime());
    }

    public static void reset() {
        STATE.reset();
        DELIVERIES.clear();
    }

    public static Snapshot snapshot() {
        SessionState.Snapshot s = STATE.snapshot();
        return new Snapshot(s.mode(), s.kills(), s.rewardedKills(), s.deaths(), s.streak(), s.playSeconds(), s.killFeedAvailable());
    }

    private static String identity(class_310 mc) {
        String server = mc.method_1576() != null ? "local:" + String.valueOf(mc.method_1576().method_27050(class_5218.field_24188).toAbsolutePath().normalize()) : (mc.method_1558() != null ? "remote:" + mc.method_1558().field_3761.toLowerCase(Locale.ROOT).strip() : "connection:" + System.identityHashCode(mc.method_1562()));
        return server + "|" + String.valueOf(mc.field_1724.method_5667());
    }

    private static Optional<KillMessageTemplate.Kill> translatedKill(class_2561 message) {
        boolean known;
        class_7417 class_74172;
        if (!message.method_10855().isEmpty() || !((class_74172 = message.method_10851()) instanceof class_2588)) {
            return Optional.empty();
        }
        class_2588 contents = (class_2588)class_74172;
        String key = contents.method_11022();
        boolean item = key.endsWith(".item");
        String base = item ? key.substring(0, key.length() - 5) : key;
        boolean bl = known = base.equals("death.fell.assist") || base.equals("death.fell.finish");
        if (base.startsWith("death.attack.")) {
            String type = base.substring("death.attack.".length());
            known = DIRECT.contains(type) || type.endsWith(".player") && ASSISTED.contains(type.substring(0, type.length() - 7)) || item && (type.equals("fireworks") || type.equals("onFire"));
        }
        Object[] args = contents.method_11023();
        if (!known || args.length != (item ? 3 : 2)) {
            return Optional.empty();
        }
        String victim = SessionTracker.argumentName(args[0]);
        String killer = SessionTracker.argumentName(args[1]);
        if (!KillMessageTemplate.isName(victim) || !KillMessageTemplate.isName(killer)) {
            return Optional.empty();
        }
        return Optional.of(new KillMessageTemplate.Kill(killer, victim));
    }

    private static String argumentName(Object value) {
        String string;
        if (value instanceof class_2561) {
            class_2561 component = (class_2561)value;
            string = component.getString();
        } else {
            String name;
            string = value instanceof String ? (name = (String)value) : "";
        }
        return string;
    }

    static {
        DELIVERIES = new MessageDeliveryGuard();
        deliveryContext = "";
        templateText = "";
        template = KillMessageTemplate.compile("");
        DIRECT = Set.of("mob", "player", "arrow", "trident", "thrown", "indirectMagic", "fireball", "witherSkull", "explosion.player", "sonic_boom", "sting", "thorns", "mace_smash", "spear");
        ASSISTED = Set.of("inFire", "lightningBolt", "onFire", "lava", "hotFloor", "inWall", "cramming", "drown", "starve", "cactus", "fall", "flyIntoWall", "outOfWorld", "generic", "magic", "wither", "anvil", "fallingBlock", "fallingStalactite", "dragonBreath", "dryout", "sweetBerryBush", "freeze", "stalagmite", "outsideBorder", "fireworks", "genericKill", "sonic_boom", "sting");
    }

    public record Snapshot(String mode, int kills, int rewardedKills, int deaths, int streak, long playSeconds, boolean killFeedAvailable) {
    }
}

