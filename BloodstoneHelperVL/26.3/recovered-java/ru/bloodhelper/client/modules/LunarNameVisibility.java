/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.scores.Team$Visibility
 */
package ru.bloodhelper.client.modules;

import net.minecraft.world.scores.Team;

public final class LunarNameVisibility {
    private LunarNameVisibility() {
    }

    public static boolean inRange(double distanceSquared, boolean discrete, boolean extended) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0) {
            return false;
        }
        return discrete ? distanceSquared < 1024.0 : extended || distanceSquared < 4096.0;
    }

    public static boolean teamAllows(Team.Visibility visibility, boolean viewerHasTeam, boolean allied, boolean visibleToViewer, boolean friendlyInvisibles) {
        return switch (visibility) {
            default -> throw new MatchException(null, null);
            case Team.Visibility.ALWAYS -> visibleToViewer;
            case Team.Visibility.NEVER -> false;
            case Team.Visibility.HIDE_FOR_OTHER_TEAMS -> {
                if (!viewerHasTeam) {
                    yield visibleToViewer;
                }
                if (allied && (visibleToViewer || friendlyInvisibles)) {
                    yield true;
                }
                yield false;
            }
            case Team.Visibility.HIDE_FOR_OWN_TEAM -> visibleToViewer && (!viewerHasTeam || !allied);
        };
    }
}

