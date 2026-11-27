package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.community.CommunityPanels;

@Mixin(value={TitleScreen.class})
public abstract class CommunityTitleMixin
extends Screen {
    protected CommunityTitleMixin(Component title) {
        super(title);
    }

    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return CommunityPanels.scroll(x, y, vertical, this.width, this.height) || super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void bloodhelper$community(GuiGraphicsExtractor g, int mx, int my, float delta, CallbackInfo ci) {
        Window window = Minecraft.getInstance().getWindow();
        CommunityPanels.title(g, window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}

