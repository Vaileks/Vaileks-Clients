/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.access;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import ru.bloodhelper.client.access.DeviceIdentity;
import ru.bloodhelper.client.access.ServiceJson;
import ru.bloodhelper.client.policy.ControlSnapshot;

final class ControlCache {
    private static final int LIMIT = 65536;

    ControlCache() {
    }

    static Cached empty() {
        return new Cached("", null, null, false);
    }

    static Cached load(Path directory) {
        Path cache = directory.resolve("control-state.json");
        Path identity = directory.resolve("device-identity.json");
        if (!Files.exists(cache, LinkOption.NOFOLLOW_LINKS)) {
            return ControlCache.empty();
        }
        try {
            if (!Files.isRegularFile(cache, LinkOption.NOFOLLOW_LINKS) || Files.size(cache) > 65536L || !Files.isRegularFile(identity, LinkOption.NOFOLLOW_LINKS) || Files.size(identity) > 8192L) {
                throw new IOException("Invalid cache");
            }
            String key = ServiceJson.string(ServiceJson.object(Files.readAllBytes(identity)), "publicKey", 512);
            JsonObject wrapper = ServiceJson.object(Files.readAllBytes(cache));
            String payload = ServiceJson.string(wrapper, "payload", 65536);
            String signed = ServiceJson.string(wrapper, "signature", 128);
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key))));
            verifier.update(("bloodhelper-control-cache-v1\n" + payload).getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(Base64.getDecoder().decode(signed))) {
                throw new IOException("Invalid cache signature");
            }
            JsonObject json = ServiceJson.object(payload.getBytes(StandardCharsets.UTF_8));
            if (ServiceJson.number(json, "schema") != 1L || !key.equals(ServiceJson.string(json, "publicKey", 512))) {
                throw new IOException("Invalid cache binding");
            }
            String uid = ServiceJson.string(json, "wireUid", 20);
            if (!uid.isEmpty() && !uid.matches("BH-[0-9]{6,12}")) {
                throw new IOException("Invalid cached UID");
            }
            ControlSnapshot device = json.get("device").isJsonNull() ? null : ControlSnapshot.parse(json.getAsJsonObject("device"));
            ControlSnapshot.Global global = json.get("global").isJsonNull() ? null : ControlSnapshot.Global.parse(json.getAsJsonObject("global"));
            return new Cached(uid, device, global, false);
        }
        catch (Exception invalid) {
            return new Cached("", null, null, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void save(Path directory, DeviceIdentity identity, String uid, ControlSnapshot device, ControlSnapshot.Global global) throws IOException, GeneralSecurityException {
        JsonObject json = new JsonObject();
        json.addProperty("schema", (Number)1);
        json.addProperty("publicKey", identity.publicKey());
        json.addProperty("wireUid", uid);
        json.add("device", (JsonElement)(device.revision() == 0L ? JsonNull.INSTANCE : device.json()));
        json.add("global", (JsonElement)(global.revision() == 0L ? JsonNull.INSTANCE : global.json()));
        String payload = json.toString();
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("payload", payload);
        wrapper.addProperty("signature", identity.signCache(payload));
        byte[] bytes = wrapper.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 65536) {
            throw new IOException("Control cache too large");
        }
        Files.createDirectories(directory, new FileAttribute[0]);
        Path target = directory.resolve("control-state.json");
        if (Files.isSymbolicLink(target)) {
            throw new IOException("Invalid cache target");
        }
        Path temporary = Files.createTempFile(directory, "control-", ".tmp", new FileAttribute[0]);
        try {
            Files.write(temporary, bytes, new OpenOption[0]);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }

    record Cached(String wireUid, ControlSnapshot device, ControlSnapshot.Global global, boolean damaged) {
    }
}

