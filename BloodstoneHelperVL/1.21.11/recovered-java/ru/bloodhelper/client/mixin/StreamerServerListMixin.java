/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_2561
 *  net.minecraft.class_4267$class_4270
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_4267;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_4267.class_4270.class})
public abstract class StreamerServerListMixin {
    @ModifyExpressionValue(method={"method_25343"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/class_642;field_3752:Ljava/lang/String;")})
    private String bloodhelper$maskName(String original) {
        return StreamerMode.hideServerAddress() ? HudStyle.tr("\u0421\u043a\u0440\u044b\u0442\u044b\u0439 \u0441\u0435\u0440\u0432\u0435\u0440", "Hidden server") : original;
    }

    @ModifyExpressionValue(method={"method_25343"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/class_642;field_3757:Lnet/minecraft/class_2561;")})
    private class_2561 bloodhelper$maskDescription(class_2561 original) {
        return StreamerMode.hideServerAddress() ? class_2561.method_43470((String)"Streamer Mode") : original;
    }

    @ModifyExpressionValue(method={"method_25343"}, at={@At(value="FIELD", opcode=180, target="Lnet/minecraft/class_4267$class_4270;field_47855:Ljava/util/List;")})
    private List<class_2561> bloodhelper$maskOnlinePlayers(List<class_2561> original) {
        return StreamerMode.hideNickname() ? List.of() : original;
    }
}

