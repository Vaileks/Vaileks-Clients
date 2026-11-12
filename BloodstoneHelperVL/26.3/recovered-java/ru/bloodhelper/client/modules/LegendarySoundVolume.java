/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.KillEffectSoundCatalog;
import ru.bloodhelper.client.modules.LegendarySoundCatalog;
import ru.bloodhelper.client.modules.LegendarySoundGain;

public final class LegendarySoundVolume {
    private static final Snapshot VANILLA = new Snapshot(1.0f, Map.of());
    private static final Snapshot MUTED = new Snapshot(0.0f, Map.of());
    private static volatile Snapshot current = VANILLA;

    private LegendarySoundVolume() {
    }

    static boolean protectedGameSound(Identifier event) {
        if (event == null || !"minecraft".equals(event.getNamespace())) {
            return false;
        }
        String path = event.getPath();
        return path.equals("entity.generic.explode") || path.equals("entity.tnt.primed") || path.startsWith("entity.minecart.") || path.equals("entity.creeper.primed");
    }

    public static void settingsChanged() {
        LegendarySoundVolume.settingsChanged(BloodHelperClient.config().legendarySounds);
    }

    public static void settingsChanged(ClientConfig.LegendarySoundSettings settings) {
        Snapshot prepared = LegendarySoundVolume.prepare(settings);
        if (!current.equals(prepared)) {
            current = prepared;
        }
    }

    static Snapshot prepare(ClientConfig.LegendarySoundSettings settings) {
        if (settings == null || !settings.enabled) {
            return VANILLA;
        }
        float minecraftVolume = (float)LegendarySoundGain.bounded(settings.minecraftVolume);
        if (minecraftVolume == 0.0f) {
            return MUTED;
        }
        HashMap<Identifier, Float> gains = new HashMap<Identifier, Float>();
        HashMap<Identifier, Float> soundFiles = new HashMap<Identifier, Float>();
        for (LegendarySoundCatalog.Entry entry : LegendarySoundCatalog.entries()) {
            float multiplier = LegendarySoundGain.multiplier(true, settings.volume, settings.abilityVolume(entry.id()));
            if (multiplier == 1.0f) continue;
            for (String eventName : entry.events()) {
                LegendarySoundVolume.add(gains, soundFiles, eventName, multiplier);
            }
        }
        for (KillEffectSoundCatalog.Entry effect : KillEffectSoundCatalog.entries()) {
            float gain = LegendarySoundGain.multiplier(true, settings.killEffectsVolume, "SELECTED".equals(settings.killEffectsMode) ? settings.killEffectVolume(effect.id()) : 1.0);
            if (gain == 1.0f) continue;
            for (String event : effect.events()) {
                LegendarySoundVolume.add(gains, soundFiles, event, gain);
            }
        }
        return minecraftVolume == 1.0f && gains.isEmpty() ? VANILLA : new Snapshot(minecraftVolume, gains, soundFiles);
    }

    private static void add(Map<Identifier, Float> gains, Map<Identifier, Float> files, String name, float gain) {
        Identifier event = Identifier.tryParse((String)name);
        if (event == null || LegendarySoundVolume.protectedGameSound(event)) {
            return;
        }
        gains.merge(event, Float.valueOf(gain), Math::min);
        if (event.getNamespace().equals("bloodstone") || event.getNamespace().equals("blooddonate") || event.getNamespace().equals("minecraft") && event.getPath().startsWith("custom.")) {
            Identifier file = Identifier.fromNamespaceAndPath((String)event.getNamespace(), (String)event.getPath().replace('.', '/'));
            files.merge(file, Float.valueOf(gain), Math::min);
        }
    }

    public static Snapshot snapshot() {
        return current;
    }

    public static boolean active() {
        return current.active();
    }

    public static float multiplier(Identifier event) {
        return current.multiplier(event);
    }

    public static float scale(float vanillaGain, Identifier event) {
        return LegendarySoundVolume.scale(vanillaGain, event, null);
    }

    public static float scale(float vanillaGain, Identifier event, Identifier soundFile) {
        Snapshot snapshot = current;
        if (!snapshot.active()) {
            return vanillaGain;
        }
        return LegendarySoundGain.apply(vanillaGain, snapshot.multiplier(event, soundFile));
    }

    public record Snapshot(float minecraftVolume, Map<Identifier, Float> gains, Map<Identifier, Float> soundFiles) {
        public Snapshot {
            minecraftVolume = (float)LegendarySoundGain.bounded(minecraftVolume);
            gains = Map.copyOf(gains);
            soundFiles = Map.copyOf(soundFiles);
        }

        public Snapshot(float minecraftVolume, Map<Identifier, Float> gains) {
            this(minecraftVolume, gains, Map.of());
        }

        public Snapshot(Map<Identifier, Float> gains) {
            this(1.0f, gains);
        }

        public float multiplier(Identifier event) {
            return this.minecraftVolume * (LegendarySoundVolume.protectedGameSound(event) ? 1.0f : this.gains.getOrDefault(event, Float.valueOf(1.0f)).floatValue());
        }

        public float multiplier(Identifier event, Identifier soundFile) {
            if (LegendarySoundVolume.protectedGameSound(event)) {
                return this.minecraftVolume;
            }
            float gain = this.gains.getOrDefault(event, Float.valueOf(1.0f)).floatValue();
            if (soundFile != null) {
                gain = Math.min(gain, this.soundFiles.getOrDefault(soundFile, Float.valueOf(1.0f)).floatValue());
            }
            return this.minecraftVolume * gain;
        }

        public boolean affects(Identifier event) {
            return this.minecraftVolume != 1.0f || this.gains.containsKey(event);
        }

        public boolean active() {
            return this.minecraftVolume != 1.0f || !this.gains.isEmpty();
        }
    }
}

