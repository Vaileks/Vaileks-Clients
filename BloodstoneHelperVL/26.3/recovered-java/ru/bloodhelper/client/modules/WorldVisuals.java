/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.ClientClockManager$ClientClockInstance
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.BiomeColors
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.attribute.EnvironmentAttribute
 *  net.minecraft.world.attribute.EnvironmentAttributeProbe
 *  net.minecraft.world.attribute.EnvironmentAttributeSystem
 *  net.minecraft.world.attribute.EnvironmentAttributeSystem$Builder
 *  net.minecraft.world.attribute.EnvironmentAttributes
 *  net.minecraft.world.attribute.SpatialAttributeInterpolator
 *  net.minecraft.world.attribute.WeatherAttributes
 *  net.minecraft.world.attribute.WeatherAttributes$WeatherAccess
 *  net.minecraft.world.clock.ClockInstance
 *  net.minecraft.world.level.ColorResolver
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.Biome$Precipitation
 *  net.minecraft.world.timeline.Timelines
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.ClientClockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.attribute.WeatherAttributes;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.timeline.Timelines;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.WorldVisualSettings;

public final class WorldVisuals {
    private static final List<EnvironmentAttribute<?>> VISUAL_ATTRIBUTES = List.of(EnvironmentAttributes.FOG_COLOR, EnvironmentAttributes.FOG_START_DISTANCE, EnvironmentAttributes.FOG_END_DISTANCE, EnvironmentAttributes.SKY_FOG_END_DISTANCE, EnvironmentAttributes.CLOUD_FOG_END_DISTANCE, EnvironmentAttributes.WATER_FOG_COLOR, EnvironmentAttributes.WATER_FOG_START_DISTANCE, EnvironmentAttributes.WATER_FOG_END_DISTANCE, EnvironmentAttributes.SKY_COLOR, EnvironmentAttributes.SUNRISE_SUNSET_COLOR, EnvironmentAttributes.CLOUD_COLOR, EnvironmentAttributes.SUN_ANGLE, EnvironmentAttributes.MOON_ANGLE, EnvironmentAttributes.STAR_ANGLE, EnvironmentAttributes.STAR_BRIGHTNESS, EnvironmentAttributes.SKY_LIGHT_COLOR, EnvironmentAttributes.SKY_LIGHT_FACTOR);
    private static ClientLevel observedLevel;
    private static EnvironmentAttributeSystem visualEnvironment;
    private static Snapshot observed;
    private static volatile Biome tintBiome;
    private static float samplePartialTick;
    private static long sampledGameTime;
    private static long sampledDayTime;

    private WorldVisuals() {
    }

    public static WorldVisualSettings settings() {
        return BloodHelperClient.config().worldVisuals;
    }

    public static boolean isVisualAttribute(EnvironmentAttribute<?> attribute) {
        return VISUAL_ATTRIBUTES.contains(attribute);
    }

    private static Biome resolveBiome(ClientLevel level, String biome) {
        Identifier id = Identifier.tryParse((String)(biome == null ? "" : biome));
        return id == null ? null : (Biome)level.registryAccess().lookupOrThrow(Registries.BIOME).getValue(id);
    }

    public static void tick(Minecraft client) {
        if (client.level == null) {
            WorldVisuals.reset();
            return;
        }
        WorldVisuals.configure(client);
        if (visualEnvironment != null) {
            visualEnvironment.invalidateTickCache();
        }
    }

    private static void configure(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }
        Snapshot next = Snapshot.of(WorldVisuals.settings());
        if (level == observedLevel && next.equals(observed)) {
            return;
        }
        Biome oldTint = tintBiome;
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
            level.clearTintCaches();
            client.levelRenderer.invalidateCompiledGeometry(client.level, client.options, client.gameRenderer.mainCamera(), client.getBlockColors());
        }
    }

    private static EnvironmentAttributeSystem createEnvironment(final ClientLevel level) {
        EnvironmentAttributeSystem.Builder builder = EnvironmentAttributeSystem.builder().addConstantLayer(level.dimensionType().attributes());
        Biome selected = tintBiome;
        if (selected != null) {
            builder.addConstantLayer(selected.getAttributes());
        } else {
            for (EnvironmentAttribute<?> attribute : VISUAL_ATTRIBUTES) {
                WorldVisuals.addActualBiomeLayer(builder, level, attribute);
            }
        }
        for (Holder timeline : level.dimensionType().timelines()) {
            builder.addTimelineLayer(timeline, clock -> {
                final ClientClockManager.ClientClockInstance nativeClock = level.clockManager().getInstance(clock);
                if (!timeline.is(Timelines.OVERWORLD_DAY)) {
                    return nativeClock;
                }
                return new ClockInstance(){

                    public float rate() {
                        return WorldVisuals.settings().timeEnabled ? 0.0f : nativeClock.rate();
                    }

                    public boolean isPaused() {
                        return WorldVisuals.settings().timeEnabled || nativeClock.isPaused();
                    }

                    public long totalTicks() {
                        return WorldVisuals.settings().visualDayTime(nativeClock.totalTicks());
                    }

                    public float partialTick() {
                        return WorldVisuals.settings().timeEnabled ? 0.0f : nativeClock.partialTick();
                    }
                };
            });
        }
        if (level.canHaveWeather()) {
            WeatherAttributes.addBuiltinLayers((EnvironmentAttributeSystem.Builder)builder, (WeatherAttributes.WeatherAccess)new WeatherAttributes.WeatherAccess(){

                public float rainLevel() {
                    return WorldVisuals.settings().rain(level.getRainLevel(samplePartialTick));
                }

                public float thunderLevel() {
                    return WorldVisuals.settings().thunder(level.getThunderLevel(samplePartialTick));
                }
            });
        }
        return builder.build();
    }

    private static <T> void addActualBiomeLayer(EnvironmentAttributeSystem.Builder builder, ClientLevel level, EnvironmentAttribute<T> attribute) {
        builder.addPositionalLayer(attribute, (original, position, interpolator) -> {
            if (interpolator != null && attribute.isSpatiallyInterpolated()) {
                return interpolator.applyAttributeLayer(attribute, original);
            }
            return ((Biome)level.getBiomeManager().getNoiseBiomeAtPosition(position.x, position.y, position.z).value()).getAttributes().applyModifier(attribute, original);
        });
    }

    public static <T> T cameraAttribute(EnvironmentAttributeProbe probe, EnvironmentAttribute<T> attribute, float partialTick, SpatialAttributeInterpolator interpolator, T original) {
        if (!WorldVisuals.isVisualAttribute(attribute)) {
            return original;
        }
        WorldVisualSettings settings = WorldVisuals.settings();
        if (!(settings.timeEnabled || settings.weatherEnabled || settings.ambienceEnabled)) {
            return original;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.gameRenderer == null) {
            return original;
        }
        Camera camera = client.gameRenderer.mainCamera();
        if (camera.attributeProbe() != probe) {
            return original;
        }
        WorldVisuals.configure(client);
        if (visualEnvironment == null) {
            return original;
        }
        samplePartialTick = partialTick;
        long gameTime = client.level.getGameTime();
        long dayTime = client.level.getOverworldClockTime();
        if (sampledGameTime != gameTime || sampledDayTime != dayTime) {
            sampledGameTime = gameTime;
            sampledDayTime = dayTime;
            visualEnvironment.invalidateTickCache();
        }
        return (T)visualEnvironment.getValue(attribute, camera.position(), interpolator);
    }

    public static float rain(Level level, float original) {
        return level.canHaveWeather() ? WorldVisuals.settings().rain(original) : original;
    }

    public static float thunder(Level level, float original) {
        return level.canHaveWeather() ? WorldVisuals.settings().thunder(original) : original;
    }

    public static Biome.Precipitation precipitation(Level level, BlockPos pos) {
        WorldVisualSettings settings = WorldVisuals.settings();
        if (!settings.weatherEnabled || !level.canHaveWeather()) {
            return null;
        }
        if (!level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return Biome.Precipitation.NONE;
        }
        return switch (Objects.toString(settings.weather, "CLEAR")) {
            case "RAIN" -> Biome.Precipitation.RAIN;
            case "SNOW" -> Biome.Precipitation.SNOW;
            default -> Biome.Precipitation.NONE;
        };
    }

    public static Integer blockTint(BlockPos pos, ColorResolver resolver) {
        Biome selected = tintBiome;
        if (selected == null || !WorldVisuals.settings().ambienceEnabled) {
            return null;
        }
        if (resolver != BiomeColors.GRASS_COLOR_RESOLVER && resolver != BiomeColors.FOLIAGE_COLOR_RESOLVER && resolver != BiomeColors.DRY_FOLIAGE_COLOR_RESOLVER && resolver != BiomeColors.WATER_COLOR_RESOLVER) {
            return null;
        }
        return resolver.getColor(selected, (double)pos.getX(), (double)pos.getZ());
    }

    public static boolean selectedBiomeAvailable() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && WorldVisuals.resolveBiome(client.level, WorldVisuals.settings().biome) != null;
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

