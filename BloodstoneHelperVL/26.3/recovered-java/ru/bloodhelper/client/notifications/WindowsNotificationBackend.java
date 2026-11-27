/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Native
 *  com.sun.jna.Platform
 *  com.sun.jna.Pointer
 *  com.sun.jna.Structure
 *  com.sun.jna.Structure$FieldOrder
 *  com.sun.jna.win32.StdCallLibrary
 */
package ru.bloodhelper.client.notifications;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.util.Arrays;
import ru.bloodhelper.client.notifications.DesktopNotification;
import ru.bloodhelper.client.notifications.DesktopNotificationService;

final class WindowsNotificationBackend
implements DesktopNotificationService.Backend {
    private static final int NIM_ADD = 0;
    private static final int NIM_MODIFY = 1;
    private static final int NIM_DELETE = 2;
    private static final int NIM_SETVERSION = 4;
    private static final int NIF_ICON = 2;
    private static final int NIF_TIP = 4;
    private static final int NIF_INFO = 16;
    private static final int NIF_REALTIME = 64;
    private static final int NIIF_INFO = 1;
    private static final int NIIF_NOSOUND = 16;
    private static final int NIIF_RESPECT_QUIET_TIME = 128;
    private NoticeData icon;

    WindowsNotificationBackend() {
    }

    @Override
    public boolean supported() {
        return Platform.isWindows();
    }

    @Override
    public boolean foreground(long window) {
        return Pointer.nativeValue((Pointer)NativeApi.USER.GetForegroundWindow()) == window;
    }

    @Override
    public boolean show(long window, DesktopNotification notification) {
        if (this.foreground(window)) {
            return false;
        }
        if (this.icon != null && Pointer.nativeValue((Pointer)this.icon.hWnd) != window) {
            this.clear();
        }
        if (this.icon == null) {
            NoticeData next = new NoticeData();
            next.hWnd = new Pointer(window);
            next.hIcon = NativeApi.USER.LoadIconW(null, Pointer.createConstant((int)32512));
            next.uFlags = 6;
            WindowsNotificationBackend.copy("BloodstoneHelperVL", next.szTip);
            if (!NativeApi.SHELL.Shell_NotifyIconW(0, next)) {
                return false;
            }
            this.icon = next;
            next.uVersion = 4;
            NativeApi.SHELL.Shell_NotifyIconW(4, next);
        }
        this.icon.uFlags = 80;
        this.icon.dwInfoFlags = 0x81 | (notification.sound() ? 0 : 16);
        WindowsNotificationBackend.copy(notification.title(), this.icon.szInfoTitle);
        WindowsNotificationBackend.copy(notification.body(), this.icon.szInfo);
        if (NativeApi.SHELL.Shell_NotifyIconW(1, this.icon)) {
            return true;
        }
        this.clear();
        return false;
    }

    @Override
    public void clear() {
        if (this.icon == null) {
            return;
        }
        NoticeData previous = this.icon;
        this.icon = null;
        NativeApi.SHELL.Shell_NotifyIconW(2, previous);
    }

    private static void copy(String value, short[] target) {
        Arrays.fill(target, (short)0);
        for (int i = 0; i < Math.min(value.length(), target.length - 1); ++i) {
            target[i] = (short)value.charAt(i);
        }
    }

    private static final class NativeApi {
        static final Shell SHELL = (Shell)Native.load((String)"shell32", Shell.class);
        static final User USER = (User)Native.load((String)"user32", User.class);

        private NativeApi() {
        }
    }

    public static interface User
    extends StdCallLibrary {
        public Pointer GetForegroundWindow();

        public Pointer LoadIconW(Pointer var1, Pointer var2);
    }

    @Structure.FieldOrder(value={"cbSize", "hWnd", "uID", "uFlags", "uCallbackMessage", "hIcon", "szTip", "dwState", "dwStateMask", "szInfo", "uVersion", "szInfoTitle", "dwInfoFlags", "guidItem", "hBalloonIcon"})
    public static final class NoticeData
    extends Structure {
        public int cbSize;
        public Pointer hWnd;
        public int uID = 16968;
        public int uFlags;
        public int uCallbackMessage;
        public Pointer hIcon;
        public short[] szTip = new short[128];
        public int dwState;
        public int dwStateMask;
        public short[] szInfo = new short[256];
        public int uVersion;
        public short[] szInfoTitle = new short[64];
        public int dwInfoFlags;
        public byte[] guidItem = new byte[16];
        public Pointer hBalloonIcon;

        public NoticeData() {
            this.cbSize = this.size();
        }
    }

    public static interface Shell
    extends StdCallLibrary {
        public boolean Shell_NotifyIconW(int var1, NoticeData var2);
    }
}

