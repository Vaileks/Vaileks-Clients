/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.level.storage.LevelResource
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.storage.LevelResource;
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
            Minecraft mc = Minecraft.getInstance();
            ClientConfig config = BloodHelperClient.config();
            if (mc.player == null || mc.level == null || !config.session.enabled || mc.isPaused()) {
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
                if (kill.isPresent() && mc.getConnection().getPlayerInfo(kill.get().victim()) == null) {
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

    public static void tick(Minecraft mc, ClientConfig config) {
        String identity;
        String context;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) {
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
        STATE.tick(identity, mc.player.getGameProfile().name(), config.session.mode, mc.player.isAlive(), config.session.enabled, mc.isPaused(), System.nanoTime());
    }

    public static void reset() {
        STATE.reset();
        DELIVERIES.clear();
    }

    public static Snapshot snapshot() {
        SessionState.Snapshot s = STATE.snapshot();
        return new Snapshot(s.mode(), s.kills(), s.rewardedKills(), s.deaths(), s.streak(), s.playSeconds(), s.killFeedAvailable());
    }

    private static String identity(Minecraft mc) {
        String server = mc.getSingleplayerServer() != null ? "local:" + String.valueOf(mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize()) : (mc.getCurrentServer() != null ? "remote:" + mc.getCurrentServer().ip.toLowerCase(Locale.ROOT).strip() : "connection:" + System.identityHashCode(mc.getConnection()));
        return server + "|" + String.valueOf(mc.player.getUUID());
    }

    private static Optional<KillMessageTemplate.Kill> translatedKill(Component message) {
        boolean known;
        ComponentContents componentContents;
        if (!message.getSiblings().isEmpty() || !((componentContents = message.getContents()) instanceof TranslatableContents)) {
            return Optional.empty();
        }
        TranslatableContents contents = (TranslatableContents)componentContents;
        String key = contents.getKey();
        boolean item = key.endsWith(".item");
        String base = item ? key.substring(0, key.length() - 5) : key;
        boolean bl = known = base.equals("death.fell.assist") || base.equals("death.fell.finish");
        if (base.startsWith("death.attack.")) {
            String type = base.substring("death.attack.".length());
            known = DIRECT.contains(type) || type.endsWith(".player") && ASSISTED.contains(type.substring(0, type.length() - 7)) || item && (type.equals("fireworks") || type.equals("onFire"));
        }
        Object[] args = contents.getArgs();
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
        if (value instanceof Component) {
            Component component = (Component)value;
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

