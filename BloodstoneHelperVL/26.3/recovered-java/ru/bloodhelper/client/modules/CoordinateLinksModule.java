/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Custom
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.CoordinateLinksSettings;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CoordinateLinkDisplay;
import ru.bloodhelper.client.modules.CoordinateLinkScreen;
import ru.bloodhelper.client.modules.CoordinateParser;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class CoordinateLinksModule {
    public static final Identifier EVENT = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"coordinate_link");
    private static final IdentityHashMap<ClickEvent, Link> LINKS = new IdentityHashMap();
    private static final ArrayDeque<ClickEvent> ORDER = new ArrayDeque();
    private static Object connection;
    private static Object settings;

    private CoordinateLinksModule() {
    }

    public static void tick(Minecraft client) {
        ClientPacketListener next = client.getConnection();
        CoordinateLinksSettings nextSettings = BloodHelperClient.config().coordinateLinks;
        if (!BloodHelperClient.config().coordinateLinks.enabled || next == null || next != connection || nextSettings != settings) {
            CoordinateLinksModule.reset();
            connection = next;
            settings = nextSettings;
        }
    }

    public static Component receiveForDisplay(Component original) {
        Minecraft client = Minecraft.getInstance();
        CoordinateLinksModule.tick(client);
        if (!CoordinateLinksModule.active(client)) {
            return original;
        }
        List<CoordinateParser.Coordinate> matches = CoordinateParser.find(original.getString(4097));
        return CoordinateLinkDisplay.apply(original, matches, coordinates -> {
            WaypointSettings.Entry entry = new WaypointSettings.Entry();
            entry.scope = WaypointsModule.currentScope();
            entry.dimension = WaypointsModule.currentDimension();
            entry.name = HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0438\u0437 \u0447\u0430\u0442\u0430", "Chat waypoint");
            entry.x = coordinates.x();
            entry.y = coordinates.y() == null ? client.player.blockPosition().getY() : coordinates.y().intValue();
            entry.z = coordinates.z();
            entry.color = ThemeColors.accent() | 0xFF000000;
            ClickEvent.Custom event = new ClickEvent.Custom(EVENT, Optional.empty());
            LINKS.put((ClickEvent)event, new Link(client.getConnection(), entry));
            ORDER.addLast((ClickEvent)event);
            while (ORDER.size() > 512) {
                LINKS.remove(ORDER.removeFirst());
            }
            return event;
        }, (Component)Component.literal((String)HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043c\u0435\u0442\u043a\u0443", "Create waypoint")), ThemeColors.timestamp());
    }

    public static boolean clicked(Style style, boolean insertion, Screen parent) {
        ClickEvent.Custom custom;
        ClickEvent clickEvent;
        if (style == null || !((clickEvent = style.getClickEvent()) instanceof ClickEvent.Custom) || !EVENT.equals((Object)(custom = (ClickEvent.Custom)clickEvent).id())) {
            return false;
        }
        if (insertion) {
            return style.getInsertion() == null;
        }
        Minecraft client = Minecraft.getInstance();
        CoordinateLinksModule.tick(client);
        Link link = LINKS.get(custom);
        if (CoordinateLinksModule.active(client) && link != null && link.connection == client.getConnection()) {
            client.gui.setScreen((Screen)new CoordinateLinkScreen(parent, link.entry, link.connection));
        }
        return true;
    }

    private static boolean active(Minecraft client) {
        return BloodHelperClient.config().coordinateLinks.enabled && !StreamerMode.hideChat() && !StreamerMode.hideCoordinates() && client.player != null && client.level != null && client.getConnection() != null;
    }

    public static void settingsChanged() {
        CoordinateLinksModule.tick(Minecraft.getInstance());
    }

    public static void reset() {
        LINKS.clear();
        ORDER.clear();
        connection = null;
        settings = null;
    }

    private record Link(Object connection, WaypointSettings.Entry entry) {
    }
}

