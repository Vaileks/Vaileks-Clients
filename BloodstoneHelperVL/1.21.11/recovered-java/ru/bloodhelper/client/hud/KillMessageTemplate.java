/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class KillMessageTemplate {
    private static final String NAME = "[A-Za-z0-9_]{1,16}";
    private final Pattern pattern;

    private KillMessageTemplate(Pattern pattern) {
        this.pattern = pattern;
    }

    static KillMessageTemplate compile(String template) {
        if (template == null || template.length() > 256 || template.isBlank()) {
            return new KillMessageTemplate(null);
        }
        if (KillMessageTemplate.occurrences(template, "{killer}") != 1 || KillMessageTemplate.occurrences(template, "{victim}") != 1) {
            return new KillMessageTemplate(null);
        }
        StringBuilder expression = new StringBuilder("\\A");
        int pos = 0;
        while (pos < template.length()) {
            int next;
            int killer = template.indexOf("{killer}", pos);
            int victim = template.indexOf("{victim}", pos);
            int n = killer < 0 ? victim : (next = victim < 0 ? killer : Math.min(killer, victim));
            if (next < 0) {
                expression.append(Pattern.quote(template.substring(pos)));
                break;
            }
            expression.append(Pattern.quote(template.substring(pos, next)));
            boolean isKiller = next == killer;
            expression.append("(?<").append(isKiller ? "killer" : "victim").append('>').append(NAME).append(')');
            pos = next + (isKiller ? "{killer}".length() : "{victim}".length());
        }
        return new KillMessageTemplate(Pattern.compile(expression.append("\\z").toString()));
    }

    Optional<Kill> parse(String message) {
        if (this.pattern == null || message == null || message.length() > 512) {
            return Optional.empty();
        }
        Matcher match = this.pattern.matcher(message);
        if (!match.matches()) {
            return Optional.empty();
        }
        return Optional.of(new Kill(match.group("killer"), match.group("victim")));
    }

    static boolean isName(String value) {
        return value != null && value.matches(NAME);
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            ++count;
            from += needle.length();
        }
        return count;
    }

    record Kill(String killer, String victim) {
    }
}

