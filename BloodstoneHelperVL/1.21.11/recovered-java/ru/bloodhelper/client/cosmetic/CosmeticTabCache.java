/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_640
 */
package ru.bloodhelper.client.cosmetic;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_2561;
import net.minecraft.class_640;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

public final class CosmeticTabCache {
    private final Map<class_640, Painted> entries = new WeakHashMap<class_640, Painted>();

    public class_2561 color(class_640 player, class_2561 original, String nickname, NicknamePalette palette, long time) {
        long frame = palette.animated() ? time / 33L : 0L;
        int signature = original.hashCode();
        Painted previous = this.entries.get(player);
        if (previous != null && previous.frame == frame && previous.signature == signature && previous.palette.equals(palette) && previous.original.equals((Object)original)) {
            return previous.result;
        }
        class_2561 result = CosmeticNameText.color(original, nickname, palette, time);
        if (this.entries.size() >= 256) {
            this.entries.clear();
        }
        this.entries.put(player, new Painted((class_2561)original.method_27661(), signature, palette, frame, result));
        return result;
    }

    private record Painted(class_2561 original, int signature, NicknamePalette palette, long frame, class_2561 result) {
    }
}

