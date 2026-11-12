/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3300
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 */
package ru.bloodhelper.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_437;
import net.minecraft.class_465;
import ru.bloodhelper.client.gui.ReGlassAtlasSources;
import ru.bloodhelper.client.gui.ReGlassGuiRules;
import ru.bloodhelper.client.gui.UiLanguage;

public final class ReGlassResources {
    private static final Map<class_2960, Boolean> VANILLA = new HashMap<class_2960, Boolean>();
    private static Object reloadToken;
    private static Set<class_2960> vanillaSprites;
    private static volatile Prepared prepared;
    private static Object checkedScreen;
    private static class_2561 checkedTitle;
    private static boolean checkedCustom;

    static void prepare(class_3300 manager, Object token) {
        prepared = new Prepared(token, ReGlassAtlasSources.vanillaSprites(manager));
    }

    private ReGlassResources() {
    }

    public static boolean vanillaTexture(class_2960 texture) {
        if (texture == null || !"minecraft".equals(texture.method_12836())) {
            return false;
        }
        ReGlassResources.refresh();
        return VANILLA.computeIfAbsent(texture, id -> class_310.method_1551().method_1478().method_14486(id).map(resource -> "vanilla".equals(resource.method_14480())).orElse(false));
    }

    public static boolean vanillaSprite(class_2960 sprite) {
        if (sprite == null || !"minecraft".equals(sprite.method_12836())) {
            return false;
        }
        ReGlassResources.refresh();
        if (vanillaSprites == null) {
            vanillaSprites = ReGlassAtlasSources.vanillaSprites(class_310.method_1551().method_1478());
        }
        return vanillaSprites.contains(sprite);
    }

    private static void refresh() {
        Object token = UiLanguage.cacheToken();
        if (reloadToken != token) {
            VANILLA.clear();
            Prepared ready = prepared;
            vanillaSprites = ready != null && ready.token == token ? ready.sprites : null;
            checkedScreen = null;
            checkedTitle = null;
            reloadToken = token;
        }
    }

    public static boolean customContainer() {
        class_437 screen = class_310.method_1551().field_1755;
        if (!(screen instanceof class_465)) {
            checkedScreen = null;
            checkedTitle = null;
            return false;
        }
        class_2561 title = screen.method_25440();
        if (screen == checkedScreen && title.equals((Object)checkedTitle)) {
            return checkedCustom;
        }
        boolean[] custom = new boolean[]{false};
        title.method_27658((style, text) -> {
            if (!style.method_27708().equals((Object)class_2583.field_24360.method_27708()) || ReGlassGuiRules.hasGuiGlyph(text)) {
                custom[0] = true;
            }
            return Optional.empty();
        }, class_2583.field_24360);
        checkedScreen = screen;
        checkedTitle = title.method_27661();
        checkedCustom = custom[0];
        return checkedCustom;
    }

    private record Prepared(Object token, Set<class_2960> sprites) {
    }
}

