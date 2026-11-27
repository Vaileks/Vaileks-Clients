/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.class_1282
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.skins;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.SkinsSettings;

public final class VeritiAnimation {
    private static final double TURN = Math.PI * 2;
    private static Object world;
    private static Object player;
    private static class_243 previous;
    private static double roll;
    private static double previousRoll;
    private static double phase;
    private static double rollOrigin;
    private static int lastTick;
    private static int jumpTick;
    private static int mouthTick;
    private static int lastHurt;
    private static boolean grounded;
    private static boolean moving;
    private static boolean backward;

    private VeritiAnimation() {
    }

    private static boolean enabled(class_310 mc) {
        SkinsSettings settings = BloodHelperClient.config().skins;
        return settings.enabled && "VERITI".equals(settings.character) && mc.field_1724 != null && mc.field_1687 != null;
    }

    public static void tick(class_310 mc) {
        if (!VeritiAnimation.enabled(mc)) {
            VeritiAnimation.clear();
            return;
        }
        class_746 local = mc.field_1724;
        if (world != mc.field_1687 || player != local) {
            VeritiAnimation.clear();
            world = mc.field_1687;
            player = local;
            previous = local.method_73189();
            grounded = local.method_24828();
            lastTick = local.field_6012;
            lastHurt = local.field_6235;
        }
        if (mc.method_1493()) {
            return;
        }
        previousRoll = roll;
        lastTick = local.field_6012;
        if (local.field_6235 > lastHurt) {
            mouthTick = lastTick;
        }
        lastHurt = local.field_6235;
        if (grounded && !local.method_24828() && local.method_18798().field_1351 > 0.05 && !local.method_31549().field_7479 && !local.method_5799() && !local.method_5765()) {
            jumpTick = lastTick;
        }
        grounded = local.method_24828();
        class_243 current = local.method_73189();
        double dx = current.field_1352 - VeritiAnimation.previous.field_1352;
        double dz = current.field_1350 - VeritiAnimation.previous.field_1350;
        double distance = Math.sqrt(dx * dx + dz * dz);
        previous = current;
        if (local.method_29504()) {
            moving = false;
            jumpTick = -1000;
            mouthTick = -1000;
            return;
        }
        if (grounded && !local.method_5765() && distance > 0.0025 && distance < 2.0 && Asset.DATA != null) {
            boolean reverse;
            double yaw = Math.toRadians(local.method_36454());
            boolean bl = reverse = dx * -Math.sin(yaw) + dz * Math.cos(yaw) < -distance * 0.15;
            if (!moving || reverse != backward) {
                phase = 0.0;
                rollOrigin = roll;
            }
            backward = reverse;
            moving = true;
            Track track = Asset.DATA.clips().get(reverse ? "walkback" : "walk").get("rotation");
            double cycles = Math.floor((phase += 0.05 * Math.clamp(distance / 0.215, 0.08, 2.5)) / (double)track.duration());
            roll = rollOrigin + cycles * (double)track.sample(track.duration(), 0) + (double)track.sample(phase % (double)track.duration(), 0);
        } else {
            moving = false;
            double upright = Math.rint(roll / (Math.PI * 2)) * (Math.PI * 2);
            if (Math.abs(upright - (roll += (upright - roll) * 0.4)) < 0.001) {
                roll = upright;
            }
        }
    }

    public static void attack(class_1297 target) {
        class_310 mc = class_310.method_1551();
        if (VeritiAnimation.enabled(mc) && target instanceof class_1309 && target != mc.field_1724) {
            mouthTick = mc.field_1724.field_6012;
        }
    }

    public static void damage(class_1297 victim, class_1282 source) {
        class_310 mc = class_310.method_1551();
        if (VeritiAnimation.enabled(mc) && (victim == mc.field_1724 || source.method_5529() == mc.field_1724)) {
            mouthTick = mc.field_1724.field_6012;
        }
    }

    public static Pose capture(float age) {
        Map<String, Track> clip;
        Track scale;
        float partial = Math.clamp(age - (float)lastTick, 0.0f, 1.0f);
        float rotation = (float)(previousRoll + (roll - previousRoll) * (double)partial);
        float y = 0.0f;
        float sx = 1.0f;
        float sy = 1.0f;
        float sz = 1.0f;
        double jump = (double)(age - (float)jumpTick) / 20.0;
        if (Asset.DATA != null && jump >= 0.0 && jump <= (double)(scale = (clip = Asset.DATA.clips().get("jump")).get("scale")).duration()) {
            y = clip.get("translation").sample(jump, 0);
            sx = scale.sample(jump, 0);
            sy = scale.sample(jump, 1);
            sz = scale.sample(jump, 2);
        }
        return new Pose(rotation, y, sx, sy, sz, age - (float)mouthTick >= 0.0f && age - (float)mouthTick < 6.0f);
    }

    private static void clear() {
        player = null;
        world = null;
        previous = null;
        rollOrigin = 0.0;
        phase = 0.0;
        previousRoll = 0.0;
        roll = 0.0;
        lastHurt = 0;
        lastTick = 0;
        jumpTick = -1000;
        mouthTick = -1000;
        grounded = false;
        backward = false;
        moving = false;
    }

    static {
        jumpTick = -1000;
        mouthTick = -1000;
    }

    private static final class Asset {
        private static final Source DATA = Asset.read();

        private Asset() {
        }

        private static Source read() {
            Source throwable2;
            block11: {
                InputStream stream = VeritiAnimation.class.getResourceAsStream("/assets/bloodhelper/skins/verity.animation.json");
                try {
                    if (stream == null) {
                        throw new IOException("Missing Verity animations");
                    }
                    Source data = (Source)new Gson().fromJson((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8), Source.class);
                    for (String clip : new String[]{"walk", "walkback", "jump"}) {
                        for (Track track : data.clips().get(clip).values()) {
                            if (track.times().length != 0 && track.times().length <= 128 && track.times().length == track.values().length) continue;
                            throw new IOException("Invalid track");
                        }
                    }
                    throwable2 = data;
                    if (stream == null) break block11;
                }
                catch (Throwable throwable) {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            }
                            catch (Throwable throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                        }
                        throw throwable;
                    }
                    catch (Exception error) {
                        LoggerFactory.getLogger((String)"BloodHelper/Skins").warn("Cannot load Verity animations", (Throwable)error);
                        return null;
                    }
                }
                stream.close();
            }
            return throwable2;
        }
    }

    private record Source(float[] pivot, Map<String, Map<String, Track>> clips) {
    }

    private record Track(float[] times, float[][] values) {
        float sample(double time, int component) {
            if (time <= (double)this.times[0]) {
                return this.values[0][component];
            }
            for (int i = 1; i < this.times.length; ++i) {
                if (!(time <= (double)this.times[i])) continue;
                float alpha = (float)((time - (double)this.times[i - 1]) / (double)(this.times[i] - this.times[i - 1]));
                return this.values[i - 1][component] + (this.values[i][component] - this.values[i - 1][component]) * alpha;
            }
            return this.values[this.values.length - 1][component];
        }

        float duration() {
            return this.times[this.times.length - 1];
        }
    }

    public record Pose(float rotation, float y, float scaleX, float scaleY, float scaleZ, boolean mouthOpen) {
    }
}

