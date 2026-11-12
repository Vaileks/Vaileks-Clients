/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

enum MediaBridgePlatform {
    WINDOWS,
    LINUX,
    UNSUPPORTED;


    static MediaBridgePlatform current() {
        return MediaBridgePlatform.fromName(System.getProperty("os.name", ""));
    }

    static MediaBridgePlatform fromName(String osName) {
        String name;
        String string = name = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        return name.startsWith("windows") ? WINDOWS : (name.equals("linux") ? LINUX : UNSUPPORTED);
    }

    String[] resources() {
        String[] stringArray;
        switch (this.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                String[] stringArray2 = new String[3];
                stringArray2[0] = "media-bridge.ps1";
                stringArray2[1] = "media-favorites.ps1";
                stringArray = stringArray2;
                stringArray2[2] = "media-sources.ps1";
                break;
            }
            case 1: {
                String[] stringArray3 = new String[1];
                stringArray = stringArray3;
                stringArray3[0] = "media-linux.py";
                break;
            }
            case 2: {
                stringArray = new String[]{};
            }
        }
        return stringArray;
    }

    List<String> arguments(Path directory, String preferredSource) throws IOException {
        ArrayList<String> args = new ArrayList<String>();
        if (this == WINDOWS) {
            String windows = System.getenv("SystemRoot");
            if (windows == null || windows.isBlank()) {
                throw new IOException("Windows directory is unavailable");
            }
            args.addAll(List.of(Path.of(windows, "System32", "WindowsPowerShell", "v1.0", "powershell.exe").toString(), "-NoLogo", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Mta", "-ExecutionPolicy", "Bypass", "-File", directory.resolve("media-bridge.ps1").toString()));
        } else if (this == LINUX) {
            Path python = List.of(Path.of("/usr/bin/python3", new String[0]), Path.of("/usr/local/bin/python3", new String[0]), Path.of("/bin/python3", new String[0])).stream().filter(Files::isExecutable).findFirst().orElseThrow(() -> new IOException("missing_python3: install Python 3"));
            args.addAll(List.of(python.toString(), "-I", "-u", directory.resolve("media-linux.py").toString()));
        } else {
            throw new IOException("unsupported_os");
        }
        if (!preferredSource.isEmpty()) {
            if (this == WINDOWS) {
                args.add("-PreferredSourceBase64");
            }
            args.add(Base64.getEncoder().encodeToString(preferredSource.getBytes(StandardCharsets.UTF_8)));
        }
        return args;
    }
}

