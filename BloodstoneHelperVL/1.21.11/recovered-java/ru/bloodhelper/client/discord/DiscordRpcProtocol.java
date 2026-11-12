/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import ru.bloodhelper.client.discord.DiscordIpc;
import ru.bloodhelper.client.discord.DiscordPresence;

final class DiscordRpcProtocol {
    static final String APPLICATION_ID = "1560978569197396022";
    static final String BLOODHELPER_IMAGE = "https://raw.githubusercontent.com/nomielua/bloodhelper-policy/9f86917f3dd57e701d9cf9a4b67843abbb62f4a9/assets/bloodhelper-logo-9d54a2cda5ae.png";
    static final int HANDSHAKE = 0;
    static final int FRAME = 1;
    static final int CLOSE = 2;
    static final int PING = 3;
    static final int PONG = 4;
    static final int MAX_PAYLOAD = 65536;

    DiscordRpcProtocol() {
    }

    static boolean validApplicationId(String value) {
        return value != null && value.matches("[1-9][0-9]{16,19}") && new BigInteger(value).bitLength() <= 64;
    }

    static byte[] encode(int opcode, byte[] payload) {
        if (opcode < 0 || opcode > 4 || payload.length > 65536) {
            throw new IllegalArgumentException("RPC frame size/opcode");
        }
        return ByteBuffer.allocate(8 + payload.length).order(ByteOrder.LITTLE_ENDIAN).putInt(opcode).putInt(payload.length).put(payload).array();
    }

    static byte[] encode(int opcode, JsonObject payload) {
        return DiscordRpcProtocol.encode(opcode, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    static JsonObject handshake(String id) {
        JsonObject value = new JsonObject();
        value.addProperty("v", (Number)1);
        value.addProperty("client_id", id);
        return value;
    }

    static JsonObject activity(long pid, String nonce, DiscordPresence presence) {
        JsonObject args = new JsonObject();
        args.addProperty("pid", (Number)pid);
        args.add("activity", (JsonElement)(presence == null ? JsonNull.INSTANCE : presence.json()));
        JsonObject value = new JsonObject();
        value.addProperty("cmd", "SET_ACTIVITY");
        value.add("args", (JsonElement)args);
        value.addProperty("nonce", nonce);
        return value;
    }

    static String string(JsonObject value, String key) {
        JsonElement member = value.get(key);
        return member != null && member.isJsonPrimitive() && member.getAsJsonPrimitive().isString() ? member.getAsString() : "";
    }

    static final class Reader {
        private final ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        private ByteBuffer payload;
        private int opcode;
        private long started;
        private boolean partial;

        Reader() {
        }

        Frame poll(DiscordIpc ipc, long now) throws IOException {
            int read;
            if (this.partial && now - this.started >= 10000000000L) {
                throw new IOException("RPC partial frame timeout");
            }
            if (this.payload == null) {
                read = ipc.read(this.header);
                if (read < 0) {
                    throw new IOException("RPC EOF");
                }
                if (read > 0 && !this.partial) {
                    this.started = now;
                    this.partial = true;
                }
                if (this.header.hasRemaining()) {
                    return null;
                }
                this.header.flip();
                this.opcode = this.header.getInt();
                int size = this.header.getInt();
                this.header.clear();
                if (this.opcode < 0 || this.opcode > 4 || size < 0 || size > 65536) {
                    throw new IOException("Invalid RPC frame header");
                }
                this.payload = ByteBuffer.allocate(size);
            }
            if (this.payload.hasRemaining()) {
                read = ipc.read(this.payload);
                if (read < 0) {
                    throw new IOException("RPC EOF");
                }
                if (this.payload.hasRemaining()) {
                    return null;
                }
            }
            Frame result = new Frame(this.opcode, this.payload.array());
            this.payload = null;
            this.partial = false;
            return result;
        }
    }

    record Frame(int opcode, byte[] payload) {
        JsonObject json() throws IOException {
            try {
                return JsonParser.parseString((String)new String(this.payload, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            catch (RuntimeException error) {
                throw new IOException("Invalid RPC JSON");
            }
        }
    }
}

