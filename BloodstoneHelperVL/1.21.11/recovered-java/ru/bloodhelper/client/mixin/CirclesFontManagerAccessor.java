package ru.bloodhelper.client.mixin;

import net.minecraft.class_2960;
import net.minecraft.class_377;
import net.minecraft.class_378;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_378.class})
public interface CirclesFontManagerAccessor {
    @Invoker(value="method_57023")
    public class_377 bloodhelper$font(class_2960 var1);
}

