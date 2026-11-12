/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.renderpearl.api.textures.FilterMode
 *  net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.modules.OpponentCooldownTracker;

public final class OpponentCooldownRenderer {
    public static final RenderStateDataKey<Label> LABEL = RenderStateDataKey.create(() -> "bloodhelper:opponent_cooldowns");
    private static final FontDescription FACE = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"ui"));
    private static final Identifier ICON_ATLAS = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"dynamic/ability_pictograms");
    private static final int ICON_CELL = 64;
    private static final float ROW_HEIGHT = 14.0f;
    private static final float WEAPON_SIZE = 12.0f;
    private static final float ABILITY_SIZE = 10.0f;
    private static TextureManager atlasOwner;
    private static long submittedLabels;

    private OpponentCooldownRenderer() {
    }

    public static long submittedLabelCount() {
        return submittedLabels;
    }

    public static void extract(Avatar avatar, AvatarRenderState state, float partialTick) {
        double range;
        AbstractClientPlayer player;
        AvatarRenderState extra = state;
        extra.setData(LABEL, null);
        Minecraft client = Minecraft.getInstance();
        ClientConfig config = BloodHelperClient.config();
        ClientConfig.OpponentCooldownSettings settings = config.opponentCooldowns;
        if (!settings.enabled || !OpponentCooldownTracker.available(client) || client.gui.hud.isHidden() || !(avatar instanceof AbstractClientPlayer) || (player = (AbstractClientPlayer)avatar) == client.player || player.isRemoved() || !player.isAlive() || player.isSpectator() || player.isInvisible() || player.isInvisibleTo((Player)client.player) || state.nameTag == null || state.nameTagAttachment == null) {
            return;
        }
        double d = range = Double.isFinite(settings.maxDistance) ? Math.clamp(settings.maxDistance, 4.0, 64.0) : 32.0;
        if (!Double.isFinite(state.distanceToCameraSq) || state.distanceToCameraSq > range * range) {
            return;
        }
        List<OpponentCooldownTracker.Entry> entries = OpponentCooldownTracker.snapshot(player.getUUID(), player.getId());
        if (entries.isEmpty()) {
            return;
        }
        Camera camera = client.gameRenderer.mainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 chest = player.getPosition(partialTick).add(0.0, (double)player.getBbHeight() * 0.55, 0.0);
        if (!OpponentCooldownRenderer.unobstructed(client, camera.position(), eye) && !OpponentCooldownRenderer.unobstructed(client, camera.position(), chest)) {
            return;
        }
        int maximum = Math.clamp((long)settings.maxVisible, 1, 8);
        ArrayList<Row> rows = new ArrayList<Row>(maximum);
        int active = 0;
        float width = 0.0f;
        for (OpponentCooldownTracker.Entry entry : entries) {
            if (entry.seconds() <= 0 || entry.item().isEmpty()) continue;
            ++active;
            if (rows.size() >= maximum) continue;
            ItemStackRenderState item = new ItemStackRenderState();
            client.getItemModelResolver().updateForLiving(item, entry.item(), ItemDisplayContext.GUI, (LivingEntity)player);
            Component text = OpponentCooldownRenderer.text(entry.seconds() + ("ru".equals(config.language) ? "\u0441" : "s"));
            int textWidth = client.font.width((FormattedText)text);
            rows.add(new Row(item, entry.icon() == null ? OpponentCooldownTracker.AbilityIcon.ABILITY : entry.icon(), text, textWidth));
            width = Math.max(width, 18.0f + (settings.abilityIcons ? 12.0f : 0.0f) + (float)textWidth + 4.0f);
        }
        if (rows.isEmpty()) {
            return;
        }
        Component overflow = active > rows.size() ? OpponentCooldownRenderer.text("+" + (active - rows.size())) : null;
        int overflowWidth = overflow == null ? 0 : client.font.width((FormattedText)overflow);
        width = Math.max(width, (float)(overflowWidth + 8));
        float scale = (float)(Double.isFinite(settings.scale) ? Math.clamp(settings.scale, 0.5, 2.0) : 1.0);
        extra.setData(LABEL, (Object)new Label(List.copyOf(rows), overflow, overflowWidth, width, scale, settings.background, settings.abilityIcons, config.textShadow && RenderQuality.shadows(), ThemeColors.text(-1), ThemeColors.icon(-1), ThemeColors.surface(-1205722072)));
    }

    private static Component text(String text) {
        return Component.literal((String)text).withStyle(style -> style.withFont(FACE));
    }

    private static boolean unobstructed(Minecraft client, Vec3 origin, Vec3 target) {
        return client.level.clip(new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, client.getCameraEntity())).getType() == HitResult.Type.MISS;
    }

    public static void submit(AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!BloodHelperClient.config().opponentCooldownsEnabled() || !OpponentCooldownTracker.available(Minecraft.getInstance())) {
            state.setData(LABEL, null);
            return;
        }
        Label label = (Label)state.getData(LABEL);
        if (label == null || state.nameTag == null || state.nameTagAttachment == null || state.isInvisible || state.isSpectator || !camera.initialized || Minecraft.getInstance().gui.hud.isHidden()) {
            return;
        }
        ++submittedLabels;
        if (label.abilityIcons) {
            OpponentCooldownRenderer.ensureAtlas();
        }
        Vec3 attachment = state.nameTagAttachment;
        float height = (float)label.rows.size() * 14.0f + (float)(label.overflow != null ? 11 : 0);
        pose.pushPose();
        double scoreboard = state.scoreText == null ? 0.0 : 0.25875;
        pose.translate(attachment.x, attachment.y + 0.5 + scoreboard, attachment.z);
        pose.rotate((Quaternionfc)camera.orientation);
        pose.scale(0.025f * label.scale, -0.025f * label.scale, 0.025f * label.scale);
        float bottom = (float)(state.showExtraEars ? -10 : 0) / label.scale - 3.0f / label.scale;
        float top = bottom - height;
        float left = -label.width / 2.0f;
        if (label.background) {
            OpponentCooldownRenderer.background(collector, pose, left, top, label.width, height, label.backgroundColor);
        }
        for (int index = 0; index < label.rows.size(); ++index) {
            Row row = label.rows.get(index);
            float y = top + (float)index * 14.0f;
            float x = left + 3.0f;
            pose.pushPose();
            pose.translate(x + 6.0f, y + 7.0f, 0.02f);
            pose.scale(12.0f, -12.0f, 12.0f);
            row.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
            x += 15.0f;
            if (label.abilityIcons) {
                OpponentCooldownRenderer.pictogram(collector, pose, row.icon, x, y + 2.0f, 10.0f, label.iconColor, state.lightCoords);
                x += 12.0f;
            }
            collector.submitText(pose, x, y + 3.0f, row.text.getVisualOrderText(), label.shadow, Font.DisplayMode.NORMAL, state.lightCoords, label.foreground, 0, 0);
        }
        if (label.overflow != null) {
            collector.submitText(pose, (float)(-label.overflowWidth) / 2.0f, top + (float)label.rows.size() * 14.0f + 1.0f, label.overflow.getVisualOrderText(), label.shadow, Font.DisplayMode.NORMAL, state.lightCoords, label.foreground, 0, 0);
        }
        pose.popPose();
    }

    private static void background(SubmitNodeCollector collector, PoseStack pose, float x, float y, float width, float height, int color) {
        collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (matrix, vertices) -> {
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

    private static void quad(VertexConsumer out, PoseStack.Pose pose, float x1, float y1, float x2, float y2, int color) {
        OpponentCooldownRenderer.vertex(out, pose, x1, y1, color);
        OpponentCooldownRenderer.vertex(out, pose, x1, y2, color);
        OpponentCooldownRenderer.vertex(out, pose, x2, y2, color);
        OpponentCooldownRenderer.vertex(out, pose, x2, y1, color);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, int color) {
        out.addVertex(pose, x, y, -0.04f).setColor(color).setLight(0xF000F0);
    }

    private static void pictogram(SubmitNodeCollector collector, PoseStack pose, OpponentCooldownTracker.AbilityIcon icon, float x, float y, float size, int color, int light) {
        float u1 = (float)icon.ordinal() / (float)OpponentCooldownTracker.AbilityIcon.values().length;
        float u2 = (float)(icon.ordinal() + 1) / (float)OpponentCooldownTracker.AbilityIcon.values().length;
        collector.submitCustomGeometry(pose, RenderTypes.text((Identifier)ICON_ATLAS), (matrix, vertices) -> {
            vertices.addVertex(matrix, x, y, 0.01f).setColor(color).setUv(u1, 0.0f).setLight(light);
            vertices.addVertex(matrix, x, y + size, 0.01f).setColor(color).setUv(u1, 1.0f).setLight(light);
            vertices.addVertex(matrix, x + size, y + size, 0.01f).setColor(color).setUv(u2, 1.0f).setLight(light);
            vertices.addVertex(matrix, x + size, y, 0.01f).setColor(color).setUv(u2, 0.0f).setLight(light);
        });
    }

    private static void ensureAtlas() {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        if (atlasOwner == manager) {
            return;
        }
        OpponentCooldownTracker.AbilityIcon[] icons = OpponentCooldownTracker.AbilityIcon.values();
        NativeImage pixels = new NativeImage(icons.length * 64, 64, false);
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
                    pixels.setPixel(icon.ordinal() * 64 + x, y, alpha << 24 | 0xFFFFFF);
                }
            }
        }
        manager.register(ICON_ATLAS, (AbstractTexture)new SmoothIconTexture(pixels));
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

    public record Row(ItemStackRenderState item, OpponentCooldownTracker.AbilityIcon icon, Component text, int textWidth) {
    }

    public record Label(List<Row> rows, Component overflow, int overflowWidth, float width, float scale, boolean background, boolean abilityIcons, boolean shadow, int foreground, int iconColor, int backgroundColor) {
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
    extends DynamicTexture {
        SmoothIconTexture(NativeImage pixels) {
            super(() -> "BLOODHELPER ability pictograms", pixels);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
    }
}

