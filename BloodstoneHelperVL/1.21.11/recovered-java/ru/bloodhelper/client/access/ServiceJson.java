/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.access;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ServiceJson {
    private ServiceJson() {
    }

    static JsonObject object(byte[] bytes) throws IOException {
        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (byte character : bytes) {
            if (quoted) {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (character == 92) {
                    escaped = true;
                    continue;
                }
                if (character != 34) continue;
                quoted = false;
                continue;
            }
            if (character == 34) {
                quoted = true;
                continue;
            }
            if (character == 123 || character == 91) {
                if (++depth <= 24) continue;
                throw new IOException("Service JSON nested too deeply");
            }
            if (character != 125 && character != 93 || --depth >= 0) continue;
            throw new IOException("Invalid service JSON");
        }
        if (quoted || depth != 0) {
            throw new IOException("Invalid service JSON");
        }
        try {
            JsonElement root = JsonParser.parseString((String)new String(bytes, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) {
                throw new IOException("Invalid service response");
            }
            return root.getAsJsonObject();
        }
        catch (JsonParseException | IllegalStateException bad) {
            throw new IOException("Invalid service response", bad);
        }
    }

    static String string(JsonObject root, String name, int limit) throws IOException {
        JsonElement value = root.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("Missing field: " + name);
        }
        String result = value.getAsString();
        if (result.length() > limit) {
            throw new IOException("Oversized field: " + name);
        }
        return result;
    }

    static long number(JsonObject root, String name) throws IOException {
        JsonElement value = root.get(name);
        if (!(value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && value.getAsString().matches("-?[0-9]{1,18}"))) {
            throw new IOException("Invalid number: " + name);
        }
        try {
            return Long.parseLong(value.getAsString());
        }
        catch (NumberFormatException bad) {
            throw new IOException("Invalid number: " + name);
        }
    }

    static boolean bool(JsonObject root, String name) throws IOException {
        JsonElement value = root.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IOException("Invalid boolean: " + name);
        }
        return value.getAsBoolean();
    }

    static String publicUid(JsonObject root) throws IOException {
        if (root.has("publicUid")) {
            String value = ServiceJson.string(root, "publicUid", 12);
            if (!value.matches("[1-9][0-9]{0,11}")) {
                throw new IOException("Invalid public UID");
            }
            return value;
        }
        String value = ServiceJson.uid(root).substring(3).replaceFirst("^0+", "");
        if (!value.matches("[1-9][0-9]{0,11}")) {
            throw new IOException("Invalid legacy display UID");
        }
        return value;
    }

    static String uid(JsonObject root) throws IOException {
        String uid = ServiceJson.string(root, "uid", 32);
        if (!uid.matches("BH-[0-9]{6,20}")) {
            throw new IOException("Invalid UID");
        }
        return uid;
    }
}

