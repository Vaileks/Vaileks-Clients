/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.Pyz1k;
import pyz1k.event.impl.EventKeyPress;
import pyz1k.module.impl.visuals.Interface;

@Mixin(value={class_309.class})
public class KeyboardMixin {
    @Inject(method={"method_1466"}, at={@At(value="HEAD")})
    private void onKey(long window, int action, class_11908 input, CallbackInfo ci) {
        if (window == class_310.method_1551().method_22683().method_4490()) {
            Pyz1k.getInstance().getEventBus().post((Object)new EventKeyPress(input.comp_4795(), action));
        }
    }

    @Inject(method={"method_1466"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHudEditorKey(long window, int action, class_11908 input, CallbackInfo ci) {
        Interface interfaceModule;
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1755 instanceof class_408 && window == mc.method_22683().method_4490() && action == 1 && (interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class)) != null && interfaceModule.handleHudEditorKeyPressed(input)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1457"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHudEditorChar(long window, class_11905 input, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1755 instanceof class_408 && window == mc.method_22683().method_4490()) {
            int codePoint;
            Interface interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
            int n = codePoint = input == null ? 0 : input.comp_4793();
            if (interfaceModule != null && interfaceModule.handleHudEditorCharTyped(codePoint)) {
                ci.cancel();
            }
        }
    }
}

