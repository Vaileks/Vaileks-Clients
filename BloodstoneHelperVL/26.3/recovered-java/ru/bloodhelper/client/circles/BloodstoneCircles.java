/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.circles;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.circles.IconTextRewriter;
import ru.bloodhelper.client.circles.LoadedGlyphColors;
import ru.bloodhelper.client.config.CirclesSettings;

public final class BloodstoneCircles {
    private static final IconTextRewriter REWRITE = new IconTextRewriter(LoadedGlyphColors::resolve);
    private static final boolean EXTERNAL = FabricLoader.getInstance().isModLoaded("oldiconsbloodstone") || FabricLoader.getInstance().isModLoaded("bloodstonecircles");

    private BloodstoneCircles() {
    }

    public static Component rewrite(Component input, boolean tab) {
        CirclesSettings c = BloodHelperClient.config().bloodstoneCircles;
        return c.enabled && !EXTERNAL && (tab ? c.tab : c.nameplates) ? REWRITE.rewrite(input) : input;
    }
}

