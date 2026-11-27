/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.OptimizerSettings;
import ru.bloodhelper.client.modules.DroppedItemFilter;

public final class OptimizerModule {
    private static volatile OptimizerSettings.Snapshot active = new OptimizerSettings().snapshot();
    private static boolean rebuildPending;
    private static long rebuildAfter;
    private static Object level;

    private OptimizerModule() {
    }

    public static void settingsChanged() {
        OptimizerSettings.Snapshot next = BloodHelperClient.config().optimizer.snapshot();
        OptimizerSettings settings = BloodHelperClient.config().optimizer;
        DroppedItemFilter.configure(settings.enabled, settings.droppedItems);
        boolean geometryChanged = next.hideGrass() != active.hideGrass();
        active = next;
        if (geometryChanged) {
            rebuildPending = true;
            rebuildAfter = System.nanoTime() + 250000000L;
        }
    }

    public static void tick(Minecraft client) {
        if (level != client.level) {
            level = client.level;
            rebuildPending = false;
        }
        if (rebuildPending && client.level != null && System.nanoTime() >= rebuildAfter) {
            rebuildPending = false;
            client.levelRenderer.invalidateCompiledGeometry(client.level, client.options, client.gameRenderer.mainCamera(), client.getBlockColors());
        }
    }

    public static boolean hideGrass(Block block) {
        return active.hideGrass() && (block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN);
    }

    public static boolean hideSign(BlockPos pos) {
        OptimizerSettings.Snapshot settings = active;
        return settings.signs() && settings.hideSign(OptimizerModule.distanceSquared(pos));
    }

    public static boolean hideSignText(BlockPos pos) {
        OptimizerSettings.Snapshot settings = active;
        return settings.signs() && settings.hideSignText(OptimizerModule.distanceSquared(pos));
    }

    private static double distanceSquared(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || pos == null) {
            return Double.NaN;
        }
        Camera camera = client.gameRenderer.mainCamera();
        if (!camera.isInitialized()) {
            return Double.NaN;
        }
        Vec3 origin = camera.position();
        double x = (double)pos.getX() + 0.5 - origin.x;
        double y = (double)pos.getY() + 0.5 - origin.y;
        double z = (double)pos.getZ() + 0.5 - origin.z;
        return x * x + y * y + z * z;
    }

    public static boolean disableEntityShadows() {
        return active.disableEntityShadows();
    }

    public static boolean skipParticle(ParticleOptions options) {
        if (!active.decorativeParticles()) {
            return false;
        }
        ParticleType type = options.getType();
        return type == ParticleTypes.ASH || type == ParticleTypes.WHITE_ASH || type == ParticleTypes.CRIMSON_SPORE || type == ParticleTypes.WARPED_SPORE || type == ParticleTypes.MYCELIUM || type == ParticleTypes.SPORE_BLOSSOM_AIR || type == ParticleTypes.FALLING_SPORE_BLOSSOM || type == ParticleTypes.CHERRY_LEAVES || type == ParticleTypes.PALE_OAK_LEAVES;
    }
}

