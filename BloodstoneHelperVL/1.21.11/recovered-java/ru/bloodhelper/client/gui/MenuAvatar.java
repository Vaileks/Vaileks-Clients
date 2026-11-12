/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_7532
 *  net.minecraft.class_8685
 */
package ru.bloodhelper.client.gui;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_7532;
import net.minecraft.class_8685;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;

public final class MenuAvatar {
    private GameProfile profile;
    private Supplier<class_8685> lookup;

    public void render(class_332 graphics, class_310 client, int x, int y, int size) {
        class_8685 skin;
        if (BloodHelperClient.config().streamer.hidesNickname()) {
            UiDraw.rounded(graphics, x, y, size, size, 5, ThemeColors.surface(-14144717));
            UiIcons.draw(graphics, "profiles", x + 5, y + 5, size - 10, -6775902);
            return;
        }
        if (client.field_1724 != null) {
            skin = client.field_1724.method_52814();
        } else {
            GameProfile current = client.method_53462();
            if (this.lookup == null || !current.equals((Object)this.profile)) {
                this.profile = current;
                this.lookup = client.method_1582().method_73544(current, false);
            }
            skin = this.lookup.get();
        }
        class_7532.method_52722((class_332)graphics, (class_8685)skin, (int)x, (int)y, (int)size);
    }
}

