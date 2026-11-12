/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_270$class_272
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_327$class_6415
 *  net.minecraft.class_4184
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  net.minecraft.class_5348
 *  net.minecraft.class_742
 *  net.minecraft.class_8646
 *  net.minecraft.class_9013
 *  net.minecraft.class_9015
 *  net.minecraft.class_9022
 *  net.minecraft.class_9025
 *  net.minecraft.class_9064
 *  net.minecraft.class_9799
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  ru.bloodhelper.client.circles.BloodstoneCircles
 *  ru.bloodhelper.client.config.ConfigStore
 *  ru.bloodhelper.client.config.NameVisibilitySettings
 *  ru.bloodhelper.client.cosmetic.CosmeticNameText
 *  ru.bloodhelper.client.cosmetic.CosmeticNames
 *  ru.bloodhelper.client.cosmetic.NicknamePalette
 *  ru.bloodhelper.client.modules.BetterNameVisibilityModule
 *  ru.bloodhelper.client.modules.LunarNamePlacement
 *  ru.bloodhelper.client.modules.LunarNameTags$Frame
 *  ru.bloodhelper.client.modules.LunarNameTags$Label
 *  ru.bloodhelper.client.modules.LunarNameVisibility
 *  ru.bloodhelper.client.modules.StreamerMode
 *  ru.bloodhelper.client.tiers.TierDisplay
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4184;
import net.minecraft.class_4597;
import net.minecraft.class_5348;
import net.minecraft.class_742;
import net.minecraft.class_8646;
import net.minecraft.class_9013;
import net.minecraft.class_9015;
import net.minecraft.class_9022;
import net.minecraft.class_9025;
import net.minecraft.class_9064;
import net.minecraft.class_9799;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.circles.BloodstoneCircles;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.NameVisibilitySettings;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.BetterNameVisibilityModule;
import ru.bloodhelper.client.modules.LunarNamePlacement;
import ru.bloodhelper.client.modules.LunarNameTags;
import ru.bloodhelper.client.modules.LunarNameVisibility;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierDisplay;

public final class LunarNameTags {
    private static final Logger LOG = LoggerFactory.getLogger((String)"BloodHelper/LunarNames");
    private static Frame frame;
    private static class_9799 memory;
    private static class_4597.class_4598 buffers;
    private static boolean reportedDraw;
    private static int drawnLastFrame;
    private static String reportedStatus;

    private LunarNameTags() {
    }

    public static void register() {
        WorldRenderEvents.END_EXTRACTION.register(LunarNameTags::extract);
        WorldRenderEvents.END_MAIN.register(LunarNameTags::draw);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            String status;
            String string = !ConfigStore.get().betterNameVisibility.enabled ? "disabled" : (!BetterNameVisibilityModule.active() ? "blocked by module policy / Safe Mode" : (StreamerMode.hideNametags() ? "hidden by streamer mode" : (client.field_1687 == null ? "waiting for a world" : (status = client.field_1690.field_1842 ? "hidden by F1" : "enabled"))));
            if (!status.equals(reportedStatus)) {
                reportedStatus = status;
                LOG.info("Player-name rendering: {}", (Object)status);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LunarNameTags.beginFrame());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LunarNameTags.beginFrame();
            if (memory != null) {
                memory.close();
            }
            memory = null;
            buffers = null;
        });
        LOG.info("Independent player-name renderer registered (Minecraft 1.21.11, BloodHelper 1.2.0 Lunar)");
        if (FabricLoader.getInstance().isModLoaded("name_visibility")) {
            LOG.warn("Separate Better Name Visibility is installed; its settings are independent of BloodHelper. Use one name-visibility module.");
        }
    }

    public static void beginFrame() {
        frame = null;
        drawnLastFrame = 0;
    }

    private static boolean active() {
        class_310 mc = class_310.method_1551();
        return mc.field_1687 != null && mc.field_1724 != null && !mc.field_1690.field_1842 && !StreamerMode.hideNametags() && BetterNameVisibilityModule.active();
    }

    public static boolean ownsCurrentFrame() {
        return frame != null && LunarNameTags.active();
    }

    public static int drawnLastFrame() {
        return drawnLastFrame;
    }

    public static int extractedLastFrame() {
        return frame == null ? 0 : LunarNameTags.frame.labels.size();
    }

    private static void extract(WorldExtractionContext context) {
        if (!LunarNameTags.active() || !context.camera().method_19332()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        NameVisibilitySettings settings = BetterNameVisibilityModule.settings();
        class_4184 camera = context.camera();
        float partial = context.tickCounter().method_60637(true);
        ArrayList<Label> labels = new ArrayList<Label>();
        for (class_742 player : context.world().method_18456()) {
            class_243 anchor;
            class_243 position;
            double distance;
            if (!LunarNameTags.eligible(player, distance = (position = player.method_30950(partial)).method_1025(camera.method_71156()), settings.unlimitedDistance) || (anchor = player.method_56072().method_55675(class_9064.field_47745, 0, player.method_61415(partial))) == null) continue;
            class_243 relative = position.method_1031(anchor.field_1352, LunarNamePlacement.anchorHeight((double)anchor.field_1351, (double)player.method_17682()), anchor.field_1350).method_1020(camera.method_71156());
            float scale = 0.025f * NameVisibilitySettings.scale((double)Math.sqrt(distance), (double)settings.distantScale);
            Quaternionf quaternionf = camera.method_23767();
            Objects.requireNonNull(mc.field_1772);
            Matrix4f pose = LunarNamePlacement.pose((class_243)relative, (Quaternionfc)quaternionf, (float)scale, (int)9);
            if (player.method_7334().name().equals("deadmau5")) {
                pose.translate(0.0f, -10.0f, 0.0f);
            }
            class_2561 name = TierDisplay.decorate((String)player.method_7334().name(), (class_2561)player.method_5476().method_27661(), (boolean)false);
            class_2561 score = LunarNameTags.score(player, distance);
            if (settings.disableTeamColors) {
                name = BetterNameVisibilityModule.withoutTeamColors((class_2561)name);
                if (score != null) {
                    score = BetterNameVisibilityModule.withoutTeamColors((class_2561)score);
                }
            }
            name = BloodstoneCircles.rewrite((class_2561)name, (boolean)false);
            Optional palette = CosmeticNames.palette((UUID)player.method_5667(), (String)player.method_7334().name(), (StreamerMode.hideNickname() || StreamerMode.hideNametags() ? 1 : 0) != 0);
            if (palette.isPresent()) {
                name = CosmeticNameText.color((class_2561)name, (String)player.method_7334().name(), (NicknamePalette)((NicknamePalette)palette.get()), (long)System.currentTimeMillis());
            }
            int light = settings.fullbright ? 0xF000F0 : mc.method_1561().method_74405(player).method_24088((class_1297)player, partial);
            labels.add(new Label(name, score, pose, player.method_21751(), light, settings.foreground(), settings.background(), distance));
        }
        labels.sort(Comparator.comparingDouble(Label::distanceSquared).reversed());
        frame = new Frame(context.worldState(), List.copyOf(labels));
    }

    private static boolean eligible(class_742 player, double distance, boolean extended) {
        class_310 mc = class_310.method_1551();
        if (player == mc.field_1724 || player == mc.method_1560() || player.method_31481() || !player.method_5805() || player.method_7325() || player.method_5782() || !LunarNameVisibility.inRange((double)distance, (boolean)player.method_21751(), (boolean)extended)) {
            return false;
        }
        if (!(player.method_5733() || player.method_16914() && mc.field_1692 == player)) {
            return false;
        }
        class_268 team = player.method_5781();
        class_268 viewerTeam = mc.field_1724.method_5781();
        return LunarNameVisibility.teamAllows((class_270.class_272)(team == null ? class_270.class_272.field_1442 : team.method_1201()), (viewerTeam != null ? 1 : 0) != 0, (team != null && team.method_1206((class_270)viewerTeam) ? 1 : 0) != 0, (!player.method_5756((class_1657)mc.field_1724) ? 1 : 0) != 0, (team != null && team.method_1199() ? 1 : 0) != 0);
    }

    private static class_2561 score(class_742 player, double distance) {
        if (distance >= 100.0) {
            return null;
        }
        class_269 board = player.method_73183().method_8428();
        class_266 objective = board.method_1189(class_8646.field_45158);
        if (objective == null) {
            return null;
        }
        return class_9013.method_55398((class_9013)board.method_55430((class_9015)player, objective), (class_9022)objective.method_55380((class_9022)class_9025.field_47566)).method_27693(" ").method_10852(objective.method_1114());
    }

    private static void draw(WorldRenderContext context) {
        Frame current = frame;
        if (current == null || current.owner != context.worldState() || !LunarNameTags.active() || current.labels.isEmpty()) {
            return;
        }
        if (buffers == null) {
            memory = new class_9799(65536);
            buffers = class_4597.method_22991((class_9799)memory);
        }
        class_327 font = class_310.method_1551().field_1772;
        for (Label label : current.labels) {
            Matrix4f pose = new Matrix4f((Matrix4fc)label.pose);
            if (label.score != null) {
                LunarNameTags.line(font, label.score, pose, label);
                Objects.requireNonNull(font);
                pose.translate(0.0f, (float)(-9) * 1.15f, 0.0f);
            }
            LunarNameTags.line(font, label.name, pose, label);
        }
        buffers.method_22993();
        drawnLastFrame = current.labels.size();
        if (!reportedDraw) {
            reportedDraw = true;
            LOG.info("Independent player names drawn: {}. Color, background, light, scale and distance use BloodHelper settings.", (Object)drawnLastFrame);
        }
    }

    private static void line(class_327 font, class_2561 text, Matrix4f pose, Label label) {
        font.method_27522(text, (float)(-font.method_27525((class_5348)text)) / 2.0f, 0.0f, label.color, false, pose, (class_4597)buffers, label.discrete ? class_327.class_6415.field_33993 : class_327.class_6415.field_33994, label.background, label.light);
    }

    static {
        reportedStatus = "";
    }
}

