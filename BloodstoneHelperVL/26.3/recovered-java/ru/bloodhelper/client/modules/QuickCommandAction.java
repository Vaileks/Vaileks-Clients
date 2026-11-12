/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Optional;
import ru.bloodhelper.client.config.QuickCommandsSettings;

public record QuickCommandAction(boolean command, String value) {
    public static Optional<QuickCommandAction> parse(String source) {
        String text = QuickCommandsSettings.clean(source, 256);
        if (text.isEmpty()) {
            return Optional.empty();
        }
        boolean command = text.startsWith("/");
        String payload = command ? text.substring(1).stripLeading() : text;
        return payload.isBlank() ? Optional.empty() : Optional.of(new QuickCommandAction(command, payload));
    }
}

