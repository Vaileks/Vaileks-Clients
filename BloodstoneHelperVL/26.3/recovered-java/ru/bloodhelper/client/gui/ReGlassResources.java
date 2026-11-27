/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.resources.ResourceManager
 */
package ru.bloodhelper.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import ru.bloodhelper.client.gui.ReGlassAtlasSources;
import ru.bloodhelper.client.gui.ReGlassGuiRules;
import ru.bloodhelper.client.gui.UiLanguage;

public final class ReGlassResources {
    private static final Map<Identifier, Boolean> VANILLA = new HashMap<Identifier, Boolean>();
    private static Object reloadToken;
    private static Set<Identifier> vanillaSprites;
    private static volatile Prepared prepared;
    private static Object checkedScreen;
    private static Component checkedTitle;
    private static boolean checkedCustom;

    static void prepare(ResourceManager manager, Object token) {
        prepared = new Prepared(token, ReGlassAtlasSources.vanillaSprites(manager));
    }

    private ReGlassResources() {
    }

    public static boolean vanillaTexture(Identifier texture) {
        if (texture == null || !"minecraft".equals(texture.getNamespace())) {
            return false;
        }
        ReGlassResources.refresh();
        return VANILLA.computeIfAbsent(texture, id -> Minecraft.getInstance().getResourceManager().getResource(id).map(resource -> "vanilla".equals(resource.sourcePackId())).orElse(false));
    }

    public static boolean vanillaSprite(Identifier sprite) {
        if (sprite == null || !"minecraft".equals(sprite.getNamespace())) {
            return false;
        }
        ReGlassResources.refresh();
        if (vanillaSprites == null) {
            vanillaSprites = ReGlassAtlasSources.vanillaSprites(Minecraft.getInstance().getResourceManager());
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
        Screen screen = Minecraft.getInstance().gui.screen();
        if (!(screen instanceof AbstractContainerScreen)) {
            checkedScreen = null;
            checkedTitle = null;
            return false;
        }
        Component title = screen.getTitle();
        if (screen == checkedScreen && title.equals((Object)checkedTitle)) {
            return checkedCustom;
        }
        boolean[] custom = new boolean[]{false};
        title.visit((style, text) -> {
            if (!style.getFont().equals((Object)Style.EMPTY.getFont()) || ReGlassGuiRules.hasGuiGlyph(text)) {
                custom[0] = true;
            }
            return Optional.empty();
        }, Style.EMPTY);
        checkedScreen = screen;
        checkedTitle = title.copy();
        checkedCustom = custom[0];
        return checkedCustom;
    }

    private record Prepared(Object token, Set<Identifier> sprites) {
    }
}

