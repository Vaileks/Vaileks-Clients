/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import java.util.ArrayDeque;
import java.util.Objects;

public final class HudEditHistory<T> {
    private final int limit;
    private final ArrayDeque<Edit<T>> undo = new ArrayDeque();
    private final ArrayDeque<Edit<T>> redo = new ArrayDeque();

    public HudEditHistory(int limit) {
        this.limit = Math.max(1, limit);
    }

    public void clear() {
        this.undo.clear();
        this.redo.clear();
    }

    public boolean canUndo() {
        return !this.undo.isEmpty();
    }

    public boolean canRedo() {
        return !this.redo.isEmpty();
    }

    public void record(T before, T after) {
        if (Objects.equals(before, after)) {
            return;
        }
        this.undo.addLast(new Edit<T>(before, after));
        while (this.undo.size() > this.limit) {
            this.undo.removeFirst();
        }
        this.redo.clear();
    }

    public T undo() {
        if (this.undo.isEmpty()) {
            return null;
        }
        Edit<T> edit = this.undo.removeLast();
        this.redo.addLast(edit);
        return edit.before;
    }

    public T redo() {
        if (this.redo.isEmpty()) {
            return null;
        }
        Edit<T> edit = this.redo.removeLast();
        this.undo.addLast(edit);
        return edit.after;
    }

    private record Edit<T>(T before, T after) {
    }
}

