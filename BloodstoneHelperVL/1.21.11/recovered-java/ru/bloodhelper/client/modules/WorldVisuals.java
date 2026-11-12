/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1163
 *  net.minecraft.class_12197
 *  net.minecraft.class_12202
 *  net.minecraft.class_12205
 *  net.minecraft.class_12205$class_12314
 *  net.minecraft.class_12206
 *  net.minecraft.class_12211
 *  net.minecraft.class_12316
 *  net.minecraft.class_12316$class_12317
 *  net.minecraft.class_12331
 *  net.minecraft.class_1937
 *  net.minecraft.class_1959
 *  net.minecraft.class_1959$class_1963
 *  net.minecraft.class_2338
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  net.minecraft.class_6539
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Objects;
import net.minecraft.class_1163;
import net.minecraft.class_12197;
import net.minecraft.class_12202;
import net.minecraft.class_12205;
import net.minecraft.class_12206;
import net.minecraft.class_12211;
import net.minecraft.class_12316;
import net.minecraft.class_12331;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_6539;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.WorldVisualSettings;

public final class WorldVisuals {
    private static final List<class_12197<?>> VISUAL_ATTRIBUTES = List.of(class_12206.field_63742, class_12206.field_64120, class_12206.field_64121, class_12206.field_64122, class_12206.field_64123, class_12206.field_63744, class_12206.field_64124, class_12206.field_64125, class_12206.field_63746, class_12206.field_64339, class_12206.field_64126, class_12206.field_64340, class_12206.field_64341, class_12206.field_64342, class_12206.field_64344, class_12206.field_64345, class_12206.field_64346);
    private static class_638 observedLevel;
    private static class_12205 visualEnvironment;
    private static Snapshot observed;
    private static volatile class_1959 tintBiome;
    private static float samplePartialTick;
    private static long sampledGameTime;
    private static long sampledDayTime;

    private WorldVisuals() {
    }

    public static WorldVisualSettings settings() {
        return BloodHelperClient.config().worldVisuals;
    }

    public static boolean isVisualAttribute(class_12197<?> attribute) {
        return VISUAL_ATTRIBUTES.contains(attribute);
    }

    private static class_1959 resolveBiome(class_638 level, String biome) {
        class_2960 id = class_2960.method_12829((String)(biome == null ? "" : biome));
        return id == null ? null : (class_1959)level.method_30349().method_30530(class_7924.field_41236).method_63535(id);
    }

    public static void tick(class_310 client) {
        if (client.field_1687 == null) {
            WorldVisuals.reset();
            return;
        }
        WorldVisuals.configure(client);
        if (visualEnvironment != null) {
            visualEnvironment.method_76405();
        }
    }

    private static void configure(class_310 client) {
        class_638 level = client.field_1687;
        if (level == null) {
            return;
        }
        Snapshot next = Snapshot.of(WorldVisuals.settings());
        if (level == observedLevel && next.equals(observed)) {
            return;
        }
        class_1959 oldTint = tintBiome;
        boolean sameLevel = level == observedLevel;
        observedLevel = level;
        observed = next;
        tintBiome = next.ambience ? WorldVisuals.resolveBiome(level, next.biome) : null;
        visualEnvironment = null;
        sampledDayTime = Long.MIN_VALUE;
        sampledGameTime = Long.MIN_VALUE;
        if (next.time || next.weather || tintBiome != null) {
            visualEnvironment = WorldVisuals.createEnvironment(level);
        }
        if (sameLevel && oldTint != tintBiome) {
            level.method_23784();
            client.field_1769.method_3279();
        }
    }

    private static class_12205 createEnvironment(final class_638 level) {
        class_12205.class_12314 builder = class_12205.method_76394().method_76417(level.method_8597().comp_5146());
        class_1959 selected = tintBiome;
        if (selected != null) {
            builder.method_76417(selected.method_75734());
        } else {
            for (class_12197<?> attribute : VISUAL_ATTRIBUTES) {
                WorldVisuals.addActualBiomeLayer(builder, level, attribute);
            }
        }
        for (class_6880 timeline : level.method_8597().comp_5222()) {
            builder.method_76420(timeline, () -> timeline.method_40225(class_12331.field_64405) ? WorldVisuals.settings().visualDayTime(level.method_8532()) : level.method_8532());
        }
        if (level.method_63020()) {
            class_12316.method_76430((class_12205.class_12314)builder, (class_12316.class_12317)new class_12316.class_12317(){

                public float method_76433() {
                    return WorldVisuals.settings().rain(level.method_8430(samplePartialTick));
                }

                public float method_76435() {
                    return WorldVisuals.settings().thunder(level.method_8478(samplePartialTick));
                }
            });
        }
        return builder.method_76410();
    }

    private static <T> void addActualBiomeLayer(class_12205.class_12314 builder, class_638 level, class_12197<T> attribute) {
        builder.method_76413(attribute, (original, position, interpolator) -> {
            if (interpolator != null && attribute.method_75654()) {
                return interpolator.method_75712(attribute, original);
            }
            return ((class_1959)level.method_22385().method_24938(position.field_1352, position.field_1351, position.field_1350).comp_349()).method_75734().method_75663(attribute, original);
        });
    }

    public static <T> T cameraAttribute(class_12202 probe, class_12197<T> attribute, float partialTick, class_12211 interpolator, T original) {
        if (!WorldVisuals.isVisualAttribute(attribute)) {
            return original;
        }
        WorldVisualSettings settings = WorldVisuals.settings();
        if (!(settings.timeEnabled || settings.weatherEnabled || settings.ambienceEnabled)) {
            return original;
        }
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1773 == null) {
            return original;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera.method_75756() != probe) {
            return original;
        }
        WorldVisuals.configure(client);
        if (visualEnvironment == null) {
            return original;
        }
        samplePartialTick = partialTick;
        long gameTime = client.field_1687.method_75260();
        long dayTime = client.field_1687.method_8532();
        if (sampledGameTime != gameTime || sampledDayTime != dayTime) {
            sampledGameTime = gameTime;
            sampledDayTime = dayTime;
            visualEnvironment.method_76405();
        }
        return (T)visualEnvironment.method_75696(attribute, camera.method_71156(), interpolator);
    }

    public static float rain(class_1937 level, float original) {
        return level.method_63020() ? WorldVisuals.settings().rain(original) : original;
    }

    public static float thunder(class_1937 level, float original) {
        return level.method_63020() ? WorldVisuals.settings().thunder(original) : original;
    }

    public static class_1959.class_1963 precipitation(class_1937 level, class_2338 pos) {
        WorldVisualSettings settings = WorldVisuals.settings();
        if (!settings.weatherEnabled || !level.method_63020()) {
            return null;
        }
        if (!level.method_8398().method_12123(pos.method_10263() >> 4, pos.method_10260() >> 4)) {
            return class_1959.class_1963.field_9384;
        }
        return switch (Objects.toString(settings.weather, "CLEAR")) {
            case "RAIN" -> class_1959.class_1963.field_9382;
            case "SNOW" -> class_1959.class_1963.field_9383;
            default -> class_1959.class_1963.field_9384;
        };
    }

    public static Integer blockTint(class_2338 pos, class_6539 resolver) {
        class_1959 selected = tintBiome;
        if (selected == null || !WorldVisuals.settings().ambienceEnabled) {
            return null;
        }
        if (resolver != class_1163.field_5665 && resolver != class_1163.field_5664 && resolver != class_1163.field_57104 && resolver != class_1163.field_5666) {
            return null;
        }
        return resolver.getColor(selected, (double)pos.method_10263(), (double)pos.method_10260());
    }

    public static boolean selectedBiomeAvailable() {
        class_310 client = class_310.method_1551();
        return client.field_1687 != null && WorldVisuals.resolveBiome(client.field_1687, WorldVisuals.settings().biome) != null;
    }

    public static void reset() {
        observedLevel = null;
        observed = null;
        visualEnvironment = null;
        tintBiome = null;
        sampledDayTime = Long.MIN_VALUE;
        sampledGameTime = Long.MIN_VALUE;
    }

    static {
        sampledGameTime = Long.MIN_VALUE;
        sampledDayTime = Long.MIN_VALUE;
    }

    private record Snapshot(boolean time, int ticks, boolean weather, String precipitation, int intensity, boolean ambience, String biome) {
        static Snapshot of(WorldVisualSettings s) {
            return new Snapshot(s.timeEnabled, s.selectedTime(), s.weatherEnabled, s.weather, s.precipitationPercent, s.ambienceEnabled, s.biome);
        }
    }
}

