/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Screenshot
 *  net.minecraft.client.gui.components.toasts.SystemToast
 *  net.minecraft.client.gui.components.toasts.SystemToast$SystemToastId
 *  net.minecraft.client.gui.components.toasts.ToastManager
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Util
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DeathObservation;
import ru.bloodhelper.client.modules.DeathScreenshotFiles;
import ru.bloodhelper.client.modules.DeathScreenshotGallery;
import ru.bloodhelper.client.modules.DeathScreenshotViewer;

public final class DeathScreenshotModule {
    private static final DeathObservation<Boolean> OBSERVATION = new DeathObservation();
    private static final AtomicInteger WRITING = new AtomicInteger();
    private static final SystemToast.SystemToastId TOAST = new SystemToast.SystemToastId(5000L);
    private static Object connection;
    private static Object level;
    private static Object deathPlayer;
    private static Capture currentDeath;
    private static Capture lastCapture;

    private DeathScreenshotModule() {
    }

    public static void tick(Minecraft client) {
        boolean alive;
        if (connection != client.getConnection() || level != client.level) {
            DeathScreenshotModule.reset();
            connection = client.getConnection();
            level = client.level;
        }
        if (!BloodHelperClient.config().deathScreenshot.enabled || connection == null || level == null || client.player == null) {
            OBSERVATION.reset();
            currentDeath = null;
            deathPlayer = null;
            return;
        }
        boolean bl = alive = client.player.isAlive() && !client.player.isDeadOrDying();
        if (alive) {
            currentDeath = null;
            deathPlayer = null;
        }
        if (OBSERVATION.sample(client.player, alive, alive ? Boolean.TRUE : null) != null) {
            DeathScreenshotModule.capture(client);
        }
    }

    private static void capture(Minecraft client) {
        Capture requested;
        Instant instant = Instant.now();
        currentDeath = lastCapture = (requested = new Capture(DeathScreenshotFiles.filename(instant, UUID.randomUUID()), instant, State.SAVING));
        deathPlayer = client.player;
        ClientPacketListener capturedConnection = client.getConnection();
        boolean notify = BloodHelperClient.config().deathScreenshot.notifySaved;
        Path gameDirectory = client.gameDirectory.toPath();
        if (WRITING.incrementAndGet() > 3) {
            WRITING.decrementAndGet();
            DeathScreenshotModule.finish(client, requested, false, capturedConnection, notify);
            return;
        }
        try {
            Screenshot.takeScreenshot((RenderTarget)client.gameRenderer.mainRenderTarget(), image -> {
                try {
                    Util.ioPool().execute(() -> {
                        boolean saved;
                        block20: {
                            Path temporary;
                            block18: {
                                saved = false;
                                temporary = null;
                                try (NativeImage twrVar0$11 = image;){
                                    Path folder = DeathScreenshotFiles.ensureDirectory(gameDirectory);
                                    temporary = Files.createTempFile(folder, ".death-", ".tmp", new FileAttribute[0]);
                                    image.writeToFile(temporary);
                                    Files.move(temporary, DeathScreenshotFiles.resolve(gameDirectory, requested.filename()), new CopyOption[0]);
                                    saved = true;
                                }
                                if (temporary == null) break block18;
                                try {
                                    Files.deleteIfExists(temporary);
                                }
                                catch (Exception twrVar0$11) {
                                    // empty catch block
                                }
                            }
                            WRITING.decrementAndGet();
                            break block20;
                            catch (Exception exception3222222222) {
                                block19: {
                                    try {
                                        LoggerFactory.getLogger(DeathScreenshotModule.class).warn("Could not save death screenshot", (Throwable)exception3222222222);
                                        if (temporary == null) break block19;
                                    }
                                    catch (Throwable throwable) {
                                        if (temporary != null) {
                                            try {
                                                Files.deleteIfExists(temporary);
                                            }
                                            catch (Exception exception2) {
                                                // empty catch block
                                            }
                                        }
                                        WRITING.decrementAndGet();
                                        throw throwable;
                                    }
                                    try {
                                        Files.deleteIfExists(temporary);
                                    }
                                    catch (Exception exception3222222222) {
                                        // empty catch block
                                    }
                                }
                                WRITING.decrementAndGet();
                            }
                        }
                        boolean success = saved;
                        client.execute(() -> DeathScreenshotModule.finish(client, requested, success, capturedConnection, notify));
                    });
                }
                catch (RuntimeException rejected) {
                    image.close();
                    WRITING.decrementAndGet();
                    client.execute(() -> DeathScreenshotModule.finish(client, requested, false, capturedConnection, notify));
                }
            });
        }
        catch (RuntimeException exception) {
            WRITING.decrementAndGet();
            LoggerFactory.getLogger(DeathScreenshotModule.class).warn("Could not capture death frame", (Throwable)exception);
            DeathScreenshotModule.finish(client, requested, false, capturedConnection, notify);
        }
    }

    private static void finish(Minecraft client, Capture capture, boolean success, Object capturedConnection, boolean notify) {
        Capture completed = new Capture(capture.filename(), capture.time(), success ? State.SAVED : State.FAILED);
        if (lastCapture != null && lastCapture.filename().equals(capture.filename())) {
            lastCapture = completed;
        }
        if (currentDeath != null && currentDeath.filename().equals(capture.filename())) {
            currentDeath = completed;
        }
        if (notify && BloodHelperClient.config().deathScreenshot.enabled && client.getConnection() == capturedConnection) {
            SystemToast.addOrUpdate((ToastManager)client.gui.toastManager(), (SystemToast.SystemToastId)TOAST, (Component)Component.literal((String)HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442 \u0441\u043c\u0435\u0440\u0442\u0438", "Death Screenshot")), (Component)Component.literal((String)(success ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0432 \u043b\u043e\u043a\u0430\u043b\u044c\u043d\u043e\u0439 \u0433\u0430\u043b\u0435\u0440\u0435\u0435", "Saved to the local gallery") : HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Could not save the image"))));
        }
    }

    static String fileForCurrentDeath(Object player) {
        return deathPlayer == player && currentDeath != null ? currentDeath.filename() : null;
    }

    public static Capture lastCapture() {
        return lastCapture;
    }

    public static Path directory() {
        return DeathScreenshotFiles.directory(Minecraft.getInstance().gameDirectory.toPath());
    }

    public static void reset() {
        OBSERVATION.reset();
        currentDeath = null;
        deathPlayer = null;
    }

    public static void openGallery(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new DeathScreenshotGallery(parent));
    }

    public static void openScreenshot(Screen parent, String filename) {
        if (DeathScreenshotFiles.owned(filename)) {
            Minecraft.getInstance().gui.setScreen((Screen)new DeathScreenshotViewer(parent, filename));
        }
    }

    public record Capture(String filename, Instant time, State state) {
    }

    public static enum State {
        SAVING,
        SAVED,
        FAILED;

    }
}

