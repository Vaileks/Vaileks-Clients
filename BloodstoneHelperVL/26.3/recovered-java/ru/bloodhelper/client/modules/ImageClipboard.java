/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.modules.ScreenshotImages;

public final class ImageClipboard {
    private ImageClipboard() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copy(final BufferedImage image) throws Exception {
        block16: {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                Path png = Files.createTempFile("bloodhelper-clipboard-", ".png", new FileAttribute[0]);
                Path script = Files.createTempFile("bloodhelper-clipboard-", ".ps1", new FileAttribute[0]);
                try {
                    Files.write(png, ScreenshotImages.png(image), new OpenOption[0]);
                    try (InputStream input = ImageClipboard.class.getResourceAsStream("/assets/bloodhelper/native/image-clipboard.ps1");){
                        if (input == null) {
                            throw new IOException("Clipboard helper missing");
                        }
                        Files.copy(input, script, StandardCopyOption.REPLACE_EXISTING);
                    }
                    String systemRoot = System.getenv("SystemRoot");
                    if (systemRoot == null) {
                        throw new IOException("Windows directory unavailable");
                    }
                    String shell = Path.of(systemRoot, "System32", "WindowsPowerShell", "v1.0", "powershell.exe").toString();
                    Process process = new ProcessBuilder(shell, "-NoProfile", "-NonInteractive", "-STA", "-WindowStyle", "Hidden", "-ExecutionPolicy", "Bypass", "-File", script.toString(), "-ImagePath", png.toString()).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
                    if (!process.waitFor(12L, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                        process.waitFor(2L, TimeUnit.SECONDS);
                        throw new IOException("Clipboard timed out");
                    }
                    if (process.exitValue() != 0) {
                        throw new IOException("Clipboard is unavailable");
                    }
                    break block16;
                }
                finally {
                    Files.deleteIfExists(png);
                    Files.deleteIfExists(script);
                }
            }
            if (GraphicsEnvironment.isHeadless()) {
                throw new IOException("Desktop clipboard unavailable");
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable(){

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.imageFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!this.isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return image;
                }
            }, null);
        }
    }
}

