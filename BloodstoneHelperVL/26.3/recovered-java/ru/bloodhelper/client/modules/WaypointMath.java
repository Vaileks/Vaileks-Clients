/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import ru.bloodhelper.client.config.WaypointSettings;

public final class WaypointMath {
    private WaypointMath() {
    }

    public static String remoteScope(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        String authority = address.strip().toLowerCase(Locale.ROOT);
        if (authority.endsWith(":25565")) {
            authority = authority.substring(0, authority.length() - 6);
        }
        if (authority.endsWith(".")) {
            authority = authority.substring(0, authority.length() - 1);
        }
        return WaypointMath.scope("remote", authority);
    }

    public static String scope(String type, String identity) {
        if (identity == null || identity.isBlank()) {
            return "";
        }
        try {
            return type + ":" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError((Object)impossible);
        }
    }

    public static boolean matches(WaypointSettings.Entry entry, String scope, String dimension) {
        return entry != null && scope != null && !scope.isEmpty() && scope.equals(entry.scope) && dimension != null && dimension.equals(entry.dimension);
    }

    public static double bearing(double playerX, double playerZ, double yaw, double targetX, double targetZ) {
        double dx = targetX - playerX;
        double dz = targetZ - playerZ;
        if (dx * dx + dz * dz < 1.0E-6) {
            return 0.0;
        }
        return WaypointMath.wrapDegrees(Math.toDegrees(Math.atan2(-dx, dz)) - yaw);
    }

    public static double wrapDegrees(double degrees) {
        return degrees - Math.floor((degrees + 180.0) / 360.0) * 360.0;
    }

    public static double distance(double x, double y, double z, WaypointSettings.Entry entry) {
        return Math.sqrt(Math.pow((double)entry.x + 0.5 - x, 2.0) + Math.pow((double)entry.y - y, 2.0) + Math.pow((double)entry.z + 0.5 - z, 2.0));
    }
}

