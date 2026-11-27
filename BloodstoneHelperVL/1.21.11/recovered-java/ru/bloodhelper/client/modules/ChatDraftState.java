/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class ChatDraftState {
    private Object connection;
    private Object settings;
    private Draft draft;

    public void update(Object nextConnection, Object nextSettings, boolean enabled) {
        if (!enabled || nextConnection == null || this.connection != nextConnection || this.settings != nextSettings) {
            this.draft = null;
        }
        this.connection = enabled ? nextConnection : null;
        this.settings = enabled ? nextSettings : null;
    }

    public Draft restore(String initial) {
        return this.connection != null && (initial == null || initial.isEmpty()) ? this.draft : null;
    }

    public void closed(Object owner, String text, int cursor, int anchor, boolean sent, boolean commandEntry) {
        String value;
        if (this.connection == null || owner != this.connection) {
            return;
        }
        String string = value = text == null ? "" : text;
        if (value.stripLeading().startsWith("/") || commandEntry && value.isBlank()) {
            return;
        }
        this.draft = sent || value.isBlank() ? null : new Draft(value, cursor, anchor);
    }

    public void clear() {
        this.connection = null;
        this.settings = null;
        this.draft = null;
    }

    public record Draft(String text, int cursor, int anchor) {
        public Draft {
            text = text == null ? "" : text.substring(0, Math.min(256, text.length()));
            cursor = Math.clamp((long)cursor, 0, text.length());
            anchor = Math.clamp((long)anchor, 0, text.length());
        }
    }
}

