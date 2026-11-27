package ru.bloodhelper.client.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.visuals.OrbitalTracker;
import ru.bloodhelper.client.visuals.WorldOverlay;

public final class OrbitalTracker {
    private static final List<Strike> strikes = new ArrayList<Strike>();
    private static Vec3 preview;
    private static Vec3 pending;
    private static ItemStack pendingItem;
    private static long pendingAt;
    private static Object world;

    private OrbitalTracker() {
    }

    public static boolean cannon(ItemStack stack) {
        return "orbital_nuke".equals(null);
    }

    private static boolean enabled() {
        LegendaryVisualSettings s = BloodHelperClient.config().legendaryVisuals;
        return BloodHelperClient.config().legendaryVisualsEnabled() && s.trails && s.self && s.orbital;
    }

    public static void reset() {
        pending = null;
        preview = null;
        pendingItem = ItemStack.EMPTY;
        pendingAt = 0L;
        strikes.clear();
        world = null;
    }

    public static void onUse(Player player, InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (!OrbitalTracker.enabled() || player != mc.player || mc.level == null || mc.gui.screen() != null || player.isShiftKeyDown()) {
            return;
        }
        ItemStack item = player.getItemInHand(hand);
        if (!OrbitalTracker.cannon(item) || player.getCooldowns().isOnCooldown(item)) {
            return;
        }
        long now = System.nanoTime() / 1000000L;
        if (pending != null && now - pendingAt < 150L) {
            return;
        }
        Vec3 aim = OrbitalTracker.aim(mc);
        if (aim == null) {
            return;
        }
        world = mc.level;
        pending = aim;
        pendingItem = item.copy();
        pendingAt = now;
    }

    private static Vec3 aim(Minecraft mc) {
        Vec3 eye = mc.player.getEyePosition();
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, eye.add(mc.player.getLookAngle().scale(512.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation().add(0.0, 0.055, 0.0);
    }

    public static void tick(Minecraft mc) {
        ItemStack item;
        if (!OrbitalTracker.enabled() || mc.player == null || mc.level == null) {
            OrbitalTracker.reset();
            return;
        }
        if (world != mc.level) {
            OrbitalTracker.reset();
            world = mc.level;
        }
        long now = System.nanoTime() / 1000000L;
        LegendaryVisualSettings settings = BloodHelperClient.config().legendaryVisuals;
        if (pending != null) {
            if (now - pendingAt > 1500L) {
                pending = null;
                pendingItem = ItemStack.EMPTY;
            } else if (mc.player.getCooldowns().isOnCooldown(pendingItem)) {
                strikes.add(new Strike(pending, now, settings.orbitalRadius));
                while (strikes.size() > 4) {
                    strikes.removeFirst();
                }
                pending = null;
                pendingItem = ItemStack.EMPTY;
            }
        }
        strikes.removeIf(s -> (double)(now - s.started) > (settings.orbitalSeconds + 0.45) * 1000.0);
        preview = null;
        ItemStack itemStack = item = OrbitalTracker.cannon(mc.player.getMainHandItem()) ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
        if (settings.orbitalPreview && mc.gui.screen() == null && !mc.player.isShiftKeyDown() && OrbitalTracker.cannon(item) && !mc.player.getCooldowns().isOnCooldown(item)) {
            preview = OrbitalTracker.aim(mc);
        }
    }

    public static void extract() {
        Vec3 aim;
        if (!OrbitalTracker.enabled()) {
            return;
        }
        LegendaryVisualSettings s = BloodHelperClient.config().legendaryVisuals;
        long now = System.nanoTime() / 1000000L;
        Minecraft mc = Minecraft.getInstance();
        if (preview != null && mc.player != null && !mc.player.isShiftKeyDown() && (aim = OrbitalTracker.aim(mc)) != null) {
            OrbitalTracker.draw(aim, s.orbitalRadius, UiDraw.alpha((int)s.orbitalColor, (int)165), true);
        }
        for (Strike strike : strikes) {
            double age = (double)(now - strike.started) / 1000.0;
            double alpha = Math.min(Math.clamp(age / 0.12, 0.0, 1.0), Math.clamp((s.orbitalSeconds + 0.45 - age) / 0.45, 0.0, 1.0));
            OrbitalTracker.draw(strike.center, strike.radius, UiDraw.alpha((int)s.orbitalColor, (int)((int)(230.0 * alpha))), false);
        }
    }

    private static void draw(Vec3 center, double radius, int color, boolean aiming) {
        WorldOverlay.ring((Vec3)center, (double)radius, (int)color);
        WorldOverlay.ring((Vec3)center, (double)0.3, (int)UiDraw.alpha((int)color, (int)((int)((double)(color >>> 24) * 0.7))));
        WorldOverlay.line((Vec3)center, (Vec3)center.add(0.0, aiming ? 2.2 : 5.0, 0.0), (int)color, (float)1.7f);
        if (BloodHelperClient.config().legendaryVisuals.names) {
            WorldOverlay.label((Vec3)center.add(0.0, 2.4, 0.0), (String)((aiming ? "\u0421\u0442\u0430\u0431\u0448\u043e\u0442 \u00b7 \u2248" : "\u0421\u0442\u0430\u0431\u0448\u043e\u0442 \u00b7 \u0443\u0434\u0430\u0440 \u00b7 \u2248") + Math.round(radius) + " \u043c"), (int)color);
        }
    }

    public static Vec3 preview() {
        return preview;
    }

    public static List<Strike> strikes() {
        return List.copyOf(strikes);
    }

    static {
        pendingItem = ItemStack.EMPTY;
    }
}

