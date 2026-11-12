/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.discord;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import ru.bloodhelper.client.discord.DiscordIpc;
import ru.bloodhelper.client.discord.WindowsDiscordIpc;

final class LocalDiscordIpc {
    LocalDiscordIpc() {
    }

    static DiscordIpc open() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.startsWith("windows")) {
            return WindowsDiscordIpc.open();
        }
        if (!os.contains("linux") && !os.contains("mac")) {
            throw new IOException("Unsupported IPC platform");
        }
        long deadline = System.nanoTime() + 500000000L;
        for (Path directory : LocalDiscordIpc.directories(System.getenv())) {
            for (int i = 0; i < 10 && System.nanoTime() < deadline; ++i) {
                SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX);
                try {
                    socket.configureBlocking(false);
                    boolean connected = socket.connect(UnixDomainSocketAddress.of(directory.resolve("discord-ipc-" + i)));
                    while (!connected && System.nanoTime() < deadline) {
                        connected = socket.finishConnect();
                        if (connected) continue;
                        LockSupport.parkNanos(1000000L);
                    }
                    if (connected) {
                        return new Unix(socket);
                    }
                }
                catch (IOException | RuntimeException exception) {
                    // empty catch block
                }
                socket.close();
            }
        }
        throw new IOException("Discord IPC unavailable");
    }

    static List<Path> directories(Map<String, String> environment) {
        LinkedHashSet<Path> paths = new LinkedHashSet<Path>();
        for (String key : new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"}) {
            String value = environment.get(key);
            if (value == null || value.isBlank()) continue;
            try {
                Path path = Path.of(value, new String[0]);
                if (!path.isAbsolute()) continue;
                paths.add(path);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
        paths.add(Path.of("/tmp", new String[0]));
        return List.copyOf(paths);
    }

    private record Unix(SocketChannel channel) implements DiscordIpc
    {
        @Override
        public int read(ByteBuffer target) throws IOException {
            return this.channel.read(target);
        }

        @Override
        public void write(byte[] frame) throws IOException {
            ByteBuffer source = ByteBuffer.wrap(frame);
            long deadline = System.nanoTime() + 250000000L;
            while (source.hasRemaining()) {
                if (System.nanoTime() >= deadline || Thread.currentThread().isInterrupted()) {
                    throw new IOException("RPC write timeout");
                }
                if (this.channel.write(source) != 0) continue;
                LockSupport.parkNanos(1000000L);
            }
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }
    }
}

