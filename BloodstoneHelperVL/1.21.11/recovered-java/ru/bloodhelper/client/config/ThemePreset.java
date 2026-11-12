/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import ru.bloodhelper.client.config.AppearancePreset;
import ru.bloodhelper.client.config.ClientConfig;

public enum ThemePreset {
    BLOOD("Blood", "\u041a\u0440\u0430\u0441\u043d\u044b\u0439 \u0430\u043a\u0446\u0435\u043d\u0442 \u00b7 \u0442\u0451\u043c\u043d\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e", "Crimson accent \u00b7 dark glass", -3401955, "DARK", "LIQUID_GLASS", -15265000, 210, 6.0, 3.0, true),
    AMOLED("AMOLED", "\u041f\u043e\u0447\u0442\u0438 \u0447\u0451\u0440\u043d\u044b\u0439 \u0444\u043e\u043d \u00b7 \u043c\u044f\u0433\u043a\u0438\u0439 \u0444\u0438\u043e\u043b\u0435\u0442\u043e\u0432\u044b\u0439", "Near-black surfaces \u00b7 soft violet", -3889921, "DARK", "MATTE", -16777216, 255, 0.0, 0.0, true),
    FROST("Frost", "\u0421\u0432\u0435\u0442\u043b\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e \u00b7 \u043b\u0435\u0434\u044f\u043d\u043e\u0439 \u0433\u043e\u043b\u0443\u0431\u043e\u0439", "Light glass \u00b7 ice blue", -14449235, "LIGHT", "LIQUID_GLASS", -2299150, 220, 9.0, 2.0, false),
    MINIMAL("Minimal", "\u0422\u043e\u043d\u043a\u0438\u0435 \u043a\u043e\u043d\u0442\u0443\u0440\u044b \u00b7 \u043d\u0435\u0439\u0442\u0440\u0430\u043b\u044c\u043d\u044b\u0439 \u0430\u043a\u0446\u0435\u043d\u0442", "Fine outlines \u00b7 neutral accent", -3025445, "DARK", "OUTLINE", -15460580, 210, 0.0, 0.0, false);

    private final String title;
    private final String descriptionRu;
    private final String descriptionEn;
    private final AppearancePreset appearance;

    private ThemePreset(String title, String descriptionRu, String descriptionEn, int accent, String mode, String surface, int backgroundColor, int alpha, double blur, double refraction, boolean shadow) {
        this.title = title;
        this.descriptionRu = descriptionRu;
        this.descriptionEn = descriptionEn;
        JsonObject visual = new JsonObject();
        visual.addProperty("accent", (Number)accent);
        visual.addProperty("themeMode", mode);
        visual.addProperty("surfaceStyle", surface);
        visual.addProperty("background", Boolean.valueOf(true));
        visual.addProperty("backgroundColor", (Number)backgroundColor);
        visual.addProperty("backgroundAlpha", (Number)alpha);
        visual.addProperty("glassBlur", (Number)blur);
        visual.addProperty("glassRefraction", (Number)refraction);
        visual.addProperty("textShadow", Boolean.valueOf(shadow));
        JsonObject document = new JsonObject();
        document.addProperty("format", "bloodhelper-appearance");
        document.addProperty("version", (Number)1);
        document.add("appearance", (JsonElement)visual);
        try {
            this.appearance = AppearancePreset.decode(document.toString());
        }
        catch (IOException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    public String title() {
        return this.title;
    }

    public String descriptionRu() {
        return this.descriptionRu;
    }

    public String descriptionEn() {
        return this.descriptionEn;
    }

    public AppearancePreset appearance() {
        return this.appearance;
    }

    public ClientConfig preview(ClientConfig current) {
        return this.appearance.preview(current);
    }
}

