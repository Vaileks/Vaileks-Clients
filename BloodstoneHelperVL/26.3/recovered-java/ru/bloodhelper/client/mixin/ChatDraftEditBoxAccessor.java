package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EditBox.class})
public interface ChatDraftEditBoxAccessor {
    @Accessor(value="highlightPos")
    public int bloodhelper$draftSelectionAnchor();
}

