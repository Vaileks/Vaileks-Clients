/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.Optional;
import java.util.regex.Pattern;

record FfaKillReceipt(boolean rewarded, String fingerprint) {
    private static final Pattern FORMAT = Pattern.compile("(?i)\u00a7[0-9a-fk-orx]");
    private static final Pattern TIMESTAMP = Pattern.compile("^\\[(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]]\\s*");
    private static final Pattern REWARD = Pattern.compile("\u0422\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b [1-9][0-9]{0,8} \u043c\u043e\u043d\u0435\u0442 \u0437\u0430 \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u043e \u0438\u0433\u0440\u043e\u043a\u0430 [\\p{Co}\\p{So}\\p{Sk} ]*[A-Za-z0-9_]{1,16}(?: \u0441 \u043c\u043d\u043e\u0436\u0438\u0442\u0435\u043b\u0435\u043c x[0-9]{1,3}(?:[.,][0-9]{1,3})?)?[.!]?");
    private static final Pattern NO_REWARD = Pattern.compile("\u0417\u0430 \u044d\u0442\u043e\u0433\u043e \u0438\u0433\u0440\u043e\u043a\u0430 \u043f\u043e\u043a\u0430 \u043d\u0435\u043b\u044c\u0437\u044f \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u043c\u043e\u043d\u0435\u0442\u044b\\. \u041f\u043e\u0434\u043e\u0436\u0434\u0438 [0-9]{1,7} \u0441\u0435\u043a\u0443\u043d\u0434[.!]?");

    static Optional<FfaKillReceipt> parse(String text) {
        if (text == null || text.length() > 768) {
            return Optional.empty();
        }
        String plain = FORMAT.matcher(text).replaceAll("").replaceAll("[\\p{Cf}]", "").replaceAll("[\\s\\p{Zs}]+", " ").strip();
        if (REWARD.matcher(plain = TIMESTAMP.matcher(plain).replaceFirst("")).matches()) {
            return Optional.of(new FfaKillReceipt(true, plain));
        }
        if (NO_REWARD.matcher(plain).matches()) {
            return Optional.of(new FfaKillReceipt(false, plain));
        }
        return Optional.empty();
    }
}

