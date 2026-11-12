/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.vl;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PrivacyGuard {
    private static volatile List<String> identities = List.of();
    private static volatile long refreshed;
    private static volatile boolean known;

    private PrivacyGuard() {
    }

    private static Object invoke(Object object, String string) throws ReflectiveOperationException {
        return object.getClass().getMethod(string, new Class[0]).invoke(object, new Object[0]);
    }

    private static synchronized void refresh() {
        long l = System.nanoTime();
        if (l - refreshed < 2000000000L && refreshed != 0L) {
            return;
        }
        refreshed = l;
        ArrayList<String> arrayList = new ArrayList<String>();
        try {
            Object object;
            Object object2;
            boolean bl;
            Class<?> clazz;
            try {
                clazz = Class.forName("net.minecraft.class_310");
                bl = false;
            }
            catch (ClassNotFoundException classNotFoundException) {
                clazz = Class.forName("net.minecraft.client.Minecraft");
                bl = true;
            }
            Object object3 = clazz.getMethod(bl ? "getInstance" : "method_1551", new Class[0]).invoke(null, new Object[0]);
            Object object4 = PrivacyGuard.invoke(object3, bl ? "getUser" : "method_1548");
            Object object5 = PrivacyGuard.invoke(object4, bl ? "getName" : "method_1676");
            Object object6 = PrivacyGuard.invoke(object4, bl ? "getProfileId" : "method_44717");
            if (object5 != null) {
                arrayList.add(object5.toString().toLowerCase(Locale.ROOT));
            }
            if (object6 != null) {
                arrayList.add(object6.toString().toLowerCase(Locale.ROOT));
                arrayList.add(object6.toString().replace("-", "").toLowerCase(Locale.ROOT));
            }
            if ((object2 = PrivacyGuard.invoke(object3, bl ? "getCurrentServer" : "method_1558")) != null && (object = object2.getClass().getField(bl ? "ip" : "field_3761").get(object2)) != null) {
                arrayList.add(object.toString().toLowerCase(Locale.ROOT));
            }
            known = true;
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            known = false;
        }
        identities = List.copyOf(arrayList);
    }

    public static boolean sensitive(String string, List<String> list) {
        String string2 = URLDecoder.decode(string, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        for (String string3 : list) {
            if (string3 == null || string3.length() < 3 || !string2.contains(string3.toLowerCase(Locale.ROOT))) continue;
            return true;
        }
        return false;
    }

    public static void checkUri(URI uRI) {
        if (uRI == null) {
            return;
        }
        PrivacyGuard.checkUrl(uRI.toString());
    }

    public static void checkUrlObject(URL uRL) {
        if (uRL != null) {
            PrivacyGuard.checkUrl(uRL.toExternalForm());
        }
    }

    public static void checkUrl(String string) {
        URI uRI = URI.create(string);
        String string2 = uRI.getHost();
        if (string2 == null) {
            return;
        }
        if (string2.equalsIgnoreCase("localhost") || string2.equals("127.0.0.1") || string2.equals("[::1]") || string2.equals("::1")) {
            return;
        }
        if (string2.equals("178.22.24.133")) {
            throw new SecurityException("Blocked device registration service");
        }
        PrivacyGuard.refresh();
        if (PrivacyGuard.sensitive(string, identities)) {
            throw new SecurityException("Blocked request containing local player identity or server address");
        }
        if (!known && Set.of("api.atiers.net", "mytiers.ru", "api.minecraftservices.com", "api.mojang.com", "mctiers.com", "pvptiers.com", "mc-heads.net", "api.mcsrvstat.us").contains(string2.toLowerCase(Locale.ROOT))) {
            throw new SecurityException("Player identity unavailable; profile request blocked");
        }
    }
}

