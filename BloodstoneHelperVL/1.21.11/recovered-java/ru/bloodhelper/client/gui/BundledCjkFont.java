/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.tukaani.xz.XZInputStream
 */
package ru.bloodhelper.client.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.tukaani.xz.XZInputStream;

public final class BundledCjkFont {
    public static final String RESOURCE = "/assets/bloodhelper/font/cjk.ttf.xz";
    public static final String SHA256 = "5e3bd6d9f99771921082a8d48dbb5a38e346da56e128f94162f9999ae69989dd";
    public static final int LENGTH = 21689056;
    private static volatile byte[] decoded;

    private BundledCjkFont() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static InputStream open() throws IOException {
        byte[] result = decoded;
        if (result != null) return new ByteArrayInputStream(result);
        Class<BundledCjkFont> clazz = BundledCjkFont.class;
        synchronized (BundledCjkFont.class) {
            result = decoded;
            if (result != null) return new ByteArrayInputStream(result);
            try (InputStream resource = BundledCjkFont.class.getResourceAsStream(RESOURCE);){
                if (resource == null) {
                    throw new IOException("Missing bundled CJK font /assets/bloodhelper/font/cjk.ttf.xz");
                }
                result = BundledCjkFont.decode(resource);
            }
            decoded = result;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return new ByteArrayInputStream(result);
        }
    }

    static byte[] decode(InputStream source) throws IOException {
        try (XZInputStream xz = new XZInputStream(source, 32768);){
            byte[] bytes = xz.readNBytes(21689057);
            if (bytes.length != 21689056 || xz.read() != -1) {
                throw new IOException("Bundled CJK font length mismatch");
            }
            try {
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
                if (!SHA256.equals(hash)) {
                    throw new IOException("Bundled CJK font checksum mismatch");
                }
            }
            catch (NoSuchAlgorithmException impossible) {
                throw new AssertionError((Object)impossible);
            }
            byte[] byArray = bytes;
            return byArray;
        }
    }
}

