/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package pyz1k.render.font.msdf;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.class_2960;
import pyz1k.render.font.GFonts;
import pyz1k.render.font.msdf.MsdfFont;

public final class MsdfFonts {
    private static final Map<GFonts, MsdfFont> FONTS = new EnumMap<GFonts, MsdfFont>(GFonts.class);
    private static boolean initialized;

    private MsdfFonts() {
    }

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            FONTS.put(GFonts.REGULAR, MsdfFonts.load("inter-medium", "inter-medium"));
            FONTS.put(GFonts.MEDIUM, MsdfFonts.load("inter-medium", "inter-medium"));
            FONTS.put(GFonts.SEMIBOLD, MsdfFonts.load("inter-semi", "inter-semi"));
            FONTS.put(GFonts.BOLD, MsdfFonts.load("inter-bold", "inter-bold"));
            FONTS.put(GFonts.OPAL_MEDIUM, MsdfFonts.load("productsans-medium", "productsans-medium"));
            FONTS.put(GFonts.OPAL_BOLD, MsdfFonts.load("productsans-bold", "productsans-bold"));
            FONTS.put(GFonts.OPAL_ICONS, MsdfFonts.load("materialicons-regular", "materialicons-regular"));
            FONTS.put(GFonts.NOTIFICATION_ICONS, MsdfFonts.load("pyz1k-notification-icons", "pyz1k-notification-icons"));
        }
    }

    public static MsdfFont get(GFonts font) {
        MsdfFonts.init();
        MsdfFont msdfFont = FONTS.get((Object)font);
        if (msdfFont == null) {
            msdfFont = FONTS.get((Object)GFonts.MEDIUM);
        }
        if (msdfFont == null) {
            throw new IllegalStateException("MSDF fonts are not initialized");
        }
        return msdfFont;
    }

    private static MsdfFont load(String jsonName, String pngName) {
        return MsdfFont.load(class_2960.method_60655((String)"pyz1k", (String)("fonts/msdf/" + jsonName + ".json")), class_2960.method_60655((String)"pyz1k", (String)("fonts/msdf/" + pngName + ".png")));
    }
}

