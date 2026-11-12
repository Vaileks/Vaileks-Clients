/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.modules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.config.LegendarySizeSettings;

public final class LegendarySizeModule {
    private LegendarySizeModule() {
    }

    public static String identifyForGround(ItemStack itemStack) {
        return null;
    }

    public static String identifyForHand(ItemStack itemStack) {
        return null;
    }

    private static String selectedIdentity(ItemStack itemStack, boolean bl) {
        return null;
    }

    public static boolean addSelected(ItemStack itemStack) {
        return false;
    }

    public static String identify(ItemStack itemStack) {
        return null;
    }

    private static String translation(Component component, int n, int[] nArray) {
        return null;
    }

    static String legacyModel(String string, int n) {
        return null;
    }

    public static float scale(String string, ItemDisplayContext itemDisplayContext) {
        return 1.0f;
    }

    private static /* synthetic */ boolean lambda$selectedIdentity$0(String string, boolean bl, LegendarySizeSettings.SelectedItem selectedItem) {
        return false;
    }
}

