/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;

public final class ChatMentionMatcher {
    public static final int MAX_TEXT = 8192;
    private static final int MAX_MATCHES = 64;

    private ChatMentionMatcher() {
    }

    public static List<Range> find(String text, String account, int from, int to) {
        if (text == null || account == null || account.isEmpty() || account.length() > 16 || text.length() > 8192 || from < 0 || to > text.length() || from > to) {
            return List.of();
        }
        ArrayList<Range> matches = null;
        int index = from;
        while (index + account.length() <= to) {
            if (text.regionMatches(true, index, account, 0, account.length())) {
                int start = index > from && text.charAt(index - 1) == '@' ? index - 1 : index;
                int end = index + account.length();
                if (!(start > from && ChatMentionMatcher.isWord(text.codePointBefore(start)) || end < to && ChatMentionMatcher.isWord(text.codePointAt(end)))) {
                    if (matches == null) {
                        matches = new ArrayList<Range>();
                    }
                    matches.add(new Range(start, end));
                    if (matches.size() == 64) break;
                    index = end - 1;
                }
            }
            ++index;
        }
        return matches == null ? List.of() : List.copyOf(matches);
    }

    private static Sender sender(String text) {
        if (text == null || text.length() > 8192) {
            return null;
        }
        int limit = Math.min(192, text.length());
        int cursor = 0;
        while (cursor < limit) {
            int close;
            char c = text.charAt(cursor);
            if (Character.isWhitespace(c)) {
                ++cursor;
                continue;
            }
            if (c == '[') {
                close = text.indexOf(93, cursor + 1);
                if (close < 0 || close >= limit) {
                    return null;
                }
                cursor = close + 1;
                continue;
            }
            if (c == '<') {
                close = text.indexOf(62, cursor + 1);
                if (close > cursor + 1 && close < limit && ChatMentionMatcher.validName(text.substring(cursor + 1, close))) {
                    return new Sender(cursor + 1, close, ChatMentionMatcher.skipSpaces(text, close + 1));
                }
                return null;
            }
            if (ChatMentionMatcher.isNameChar(c)) {
                int end;
                for (end = cursor; end < limit && ChatMentionMatcher.isNameChar(text.charAt(end)); ++end) {
                }
                int separator = ChatMentionMatcher.skipSpaces(text, end);
                if (end - cursor <= 16 && separator < text.length() && (text.charAt(separator) == ':' || text.charAt(separator) == '\u00bb')) {
                    return new Sender(cursor, end, ChatMentionMatcher.skipSpaces(text, separator + 1));
                }
                for (int i = cursor; i < end; ++i) {
                    if (Character.isDigit(text.charAt(i))) continue;
                    return null;
                }
                cursor = end;
                continue;
            }
            int cp = text.codePointAt(cursor);
            int type = Character.getType(cp);
            if (type != 18 && type != 28 && type != 25 && type != 27) {
                return null;
            }
            cursor += Character.charCount(cp);
        }
        return null;
    }

    public static int systemBodyStart(String text) {
        Sender sender = ChatMentionMatcher.sender(text);
        return sender == null ? 0 : sender.body;
    }

    public static boolean systemIsOwnMessage(String text, String account, int bodyStart) {
        Sender sender = ChatMentionMatcher.sender(text);
        return account != null && sender != null && sender.body == bodyStart && text.substring(sender.start, sender.end).equalsIgnoreCase(account);
    }

    private static int skipSpaces(String text, int start) {
        while (start < text.length() && text.charAt(start) == ' ') {
            ++start;
        }
        return start;
    }

    private static boolean validName(String text) {
        if (text.isEmpty() || text.length() > 16) {
            return false;
        }
        for (int i = 0; i < text.length(); ++i) {
            if (ChatMentionMatcher.isNameChar(text.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static boolean isNameChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }

    private static boolean isWord(int codePoint) {
        return Character.isLetterOrDigit(codePoint) || codePoint == 95;
    }

    public record Range(int start, int end) {
    }

    private record Sender(int start, int end, int body) {
    }
}

