/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 *  net.minecraft.network.chat.MutableComponent
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;

public final class ChatMessageGroups {
    private final IdentityHashMap<Component, Group> groups = new IdentityHashMap();
    private final LinkedList<Group> order = new LinkedList();
    private final int capacity;
    private int retained;

    public ChatMessageGroups(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity");
        }
        this.capacity = capacity;
    }

    public Result accept(Component key, GuiMessage incoming, GuiMessage previous, boolean enabled) {
        Group group;
        Group group2 = group = previous == null ? null : this.groups.get(previous.content());
        boolean repeat = enabled && group != null && group.key.equals((Object)key) && Objects.equals(previous.tag(), incoming.tag()) && previous.source() == incoming.source() && previous.signature() == null == (incoming.signature() == null) && group.originals.size() < this.capacity;
        ArrayList<GuiMessage> originals = new ArrayList<GuiMessage>();
        if (repeat) {
            originals.addAll(group.originals);
            this.forget(group);
        }
        originals.add(incoming);
        GuiMessage display = this.remember(key, originals);
        return new Result(display, repeat);
    }

    public void rebind(Component prepared, GuiMessage stored) {
        Group group = this.groups.get(prepared);
        if (group == null || prepared == stored.content()) {
            return;
        }
        Group rebound = new Group(group.key, stored, group.originals);
        this.groups.remove(prepared);
        this.groups.put(stored.content(), rebound);
        int index = this.order.indexOf(group);
        if (index >= 0) {
            this.order.set(index, rebound);
        }
    }

    public boolean delete(MessageSignature signature, List<GuiMessage> messages) {
        if (signature == null) {
            return false;
        }
        for (int i = 0; i < messages.size(); ++i) {
            ArrayList<GuiMessage> remaining;
            Group group = this.groups.get(messages.get(i).content());
            if (group == null || group.originals.size() < 2 || !(remaining = new ArrayList<GuiMessage>(group.originals)).removeIf(message -> signature.equals((Object)message.signature()))) continue;
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

    private GuiMessage remember(Component key, List<GuiMessage> originals) {
        GuiMessage newest = originals.getLast();
        MutableComponent content = Component.empty().append(newest.content());
        if (originals.size() > 1) {
            content.append((Component)Component.literal((String)(" (\u00d7" + originals.size() + ")")).withStyle(ChatFormatting.GRAY));
        }
        GuiMessage display = new GuiMessage(newest.addedTime(), (Component)content, newest.signature(), newest.source(), newest.tag());
        Group group = new Group((Component)key.copy(), display, List.copyOf(originals));
        this.groups.put((Component)content, group);
        this.order.addLast(group);
        this.retained += originals.size();
        while (this.retained > this.capacity) {
            this.forget(this.order.getFirst());
        }
        return display;
    }

    private void forget(Group group) {
        this.groups.remove(group.display.content());
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

    private record Group(Component key, GuiMessage display, List<GuiMessage> originals) {
    }

    public record Result(GuiMessage message, boolean replacesPrevious) {
    }
}

