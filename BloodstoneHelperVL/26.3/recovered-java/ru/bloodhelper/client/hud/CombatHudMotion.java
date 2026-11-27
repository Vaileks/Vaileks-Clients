/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.bloodhelper.client.hud.HudLayout;

public final class CombatHudMotion {
    private static final long DURATION = 240000000L;
    private final Map<String, State> states = new LinkedHashMap<String, State>();

    public void reset() {
        this.states.clear();
    }

    public HudLayout.Placement place(String key, HudLayout.Placement target, int width, int height, int screenWidth, int screenHeight, boolean combat, boolean editing, boolean animate, long now) {
        State state = this.states.get(key);
        if (state == null) {
            state = new State();
            state.combat = combat;
            state.last = target;
            if (this.states.size() >= 272) {
                this.states.remove(this.states.keySet().iterator().next());
            }
            this.states.put(key, state);
        }
        if (state.combat != combat) {
            state.combat = combat;
            state.started = now;
            state.moving = true;
            state.fromX = state.last.x();
            state.fromY = state.last.y();
            state.fromScale = state.last.scale();
        }
        double progress = Math.clamp((double)(now - state.started) / 2.4E8, 0.0, 1.0);
        if (editing || !animate || progress >= 1.0) {
            state.moving = false;
        }
        if (!state.moving) {
            state.last = target;
            return state.last;
        }
        double blend = 1.0 - Math.pow(1.0 - progress, 3.0);
        double scale = state.fromScale + (target.scale() - state.fromScale) * blend;
        scale = Math.min(scale, Math.min((double)Math.max(1, screenWidth - 4) / (double)Math.max(1, width), (double)Math.max(1, screenHeight - 4) / (double)Math.max(1, height)));
        int w = (int)Math.ceil((double)width * scale);
        int h = (int)Math.ceil((double)height * scale);
        int x = (int)Math.round(state.fromX + ((double)target.x() - state.fromX) * blend);
        int y = (int)Math.round(state.fromY + ((double)target.y() - state.fromY) * blend);
        state.last = new HudLayout.Placement(Math.clamp((long)x, 0, Math.max(0, screenWidth - w)), Math.clamp((long)y, 0, Math.max(0, screenHeight - h)), w, h, scale);
        return state.last;
    }

    private static final class State {
        boolean combat;
        boolean moving;
        long started;
        double fromX;
        double fromY;
        double fromScale;
        HudLayout.Placement last;

        private State() {
        }
    }
}

