/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_11506
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_634
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_634;
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
    public static final class_2960 EVENT = class_2960.method_60655((String)"bloodhelper", (String)"coordinate_link");
    private static final IdentityHashMap<class_2558, Link> LINKS = new IdentityHashMap();
    private static final ArrayDeque<class_2558> ORDER = new ArrayDeque();
    private static Object connection;
    private static Object settings;

    private CoordinateLinksModule() {
    }

    public static void tick(class_310 client) {
        class_634 next = client.method_1562();
        CoordinateLinksSettings nextSettings = BloodHelperClient.config().coordinateLinks;
        if (!BloodHelperClient.config().coordinateLinks.enabled || next == null || next != connection || nextSettings != settings) {
            CoordinateLinksModule.reset();
            connection = next;
            settings = nextSettings;
        }
    }

    public static class_2561 receiveForDisplay(class_2561 original) {
        class_310 client = class_310.method_1551();
        CoordinateLinksModule.tick(client);
        if (!CoordinateLinksModule.active(client)) {
            return original;
        }
        List<CoordinateParser.Coordinate> matches = CoordinateParser.find(original.method_10858(4097));
        return CoordinateLinkDisplay.apply(original, matches, coordinates -> {
            WaypointSettings.Entry entry = new WaypointSettings.Entry();
            entry.scope = WaypointsModule.currentScope();
            entry.dimension = WaypointsModule.currentDimension();
            entry.name = HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0438\u0437 \u0447\u0430\u0442\u0430", "Chat waypoint");
            entry.x = coordinates.x();
            entry.y = coordinates.y() == null ? client.field_1724.method_24515().method_10264() : coordinates.y().intValue();
            entry.z = coordinates.z();
            entry.color = ThemeColors.accent() | 0xFF000000;
            class_2558.class_11506 event = new class_2558.class_11506(EVENT, Optional.empty());
            LINKS.put((class_2558)event, new Link(client.method_1562(), entry));
            ORDER.addLast((class_2558)event);
            while (ORDER.size() > 512) {
                LINKS.remove(ORDER.removeFirst());
            }
            return event;
        }, (class_2561)class_2561.method_43470((String)HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043c\u0435\u0442\u043a\u0443", "Create waypoint")), ThemeColors.timestamp());
    }

    public static boolean clicked(class_2583 style, boolean insertion, class_437 parent) {
        class_2558.class_11506 custom;
        class_2558 class_25582;
        if (style == null || !((class_25582 = style.method_10970()) instanceof class_2558.class_11506) || !EVENT.equals((Object)(custom = (class_2558.class_11506)class_25582).comp_4378())) {
            return false;
        }
        if (insertion) {
            return style.method_10955() == null;
        }
        class_310 client = class_310.method_1551();
        CoordinateLinksModule.tick(client);
        Link link = LINKS.get(custom);
        if (CoordinateLinksModule.active(client) && link != null && link.connection == client.method_1562()) {
            client.method_1507((class_437)new CoordinateLinkScreen(parent, link.entry, link.connection));
        }
        return true;
    }

    private static boolean active(class_310 client) {
        return BloodHelperClient.config().coordinateLinks.enabled && !StreamerMode.hideChat() && !StreamerMode.hideCoordinates() && client.field_1724 != null && client.field_1687 != null && client.method_1562() != null;
    }

    public static void settingsChanged() {
        CoordinateLinksModule.tick(class_310.method_1551());
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

