/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.gui;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;

public final class ReGlassCompat {
    private static final boolean INSTALLED = FabricLoader.getInstance().isModLoaded("reglass");
    private static final int MAX_SURFACES = 40;
    private static boolean checked;
    private static boolean failed;
    private static Method create;
    private static Method dimensions;
    private static Method cornerRadius;
    private static Method style;
    private static Method render;
    private static Method styleCreate;
    private static Method tint;
    private static Method blur;
    private static Method shadow;
    private static Method layer;
    private static Method smoothing;
    private static final Method[] REF_GET;
    private static final Method[] REF_SET;
    private static Method uniformsGet;
    private static Method applyBlur;
    private static Method count;
    private static int surfaces;
    private static long submitted;
    private static long failures;
    private static long fallbacks;
    private static final Map<StyleKey, Object> STYLES;

    private ReGlassCompat() {
    }

    public static void beginFrame() {
        surfaces = 0;
    }

    public static boolean installed() {
        return INSTALLED;
    }

    public static long submitted() {
        return submitted;
    }

    public static long failures() {
        return failures;
    }

    public static long fallbacks() {
        return fallbacks;
    }

    public static String status(boolean ru) {
        if (!INSTALLED) {
            return ru ? "\u0412\u0441\u0442\u0440\u043e\u0435\u043d\u043d\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e \u0434\u043b\u044f \u043a\u043d\u043e\u043f\u043e\u043a, \u043c\u0435\u043d\u044e \u0438 HUD" : "Built-in glass for buttons, menus and HUD";
        }
        if (failed) {
            return ru ? "API \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u2014 \u0431\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u044b\u0439 \u043c\u0430\u0442\u043e\u0432\u044b\u0439 \u0444\u043e\u043d" : "API unavailable \u2014 safe matte fallback";
        }
        return ru ? "\u0412\u043d\u0435\u0448\u043d\u0438\u0439 ReGlass \u00b7 \u0432\u0441\u0442\u0440\u043e\u0435\u043d\u043d\u0430\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u0430" : "External ReGlass \u00b7 built-in feature locked";
    }

    private static void initialize(Class<?> contextClass) throws ReflectiveOperationException {
        Class<?> api = Class.forName("restudio.reglass.client.api.ReGlassApi");
        for (Method method : api.getMethods()) {
            if (!method.getName().equals("create") || method.getParameterCount() != 1 || !method.getParameterTypes()[0].isAssignableFrom(contextClass)) continue;
            create = method;
        }
        if (create == null) {
            throw new NoSuchMethodException("ReGlassApi.create(context)");
        }
        Class<?> builder = create.getReturnType();
        Class<?> widgetStyle = Class.forName("restudio.reglass.client.api.WidgetStyle");
        dimensions = builder.getMethod("dimensions", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        cornerRadius = builder.getMethod("cornerRadius", Float.TYPE);
        style = builder.getMethod("style", widgetStyle);
        render = builder.getMethod("render", new Class[0]);
        styleCreate = widgetStyle.getMethod("create", new Class[0]);
        tint = widgetStyle.getMethod("tint", Integer.TYPE, Float.TYPE);
        blur = widgetStyle.getMethod("blurRadius", Integer.TYPE);
        shadow = widgetStyle.getMethod("shadow", Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
        layer = widgetStyle.getMethod("layer", Integer.TYPE);
        smoothing = widgetStyle.getMethod("smoothing", Float.TYPE);
        String[] getters = new String[]{"getRefThickness", "getRefFactor", "getRefDispersion", "getRefFresnelRange", "getRefFresnelHardness", "getRefFresnelFactor"};
        String[] setters = new String[]{"refractionThickness", "refractionFactor", "refractionDispersion", "fresnelRange", "fresnelHardness", "fresnelFactor"};
        for (int i = 0; i < getters.length; ++i) {
            ReGlassCompat.REF_GET[i] = widgetStyle.getMethod(getters[i], new Class[0]);
            ReGlassCompat.REF_SET[i] = widgetStyle.getMethod(setters[i], Float.TYPE);
        }
        Class<?> uniforms = Class.forName("restudio.reglass.client.LiquidGlassUniforms");
        uniformsGet = uniforms.getMethod("get", new Class[0]);
        applyBlur = uniforms.getMethod("tryApplyBlur", create.getParameterTypes()[0]);
        count = uniforms.getMethod("getCount", new Class[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean draw(class_332 graphics, int x, int y, int width, int height, int radius, int color) {
        if (!INSTALLED || failed) {
            return false;
        }
        Matrix3x2fStack pose = graphics.method_51448();
        if (Math.abs(pose.m01()) > 1.0E-4f || Math.abs(pose.m10()) > 1.0E-4f || pose.m00() <= 0.0f || pose.m11() <= 0.0f) {
            return false;
        }
        int left = Math.round((float)x * pose.m00() + pose.m20());
        int top = Math.round((float)y * pose.m11() + pose.m21());
        int right = Math.round((float)(x + width) * pose.m00() + pose.m20());
        int bottom = Math.round((float)(y + height) * pose.m11() + pose.m21());
        int corner = Math.round((float)radius * Math.min(pose.m00(), pose.m11()));
        pose.pushMatrix();
        try {
            pose.identity();
            boolean bl = ReGlassCompat.drawScreenSpace(graphics, left, top, right - left, bottom - top, corner, color);
            return bl;
        }
        finally {
            pose.popMatrix();
        }
    }

    private static boolean drawScreenSpace(Object graphics, int x, int y, int width, int height, int radius, int color) {
        if (!INSTALLED || failed || width <= 0 || height <= 0 || color >>> 24 == 0) {
            return false;
        }
        ClientConfig config = BloodHelperClient.config();
        if (!"LIQUID_GLASS".equals(config.surfaceStyle) || config.glassBlur <= 0.0) {
            return false;
        }
        try {
            if (!checked) {
                checked = true;
                ReGlassCompat.initialize(graphics.getClass());
            }
            Object uniforms = uniformsGet.invoke(null, new Object[0]);
            if (surfaces >= 40 || ((Number)count.invoke(uniforms, new Object[0])).intValue() >= 56) {
                ++fallbacks;
                return false;
            }
            StyleKey key = new StyleKey(color, Math.clamp(Math.round(config.glassBlur), 1, 16), config.glassRefraction, width > 200 && height > 150 ? 1 : 3);
            Object material = STYLES.get(key);
            if (material == null) {
                int i;
                material = styleCreate.invoke(null, new Object[0]);
                tint.invoke(material, color & 0xFFFFFF, Float.valueOf((float)(color >>> 24) / 255.0f * 0.72f));
                blur.invoke(material, key.radius());
                shadow.invoke(material, Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(0.0f));
                layer.invoke(material, key.layer());
                smoothing.invoke(material, Float.valueOf(0.0f));
                float[] refraction = new float[REF_GET.length];
                for (i = 0; i < refraction.length; ++i) {
                    refraction[i] = ((Number)REF_GET[i].invoke(material, new Object[0])).floatValue();
                }
                refraction[1] = refraction[1] * (float)(key.refraction() / 3.0);
                refraction[2] = refraction[2] * (float)(key.refraction() / 3.0);
                for (i = 0; i < refraction.length; ++i) {
                    REF_SET[i].invoke(material, Float.valueOf(refraction[i]));
                }
                if (STYLES.size() >= 256) {
                    STYLES.clear();
                }
                STYLES.put(key, material);
            }
            Object builder = create.invoke(null, graphics);
            dimensions.invoke(builder, x, y, width, height);
            cornerRadius.invoke(builder, Float.valueOf(Math.max(0, radius)));
            style.invoke(builder, material);
            applyBlur.invoke(uniforms, graphics);
            render.invoke(builder, new Object[0]);
            ++surfaces;
            ++submitted;
            return true;
        }
        catch (LinkageError | ReflectiveOperationException | RuntimeException error) {
            failed = true;
            ++failures;
            STYLES.clear();
            LoggerFactory.getLogger((String)"BloodHelper/ReGlass").warn("ReGlass API unavailable; using matte surfaces without a second blur renderer", error);
            return false;
        }
    }

    static {
        REF_GET = new Method[6];
        REF_SET = new Method[6];
        STYLES = new LinkedHashMap<StyleKey, Object>();
    }

    private record StyleKey(int color, int radius, double refraction, int layer) {
    }
}

