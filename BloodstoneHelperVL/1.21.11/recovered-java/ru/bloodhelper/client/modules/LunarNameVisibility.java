/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_270$class_272
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_270;

public final class LunarNameVisibility {
    private LunarNameVisibility() {
    }

    public static boolean inRange(double distanceSquared, boolean discrete, boolean extended) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0) {
            return false;
        }
        return discrete ? distanceSquared < 1024.0 : extended || distanceSquared < 4096.0;
    }

    public static boolean teamAllows(class_270.class_272 visibility, boolean viewerHasTeam, boolean allied, boolean visibleToViewer, boolean friendlyInvisibles) {
        return switch (visibility) {
            default -> throw new MatchException(null, null);
            case class_270.class_272.field_1442 -> visibleToViewer;
            case class_270.class_272.field_1443 -> false;
            case class_270.class_272.field_1444 -> {
                if (!viewerHasTeam) {
                    yield visibleToViewer;
                }
                if (allied && (visibleToViewer || friendlyInvisibles)) {
                    yield true;
                }
                yield false;
            }
            case class_270.class_272.field_1446 -> visibleToViewer && (!viewerHasTeam || !allied);
        };
    }
}

