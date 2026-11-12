/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.bloodhelper.client.skins;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.SkinsSettings;
import ru.bloodhelper.client.skins.SharedSkins;
import ru.bloodhelper.client.skins.SkinMesh;
import ru.bloodhelper.client.skins.VeritiAnimation;

public final class SkinsRenderer {
    private static final Map<String, class_2960> TEXTURES = new ConcurrentHashMap<String, class_2960>();

    private static class_2960 texture(SkinMesh mesh, SkinMesh.Surface surface) {
        return TEXTURES.computeIfAbsent(mesh.textures()[surface.material()], path -> class_2960.method_60655((String)"bloodhelper", (String)path));
    }

    private SkinsRenderer() {
    }

    public static SkinMesh selected(Object value) {
        if (!(value instanceof class_10055)) {
            return null;
        }
        class_10055 state = (class_10055)value;
        class_310 mc = class_310.method_1551();
        SkinsSettings settings = BloodHelperClient.config().skins;
        if (mc.field_1724 == null || state.field_53542 || state.field_53333) {
            return null;
        }
        if (state.field_53528 == mc.field_1724.method_5628()) {
            return settings.enabled && !mc.field_1690.method_31044().method_31034() ? SkinMesh.get(settings.character) : null;
        }
        if (!settings.showOthers || mc.field_1687 == null) {
            return null;
        }
        class_1297 entity = mc.field_1687.method_8469(state.field_53528);
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        String model = SharedSkins.model(player.method_5667(), player.method_7334().name());
        return model.isEmpty() ? null : SkinMesh.get(model);
    }

    public static boolean heldItems(Object state) {
        return BloodHelperClient.config().skins.heldItems && SkinsRenderer.selected(state) == SkinMesh.get("RUDY");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void submit(SkinMesh mesh, class_10055 state, class_591 model, class_4587 poses, class_11659 collector, int light, int overlay, int tint, int outline) {
        model.method_62110(state);
        boolean veriti = mesh == SkinMesh.get("VERITI");
        VeritiAnimation.Pose motion = veriti ? VeritiAnimation.capture(state.field_53328) : null;
        for (int index = 0; index < mesh.bones().length; ++index) {
            SkinMesh.Bone bone;
            if (veriti && index != (motion.mouthOpen() ? 1 : 0) || (bone = mesh.bones()[index]).surfaces().length == 0) continue;
            poses.method_22903();
            try {
                if (veriti) {
                    poses.method_46416(bone.x(), bone.y() + motion.y(), bone.z());
                    poses.method_22905(motion.scaleX(), motion.scaleY(), motion.scaleZ());
                    poses.method_22907((Quaternionfc)class_7833.field_40714.rotation(motion.rotation()));
                    poses.method_46416(-bone.x(), -bone.y(), -bone.z());
                } else {
                    SkinsRenderer.animate(poses, bone, index, model);
                }
                for (SkinMesh.Surface surface : bone.surfaces()) {
                    class_2960 texture = SkinsRenderer.texture(mesh, surface);
                    collector.method_73483(poses, class_12249.method_75994((class_2960)texture), (pose, vertices) -> SkinsRenderer.draw(surface, pose, vertices, light, overlay, tint));
                    if (outline == 0) continue;
                    collector.method_73483(poses, class_12249.method_76018((class_2960)texture), (pose, vertices) -> SkinsRenderer.draw(surface, pose, vertices, light, overlay, outline));
                }
                continue;
            }
            finally {
                poses.method_22909();
            }
        }
    }

    private static class_630 part(class_591 model, int index) {
        return switch (index) {
            case 0 -> model.field_3391;
            case 1 -> model.field_3398;
            case 2 -> model.field_27433;
            case 3 -> model.field_3401;
            case 4 -> model.field_3397;
            default -> model.field_3392;
        };
    }

    private static void animate(class_4587 poses, SkinMesh.Bone bone, int index, class_591 model) {
        float restX;
        class_630 part = SkinsRenderer.part(model, index);
        float f = index == 2 ? 5.0f : (index == 3 ? -5.0f : (index == 4 ? 1.9f : (restX = index == 5 ? -1.9f : 0.0f)));
        float restY = index == 2 || index == 3 ? 2.0f : (index >= 4 ? 12.0f : 0.0f);
        poses.method_46416((part.field_3657 - restX) / 16.0f, (part.field_3656 - restY) / 16.0f, part.field_3655 / 16.0f);
        poses.method_46416(bone.x(), bone.y(), bone.z());
        if (part.field_3674 != 0.0f) {
            poses.method_22907((Quaternionfc)class_7833.field_40718.rotation(part.field_3674));
        }
        if (part.field_3675 != 0.0f) {
            poses.method_22907((Quaternionfc)class_7833.field_40716.rotation(part.field_3675));
        }
        if (part.field_3654 != 0.0f) {
            poses.method_22907((Quaternionfc)class_7833.field_40714.rotation(part.field_3654));
        }
        poses.method_46416(-bone.x(), -bone.y(), -bone.z());
    }

    private static void draw(SkinMesh.Surface surface, class_4587.class_4665 pose, class_4588 target, int light, int overlay, int tint) {
        float[] v = surface.vertices();
        for (int p = 0; p < v.length; p += 8) {
            target.method_56824(pose, v[p], v[p + 1], v[p + 2]).method_39415(tint).method_22913(v[p + 6], v[p + 7]).method_22922(overlay).method_60803(light).method_60831(pose, v[p + 3], v[p + 4], v[p + 5]);
        }
    }
}

