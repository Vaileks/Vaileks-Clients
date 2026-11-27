/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ChatHistory<T> {
    private int capacity;
    private final List<Entry<T>> entries = new ArrayList<Entry<T>>();
    private long sequence;

    public ChatHistory(int capacity) {
        this.setCapacity(capacity);
    }

    public void add(T value, String text, Object signature, Instant receivedAt) {
        this.entries.add(new Entry<T>(++this.sequence, receivedAt, Objects.requireNonNull(value), Objects.requireNonNull(text), signature));
        if (this.entries.size() > this.capacity) {
            this.entries.remove(0);
        }
    }

    public List<Row<T>> rows(String query, boolean collapse) {
        String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
        ArrayList<Row<T>> grouped = new ArrayList<Row<T>>();
        for (Entry<T> entry : this.entries) {
            int lastIndex = grouped.size() - 1;
            if (collapse && lastIndex >= 0 && ((Row)grouped.get((int)lastIndex)).latest.value.equals(entry.value)) {
                Row previous = (Row)grouped.get(lastIndex);
                grouped.set(lastIndex, new Row<T>(entry, previous.count + 1));
                continue;
            }
            grouped.add(new Row<T>(entry, 1));
        }
        return grouped.stream().filter(row -> row.latest.text.toLowerCase(Locale.ROOT).contains(needle)).toList();
    }

    public void delete(Object signature) {
        if (signature != null) {
            this.entries.removeIf(entry -> signature.equals(entry.signature));
        }
    }

    public int size() {
        return this.entries.size();
    }

    public void clear() {
        this.entries.clear();
    }

    public void setCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        if (this.entries.size() > capacity) {
            this.entries.subList(0, this.entries.size() - capacity).clear();
        }
    }

    public record Entry<T>(long id, Instant receivedAt, T value, String text, Object signature) {
    }

    public record Row<T>(Entry<T> latest, int count) {
    }
}

