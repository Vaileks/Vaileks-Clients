package ru.bloodhelper.client.mixin;

import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_465.class})
public interface ContainerBoundsAccessor {
    @Invoker(value="method_2383")
    public void bloodhelper$clickSlot(class_1735 var1, int var2, int var3, class_1713 var4);

    @Accessor(value="field_2776")
    public int bloodhelper$left();

    @Accessor(value="field_2800")
    public int bloodhelper$top();

    @Accessor(value="field_2792")
    public int bloodhelper$width();

    @Accessor(value="field_2779")
    public int bloodhelper$height();
}

