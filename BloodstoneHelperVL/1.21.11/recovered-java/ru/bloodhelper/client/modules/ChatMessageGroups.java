/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_303
 *  net.minecraft.class_5250
 *  net.minecraft.class_7469
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_5250;
import net.minecraft.class_7469;

public final class ChatMessageGroups {
    private final IdentityHashMap<class_2561, Group> groups = new IdentityHashMap();
    private final LinkedList<Group> order = new LinkedList();
    private final int capacity;
    private int retained;

    public ChatMessageGroups(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity");
        }
        this.capacity = capacity;
    }

    public Result accept(class_2561 key, class_303 incoming, class_303 previous, boolean enabled) {
        Group group;
        Group group2 = group = previous == null ? null : this.groups.get(previous.comp_893());
        boolean repeat = enabled && group != null && group.key.equals((Object)key) && Objects.equals(previous.comp_894(), incoming.comp_894()) && previous.comp_915() == null == (incoming.comp_915() == null) && group.originals.size() < this.capacity;
        ArrayList<class_303> originals = new ArrayList<class_303>();
        if (repeat) {
            originals.addAll(group.originals);
            this.forget(group);
        }
        originals.add(incoming);
        class_303 display = this.remember(key, originals);
        return new Result(display, repeat);
    }

    public void rebind(class_2561 prepared, class_303 stored) {
        Group group = this.groups.get(prepared);
        if (group == null || prepared == stored.comp_893()) {
            return;
        }
        Group rebound = new Group(group.key, stored, group.originals);
        this.groups.remove(prepared);
        this.groups.put(stored.comp_893(), rebound);
        int index = this.order.indexOf(group);
        if (index >= 0) {
            this.order.set(index, rebound);
        }
    }

    public boolean delete(class_7469 signature, List<class_303> messages) {
        if (signature == null) {
            return false;
        }
        for (int i = 0; i < messages.size(); ++i) {
            ArrayList<class_303> remaining;
            Group group = this.groups.get(messages.get(i).comp_893());
            if (group == null || group.originals.size() < 2 || !(remaining = new ArrayList<class_303>(group.originals)).removeIf(message -> signature.equals((Object)message.comp_915()))) continue;
            this.forget(group);
            if (remaining.isEmpty()) {
                messages.remove(i);
            } else {
                messages.set(i, this.remember(group.key, remaining));
            }
            return true;
        }
        return false;
    }

    private class_303 remember(class_2561 key, List<class_303> originals) {
        class_303 newest = originals.getLast();
        class_5250 content = class_2561.method_43473().method_10852(newest.comp_893());
        if (originals.size() > 1) {
            content.method_10852((class_2561)class_2561.method_43470((String)(" (\u00d7" + originals.size() + ")")).method_27692(class_124.field_1080));
        }
        class_303 display = new class_303(newest.comp_892(), (class_2561)content, newest.comp_915(), newest.comp_894());
        Group group = new Group((class_2561)key.method_27661(), display, List.copyOf(originals));
        this.groups.put((class_2561)content, group);
        this.order.addLast(group);
        this.retained += originals.size();
        while (this.retained > this.capacity) {
            this.forget(this.order.getFirst());
        }
        return display;
    }

    private void forget(Group group) {
        this.groups.remove(group.display.comp_893());
        this.order.remove(group);
        this.retained -= group.originals.size();
    }

    public int retainedMessages() {
        return this.retained;
    }

    public void clear() {
        this.groups.clear();
        this.order.clear();
        this.retained = 0;
    }

    private record Group(class_2561 key, class_303 display, List<class_303> originals) {
    }

    public record Result(class_303 message, boolean replacesPrevious) {
    }
}

