/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.policy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record ServerModuleRule(boolean ips, List<Address> bannedIps) {
    public static final Map<String, ServerModuleRule> DEFAULTS = Map.of();

    public ServerModuleRule {
        bannedIps = List.copyOf(bannedIps);
    }

    public static ServerModuleRule parse(JsonObject rule) {
        boolean ips = true;
        if (rule.has("ips")) {
            JsonElement value = rule.get("ips");
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                throw new IllegalArgumentException("ips must be a boolean");
            }
            ips = value.getAsBoolean();
        }
        ArrayList<Address> banned = new ArrayList<Address>();
        if (rule.has("banned-ips")) {
            if (!rule.get("banned-ips").isJsonArray() || rule.getAsJsonArray("banned-ips").size() > 256) {
                throw new IllegalArgumentException("banned-ips must be a bounded array");
            }
            for (JsonElement entry : rule.getAsJsonArray("banned-ips")) {
                if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException("Server address must be a string");
                }
                banned.add(Address.parse(entry.getAsString(), true));
            }
        }
        return new ServerModuleRule(ips, banned);
    }

    public boolean blocks(String serverAddress) {
        Address server;
        if (!this.ips || this.bannedIps.isEmpty() || serverAddress == null) {
            return false;
        }
        try {
            server = Address.parse(serverAddress, false);
        }
        catch (IllegalArgumentException error) {
            return false;
        }
        return this.bannedIps.stream().anyMatch(pattern -> pattern.matches(server));
    }

    public static boolean allows(Map<String, ServerModuleRule> rules, String module, String address) {
        ServerModuleRule rule = rules.get(module);
        return rule == null || !rule.blocks(address);
    }

    public static String contextAddress(boolean connectedWorld, boolean localConnection, String selectedAddress) {
        return connectedWorld && !localConnection ? selectedAddress : null;
    }

    public record Address(String host, int port, boolean wildcard) {
        static Address parse(String text, boolean pattern) {
            boolean wildcard;
            if (text == null || text.length() > 300) {
                throw new IllegalArgumentException("Invalid server address");
            }
            String value = text.strip().toLowerCase(Locale.ROOT);
            boolean bl = wildcard = pattern && value.startsWith("*.");
            if (wildcard) {
                value = value.substring(2);
            }
            String host = value;
            int port = -1;
            if (value.startsWith("[")) {
                int end = value.indexOf(93);
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed IPv6 address");
                }
                host = value.substring(1, end);
                String tail = value.substring(end + 1);
                if (!tail.isEmpty()) {
                    if (!tail.startsWith(":")) {
                        throw new IllegalArgumentException("Invalid address suffix");
                    }
                    port = Address.port(tail.substring(1));
                }
                if (!host.contains(":")) {
                    throw new IllegalArgumentException("Brackets require IPv6");
                }
            } else if (value.indexOf(58) >= 0 && value.indexOf(58) == value.lastIndexOf(58)) {
                int colon = value.indexOf(58);
                host = value.substring(0, colon);
                port = Address.port(value.substring(colon + 1));
            }
            if (host.contains(":")) {
                if (wildcard || !host.matches("[0-9a-f:.]+")) {
                    throw new IllegalArgumentException("Invalid IPv6 address");
                }
                try {
                    host = InetAddress.getByName(host).getHostAddress();
                }
                catch (UnknownHostException error) {
                    throw new IllegalArgumentException("Invalid IPv6 address", error);
                }
            } else {
                if (host.endsWith(".")) {
                    host = host.substring(0, host.length() - 1);
                }
                if ((host = IDN.toASCII(host, 2).toLowerCase(Locale.ROOT)).isEmpty() || host.length() > 253 || host.endsWith(".") || !host.matches("[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?")) {
                    throw new IllegalArgumentException("Invalid server host");
                }
                if (host.matches("[0-9.]+")) {
                    String[] parts = host.split("\\.", -1);
                    if (wildcard || parts.length != 4) {
                        throw new IllegalArgumentException("Invalid IPv4 address");
                    }
                    for (String part : parts) {
                        if (part.matches("0|[1-9][0-9]{0,2}") && Integer.parseInt(part) <= 255) continue;
                        throw new IllegalArgumentException("Invalid IPv4 address");
                    }
                }
            }
            return new Address(host, port, wildcard);
        }

        private static int port(String value) {
            if (!value.matches("[0-9]{1,5}")) {
                throw new IllegalArgumentException("Invalid server port");
            }
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid server port");
            }
            return port;
        }

        boolean matches(Address server) {
            return (this.port == -1 || this.port == (server.port == -1 ? 25565 : server.port)) && (this.wildcard ? server.host.endsWith("." + this.host) : this.host.equals(server.host));
        }
    }
}

