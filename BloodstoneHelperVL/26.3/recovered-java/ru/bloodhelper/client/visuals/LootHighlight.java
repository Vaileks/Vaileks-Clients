/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.visuals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.modules.LegendaryItemCatalog;

public final class LootHighlight {
    private static List<Found> items = List.of();
    private static Map<Integer, Integer> glow = Map.of();
    private static int ticks;
    private static Object world;
    private static final Set<String> EXTRA;

    private LootHighlight() {
    }

    public static void reset() {
    }

    public static Kind kindOf(ItemStack itemStack) {
        return null;
    }

    private static boolean enabled(Kind kind) {
        return false;
    }

    private static int color(Kind kind) {
        return 0;
    }

    public static void tick(Minecraft minecraft) {
    }

    public static void extract(float f) {
    }

    public static int selectedCount() {
        return 0;
    }

    public static int glowColor(Entity entity) {
        return 0;
    }

    private static /* synthetic */ boolean lambda$kindOf$0(String string, LegendaryItemCatalog.Weapon weapon) {
        return false;
    }

    static {
        EXTRA = Set.of("\u043a\u043e\u0440\u043e\u043d\u0430", "\u043f\u043b\u0430\u043c\u0435\u043d\u043d\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442", "\u0431\u0430\u043b\u0438\u0441\u0442\u0430", "\u0431\u0430\u043b\u043b\u0438\u0441\u0442\u0430", "\u043d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u043e\u0435 \u043a\u043e\u043f\u044c\u0435", "\u043a\u0438\u0440\u043a\u0430 \u043f\u0432\u0435", "\u043a\u0440\u043e\u0432\u0430\u0432\u044b\u0439 \u043c\u0435\u0447", "\u043a\u0440\u043e\u0432\u0430\u0432\u044b\u0439 \u0433\u0430\u0440\u043f\u0443\u043d", "\u043a\u0440\u043e\u0432\u0430\u0432\u0430\u044f \u0431\u0443\u043b\u0430\u0432\u0430");
    }

    public static enum Kind {
        LEGENDARY,
        TOTEM,
        APPLE;

    }

    private record Found(ItemEntity entity, Kind kind, double distance) {
    }
}

