/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NotificationSoundSettings {
    public static final List<String> SOUNDS = List.of("minecraft:ui.button.click", "minecraft:block.note_block.pling", "minecraft:block.note_block.bell", "minecraft:entity.experience_orb.pickup", "minecraft:block.amethyst_block.chime", "minecraft:block.note_block.chime", "minecraft:entity.player.levelup", "minecraft:block.note_block.harp");
    public boolean enabled = false;
    public Map<String, Choice> events = new LinkedHashMap<String, Choice>();

    public Choice choice(Event event) {
        if (this.events == null) {
            this.events = new LinkedHashMap<String, Choice>();
        }
        return this.events.computeIfAbsent(event.name(), ignored -> new Choice());
    }

    public void sanitize() {
        LinkedHashMap<String, Choice> clean = new LinkedHashMap<String, Choice>();
        for (Event event : Event.values()) {
            Choice choice;
            Choice choice2 = choice = this.events == null ? null : this.events.get(event.name());
            if (choice == null) continue;
            choice.sanitize();
            clean.put(event.name(), choice);
        }
        this.events = clean;
    }

    public float volume(Event event, float original) {
        if (!this.enabled) {
            return Float.isFinite(original) ? Math.clamp(original, 0.0f, 1.0f) : 0.0f;
        }
        Choice choice = this.choice(event);
        return choice.silent ? 0.0f : (float)Math.clamp((long)choice.volume, 0, 100) / 100.0f;
    }

    public static enum Event {
        INVENTORY,
        ARMOR,
        ITEM,
        EFFECT,
        REMINDER,
        COMMAND_RESULT,
        PLAYER_RETURN,
        CHAT_MENTION,
        CHAT_VIOLATION,
        COOLDOWN_READY,
        STAFF_CHECK;

    }

    public static final class Choice {
        public String sound = SOUNDS.getFirst();
        public int volume = 18;
        public boolean silent = false;

        public void sanitize() {
            if (!SOUNDS.contains(this.sound)) {
                this.sound = SOUNDS.getFirst();
            }
            this.volume = Math.clamp((long)this.volume, 0, 100);
        }
    }
}

