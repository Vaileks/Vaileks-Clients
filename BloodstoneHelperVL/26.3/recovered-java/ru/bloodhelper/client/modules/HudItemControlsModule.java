/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.numbers.NumberFormat
 *  net.minecraft.network.chat.numbers.StyledFormat
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerScoreEntry
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Scoreboard
 *  net.minecraft.world.scores.Team
 */
package ru.bloodhelper.client.modules;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.HudItemControlsSettings;

public final class HudItemControlsModule {
    public static float hudFov = 70.0f;

    private HudItemControlsModule() {
    }

    public static HudItemControlsSettings settings() {
        return BloodHelperClient.config().hudItemControls;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHotbar(GuiGraphicsExtractor g) {
        Minecraft c = Minecraft.getInstance();
        HudItemControlsSettings.Hotbar s = HudItemControlsModule.settings().hotbar;
        if (c.player == null || c.player.isSpectator() || c.gui.hud.isHidden() || !s.markersEnabled && !s.keysEnabled) {
            return;
        }
        int left = g.guiWidth() / 2 - 91;
        int top = g.guiHeight() - 22;
        for (int i = 0; i < 9; ++i) {
            Object label;
            int x = left + 3 + 20 * i;
            if (s.markersEnabled && s.marked[i]) {
                int y = top + (s.markerEdge == HudItemControlsSettings.Edge.TOP ? 1 : 20);
                int color = (int)Math.round(s.markerOpacity * 255.0) << 24 | s.colors[i];
                g.fill(x, y, x + 16, y + 2, color);
            }
            if (!s.keysEnabled) continue;
            KeyMapping key = c.options.keyHotbarSlots[i];
            Object object = label = key.isUnbound() ? "\u2014" : key.getTranslatedKeyMessage().getString();
            if (key.saveString().startsWith("key.mouse.")) {
                String suffix;
                label = switch (suffix = key.saveString().substring("key.mouse.".length())) {
                    case "left" -> "M1";
                    case "right" -> "M2";
                    case "middle" -> "M3";
                    default -> "M" + suffix;
                };
            }
            float scale = (float)Math.min(s.keyScale, 18.0 / (double)Math.max(1, c.font.width((String)label)));
            int y = top + (s.keyEdge == HudItemControlsSettings.Edge.TOP ? -8 : 13) + s.keyOffset;
            y = Math.clamp((long)y, 0, Math.max(0, g.guiHeight() - (int)Math.ceil(9.0f * scale)));
            g.pose().pushMatrix();
            try {
                g.pose().translate((float)(x + 8) - (float)c.font.width((String)label) * scale / 2.0f, (float)y);
                g.pose().scale(scale, scale);
                g.text(c.font, (String)label, 0, 0, (int)Math.round(s.keyOpacity * 255.0) << 24 | 0xFFFFFF, true);
                continue;
            }
            finally {
                g.pose().popMatrix();
            }
        }
    }

    public static void transform(GuiGraphicsExtractor g, HudItemControlsSettings.Placement p, float vanillaX, float vanillaY, float left, float top, float width, float height) {
        HudItemControlsSettings.Box box = HudItemControlsSettings.fit(p, g.guiWidth(), g.guiHeight(), width, height);
        g.pose().translate(box.x() - left * box.scale(), box.y() - top * box.scale());
        g.pose().scale(box.scale(), box.scale());
        g.pose().translate(-vanillaX, -vanillaY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderScoreboard(GuiGraphicsExtractor g, Objective objective) {
        Minecraft c = Minecraft.getInstance();
        HudItemControlsSettings.Scoreboard s = HudItemControlsModule.settings().scoreboard;
        Scoreboard board = objective.getScoreboard();
        NumberFormat format = objective.numberFormatOrDefault((NumberFormat)StyledFormat.SIDEBAR_DEFAULT);
        List<ScoreLine> lines = board.listPlayerScores(objective).stream().filter(e -> !e.isHidden()).sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER)).limit(15L).map(e -> new ScoreLine((Component)PlayerTeam.formatNameForTeam((Team)board.getPlayersTeam(e.owner()), (Component)e.ownerName()), (Component)e.formatValue(format))).toList();
        int width = c.font.width((FormattedText)objective.getDisplayName());
        for (ScoreLine line : lines) {
            width = Math.max(width, c.font.width((FormattedText)line.name) + (c.font.width((FormattedText)line.value) == 0 ? 0 : c.font.width((FormattedText)line.value) + 6));
        }
        int row = 9 + s.lineSpacing;
        int height = row * (lines.size() + 1) + 6;
        float scale = (float)Math.min(s.scale, Math.min(((double)g.guiWidth() - 4.0) / (double)(width += 8), ((double)g.guiHeight() - 4.0) / (double)height));
        scale = Math.max(0.01f, scale);
        float x = HudItemControlsSettings.placed(s.x, g.guiWidth(), (float)width * scale);
        float y = HudItemControlsSettings.placed(s.y, g.guiHeight(), (float)height * scale);
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, y);
            g.pose().scale(scale, scale);
            if (s.backgroundOpacity > 0.0) {
                g.fill(0, 0, width, height, (int)Math.round(s.backgroundOpacity * 255.0) << 24 | s.backgroundColor);
            }
            g.text(c.font, objective.getDisplayName(), (width - c.font.width((FormattedText)objective.getDisplayName())) / 2, 3, -1, false);
            for (int i = 0; i < lines.size(); ++i) {
                ScoreLine line = lines.get(i);
                int ly = 3 + (i + 1) * row;
                g.text(c.font, line.name, 4, ly, -1, false);
                g.text(c.font, line.value, width - 4 - c.font.width((FormattedText)line.value), ly, -1, false);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    public static boolean replacesHearts() {
        HudItemControlsSettings.Health s = HudItemControlsModule.settings().health;
        return s.enabled && s.replaceVanilla;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHealth(GuiGraphicsExtractor g) {
        int width;
        Minecraft c = Minecraft.getInstance();
        HudItemControlsSettings.Health s = HudItemControlsModule.settings().health;
        if (!s.enabled || c.player == null || c.gui.hud.isHidden() || c.player.isSpectator() || c.gameMode == null || !c.gameMode.canHurtPlayer()) {
            return;
        }
        float health = Math.max(0.0f, c.player.getHealth());
        float maximum = Math.max(1.0f, c.player.getMaxHealth());
        float absorption = Math.max(0.0f, c.player.getAbsorptionAmount());
        int normalHearts = Math.min(100, (int)Math.ceil(maximum / 2.0f));
        int goldenHearts = Math.min(100, (int)Math.ceil(absorption / 2.0f));
        String normal = HudItemControlsModule.number(health) + " / " + HudItemControlsModule.number(maximum);
        String golden = absorption > 0.0f ? "  +" + HudItemControlsModule.number(absorption) : "";
        int n = width = s.style == HudItemControlsSettings.HealthStyle.HEARTS ? Math.min(10, normalHearts + goldenHearts) * 9 : Math.max(100, c.font.width(normal + golden));
        int height = s.style == HudItemControlsSettings.HealthStyle.HEARTS ? (normalHearts + goldenHearts + 9) / 10 * 10 : (s.style == HudItemControlsSettings.HealthStyle.BAR ? 22 : 10);
        float scale = (float)Math.min(s.scale, Math.min((double)g.guiWidth() / (double)Math.max(1, width), (double)g.guiHeight() / (double)Math.max(1, height)));
        g.pose().pushMatrix();
        try {
            g.pose().translate(HudItemControlsSettings.placed(s.x, g.guiWidth(), (float)width * scale), HudItemControlsSettings.placed(s.y, g.guiHeight(), (float)height * scale));
            g.pose().scale(scale, scale);
            if (s.style == HudItemControlsSettings.HealthStyle.HEARTS) {
                for (int i = 0; i < normalHearts + goldenHearts; ++i) {
                    boolean gold = i >= normalHearts;
                    float amount = gold ? absorption - (float)((i - normalHearts) * 2) : health - (float)(i * 2);
                    int x = i % 10 * 9;
                    int y = i / 10 * 10;
                    HudItemControlsModule.heart(g, "container", x, y);
                    if (!(amount > 0.0f)) continue;
                    HudItemControlsModule.heart(g, (gold ? "absorbing_" : "") + (amount <= 1.0f ? "half" : "full"), x, y);
                }
            } else {
                if (s.style == HudItemControlsSettings.HealthStyle.BAR) {
                    g.fill(0, 0, width, 7, -1339808726);
                    g.fill(0, 0, Math.round((float)width * Math.min(1.0f, health / maximum)), 7, -1219476);
                    if (absorption > 0.0f) {
                        g.fill(0, 8, Math.round((float)width * Math.min(1.0f, absorption / maximum)), 11, -12967);
                    }
                }
                int ty = s.style == HudItemControlsSettings.HealthStyle.BAR ? 13 : 0;
                g.text(c.font, normal, 0, ty, -1, true);
                if (absorption > 0.0f) {
                    g.text(c.font, golden, c.font.width(normal), ty, -12967, true);
                }
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    private static String number(float value) {
        return value == (float)((int)value) ? Integer.toString((int)value) : String.format(Locale.ROOT, "%.1f", Float.valueOf(value));
    }

    private static void heart(GuiGraphicsExtractor g, String name, int x, int y) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace((String)("hud/heart/" + name)), x, y, 9, 9);
    }

    private record ScoreLine(Component name, Component value) {
    }
}

