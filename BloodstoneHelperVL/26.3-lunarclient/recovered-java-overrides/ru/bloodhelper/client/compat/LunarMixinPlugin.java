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
    public void onLoad(String string) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String string, String string2) {
        String string3;
        return switch (string3 = string2.substring(string2.lastIndexOf(46) + 1)) {
            case "BetterNameDistanceMixin", "BetterNameStyleMixin" -> true;
            case "LunarNameTagMixin" -> false;
            default -> true;
        };
    }

    public void acceptTargets(Set<String> set, Set<String> set2) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
    }

    public void postApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
        LunarMixinPlugin.register(classNode, string2);
    }

    static void register(ClassNode classNode, String string) {
        String string2;
        switch (string2 = string.substring(string.lastIndexOf(46) + 1)) {
            case "FreeLookMouseMixin": {
                OptionalMixinHooks.MOUSE_TURN.register(() -> LunarMixinPlugin.check(classNode, "bloodhelper$freeLook", string2, "FreeLook mouse input"));
                break;
            }
            case "FreeLookCameraMixin": {
                OptionalMixinHooks.CAMERA_ROTATION.register(() -> LunarMixinPlugin.check(classNode, "bloodhelper$camera", string2, "FreeLook camera rotation"));
                break;
            }
            case "CrosshairMixin": {
                OptionalMixinHooks.CROSSHAIR_SPRITE.register(() -> LunarMixinPlugin.check(classNode, "bloodhelper$crosshair", string2, "BloodHelper crosshair (client crosshair retained)"));
                OptionalMixinHooks.CROSSHAIR_PERSPECTIVE.register(() -> LunarMixinPlugin.check(classNode, "bloodhelper$thirdPerson", string2, "Crosshair in F5"));
                break;
            }
            case "BetterNameDistanceMixin": {
                boolean bl = LunarMixinPlugin.check(classNode, "bloodhelper$nameDistance", string2, "Extended name distance");
                OptionalMixinHooks.NAME_DISTANCE.register(() -> bl);
                break;
            }
        }
    }

    static boolean invoked(ClassNode classNode, String string) {
        for (MethodNode methodNode : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (!(abstractInsnNode instanceof MethodInsnNode)) continue;
                MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                if (!methodInsnNode.owner.equals(classNode.name) || !methodInsnNode.name.endsWith(string)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean check(ClassNode classNode, String string, String string2, String string3) {
        boolean bl = LunarMixinPlugin.invoked(classNode, string);
        if (!bl) {
            LoggerFactory.getLogger((String)"BloodHelper/Lunar").warn("{}: vanilla call site was replaced; {} unavailable. Other required mixins remain checked.", (Object)string2, (Object)string3);
        }
        return bl;
    }
}

