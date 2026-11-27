/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.resources.ResourceManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.gui.BundledCjkFont;

@Mixin(value={TrueTypeGlyphProviderDefinition.class})
public abstract class CompressedCjkFontMixin {
    @WrapOperation(method={"load"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/packs/resources/ResourceManager;open(Lnet/minecraft/resources/Identifier;)Ljava/io/InputStream;")})
    private InputStream bloodhelper$cjk(ResourceManager resources, Identifier id, Operation<InputStream> original) throws IOException {
        if ("bloodhelper".equals(id.getNamespace()) && "font/cjk.ttf".equals(id.getPath()) && resources.getResource(id).isEmpty()) {
            return BundledCjkFont.open();
        }
        return (InputStream)original.call(new Object[]{resources, id});
    }
}

