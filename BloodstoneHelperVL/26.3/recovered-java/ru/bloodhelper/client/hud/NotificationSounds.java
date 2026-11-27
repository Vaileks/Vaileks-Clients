/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 */
package ru.bloodhelper.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.hud.AlertSoundGate;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.hud.Notifications;

public final class NotificationSounds {
    private static final AlertSoundGate GATE = new AlertSoundGate();
    private static Object connection;
    private static Object configuration;

    private NotificationSounds() {
    }

    public static void play(Minecraft client, float pitch, float volume) {
        NotificationSounds.play(client, NotificationSoundSettings.Event.INVENTORY, pitch, volume);
    }

    public static void play(Minecraft client, NotificationSoundSettings.Event event, float pitch, float volume) {
        if (NotificationInbox.snoozed()) {
            return;
        }
        NotificationSoundSettings settings = BloodHelperClient.config().notificationSounds;
        if ((volume = settings.volume(event, volume)) <= 0.0f) {
            return;
        }
        if (connection != client.getConnection() || configuration != BloodHelperClient.config()) {
            GATE.clear();
            connection = client.getConnection();
            configuration = BloodHelperClient.config();
        }
        if (GATE.acquire(Notifications.now())) {
            NotificationSounds.emit(client, settings, event, pitch, volume);
        }
    }

    public static void preview(Minecraft client, NotificationSoundSettings.Event event) {
        if (NotificationInbox.snoozed()) {
            return;
        }
        NotificationSoundSettings settings = BloodHelperClient.config().notificationSounds;
        NotificationSoundSettings.Choice choice = settings.choice(event);
        if (!choice.silent && choice.volume > 0) {
            NotificationSounds.emit(client, settings, event, 1.0f, (float)Math.clamp((long)choice.volume, 0, 100) / 100.0f, true);
        }
    }

    private static void emit(Minecraft client, NotificationSoundSettings settings, NotificationSoundSettings.Event event, float pitch, float volume) {
        NotificationSounds.emit(client, settings, event, pitch, volume, settings.enabled);
    }

    private static void emit(Minecraft client, NotificationSoundSettings settings, NotificationSoundSettings.Event event, float pitch, float volume, boolean custom) {
        Identifier id;
        SoundEvent sound = (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value();
        if (custom && (id = Identifier.tryParse((String)settings.choice((NotificationSoundSettings.Event)event).sound)) != null) {
            sound = BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(sound);
        }
        client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)sound, (float)(custom ? 1.0f : pitch), (float)volume));
    }
}

