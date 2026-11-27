/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.class_1799;
import net.minecraft.class_9334;

public final class InventoryLayoutPlan {
    private InventoryLayoutPlan() {
    }

    public static Plan create(class_1799[] current, class_1799[] template, boolean[] locked) {
        return InventoryLayoutPlan.create(current, template, locked, (index, item) -> true);
    }

    public static Plan create(class_1799[] current, class_1799[] template, boolean[] locked, BiPredicate<Integer, class_1799> canPlace) {
        if (current.length != 36 && current.length != 41 || locked.length != current.length || template.length != 36 && template.length != 41 || template.length > current.length) {
            throw new IllegalArgumentException("Expected 36 legacy or 41 equipment slots");
        }
        class_1799[] working = (class_1799[])Arrays.stream(current).map(class_1799::method_7972).toArray(class_1799[]::new);
        ArrayList<Swap> result = new ArrayList<Swap>();
        int missing = 0;
        int buffer = -1;
        for (int i = 0; i < 9; ++i) {
            if (locked[i]) continue;
            buffer = i;
        }
        if (template.length == 41) {
            for (int dst = 36; dst < 41; ++dst) {
                List<Swap> transaction;
                if (locked[dst] || InventoryLayoutPlan.equivalent(working[dst], template[dst])) continue;
                int src = -1;
                for (int i = 0; i < 41; ++i) {
                    if (i == dst || i >= 36 && i != 40 || locked[i] || template[dst].method_7960() && i >= 36 || !InventoryLayoutPlan.equivalent(working[i], template[dst])) continue;
                    src = i;
                    break;
                }
                if (src < 0 || !canPlace.test(dst, working[src])) {
                    ++missing;
                    continue;
                }
                if (src < 9) {
                    transaction = List.of(new Swap(dst, src));
                } else {
                    if (buffer < 0) {
                        return new Plan(List.of(), missing, true);
                    }
                    transaction = List.of(new Swap(src, buffer), new Swap(dst, buffer), new Swap(src, buffer));
                }
                class_1799[] trial = (class_1799[])working.clone();
                boolean legal = true;
                for (Swap swap : transaction) {
                    class_1799 a = trial[swap.slot];
                    class_1799 b = trial[swap.hotbar];
                    if (!canPlace.test(swap.slot, b) || !canPlace.test(swap.hotbar, a)) {
                        legal = false;
                        break;
                    }
                    trial[swap.slot] = b;
                    trial[swap.hotbar] = a;
                }
                if (!legal) {
                    ++missing;
                    continue;
                }
                result.addAll(transaction);
                working = trial;
            }
        }
        Plan inventory = InventoryLayoutPlan.ordinary(Arrays.copyOf(working, 36), Arrays.copyOf(template, 36), Arrays.copyOf(locked, 36));
        if (inventory.needsBuffer) {
            return new Plan(List.of(), missing + inventory.missing, true);
        }
        result.addAll(inventory.swaps);
        return new Plan(List.copyOf(result), missing + inventory.missing, false);
    }

    private static boolean equivalent(class_1799 a, class_1799 b) {
        if (a.method_7960() || b.method_7960()) {
            return a.method_7960() && b.method_7960();
        }
        a = a.method_7972();
        b = b.method_7972();
        a.method_57381(class_9334.field_49629);
        b.method_57381(class_9334.field_49629);
        return InventoryLayoutPlan.same(a, b);
    }

    private static Plan ordinary(class_1799[] current, class_1799[] template, boolean[] locked) {
        int src;
        int dst;
        int i;
        int[] desired = new int[36];
        Arrays.fill(desired, -1);
        boolean[] used = new boolean[36];
        int missing = 0;
        int buffer = -1;
        class_1799[] have = InventoryLayoutPlan.normalized(current);
        class_1799[] want = InventoryLayoutPlan.normalized(template);
        for (i = 0; i < 36; ++i) {
            if (i < 9 && !locked[i]) {
                buffer = i;
            }
            if (!locked[i]) continue;
            desired[i] = i;
            used[i] = true;
        }
        for (i = 0; i < 36; ++i) {
            if (locked[i] || want[i].method_7960() || !InventoryLayoutPlan.same(have[i], want[i])) continue;
            desired[i] = i;
            used[i] = true;
        }
        for (dst = 0; dst < 36; ++dst) {
            if (locked[dst] || desired[dst] >= 0 || want[dst].method_7960()) continue;
            for (src = 0; src < 36; ++src) {
                if (used[src] || !InventoryLayoutPlan.same(have[src], want[dst])) continue;
                desired[dst] = src;
                used[src] = true;
                break;
            }
            if (desired[dst] >= 0) continue;
            ++missing;
        }
        for (dst = 0; dst < 36; ++dst) {
            int empty;
            if (desired[dst] >= 0 || !want[dst].method_7960()) continue;
            int n = empty = !used[dst] && have[dst].method_7960() ? dst : -1;
            if (empty < 0) {
                for (int src2 = 0; src2 < 36; ++src2) {
                    if (used[src2] || !have[src2].method_7960()) continue;
                    empty = src2;
                    break;
                }
            }
            if (empty < 0) continue;
            desired[dst] = empty;
            used[empty] = true;
        }
        for (i = 0; i < 36; ++i) {
            if (desired[i] >= 0 || used[i]) continue;
            desired[i] = i;
            used[i] = true;
        }
        block7: for (dst = 0; dst < 36; ++dst) {
            if (desired[dst] >= 0) continue;
            for (src = 0; src < 36; ++src) {
                if (used[src]) continue;
                desired[dst] = src;
                used[src] = true;
                continue block7;
            }
        }
        ArrayList<Swap> swaps = new ArrayList<Swap>();
        int[] at = new int[36];
        for (int i2 = 0; i2 < 36; ++i2) {
            at[i2] = i2;
        }
        for (int dst2 = 0; dst2 < 36; ++dst2) {
            if (at[dst2] == desired[dst2]) continue;
            int src3 = 0;
            while (at[src3] != desired[dst2]) {
                ++src3;
            }
            if (dst2 < 9) {
                swaps.add(new Swap(src3, dst2));
            } else if (src3 < 9) {
                swaps.add(new Swap(dst2, src3));
            } else {
                if (buffer < 0) {
                    return new Plan(List.of(), missing, true);
                }
                swaps.add(new Swap(dst2, buffer));
                swaps.add(new Swap(src3, buffer));
                swaps.add(new Swap(dst2, buffer));
            }
            int item = at[dst2];
            at[dst2] = at[src3];
            at[src3] = item;
        }
        return new Plan(List.copyOf(swaps), missing, false);
    }

    private static class_1799[] normalized(class_1799[] items) {
        class_1799[] result = new class_1799[36];
        for (int i = 0; i < 36; ++i) {
            result[i] = items[i].method_7972();
            if (result[i].method_7960()) continue;
            result[i].method_57381(class_9334.field_49629);
        }
        return result;
    }

    private static boolean same(class_1799 a, class_1799 b) {
        return class_1799.method_31577((class_1799)a, (class_1799)b);
    }

    public record Plan(List<Swap> swaps, int missing, boolean needsBuffer) {
    }

    public record Swap(int slot, int hotbar) {
    }
}

