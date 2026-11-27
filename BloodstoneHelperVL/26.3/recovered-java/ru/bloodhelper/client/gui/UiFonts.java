/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$Provider
 *  net.minecraft.client.gui.GlyphSource
 *  net.minecraft.client.gui.font.glyphs.EffectGlyph
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.FormattedCharSequence
 */
package ru.bloodhelper.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import ru.bloodhelper.client.mixin.FontProviderAccessor;

public final class UiFonts {
    private static final FontDescription WORLD_FACE = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"ui"));
    private static Font world;
    private static Font owner;

    private UiFonts() {
    }

    public static FormattedCharSequence worldText(String value) {
        return Component.literal((String)value).withStyle(style -> style.withFont(WORLD_FACE)).getVisualOrderText();
    }

    public static Font world() {
        Font base = Minecraft.getInstance().font;
        if (base != owner) {
            final Font.Provider provider = ((FontProviderAccessor)base).bloodhelper$provider();
            world = new Font(new Font.Provider(){

                public GlyphSource glyphs(FontDescription requested) {
                    return provider.glyphs(FontDescription.DEFAULT.equals((Object)requested) ? WORLD_FACE : requested);
                }

                public EffectGlyph effect() {
                    return provider.effect();
                }
            });
            owner = base;
        }
        return world;
    }
}

