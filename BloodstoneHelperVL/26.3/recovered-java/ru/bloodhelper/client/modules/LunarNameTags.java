/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.state.level.LevelRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.numbers.NumberFormat
 *  net.minecraft.network.chat.numbers.StyledFormat
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityAttachment
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.scores.DisplaySlot
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.ReadOnlyScoreInfo
 *  net.minecraft.world.scores.ScoreHolder
 *  net.minecraft.world.scores.Scoreboard
 *  net.minecraft.world.scores.Team
 *  net.minecraft.world.scores.Team$Visibility
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
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
import ru.bloodhelper.client.modules.LunarNameVisibility;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierDisplay;

public final class LunarNameTags {
    private static final Logger LOG = LoggerFactory.getLogger((String)"BloodHelper/LunarNames");
    private static Frame frame;
    private static boolean reportedDraw;
    private static int drawnLastFrame;
    private static String reportedStatus;

    private LunarNameTags() {
    }

    public static void register() {
    }

    public static void beginFrame() {
        frame = null;
        drawnLastFrame = 0;
    }

    private static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return false;
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

    private static void extract(LevelExtractionContext context) {
        if (!LunarNameTags.active() || !context.camera().isInitialized()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        NameVisibilitySettings settings = BetterNameVisibilityModule.settings();
        Camera camera = context.camera();
        float partial = context.deltaTracker().getGameTimeDeltaPartialTick(true);
        ArrayList<Label> labels = new ArrayList<Label>();
        for (AbstractClientPlayer player : context.level().players()) {
            Vec3 anchor;
            Vec3 position;
            double distance;
            if (!LunarNameTags.eligible(player, distance = (position = player.getPosition(partial)).distanceToSqr(camera.position()), settings.unlimitedDistance) || (anchor = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getYRot(partial))) == null) continue;
            Vec3 relative = position.add(anchor.x, LunarNamePlacement.anchorHeight(anchor.y, player.getBbHeight()), anchor.z).subtract(camera.position());
            float scale = 0.025f * NameVisibilitySettings.scale(Math.sqrt(distance), settings.distantScale);
            Quaternionf quaternionf = camera.rotation();
            Objects.requireNonNull(mc.font);
            Matrix4f pose = LunarNamePlacement.pose(relative, (Quaternionfc)quaternionf, scale, 9);
            if (player.getGameProfile().name().equals("deadmau5")) {
                pose.translate(0.0f, -10.0f, 0.0f);
            }
            Component name = TierDisplay.decorate(player.getGameProfile().name(), (Component)player.getDisplayName().copy(), false);
            Component score = LunarNameTags.score(player, distance);
            if (settings.disableTeamColors) {
                name = BetterNameVisibilityModule.withoutTeamColors(name);
                if (score != null) {
                    score = BetterNameVisibilityModule.withoutTeamColors(score);
                }
            }
            name = BloodstoneCircles.rewrite(name, false);
            Optional<NicknamePalette> palette = CosmeticNames.palette(player.getUUID(), player.getGameProfile().name(), StreamerMode.hideNickname() || StreamerMode.hideNametags());
            if (palette.isPresent()) {
                name = CosmeticNameText.color(name, player.getGameProfile().name(), palette.get(), System.currentTimeMillis());
            }
            int light = settings.fullbright ? 0xF000F0 : mc.getEntityRenderDispatcher().getPackedLightCoords((Entity)player, partial);
            labels.add(new Label(name, score, pose, player.isDiscrete(), light, settings.foreground(), settings.background(), distance));
        }
        labels.sort(Comparator.comparingDouble(Label::distanceSquared).reversed());
        frame = new Frame(context.levelState(), List.copyOf(labels));
    }

    private static boolean eligible(AbstractClientPlayer player, double distance, boolean extended) {
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player || player == mc.getCameraEntity() || player.isRemoved() || !player.isAlive() || player.isSpectator() || player.isVehicle() || !LunarNameVisibility.inRange(distance, player.isDiscrete(), extended)) {
            return false;
        }
        if (!(player.shouldShowName() || player.hasCustomName() && mc.crosshairPickEntity == player)) {
            return false;
        }
        PlayerTeam team = player.getTeam();
        PlayerTeam viewerTeam = mc.player.getTeam();
        return LunarNameVisibility.teamAllows(team == null ? Team.Visibility.ALWAYS : team.getNameTagVisibility(), viewerTeam != null, team != null && team.isAlliedTo((Team)viewerTeam), !player.isInvisibleTo((Player)mc.player), team != null && team.canSeeFriendlyInvisibles());
    }

    private static Component score(AbstractClientPlayer player, double distance) {
        if (distance >= 100.0) {
            return null;
        }
        Scoreboard board = player.level().getScoreboard();
        Objective objective = board.getDisplayObjective(DisplaySlot.BELOW_NAME);
        if (objective == null) {
            return null;
        }
        ReadOnlyScoreInfo info = board.getPlayerScoreInfo((ScoreHolder)player, objective);
        return (info == null ? objective.numberFormatOrDefault((NumberFormat)StyledFormat.NO_STYLE).format(0) : info.formatValue(objective.numberFormatOrDefault((NumberFormat)StyledFormat.NO_STYLE))).append(" ").append(objective.getDisplayName());
    }

    private static void draw(LevelRenderContext context) {
        Frame current = frame;
        if (current == null || current.owner != context.levelState() || !LunarNameTags.active() || current.labels.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        SubmitNodeCollector collector = context.submitNodeCollector();
        for (Label label : current.labels) {
            PoseStack pose = new PoseStack();
            pose.mulPose((Matrix4fc)label.pose);
            if (label.score != null) {
                LunarNameTags.line(font, label.score, pose, label, collector);
                Objects.requireNonNull(font);
                pose.translate(0.0f, (float)(-9) * 1.15f, 0.0f);
            }
            LunarNameTags.line(font, label.name, pose, label, collector);
        }
        drawnLastFrame = current.labels.size();
        if (!reportedDraw) {
            reportedDraw = true;
            LOG.info("Independent player names drawn: {}. Color, background, light, scale and distance use BloodHelper settings.", (Object)drawnLastFrame);
        }
    }

    private static void line(Font font, Component text, PoseStack pose, Label label, SubmitNodeCollector collector) {
        collector.submitText(pose, (float)(-font.width((FormattedText)text)) / 2.0f, 0.0f, text.getVisualOrderText(), false, label.discrete ? Font.DisplayMode.NORMAL : Font.DisplayMode.SEE_THROUGH, label.light, label.color, label.background, 0);
    }

    private static /* synthetic */ void lambda$register$2(Minecraft client) {
        LunarNameTags.beginFrame();
    }

    private static /* synthetic */ void lambda$register$1(ClientPacketListener handler, Minecraft client) {
        LunarNameTags.beginFrame();
    }

    private static /* synthetic */ void lambda$register$0(Minecraft client) {
        String status;
        String string = !ConfigStore.get().betterNameVisibility.enabled ? "disabled" : (!BetterNameVisibilityModule.active() ? "blocked by module policy / Safe Mode" : (StreamerMode.hideNametags() ? "hidden by streamer mode" : (client.level == null ? "waiting for a world" : (status = client.gui.hud.isHidden() ? "hidden by F1" : "enabled"))));
        if (!status.equals(reportedStatus)) {
            reportedStatus = status;
            LOG.info("Player-name rendering: {}", (Object)status);
        }
    }

    static {
        reportedStatus = "";
    }

    private record Frame(LevelRenderState owner, List<Label> labels) {
    }

    private record Label(Component name, Component score, Matrix4f pose, boolean discrete, int light, int color, int background, double distanceSquared) {
    }
}

