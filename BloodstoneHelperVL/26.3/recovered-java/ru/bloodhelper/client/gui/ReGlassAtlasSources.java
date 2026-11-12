/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource$DiscardableLoader
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource$Output
 *  net.minecraft.client.renderer.texture.atlas.SpriteSources
 *  net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister
 *  net.minecraft.client.renderer.texture.atlas.sources.SingleFile
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 */
package ru.bloodhelper.client.gui;

import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

final class ReGlassAtlasSources {
    private static final Identifier ATLAS = Identifier.withDefaultNamespace((String)"atlases/gui.json");

    private ReGlassAtlasSources() {
    }

    static Set<Identifier> vanillaSprites(ResourceManager resources) {
        final HashMap sprites = new HashMap();
        for (Resource atlas : resources.getResourceStack(ATLAS)) {
            try {
                BufferedReader reader = atlas.openAsReader();
                try {
                    List sources = (List)SpriteSources.FILE_CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)JsonParser.parseReader((Reader)reader)).getOrThrow();
                    for (final SpriteSource source : sources) {
                        source.run(resources, new SpriteSource.Output(){

                            public void add(Identifier id, Resource resource) {
                                sprites.put(id, ReGlassAtlasSources.directGuiTexture(source, id) && "vanilla".equals(resource.sourcePackId()));
                            }

                            public void add(Identifier id, SpriteSource.DiscardableLoader loader) {
                                sprites.put(id, false);
                                loader.discard();
                            }

                            public void removeAll(Predicate<Identifier> filter) {
                                sprites.keySet().removeIf(filter);
                            }
                        });
                    }
                }
                finally {
                    if (reader == null) continue;
                    reader.close();
                }
            }
            catch (IOException | RuntimeException exception) {}
        }
        return sprites.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    private static boolean directGuiTexture(SpriteSource source, Identifier sprite) {
        SingleFile file;
        if (!"minecraft".equals(sprite.getNamespace())) {
            return false;
        }
        if (source instanceof DirectoryLister) {
            DirectoryLister directory = (DirectoryLister)source;
            return directory.sourcePath().equals("gui/sprites") && directory.idPrefix().isEmpty();
        }
        return source instanceof SingleFile && (file = (SingleFile)source).resourceId().equals((Object)Identifier.withDefaultNamespace((String)("gui/sprites/" + sprite.getPath())));
    }
}

