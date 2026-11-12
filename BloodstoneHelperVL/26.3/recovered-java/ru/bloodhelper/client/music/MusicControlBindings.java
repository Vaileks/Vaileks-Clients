/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.music;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.music.MediaService;

public final class MusicControlBindings {
    private static KeyMapping toggle;
    private static KeyMapping previous;
    private static KeyMapping next;
    private static KeyMapping backward;
    private static KeyMapping forward;
    private static KeyMapping favorite;

    private MusicControlBindings() {
    }

    public static void register(KeyMapping.Category category) {
        toggle = MusicControlBindings.register("music_toggle", category);
        previous = MusicControlBindings.register("music_previous", category);
        next = MusicControlBindings.register("music_next", category);
        backward = MusicControlBindings.register("music_backward", category);
        forward = MusicControlBindings.register("music_forward", category);
        favorite = MusicControlBindings.register("music_favorite", category);
    }

    private static KeyMapping register(String name, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper." + name, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
    }

    public static void tick(Minecraft client) {
        boolean pause = MusicControlBindings.consume(toggle);
        boolean prev = MusicControlBindings.consume(previous);
        boolean skip = MusicControlBindings.consume(next);
        int back = MusicControlBindings.consumeCount(backward);
        int ahead = MusicControlBindings.consumeCount(forward);
        boolean save = MusicControlBindings.consume(favorite);
        if (client.player == null || client.gui.screen() != null || !BloodHelperClient.config().music.enabled) {
            return;
        }
        MediaService media = BloodHelperClient.media();
        if (pause) {
            media.playPause();
        }
        if (prev) {
            media.previous();
        }
        if (skip) {
            media.next();
        }
        if (save && !prev && !skip) {
            media.addFavorite(media.snapshot().trackToken());
        }
        if (prev || skip || back == ahead) {
            return;
        }
        media.seekBy((long)(ahead - back) * 10000L);
    }

    private static boolean consume(KeyMapping key) {
        return MusicControlBindings.consumeCount(key) > 0;
    }

    private static int consumeCount(KeyMapping key) {
        int presses = 0;
        if (key != null) {
            while (key.consumeClick()) {
                presses = Math.min(16, presses + 1);
            }
        }
        return presses;
    }
}

