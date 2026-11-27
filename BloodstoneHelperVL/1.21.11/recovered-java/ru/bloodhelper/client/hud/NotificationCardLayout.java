/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import ru.bloodhelper.client.hud.AlertTracker;

final class NotificationCardLayout {
    static final int WIDTH = 214;
    static final int ITEM_HEIGHT = 20;
    static final int ITEM_TEXT_X = 23;
    static final int GAP = 3;
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final Pattern LINE_BREAKS = Pattern.compile("[\\r\\n\\t\\u0085\\u2028\\u2029]+");

    private NotificationCardLayout() {
    }

    static String itemBody(int count, String language) {
        return "ru".equals(language) ? "\u041e\u0441\u0442\u0430\u043b\u043e\u0441\u044c " + Math.max(0, count) + " \u0448\u0442." : "Remaining: " + Math.max(0, count);
    }

    static String itemId(AlertTracker.Notice notice) {
        return NotificationCardLayout.payloadId(notice.icon(), "item:");
    }

    static String payloadId(String icon, String prefix) {
        if (icon == null || !icon.startsWith(prefix) || icon.length() > prefix.length() + 256) {
            return "";
        }
        String id = icon.substring(prefix.length());
        return ITEM_ID.matcher(id).matches() ? id : "";
    }

    static String line(AlertTracker.Notice notice) {
        String title = NotificationCardLayout.singleLine(notice.title());
        String body = NotificationCardLayout.singleLine(notice.body());
        if (notice.key().startsWith("feature:chatMentions:")) {
            return body;
        }
        if (title.isEmpty()) {
            return body;
        }
        return body.isEmpty() ? title : title + ": " + body;
    }

    private static String singleLine(String text) {
        return text == null ? "" : LINE_BREAKS.matcher(text).replaceAll(" ").strip();
    }

    static AlertTracker.Notice itemExample(String language, String prefix) {
        return new AlertTracker.Notice(prefix + "item:minecraft:ender_pearl", "", NotificationCardLayout.itemBody(13, language), "item:minecraft:ender_pearl", 0L, 4000L);
    }

    static Card card(AlertTracker.Notice notice, boolean details, int textWidth, double visibility) {
        int width = Math.clamp(23L + (long)Math.max(0, textWidth) + 7L, 64, 214);
        return new Card(width, 20, visibility);
    }

    static Stack stack(List<Card> cards, String alignment) {
        return NotificationCardLayout.stack(cards, alignment, 0);
    }

    static Stack stack(List<Card> cards, String alignment, int minimumWidth) {
        int width = Math.clamp((long)minimumWidth, 0, 214);
        double occupied = 0.0;
        double lastVisibility = 0.0;
        for (Card card : cards) {
            if (card.visibility() <= 0.0) continue;
            width = Math.max(width, card.width());
            occupied += (double)(card.height() + 3) * card.visibility();
            lastVisibility = card.visibility();
        }
        int height = occupied < 1.0 ? 0 : Math.max(1, (int)Math.ceil(occupied - 3.0 * lastVisibility));
        ArrayList<Placement> placements = new ArrayList<Placement>(cards.size());
        double cursor = 0.0;
        for (Card card : cards) {
            int x = switch (alignment) {
                case "RIGHT" -> width - card.width();
                case "CENTER" -> (width - card.width()) / 2;
                default -> 0;
            };
            int y = (int)Math.round(cursor);
            int clip = Math.max(0, Math.min((int)Math.ceil((double)card.height() * card.visibility()), height - y));
            int inset = (int)Math.round(4.0 * (1.0 - card.visibility()));
            placements.add(new Placement(x, y, card.width(), card.height(), clip, inset));
            cursor += (double)(card.height() + 3) * card.visibility();
        }
        return new Stack(width, height, List.copyOf(placements));
    }

    record Card(int width, int height, double visibility) {
        Card {
            visibility = Double.isFinite(visibility) ? Math.clamp(visibility, 0.0, 1.0) : 0.0;
        }
    }

    record Stack(int width, int height, List<Placement> cards) {
    }

    record Placement(int x, int y, int width, int height, int clipHeight, int inset) {
    }
}

