/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
 *  net.minecraft.class_10055
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10444
 *  net.minecraft.class_1060
 *  net.minecraft.class_11659
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_11890
 *  net.minecraft.class_12075
 *  net.minecraft.class_12249
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327$class_6415
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_5348
 *  net.minecraft.class_742
 *  net.minecraft.class_811
 *  org.joml.Quaternionfc
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.class_10055;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10444;
import net.minecraft.class_1060;
import net.minecraft.class_11659;
import net.minecraft.class_11719;
import net.minecraft.class_11890;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3959;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5348;
import net.minecraft.class_742;
import net.minecraft.class_811;
import org.joml.Quaternionfc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.modules.OpponentCooldownTracker;

public final class OpponentCooldownRenderer {
    public static final RenderStateDataKey<Label> LABEL = RenderStateDataKey.create(() -> "bloodhelper:opponent_cooldowns");
    private static final class_11719 FACE = new class_11719.class_11721(class_2960.method_60655((String)"bloodhelper", (String)"ui"));
    private static final class_2960 ICON_ATLAS = class_2960.method_60655((String)"bloodhelper", (String)"dynamic/ability_pictograms");
    private static final int ICON_CELL = 64;
    private static final float ROW_HEIGHT = 14.0f;
    private static final float WEAPON_SIZE = 12.0f;
    private static final float ABILITY_SIZE = 10.0f;
    private static class_1060 atlasOwner;
    private static long submittedLabels;

    private OpponentCooldownRenderer() {
    }

    public static long submittedLabelCount() {
        return submittedLabels;
    }

    public static void extract(class_11890 avatar, class_10055 state, float partialTick) {
        double range;
        class_742 player;
        class_10055 extra = state;
        extra.setData(LABEL, null);
        class_310 client = class_310.method_1551();
        ClientConfig config = BloodHelperClient.config();
        ClientConfig.OpponentCooldownSettings settings = config.opponentCooldowns;
        if (!settings.enabled || !OpponentCooldownTracker.available(client) || client.field_1690.field_1842 || !(avatar instanceof class_742) || (player = (class_742)avatar) == client.field_1724 || player.method_31481() || !player.method_5805() || player.method_7325() || player.method_5767() || player.method_5756((class_1657)client.field_1724) || state.field_53337 == null || state.field_53338 == null) {
            return;
        }
        double d = range = Double.isFinite(settings.maxDistance) ? Math.clamp(settings.maxDistance, 4.0, 64.0) : 32.0;
        if (!Double.isFinite(state.field_53332) || state.field_53332 > range * range) {
            return;
        }
        List<OpponentCooldownTracker.Entry> entries = OpponentCooldownTracker.snapshot(player.method_5667(), player.method_5628());
        if (entries.isEmpty()) {
            return;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (!camera.method_19332()) {
            return;
        }
        class_243 eye = player.method_5836(partialTick);
        class_243 chest = player.method_30950(partialTick).method_1031(0.0, (double)player.method_17682() * 0.55, 0.0);
        if (!OpponentCooldownRenderer.unobstructed(client, camera.method_71156(), eye) && !OpponentCooldownRenderer.unobstructed(client, camera.method_71156(), chest)) {
            return;
        }
        int maximum = Math.clamp((long)settings.maxVisible, 1, 8);
        ArrayList<Row> rows = new ArrayList<Row>(maximum);
        int active = 0;
        float width = 0.0f;
        for (OpponentCooldownTracker.Entry entry : entries) {
            if (entry.seconds() <= 0 || entry.item().method_7960()) continue;
            ++active;
            if (rows.size() >= maximum) continue;
            class_10444 item = new class_10444();
            client.method_65386().method_65597(item, entry.item(), class_811.field_4317, (class_1309)player);
            class_2561 text = OpponentCooldownRenderer.text(entry.seconds() + ("ru".equals(config.language) ? "\u0441" : "s"));
            int textWidth = client.field_1772.method_27525((class_5348)text);
            rows.add(new Row(item, entry.icon() == null ? OpponentCooldownTracker.AbilityIcon.ABILITY : entry.icon(), text, textWidth));
            width = Math.max(width, 18.0f + (settings.abilityIcons ? 12.0f : 0.0f) + (float)textWidth + 4.0f);
        }
        if (rows.isEmpty()) {
            return;
        }
        class_2561 overflow = active > rows.size() ? OpponentCooldownRenderer.text("+" + (active - rows.size())) : null;
        int overflowWidth = overflow == null ? 0 : client.field_1772.method_27525((class_5348)overflow);
        width = Math.max(width, (float)(overflowWidth + 8));
        float scale = (float)(Double.isFinite(settings.scale) ? Math.clamp(settings.scale, 0.5, 2.0) : 1.0);
        extra.setData(LABEL, (Object)new Label(List.copyOf(rows), overflow, overflowWidth, width, scale, settings.background, settings.abilityIcons, config.textShadow && RenderQuality.shadows(), ThemeColors.text(-1), ThemeColors.icon(-1), ThemeColors.surface(-1205722072)));
    }

    private static class_2561 text(String text) {
        return class_2561.method_43470((String)text).method_27694(style -> style.method_27704(FACE));
    }

    private static boolean unobstructed(class_310 client, class_243 origin, class_243 target) {
        return client.field_1687.method_17742(new class_3959(origin, target, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, client.method_1560())).method_17783() == class_239.class_240.field_1333;
    }

    public static void submit(class_10055 state, class_4587 pose, class_11659 collector, class_12075 camera) {
        if (!BloodHelperClient.config().opponentCooldownsEnabled() || !OpponentCooldownTracker.available(class_310.method_1551())) {
            state.setData(LABEL, null);
            return;
        }
        Label label = (Label)state.getData(LABEL);
        if (label == null || state.field_53337 == null || state.field_53338 == null || state.field_53333 || state.field_53542 || !camera.field_63079 || class_310.method_1551().field_1690.field_1842) {
            return;
        }
        ++submittedLabels;
        if (label.abilityIcons) {
            OpponentCooldownRenderer.ensureAtlas();
        }
        class_243 attachment = state.field_53338;
        float height = (float)label.rows.size() * 14.0f + (float)(label.overflow != null ? 11 : 0);
        pose.method_22903();
        double scoreboard = state.field_53525 == null ? 0.0 : 0.25875;
        pose.method_22904(attachment.field_1352, attachment.field_1351 + 0.5 + scoreboard, attachment.field_1350);
        pose.method_22907((Quaternionfc)camera.field_63081);
        pose.method_22905(0.025f * label.scale, -0.025f * label.scale, 0.025f * label.scale);
        float bottom = (float)(state.field_62758 ? -10 : 0) / label.scale - 3.0f / label.scale;
        float top = bottom - height;
        float left = -label.width / 2.0f;
        if (label.background) {
            OpponentCooldownRenderer.background(collector, pose, left, top, label.width, height, label.backgroundColor);
        }
        for (int index = 0; index < label.rows.size(); ++index) {
            Row row = label.rows.get(index);
            float y = top + (float)index * 14.0f;
            float x = left + 3.0f;
            pose.method_22903();
            pose.method_46416(x + 6.0f, y + 7.0f, 0.02f);
            pose.method_22905(12.0f, -12.0f, 12.0f);
            row.item.method_65604(pose, collector, state.field_61820, class_4608.field_21444, 0);
            pose.method_22909();
            x += 15.0f;
            if (label.abilityIcons) {
                OpponentCooldownRenderer.pictogram(collector, pose, row.icon, x, y + 2.0f, 10.0f, label.iconColor, state.field_61820);
                x += 12.0f;
            }
            collector.method_73478(pose, x, y + 3.0f, row.text.method_30937(), label.shadow, class_327.class_6415.field_33993, state.field_61820, label.foreground, 0, 0);
        }
        if (label.overflow != null) {
            collector.method_73478(pose, (float)(-label.overflowWidth) / 2.0f, top + (float)label.rows.size() * 14.0f + 1.0f, label.overflow.method_30937(), label.shadow, class_327.class_6415.field_33993, state.field_61820, label.foreground, 0, 0);
        }
        pose.method_22909();
    }

    private static void background(class_11659 collector, class_4587 pose, float x, float y, float width, float height, int color) {
        collector.method_73483(pose, class_12249.method_75999(), (matrix, vertices) -> {
            float r = 2.0f;
            OpponentCooldownRenderer.quad(vertices, matrix, x + r, y, x + width - r, y + height, color);
            OpponentCooldownRenderer.quad(vertices, matrix, x, y + r, x + r, y + height - r, color);
            OpponentCooldownRenderer.quad(vertices, matrix, x + width - r, y + r, x + width, y + height - r, color);
            for (int corner = 0; corner < 4; ++corner) {
                float cx = corner == 0 || corner == 3 ? x + r : x + width - r;
                float cy = corner < 2 ? y + r : y + height - r;
                double start = Math.PI + (double)corner * Math.PI / 2.0;
                for (int segment = 0; segment < 8; ++segment) {
                    double a = start + (double)segment * Math.PI / 16.0;
                    double b = a + 0.19634954084936207;
                    OpponentCooldownRenderer.vertex(vertices, matrix, cx, cy, color);
                    OpponentCooldownRenderer.vertex(vertices, matrix, cx + (float)Math.cos(b) * r, cy + (float)Math.sin(b) * r, color);
                    OpponentCooldownRenderer.vertex(vertices, matrix, cx + (float)Math.cos(a) * r, cy + (float)Math.sin(a) * r, color);
                    OpponentCooldownRenderer.vertex(vertices, matrix, cx, cy, color);
                }
            }
        });
    }

    private static void quad(class_4588 out, class_4587.class_4665 pose, float x1, float y1, float x2, float y2, int color) {
        OpponentCooldownRenderer.vertex(out, pose, x1, y1, color);
        OpponentCooldownRenderer.vertex(out, pose, x1, y2, color);
        OpponentCooldownRenderer.vertex(out, pose, x2, y2, color);
        OpponentCooldownRenderer.vertex(out, pose, x2, y1, color);
    }

    private static void vertex(class_4588 out, class_4587.class_4665 pose, float x, float y, int color) {
        out.method_56824(pose, x, y, -0.04f).method_39415(color).method_60803(0xF000F0);
    }

    private static void pictogram(class_11659 collector, class_4587 pose, OpponentCooldownTracker.AbilityIcon icon, float x, float y, float size, int color, int light) {
        float u1 = (float)icon.ordinal() / (float)OpponentCooldownTracker.AbilityIcon.values().length;
        float u2 = (float)(icon.ordinal() + 1) / (float)OpponentCooldownTracker.AbilityIcon.values().length;
        collector.method_73483(pose, class_12249.method_76022((class_2960)ICON_ATLAS), (matrix, vertices) -> {
            vertices.method_56824(matrix, x, y, 0.01f).method_39415(color).method_22913(u1, 0.0f).method_60803(light);
            vertices.method_56824(matrix, x, y + size, 0.01f).method_39415(color).method_22913(u1, 1.0f).method_60803(light);
            vertices.method_56824(matrix, x + size, y + size, 0.01f).method_39415(color).method_22913(u2, 1.0f).method_60803(light);
            vertices.method_56824(matrix, x + size, y, 0.01f).method_39415(color).method_22913(u2, 0.0f).method_60803(light);
        });
    }

    private static void ensureAtlas() {
        class_1060 manager = class_310.method_1551().method_1531();
        if (atlasOwner == manager) {
            return;
        }
        OpponentCooldownTracker.AbilityIcon[] icons = OpponentCooldownTracker.AbilityIcon.values();
        class_1011 pixels = new class_1011(icons.length * 64, 64, false);
        for (OpponentCooldownTracker.AbilityIcon icon : icons) {
            List<Stroke> path = OpponentCooldownRenderer.path(icon);
            for (int y = 0; y < 64; ++y) {
                for (int x = 0; x < 64; ++x) {
                    double px = ((double)x + 0.5) * 24.0 / 64.0;
                    double py = ((double)y + 0.5) * 24.0 / 64.0;
                    double distance = Double.POSITIVE_INFINITY;
                    for (Stroke stroke : path) {
                        distance = Math.min(distance, stroke.distance(px, py));
                    }
                    int alpha = (int)Math.round(255.0 * Math.clamp((1.2375 - distance) / 0.375, 0.0, 1.0));
                    pixels.method_61941(icon.ordinal() * 64 + x, y, alpha << 24 | 0xFFFFFF);
                }
            }
        }
        manager.method_4616(ICON_ATLAS, (class_1044)new SmoothIconTexture(pixels));
        atlasOwner = manager;
    }

    private static List<Stroke> path(OpponentCooldownTracker.AbilityIcon icon) {
        ArrayList<Stroke> strokes = new ArrayList<Stroke>();
        switch (icon) {
            case BOOST: {
                OpponentCooldownRenderer.line(strokes, 12.0, 20.0, 12.0, 4.0);
                OpponentCooldownRenderer.line(strokes, 5.0, 11.0, 12.0, 4.0);
                OpponentCooldownRenderer.line(strokes, 12.0, 4.0, 19.0, 11.0);
                OpponentCooldownRenderer.line(strokes, 5.0, 20.0, 8.0, 20.0);
                OpponentCooldownRenderer.line(strokes, 16.0, 20.0, 19.0, 20.0);
                break;
            }
            case TELEPORT: {
                OpponentCooldownRenderer.ellipse(strokes, 8.0, 12.0, 4.0, 8.0, 0.0, Math.PI * 2);
                OpponentCooldownRenderer.line(strokes, 10.0, 12.0, 21.0, 12.0);
                OpponentCooldownRenderer.line(strokes, 17.0, 8.0, 21.0, 12.0);
                OpponentCooldownRenderer.line(strokes, 21.0, 12.0, 17.0, 16.0);
                break;
            }
            case SHADOW: {
                OpponentCooldownRenderer.ellipse(strokes, 12.0, 12.0, 8.0, 8.0, 1.4765485471872026, 5.969026041820607);
                OpponentCooldownRenderer.ellipse(strokes, 17.0, 8.0, 8.0, 8.0, 1.6022122533307945, 3.330088212805181);
                OpponentCooldownRenderer.line(strokes, 18.0, 2.0, 18.0, 6.0);
                OpponentCooldownRenderer.line(strokes, 16.0, 4.0, 20.0, 4.0);
                break;
            }
            case SHIELD: {
                OpponentCooldownRenderer.line(strokes, 4.0, 5.0, 12.0, 2.0);
                OpponentCooldownRenderer.line(strokes, 12.0, 2.0, 20.0, 5.0);
                OpponentCooldownRenderer.line(strokes, 20.0, 5.0, 19.0, 13.0);
                OpponentCooldownRenderer.line(strokes, 19.0, 13.0, 16.0, 18.0);
                OpponentCooldownRenderer.line(strokes, 16.0, 18.0, 12.0, 21.0);
                OpponentCooldownRenderer.line(strokes, 12.0, 21.0, 8.0, 18.0);
                OpponentCooldownRenderer.line(strokes, 8.0, 18.0, 5.0, 13.0);
                OpponentCooldownRenderer.line(strokes, 5.0, 13.0, 4.0, 5.0);
                break;
            }
            case SHRINK: {
                OpponentCooldownRenderer.line(strokes, 3.0, 3.0, 9.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 9.0, 4.0, 9.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 4.0, 9.0, 9.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 21.0, 21.0, 15.0, 15.0);
                OpponentCooldownRenderer.line(strokes, 15.0, 20.0, 15.0, 15.0);
                OpponentCooldownRenderer.line(strokes, 20.0, 15.0, 15.0, 15.0);
                OpponentCooldownRenderer.line(strokes, 21.0, 3.0, 15.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 15.0, 4.0, 15.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 20.0, 9.0, 15.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 3.0, 21.0, 9.0, 15.0);
                OpponentCooldownRenderer.line(strokes, 9.0, 20.0, 9.0, 15.0);
                OpponentCooldownRenderer.line(strokes, 4.0, 15.0, 9.0, 15.0);
                break;
            }
            case REAP: {
                OpponentCooldownRenderer.line(strokes, 7.0, 21.0, 14.0, 3.0);
                OpponentCooldownRenderer.line(strokes, 8.0, 6.0, 14.0, 3.0);
                OpponentCooldownRenderer.line(strokes, 14.0, 3.0, 19.0, 6.0);
                OpponentCooldownRenderer.line(strokes, 19.0, 6.0, 21.0, 12.0);
                OpponentCooldownRenderer.line(strokes, 21.0, 12.0, 17.0, 9.0);
                OpponentCooldownRenderer.line(strokes, 17.0, 9.0, 11.0, 8.0);
                break;
            }
            case WAVE: {
                for (int row = 0; row < 3; ++row) {
                    for (int i = 0; i < 24; ++i) {
                        double x1 = 3.0 + (double)i * 0.75;
                        double x2 = x1 + 0.75;
                        OpponentCooldownRenderer.line(strokes, x1, (double)(6 + row * 6) + Math.sin((x1 - 3.0) / 18.0 * Math.PI * 2.0) * 1.8, x2, (double)(6 + row * 6) + Math.sin((x2 - 3.0) / 18.0 * Math.PI * 2.0) * 1.8);
                    }
                }
                break;
            }
            case ABILITY: {
                OpponentCooldownRenderer.line(strokes, 12.0, 3.0, 20.0, 12.0);
                OpponentCooldownRenderer.line(strokes, 20.0, 12.0, 12.0, 21.0);
                OpponentCooldownRenderer.line(strokes, 12.0, 21.0, 4.0, 12.0);
                OpponentCooldownRenderer.line(strokes, 4.0, 12.0, 12.0, 3.0);
                OpponentCooldownRenderer.line(strokes, 12.0, 10.0, 12.0, 14.0);
            }
        }
        return strokes;
    }

    private static void line(List<Stroke> out, double x1, double y1, double x2, double y2) {
        out.add(new Stroke(x1, y1, x2, y2));
    }

    private static void ellipse(List<Stroke> out, double x, double y, double rx, double ry, double start, double end) {
        for (int i = 0; i < 48; ++i) {
            double a = start + (end - start) * (double)i / 48.0;
            double b = start + (end - start) * (double)(i + 1) / 48.0;
            OpponentCooldownRenderer.line(out, x + Math.cos(a) * rx, y + Math.sin(a) * ry, x + Math.cos(b) * rx, y + Math.sin(b) * ry);
        }
    }

    public record Row(class_10444 item, OpponentCooldownTracker.AbilityIcon icon, class_2561 text, int textWidth) {
    }

    public record Label(List<Row> rows, class_2561 overflow, int overflowWidth, float width, float scale, boolean background, boolean abilityIcons, boolean shadow, int foreground, int iconColor, int backgroundColor) {
    }

    private record Stroke(double x1, double y1, double x2, double y2) {
        double distance(double x, double y) {
            double dx = this.x2 - this.x1;
            double dy = this.y2 - this.y1;
            double t = Math.clamp(((x - this.x1) * dx + (y - this.y1) * dy) / Math.max(1.0E-5, dx * dx + dy * dy), 0.0, 1.0);
            return Math.hypot(x - this.x1 - t * dx, y - this.y1 - t * dy);
        }
    }

    private static final class SmoothIconTexture
    extends class_1043 {
        SmoothIconTexture(class_1011 pixels) {
            super(() -> "BLOODHELPER ability pictograms", pixels);
            this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        }
    }
}

