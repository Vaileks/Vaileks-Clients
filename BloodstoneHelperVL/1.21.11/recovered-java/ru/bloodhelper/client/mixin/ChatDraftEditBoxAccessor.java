package ru.bloodhelper.client.mixin;

import net.minecraft.class_342;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_342.class})
public interface ChatDraftEditBoxAccessor {
    @Accessor(value="field_2101")
    public int bloodhelper$draftSelectionAnchor();
}

