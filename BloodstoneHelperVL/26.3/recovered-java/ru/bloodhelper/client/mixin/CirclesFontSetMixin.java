/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphProvider
 *  com.mojang.blaze3d.font.UnbakedGlyph
 *  com.mojang.blaze3d.platform.NativeImage
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  net.minecraft.client.gui.font.FontSet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.List;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.circles.DominantColor;
import ru.bloodhelper.client.circles.GlyphColorSource;
import ru.bloodhelper.client.mixin.CirclesBitmapAccessor;
import ru.bloodhelper.client.mixin.CirclesImageAccessor;

@Mixin(value={FontSet.class})
public abstract class CirclesFontSetMixin
implements GlyphColorSource {
    @Shadow
    private List<GlyphProvider> activeProviders;
    @Unique
    private final Int2IntMap bloodhelper$colors = new Int2IntOpenHashMap();

    @Override
    public int bloodstonecircles$getColor(int point) {
        if (this.bloodhelper$colors.containsKey(point)) {
            return this.bloodhelper$colors.get(point);
        }
        int color = -1;
        for (GlyphProvider provider : this.activeProviders) {
            UnbakedGlyph glyph = provider.getGlyph(point);
            if (glyph == null) continue;
            if (!(glyph instanceof CirclesBitmapAccessor)) break;
            CirclesBitmapAccessor bitmap = (CirclesBitmapAccessor)glyph;
            NativeImage image = ((CirclesImageAccessor)bitmap.bloodhelper$data()).bloodhelper$image();
            int x = bitmap.bloodhelper$x();
            int y = bitmap.bloodhelper$y();
            color = DominantColor.extract(bitmap.bloodhelper$width(), bitmap.bloodhelper$height(), (a, b) -> image.getPixel(x + a, y + b));
            break;
        }
        if (this.bloodhelper$colors.size() >= 4096) {
            this.bloodhelper$colors.clear();
        }
        this.bloodhelper$colors.put(point, color);
        return color;
    }

    @Inject(method={"resetTextures"}, at={@At(value="HEAD")})
    private void bloodhelper$reload(CallbackInfo ci) {
        this.bloodhelper$colors.clear();
    }
}

