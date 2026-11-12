/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11631
 *  net.minecraft.class_11635
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1676
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4604
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_11631;
import net.minecraft.class_11635;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4604;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.PlayerDisplaySettings;
import ru.bloodhelper.client.modules.HitboxRenderBounds;
import ru.bloodhelper.client.modules.HitboxVisibility;

public final class VisualHitboxes {
    private static final HitboxVisibility VISIBILITY = new HitboxVisibility();

    private VisualHitboxes() {
    }

    public static boolean visible() {
        return VISIBILITY.visible(BloodHelperClient.config().hitboxes.enabled);
    }

    public static boolean toggleVisibility() {
        return VISIBILITY.toggle(BloodHelperClient.config().hitboxes.enabled);
    }

    public static void tick(class_310 mc) {
        VisualHitboxes.visible();
        if (!BloodHelperClient.config().hitboxes.enabled) {
            return;
        }
        if (mc.field_61504.method_72772(class_11631.field_61581)) {
            mc.field_61504.method_72767(class_11631.field_61581, class_11635.field_61595);
        }
    }

    private static boolean direction(class_1297 entity, PlayerDisplaySettings.Hitboxes c) {
        return c.direction && (entity instanceof class_1657 ? c.directionPlayers : (entity instanceof class_1309 ? c.directionMobs : (entity instanceof class_1542 ? c.directionItems : (entity instanceof class_1676 ? c.directionProjectiles : c.directionOthers))));
    }

    public static void render(class_4604 frustum, float partial) {
        PlayerDisplaySettings.Hitboxes c = BloodHelperClient.config().hitboxes;
        class_310 mc = class_310.method_1551();
        if (!VisualHitboxes.visible() || mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        for (class_1297 entity : mc.field_1687.method_18112()) {
            class_243 directionEnd;
            boolean show;
            if (entity.method_5767() || entity.method_31481() || entity == mc.method_1560() && mc.field_1690.method_31044().method_31034() || !(show = entity instanceof class_1657 ? c.players : (entity instanceof class_1309 ? c.mobs : (entity instanceof class_1542 ? c.items : (entity instanceof class_1676 ? c.projectiles : c.others)))) || mc.field_1724.method_5858(entity) > c.distance * c.distance) continue;
            class_238 box = HitboxRenderBounds.interpolated(entity.method_5829(), entity.method_73189(), entity.method_30950(partial));
            class_243 eyePosition = entity.method_5836(partial);
            class_243 class_2432 = directionEnd = VisualHitboxes.direction(entity, c) ? eyePosition.method_1019(entity.method_5828(partial).method_1021(c.directionLength)) : null;
            if (frustum != null && !frustum.method_23093(HitboxRenderBounds.culling(box, eyePosition, directionEnd))) continue;
            if (c.boxes) {
                class_12180.method_75541((class_238)box, (class_12179)class_12179.method_75535((int)c.color));
            }
            if (c.eyeHeight && entity instanceof class_1309) {
                double eye = box.field_1322 + (double)entity.method_5751();
                class_12180.method_75541((class_238)new class_238(box.field_1323, eye - 0.005, box.field_1321, box.field_1320, eye + 0.005, box.field_1324), (class_12179)class_12179.method_75535((int)c.eyeColor));
            }
            if (!VisualHitboxes.direction(entity, c)) continue;
            class_243 eye = entity.method_5836(partial);
            class_12180.method_75545((class_243)eye, (class_243)eye.method_1019(entity.method_5828(partial).method_1021(c.directionLength)), (int)c.directionColor);
        }
    }
}

