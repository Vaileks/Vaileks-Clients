/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_307
 */
package ru.bloodhelper.client.music;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.music.MediaService;

public final class MusicControlBindings {
    private static class_304 toggle;
    private static class_304 previous;
    private static class_304 next;
    private static class_304 backward;
    private static class_304 forward;
    private static class_304 favorite;

    private MusicControlBindings() {
    }

    public static void register(class_304.class_11900 category) {
        toggle = MusicControlBindings.register("music_toggle", category);
        previous = MusicControlBindings.register("music_previous", category);
        next = MusicControlBindings.register("music_next", category);
        backward = MusicControlBindings.register("music_backward", category);
        forward = MusicControlBindings.register("music_forward", category);
        favorite = MusicControlBindings.register("music_favorite", category);
    }

    private static class_304 register(String name, class_304.class_11900 category) {
        return KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper." + name, class_3675.class_307.field_1668, class_3675.field_16237.method_1444(), category));
    }

    public static void tick(class_310 client) {
        boolean pause = MusicControlBindings.consume(toggle);
        boolean prev = MusicControlBindings.consume(previous);
        boolean skip = MusicControlBindings.consume(next);
        int back = MusicControlBindings.consumeCount(backward);
        int ahead = MusicControlBindings.consumeCount(forward);
        boolean save = MusicControlBindings.consume(favorite);
        if (client.field_1724 == null || client.field_1755 != null || !BloodHelperClient.config().music.enabled) {
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

    private static boolean consume(class_304 key) {
        return MusicControlBindings.consumeCount(key) > 0;
    }

    private static int consumeCount(class_304 key) {
        int presses = 0;
        if (key != null) {
            while (key.method_1436()) {
                presses = Math.min(16, presses + 1);
            }
        }
        return presses;
    }
}

