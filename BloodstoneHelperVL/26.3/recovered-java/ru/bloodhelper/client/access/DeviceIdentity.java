/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DeviceIdentity {
    private final KeyPair keys;
    private final String deviceHash;

    private DeviceIdentity(KeyPair keys, String deviceHash) {
        this.keys = keys;
        this.deviceHash = deviceHash;
    }

    String publicKey() {
        return Base64.getEncoder().encodeToString(this.keys.getPublic().getEncoded());
    }

    String deviceHash() {
        return this.deviceHash;
    }

    String signControl(String uid, long epochSeconds, String requestNonce) throws GeneralSecurityException, IOException {
        if (uid == null || !uid.matches("BH-[0-9]{6,12}") || epochSeconds < 0L || epochSeconds > 10000000000L || requestNonce == null || !requestNonce.matches("[A-Za-z0-9_-]{32,96}")) {
            throw new IOException("Invalid control request");
        }
        return this.sign(DeviceIdentity.controlRequestBytes(uid, epochSeconds, requestNonce));
    }

    static byte[] controlRequestBytes(String uid, long epochSeconds, String requestNonce) {
        return ("bloodhelper-control-v1\n" + uid + "\n" + epochSeconds + "\n" + requestNonce).getBytes(StandardCharsets.UTF_8);
    }

    String signGameProfile(String uid, long time, String nonce, String uuid, String name) throws GeneralSecurityException, IOException {
        if (!(uid != null && uid.matches("BH-[0-9]{6,12}") && time >= 0L && time <= 10000000000L && nonce != null && nonce.matches("[A-Za-z0-9_-]{32,96}") && uuid != null && uuid.matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}") && name != null && name.matches("[A-Za-z0-9_]{3,16}"))) {
            throw new IOException("Invalid game profile");
        }
        return this.sign(("bloodhelper-game-profile-v1\n" + uid + "\n" + time + "\n" + nonce + "\n" + uuid + "\n" + name).getBytes(StandardCharsets.UTF_8));
    }

    String signCache(String payload) throws GeneralSecurityException {
        return this.sign(("bloodhelper-control-cache-v1\n" + payload).getBytes(StandardCharsets.UTF_8));
    }

    private String sign(byte[] bytes) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(this.keys.getPrivate());
        signature.update(bytes);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    String signChallengeRequest(String uid, long epochSeconds, String requestNonce) throws GeneralSecurityException, IOException {
        if (uid == null || !uid.matches("BH-[0-9]{6,12}") || epochSeconds < 0L || epochSeconds > 10000000000L || requestNonce == null || !requestNonce.matches("[A-Za-z0-9_-]{32,96}")) {
            throw new IOException("Invalid challenge request");
        }
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(this.keys.getPrivate());
        signature.update(DeviceIdentity.challengeRequestBytes(uid, epochSeconds, requestNonce));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    static byte[] challengeRequestBytes(String uid, long epochSeconds, String requestNonce) {
        return ("bloodhelper-challenge-v1\n" + uid + "\n" + epochSeconds + "\n" + requestNonce).getBytes(StandardCharsets.UTF_8);
    }

    String signChallenge(String uid, String challenge, String nonce) throws GeneralSecurityException, IOException {
        if (!(uid.matches("BH-[0-9]{6,20}") && challenge.matches("[A-Za-z0-9_-]{8,160}") && nonce.matches("[A-Za-z0-9_+/=-]{16,256}"))) {
            throw new IOException("Invalid authentication challenge");
        }
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(this.keys.getPrivate());
        signature.update(DeviceIdentity.challengeBytes(uid, challenge, nonce));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    static byte[] challengeBytes(String uid, String challenge, String nonce) {
        return ("bloodhelper-auth-v1\n" + uid + "\n" + challenge + "\n" + nonce).getBytes(StandardCharsets.UTF_8);
    }

    static Path sharedDirectory() {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            String local = System.getenv("LOCALAPPDATA");
            return (local != null && !local.isBlank() ? Path.of(local, new String[0]) : Path.of(System.getProperty("user.home"), "AppData", "Local")).resolve("BloodHelper/access");
        }
        String xdg = System.getenv("XDG_DATA_HOME");
        Path base = xdg != null && !xdg.isBlank() && Path.of(xdg, new String[0]).isAbsolute() ? Path.of(xdg, new String[0]) : Path.of(System.getProperty("user.home"), ".local", "share");
        return base.resolve("bloodhelper/access");
    }

    static synchronized DeviceIdentity loadOrCreate(Path path) throws IOException, GeneralSecurityException {
        return null;
    }

    private static DeviceIdentity readOrCreate(Path path) throws IOException, GeneralSecurityException {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String hardwareIdentifier() {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            Process process = null;
            try {
                process = new ProcessBuilder("reg.exe", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid").redirectErrorStream(true).start();
                if (!process.waitFor(3L, TimeUnit.SECONDS)) {
                    String string = "";
                    return string;
                }
                String output = new String(process.getInputStream().readNBytes(4096), StandardCharsets.UTF_8);
                Matcher match = Pattern.compile("(?i)MachineGuid\\s+REG_SZ\\s+([0-9a-f-]{32,64})").matcher(output);
                String string = match.find() ? "windows:" + match.group(1).toLowerCase(Locale.ROOT) : "";
                return string;
            }
            catch (Exception ignored) {
                String string = "";
                return string;
            }
            finally {
                if (process != null) {
                    process.destroyForcibly();
                }
            }
        }
        try {
            Path machine = Path.of("/etc/machine-id", new String[0]);
            if (Files.size(machine) > 256L) {
                return "";
            }
            String value = Files.readString(machine).strip();
            return value.matches("[0-9a-fA-F]{32}") ? "linux:" + value.toLowerCase(Locale.ROOT) : "";
        }
        catch (IOException ignored) {
            return "";
        }
    }

    private static void restrict(Path path, boolean directory) throws IOException {
        if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(path, directory ? EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE) : EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }
    }
}

