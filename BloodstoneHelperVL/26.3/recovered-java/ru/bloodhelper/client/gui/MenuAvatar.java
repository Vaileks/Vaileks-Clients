/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.PlayerFaceExtractor
 *  net.minecraft.world.entity.player.PlayerSkin
 */
package ru.bloodhelper.client.gui;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.world.entity.player.PlayerSkin;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;

public final class MenuAvatar {
    private GameProfile profile;
    private Supplier<PlayerSkin> lookup;

    public void render(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, int size) {
        PlayerSkin skin;
        if (BloodHelperClient.config().streamer.hidesNickname()) {
            UiDraw.rounded(graphics, x, y, size, size, 5, ThemeColors.surface(-14144717));
            UiIcons.draw(graphics, "profiles", x + 5, y + 5, size - 10, -6775902);
            return;
        }
        if (client.player != null) {
            skin = client.player.getSkin();
        } else {
            GameProfile current = client.getGameProfile();
            if (this.lookup == null || !current.equals((Object)this.profile)) {
                this.profile = current;
                this.lookup = client.getSkinManager().createLookup(current, false);
            }
            skin = this.lookup.get();
        }
        PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)graphics, (PlayerSkin)skin, (int)x, (int)y, (int)size);
    }
}

