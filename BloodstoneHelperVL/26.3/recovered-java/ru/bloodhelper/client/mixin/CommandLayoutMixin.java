package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.bloodhelper.client.modules.CommandLayoutModule;

@Mixin(value={ChatScreen.class})
public abstract class CommandLayoutMixin {
    @ModifyVariable(method={"handleChatInput"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=0)
    private String bloodhelper$correctCommandLayout(String message) {
        return CommandLayoutModule.correct(message);
    }
}

