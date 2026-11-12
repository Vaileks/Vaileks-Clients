/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 */
package ru.bloodhelper.client.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import ru.bloodhelper.client.compat.OptionalMixinHooks;

public final class LunarMixinPlugin
implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name;
        return switch (name = mixinClassName.substring(mixinClassName.lastIndexOf(46) + 1)) {
            case "BetterNameDistanceMixin", "BetterNameStyleMixin" -> true;
            case "LunarNameTagMixin" -> false;
            default -> true;
        };
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {
    }

    public void postApply(String targetClassName, ClassNode target, String mixinClassName, IMixinInfo info) {
    }

    static void register(ClassNode target, String mixinClassName) {
        String name;
        switch (name = mixinClassName.substring(mixinClassName.lastIndexOf(46) + 1)) {
            case "FreeLookMouseMixin": {
                OptionalMixinHooks.MOUSE_TURN.register(() -> LunarMixinPlugin.check(target, "bloodhelper$mouseTurn", name, "FreeLook and Zoom mouse sensitivity"));
                break;
            }
            case "FreeLookCameraMixin": {
                OptionalMixinHooks.CAMERA_ROTATION.register(() -> LunarMixinPlugin.check(target, "bloodhelper$camera", name, "FreeLook camera rotation"));
                break;
            }
            case "CrosshairMixin": {
                OptionalMixinHooks.CROSSHAIR_SPRITE.register(() -> LunarMixinPlugin.check(target, "bloodhelper$crosshair", name, "BloodHelper crosshair (client crosshair retained)"));
                OptionalMixinHooks.CROSSHAIR_PERSPECTIVE.register(() -> LunarMixinPlugin.check(target, "bloodhelper$thirdPerson", name, "Crosshair in F5"));
                break;
            }
            case "BetterNameDistanceMixin": {
                boolean present = LunarMixinPlugin.check(target, "bloodhelper$nameDistance", name, "Extended name distance");
                OptionalMixinHooks.NAME_DISTANCE.register(() -> present);
                break;
            }
        }
    }

    static boolean invoked(ClassNode target, String handler) {
        for (MethodNode method : target.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)instruction;
                if (!call.owner.equals(target.name) || !call.name.endsWith(handler)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean check(ClassNode target, String handler, String mixin, String feature) {
        boolean present = LunarMixinPlugin.invoked(target, handler);
        if (!present) {
            LoggerFactory.getLogger((String)"BloodHelper/Lunar").warn("{}: vanilla call site was replaced; {} unavailable. Other required mixins remain checked.", (Object)mixin, (Object)feature);
        }
        return present;
    }
}

