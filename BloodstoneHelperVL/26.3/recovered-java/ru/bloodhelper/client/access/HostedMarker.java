/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.access;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import ru.bloodhelper.client.access.ServiceJson;

public record HostedMarker(String id, String label, String dimension, int x, int y, int z) {
    static HostedMarker parse(JsonObject object) throws IOException {
        String id = ServiceJson.string(object, "id", 64);
        String label = ServiceJson.string(object, "label", 96);
        String dimension = ServiceJson.string(object, "dimension", 128);
        long x = HostedMarker.coordinate(object, "x");
        long y = HostedMarker.coordinate(object, "y");
        long z = HostedMarker.coordinate(object, "z");
        if (!id.matches("[A-Za-z0-9_-]{1,64}") || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || Math.abs(x) > 30000000L || y < -2048L || y > 2048L || Math.abs(z) > 30000000L || label.isBlank() || label.codePoints().anyMatch(Character::isISOControl)) {
            throw new IOException("Invalid marker");
        }
        return new HostedMarker(id, label, dimension, (int)x, (int)y, (int)z);
    }

    private static long coordinate(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IOException("Invalid marker coordinate");
        }
        double number = value.getAsDouble();
        if (!Double.isFinite(number) || Math.abs(number) > 3.0E7) {
            throw new IOException("Invalid marker coordinate");
        }
        return (long)Math.floor(number);
    }

    JsonObject payload() {
        JsonObject object = new JsonObject();
        object.addProperty("label", this.label);
        object.addProperty("dimension", this.dimension);
        object.addProperty("x", (Number)this.x);
        object.addProperty("y", (Number)this.y);
        object.addProperty("z", (Number)this.z);
        return object;
    }
}

