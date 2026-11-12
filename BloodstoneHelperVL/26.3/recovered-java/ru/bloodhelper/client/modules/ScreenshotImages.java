/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public final class ScreenshotImages {
    public static final int MAX_PIXELS = 0x1000000;

    private ScreenshotImages() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage read(Path path) throws IOException {
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.size(path) > 0x4000000L) {
            throw new IOException("Invalid image");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile());){
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("Unknown image format");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                if (w < 1 || h < 1 || (long)w * (long)h > 0x1000000L) {
                    throw new IOException("Image is too large");
                }
                BufferedImage bufferedImage = reader.read(0);
                reader.dispose();
                return bufferedImage;
            }
            catch (Throwable throwable) {
                reader.dispose();
                throw throwable;
            }
        }
    }

    public static BufferedImage copy(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), 2);
        Graphics2D g = result.createGraphics();
        try {
            g.drawImage((Image)image, 0, 0, null);
        }
        finally {
            g.dispose();
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage edit(BufferedImage source, Tool tool, int x1, int y1, int x2, int y2) {
        x1 = Math.clamp((long)x1, 0, source.getWidth() - 1);
        x2 = Math.clamp((long)x2, 0, source.getWidth() - 1);
        y1 = Math.clamp((long)y1, 0, source.getHeight() - 1);
        y2 = Math.clamp((long)y2, 0, source.getHeight() - 1);
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        int width = Math.abs(x2 - x1) + 1;
        int height = Math.abs(y2 - y1) + 1;
        if (tool == Tool.CROP) {
            return ScreenshotImages.copy(source.getSubimage(left, top, width, height));
        }
        BufferedImage result = ScreenshotImages.copy(source);
        Graphics2D g = result.createGraphics();
        try {
            if (tool == Tool.REDACT) {
                g.setColor(new Color(20, 22, 28));
                g.fillRect(left, top, width, height);
            } else {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float stroke = Math.max(3.0f, (float)source.getWidth() / 500.0f);
                g.setStroke(new BasicStroke(stroke, 1, 1));
                g.setColor(new Color(255, 72, 92));
                g.drawLine(x1, y1, x2, y2);
                double angle = Math.atan2(y2 - y1, x2 - x1);
                double head = Math.max(12.0f, stroke * 4.0f);
                for (double shift : new double[]{-0.55, 0.55}) {
                    g.drawLine(x2, y2, x2 - (int)(Math.cos(angle + shift) * head), y2 - (int)(Math.sin(angle + shift) * head));
                }
            }
        }
        finally {
            g.dispose();
        }
        return result;
    }

    public static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write((RenderedImage)image, "PNG", bytes)) {
            throw new IOException("PNG encoder unavailable");
        }
        return bytes.toByteArray();
    }

    public static Path saveCopy(Path original, BufferedImage image) throws IOException {
        String name = original.getFileName().toString().replaceFirst("(?i)\\.png$", "");
        Path destination = original.resolveSibling(name + "-edited-" + UUID.randomUUID().toString().substring(0, 8) + ".png");
        try (OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);){
            if (!ImageIO.write((RenderedImage)image, "PNG", out)) {
                throw new IOException("PNG encoder unavailable");
            }
        }
        return destination;
    }

    public static enum Tool {
        CROP,
        REDACT,
        ARROW;

    }
}

