/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.resourcepack;

import java.net.URI;
import java.util.Locale;

public final class PackOfferPolicy {
    private PackOfferPolicy() {
    }

    public static boolean isBloodstoneAddress(String address) {
        if (address == null) {
            return false;
        }
        String host = address.strip().toLowerCase(Locale.ROOT);
        int colon = host.indexOf(58);
        if (colon >= 0) {
            String port = host.substring(colon + 1);
            if (!port.matches("[0-9]{1,5}")) {
                return false;
            }
            int value = Integer.parseInt(port);
            if (value < 1 || value > 65535) {
                return false;
            }
            host = host.substring(0, colon);
        }
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return host.equals("bloodstone.gg") || host.matches("[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.bloodstone\\.gg");
    }

    public static URI remoteUrl(String text) {
        if (text == null) {
            return null;
        }
        try {
            URI uri = URI.create(text);
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
                return null;
            }
            if (uri.getRawFragment() != null) {
                if (!uri.getRawFragment().matches("(?i)[a-f0-9]{40}")) {
                    return null;
                }
                return URI.create(text.substring(0, text.indexOf(35)));
            }
            return uri;
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

