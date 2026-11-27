/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Optional;
import ru.bloodhelper.client.config.QuickCommandsSettings;
import ru.bloodhelper.client.modules.QuickCommandAction;

public final class QuickCommandWheelState {
    public static final double INNER_RADIUS = 45.0;
    public static final double OUTER_RADIUS = 124.0;
    private final QuickCommandAction[] actions = new QuickCommandAction[4];
    private final String[] labels = new String[4];
    private boolean finished;

    public QuickCommandWheelState(QuickCommandsSettings settings) {
        for (int i = 0; i < 4; ++i) {
            QuickCommandsSettings.Slot slot = settings.slots != null && i < settings.slots.length ? settings.slots[i] : null;
            String value = slot == null ? "" : QuickCommandsSettings.clean(slot.value, 256);
            this.actions[i] = QuickCommandAction.parse(value).orElse(null);
            String label = slot == null ? "" : QuickCommandsSettings.clean(slot.label, 28);
            this.labels[i] = label.isEmpty() ? value : label;
        }
    }

    public String label(int index) {
        return this.labels[index];
    }

    public QuickCommandAction action(int index) {
        return this.actions[index];
    }

    public boolean finished() {
        return this.finished;
    }

    public void cancel() {
        this.finished = true;
    }

    public Optional<QuickCommandAction> select(int index) {
        if (this.finished || index < 0 || index >= this.actions.length || this.actions[index] == null) {
            return Optional.empty();
        }
        this.finished = true;
        return Optional.of(this.actions[index]);
    }

    public static int sector(double x, double y) {
        double squared = x * x + y * y;
        if (!Double.isFinite(squared) || squared < 2025.0 || squared > 15376.0 || Math.abs(Math.abs(x) - Math.abs(y)) < 3.0) {
            return -1;
        }
        if (Math.abs(y) > Math.abs(x)) {
            return y < 0.0 ? 0 : 2;
        }
        return x > 0.0 ? 1 : 3;
    }
}

