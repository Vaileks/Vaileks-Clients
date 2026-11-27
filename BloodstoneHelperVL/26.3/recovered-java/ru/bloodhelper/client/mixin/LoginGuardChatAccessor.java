package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ChatScreen.class})
public interface LoginGuardChatAccessor {
    @Accessor(value="input")
    public EditBox bloodhelper$loginInput();
}

