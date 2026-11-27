/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2394
 *  net.minecraft.class_2396
 *  net.minecraft.class_2398
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
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

    public static void tick(class_310 client) {
        if (level != client.field_1687) {
            level = client.field_1687;
            rebuildPending = false;
        }
        if (rebuildPending && client.field_1687 != null && System.nanoTime() >= rebuildAfter) {
            rebuildPending = false;
            client.field_1769.method_3279();
        }
    }

    public static boolean hideGrass(class_2248 block) {
        return active.hideGrass() && (block == class_2246.field_10479 || block == class_2246.field_10214 || block == class_2246.field_10112 || block == class_2246.field_10313);
    }

    public static boolean hideSign(class_2338 pos) {
        OptimizerSettings.Snapshot settings = active;
        return settings.signs() && settings.hideSign(OptimizerModule.distanceSquared(pos));
    }

    public static boolean hideSignText(class_2338 pos) {
        OptimizerSettings.Snapshot settings = active;
        return settings.signs() && settings.hideSignText(OptimizerModule.distanceSquared(pos));
    }

    private static double distanceSquared(class_2338 pos) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || pos == null) {
            return Double.NaN;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (!camera.method_19332()) {
            return Double.NaN;
        }
        class_243 origin = camera.method_71156();
        double x = (double)pos.method_10263() + 0.5 - origin.field_1352;
        double y = (double)pos.method_10264() + 0.5 - origin.field_1351;
        double z = (double)pos.method_10260() + 0.5 - origin.field_1350;
        return x * x + y * y + z * z;
    }

    public static boolean disableEntityShadows() {
        return active.disableEntityShadows();
    }

    public static boolean skipParticle(class_2394 options) {
        if (!active.decorativeParticles()) {
            return false;
        }
        class_2396 type = options.method_10295();
        return type == class_2398.field_22247 || type == class_2398.field_23956 || type == class_2398.field_22248 || type == class_2398.field_22249 || type == class_2398.field_11219 || type == class_2398.field_28803 || type == class_2398.field_28802 || type == class_2398.field_43379 || type == class_2398.field_55169;
    }
}

