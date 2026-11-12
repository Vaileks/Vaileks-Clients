/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.discord;

import java.io.IOException;
import java.nio.ByteBuffer;

interface DiscordIpc
extends AutoCloseable {
    public int read(ByteBuffer var1) throws IOException;

    public void write(byte[] var1) throws IOException;

    @Override
    public void close() throws IOException;

    @FunctionalInterface
    public static interface Connector {
        public DiscordIpc open() throws IOException;
    }
}

