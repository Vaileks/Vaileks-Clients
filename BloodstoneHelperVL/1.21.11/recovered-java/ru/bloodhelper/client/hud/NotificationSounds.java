/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.hud;

import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_7923;
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

    public static void play(class_310 client, float pitch, float volume) {
        NotificationSounds.play(client, NotificationSoundSettings.Event.INVENTORY, pitch, volume);
    }

    public static void play(class_310 client, NotificationSoundSettings.Event event, float pitch, float volume) {
        if (NotificationInbox.snoozed()) {
            return;
        }
        NotificationSoundSettings settings = BloodHelperClient.config().notificationSounds;
        if ((volume = settings.volume(event, volume)) <= 0.0f) {
            return;
        }
        if (connection != client.method_1562() || configuration != BloodHelperClient.config()) {
            GATE.clear();
            connection = client.method_1562();
            configuration = BloodHelperClient.config();
        }
        if (GATE.acquire(Notifications.now())) {
            NotificationSounds.emit(client, settings, event, pitch, volume);
        }
    }

    public static void preview(class_310 client, NotificationSoundSettings.Event event) {
        if (NotificationInbox.snoozed()) {
            return;
        }
        NotificationSoundSettings settings = BloodHelperClient.config().notificationSounds;
        NotificationSoundSettings.Choice choice = settings.choice(event);
        if (!choice.silent && choice.volume > 0) {
            NotificationSounds.emit(client, settings, event, 1.0f, (float)Math.clamp((long)choice.volume, 0, 100) / 100.0f, true);
        }
    }

    private static void emit(class_310 client, NotificationSoundSettings settings, NotificationSoundSettings.Event event, float pitch, float volume) {
        NotificationSounds.emit(client, settings, event, pitch, volume, settings.enabled);
    }

    private static void emit(class_310 client, NotificationSoundSettings settings, NotificationSoundSettings.Event event, float pitch, float volume, boolean custom) {
        class_2960 id;
        class_3414 sound = (class_3414)class_3417.field_15015.comp_349();
        if (custom && (id = class_2960.method_12829((String)settings.choice((NotificationSoundSettings.Event)event).sound)) != null) {
            sound = class_7923.field_41172.method_17966(id).orElse(sound);
        }
        client.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)sound, (float)(custom ? 1.0f : pitch), (float)volume));
    }
}

