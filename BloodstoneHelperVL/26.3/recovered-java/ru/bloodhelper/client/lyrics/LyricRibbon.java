/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ru.bloodhelper.client.lyrics.LrcParser;
import ru.bloodhelper.client.lyrics.LyricLine;

public final class LyricRibbon {
    private String track = "";
    private List<LyricLine> source = List.of();
    private int current = -1;
    private int visible = 3;
    private long lastTime = Long.MIN_VALUE;
    private long lastFrame;
    private long changed;
    private double startShift;
    public static final long DURATION_NS = 360000000L;

    public Frame update(String identity, List<LyricLine> lines, long time, int count, long now, boolean animate) {
        int last;
        boolean reset;
        count = Math.clamp((long)count, 1, 3);
        int index = LrcParser.indexAt(lines, time);
        boolean empty = index < 0 || lines.get(index).text().isBlank();
        long elapsedMs = this.lastFrame == 0L ? 0L : Math.max(0L, (now - this.lastFrame) / 1000000L);
        boolean jumped = this.lastTime != Long.MIN_VALUE && (time < this.lastTime - 180L || time - this.lastTime > elapsedMs + 650L);
        boolean bl = reset = !animate || !this.track.equals(identity) || this.source != lines && !this.source.equals(lines) || now < this.lastFrame || now - this.lastFrame > 1500000000L || jumped || this.visible != count;
        if (reset || empty || this.current < 0 || index != this.current && index != this.current + 1) {
            this.startShift = 0.0;
            this.changed = 0L;
        } else if (index != this.current) {
            this.startShift = Math.min(2.0, this.shift(now) + 1.0);
            this.changed = now;
        }
        this.track = identity;
        this.source = lines;
        this.current = empty ? -1 : index;
        this.lastTime = time;
        this.lastFrame = now;
        this.visible = count;
        if (empty) {
            return Frame.empty(count);
        }
        double shift = this.shift(now);
        ArrayList<Row> rows = new ArrayList<Row>();
        for (int first = index; first > Math.max(0, index - 3) && !lines.get(first - 1).text().isBlank(); --first) {
        }
        for (last = index; last < Math.min(lines.size() - 1, index + 1) && !lines.get(last + 1).text().isBlank(); ++last) {
        }
        for (int i = first; i <= last; ++i) {
            double maximum;
            double slot = (double)(i - index) + shift;
            double minimum = count == 3 ? -1.5 : -0.52;
            double d = maximum = count == 1 ? 0.52 : 1.5;
            if (slot <= minimum || slot >= maximum) continue;
            double angle = slot * 0.75;
            double cosine = Math.cos(angle);
            double perspective = 1.0 / (1.0 + 0.16 * (1.0 - cosine) - 0.055 * Math.sin(angle));
            double focus = Math.max(0.0, 1.0 - Math.abs(slot));
            double edge = Math.min(1.0, Math.min((slot - minimum) * 2.0, (maximum - slot) * 2.0));
            double opacity = (slot < 0.0 ? 0.4 + 0.6 * focus : 0.72 + 0.28 * focus) * edge;
            rows.add(new Row(lines.get(i), slot, 17.0 * Math.sin(angle) / Math.sin(0.75), (0.92 + 0.08 * cosine) * perspective, Math.max(0.12, cosine * perspective), opacity, focus, Math.clamp(slot, 0.0, 1.0), i == index));
        }
        rows.sort(Comparator.comparingDouble(Row::focus));
        return new Frame(rows, count);
    }

    private double shift(long now) {
        if (this.changed == 0L) {
            return 0.0;
        }
        double t = Math.clamp((double)(now - this.changed) / 3.6E8, 0.0, 1.0);
        return this.startShift * (1.0 - t * t * (3.0 - 2.0 * t));
    }

    public void clear() {
        this.track = "";
        this.source = List.of();
        this.current = -1;
        this.lastTime = Long.MIN_VALUE;
        this.changed = 0L;
        this.lastFrame = 0L;
        this.startShift = 0.0;
    }

    public record Frame(List<Row> rows, int count) {
        public Frame {
            rows = List.copyOf(rows);
        }

        public static Frame empty(int count) {
            return new Frame(List.of(), Math.clamp((long)count, 1, 3));
        }

        public int height() {
            return 20 + (this.count - 1) * 17;
        }

        public int centerOffset() {
            return 8 + (this.count == 3 ? 17 : 0);
        }
    }

    public record Row(LyricLine line, double slot, double y, double scaleX, double scaleY, double opacity, double focus, double upcoming, boolean active) {
    }
}

