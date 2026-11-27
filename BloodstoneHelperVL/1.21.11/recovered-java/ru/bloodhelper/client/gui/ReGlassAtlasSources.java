/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_7948
 *  net.minecraft.class_7948$class_7949
 *  net.minecraft.class_7948$class_7950
 *  net.minecraft.class_7952
 *  net.minecraft.class_7954
 *  net.minecraft.class_7955
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
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_7948;
import net.minecraft.class_7952;
import net.minecraft.class_7954;
import net.minecraft.class_7955;

final class ReGlassAtlasSources {
    private static final class_2960 ATLAS = class_2960.method_60656((String)"atlases/gui.json");

    private ReGlassAtlasSources() {
    }

    static Set<class_2960> vanillaSprites(class_3300 resources) {
        final HashMap sprites = new HashMap();
        for (class_3298 atlas : resources.method_14489(ATLAS)) {
            try {
                BufferedReader reader = atlas.method_43039();
                try {
                    List sources = (List)class_7952.field_41397.parse((DynamicOps)JsonOps.INSTANCE, (Object)JsonParser.parseReader((Reader)reader)).getOrThrow();
                    for (final class_7948 source : sources) {
                        source.method_47673(resources, new class_7948.class_7949(){

                            public void method_47674(class_2960 id, class_3298 resource) {
                                sprites.put(id, ReGlassAtlasSources.directGuiTexture(source, id) && "vanilla".equals(resource.method_14480()));
                            }

                            public void method_47670(class_2960 id, class_7948.class_7950 loader) {
                                sprites.put(id, false);
                                loader.method_47676();
                            }

                            public void method_47671(Predicate<class_2960> filter) {
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

    private static boolean directGuiTexture(class_7948 source, class_2960 sprite) {
        class_7955 file;
        if (!"minecraft".equals(sprite.method_12836())) {
            return false;
        }
        if (source instanceof class_7954) {
            class_7954 directory = (class_7954)source;
            return directory.comp_3613().equals("gui/sprites") && directory.comp_3614().isEmpty();
        }
        return source instanceof class_7955 && (file = (class_7955)source).comp_3619().equals((Object)class_2960.method_60656((String)("gui/sprites/" + sprite.method_12832())));
    }
}

