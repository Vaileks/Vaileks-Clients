/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Native
 *  com.sun.jna.Pointer
 *  com.sun.jna.WString
 *  com.sun.jna.ptr.IntByReference
 *  com.sun.jna.win32.StdCallLibrary
 */
package ru.bloodhelper.client.discord;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.io.IOException;
import java.nio.ByteBuffer;
import ru.bloodhelper.client.discord.DiscordIpc;

final class WindowsDiscordIpc
implements DiscordIpc {
    private Pointer handle;
    private final byte[] buffer = new byte[8192];
    private final IntByReference transferred = new IntByReference();

    private WindowsDiscordIpc(Pointer handle) {
        this.handle = handle;
    }

    static DiscordIpc open() throws IOException {
        for (int i = 0; i < 10; ++i) {
            Pointer handle = Api.KERNEL.CreateFileW(new WString("\\\\?\\pipe\\discord-ipc-" + i), -1073741824, 0, null, 3, 0, null);
            if (handle == null || Pointer.nativeValue((Pointer)handle) == -1L) continue;
            if (Api.KERNEL.SetNamedPipeHandleState(handle, new IntByReference(1), null, null)) {
                return new WindowsDiscordIpc(handle);
            }
            Api.KERNEL.CloseHandle(handle);
        }
        throw new IOException("Discord named pipe unavailable");
    }

    @Override
    public int read(ByteBuffer target) throws IOException {
        if (this.handle == null) {
            return -1;
        }
        int size = Math.min(target.remaining(), this.buffer.length);
        if (size == 0) {
            return 0;
        }
        this.transferred.setValue(0);
        if (!Api.KERNEL.ReadFile(this.handle, this.buffer, size, this.transferred, null)) {
            int error = Native.getLastError();
            if (error == 232) {
                return 0;
            }
            if (error == 109 || error == 233) {
                return -1;
            }
            throw new IOException("Discord pipe read failed (" + error + ")");
        }
        int count = this.transferred.getValue();
        target.put(this.buffer, 0, count);
        return count;
    }

    @Override
    public void write(byte[] frame) throws IOException {
        if (this.handle == null) {
            throw new IOException("Discord pipe closed");
        }
        this.transferred.setValue(0);
        if (!Api.KERNEL.WriteFile(this.handle, frame, frame.length, this.transferred, null) || this.transferred.getValue() != frame.length) {
            throw new IOException("Discord pipe write incomplete");
        }
    }

    @Override
    public void close() {
        if (this.handle != null) {
            Api.KERNEL.CloseHandle(this.handle);
            this.handle = null;
        }
    }

    private static final class Api {
        static final Kernel KERNEL = (Kernel)Native.load((String)"kernel32", Kernel.class);

        private Api() {
        }
    }

    public static interface Kernel
    extends StdCallLibrary {
        public Pointer CreateFileW(WString var1, int var2, int var3, Pointer var4, int var5, int var6, Pointer var7);

        public boolean SetNamedPipeHandleState(Pointer var1, IntByReference var2, Pointer var3, Pointer var4);

        public boolean ReadFile(Pointer var1, byte[] var2, int var3, IntByReference var4, Pointer var5);

        public boolean WriteFile(Pointer var1, byte[] var2, int var3, IntByReference var4, Pointer var5);

        public boolean CloseHandle(Pointer var1);
    }
}

