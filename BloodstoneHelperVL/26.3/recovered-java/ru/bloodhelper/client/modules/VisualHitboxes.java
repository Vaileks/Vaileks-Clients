/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.debug.DebugScreenEntries
 *  net.minecraft.client.gui.components.debug.DebugScreenEntryStatus
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.gizmos.GizmoStyle
 *  net.minecraft.gizmos.Gizmos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public static void tick(Minecraft mc) {
        VisualHitboxes.visible();
        if (!BloodHelperClient.config().hitboxes.enabled) {
            return;
        }
        if (mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            mc.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
        }
    }

    private static boolean direction(Entity entity, PlayerDisplaySettings.Hitboxes c) {
        return c.direction && (entity instanceof Player ? c.directionPlayers : (entity instanceof LivingEntity ? c.directionMobs : (entity instanceof ItemEntity ? c.directionItems : (entity instanceof Projectile ? c.directionProjectiles : c.directionOthers))));
    }

    public static void render(Frustum frustum, float partial) {
        PlayerDisplaySettings.Hitboxes c = BloodHelperClient.config().hitboxes;
        Minecraft mc = Minecraft.getInstance();
        if (!VisualHitboxes.visible() || mc.level == null || mc.player == null) {
            return;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            Vec3 directionEnd;
            boolean show;
            if (entity.isInvisible() || entity.isRemoved() || entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson() || !(show = entity instanceof Player ? c.players : (entity instanceof LivingEntity ? c.mobs : (entity instanceof ItemEntity ? c.items : (entity instanceof Projectile ? c.projectiles : c.others)))) || mc.player.distanceToSqr(entity) > c.distance * c.distance) continue;
            AABB box = HitboxRenderBounds.interpolated(entity.getBoundingBox(), entity.position(), entity.getPosition(partial));
            Vec3 eyePosition = entity.getEyePosition(partial);
            Vec3 vec3 = directionEnd = VisualHitboxes.direction(entity, c) ? eyePosition.add(entity.getViewVector(partial).scale(c.directionLength)) : null;
            if (frustum != null && !frustum.isVisible(HitboxRenderBounds.culling(box, eyePosition, directionEnd))) continue;
            if (c.boxes) {
                Gizmos.cuboid((AABB)box, (GizmoStyle)GizmoStyle.stroke((int)c.color));
            }
            if (c.eyeHeight && entity instanceof LivingEntity) {
                double eye = box.minY + (double)entity.getEyeHeight();
                Gizmos.cuboid((AABB)new AABB(box.minX, eye - 0.005, box.minZ, box.maxX, eye + 0.005, box.maxZ), (GizmoStyle)GizmoStyle.stroke((int)c.eyeColor));
            }
            if (!VisualHitboxes.direction(entity, c)) continue;
            Vec3 eye = entity.getEyePosition(partial);
            Gizmos.line((Vec3)eye, (Vec3)eye.add(entity.getViewVector(partial).scale(c.directionLength)), (int)c.directionColor);
        }
    }
}

