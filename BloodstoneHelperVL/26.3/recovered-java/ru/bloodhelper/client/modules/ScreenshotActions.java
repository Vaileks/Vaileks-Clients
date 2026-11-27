/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Custom
 *  net.minecraft.network.chat.ClickEvent$OpenFile
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Util
 */
package ru.bloodhelper.client.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ImageClipboard;
import ru.bloodhelper.client.modules.ScreenshotEditorScreen;
import ru.bloodhelper.client.modules.ScreenshotImages;

public final class ScreenshotActions {
    public static final Identifier EVENT = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"screenshot_action");
    private static final IdentityHashMap<ClickEvent, Action> ACTIONS = new IdentityHashMap();
    private static final ArrayDeque<ClickEvent> ORDER = new ArrayDeque();
    private static final AtomicBoolean COPYING = new AtomicBoolean();

    private ScreenshotActions() {
    }

    public static Consumer<Component> decorate(Consumer<Component> original) {
        return message -> {
            if (!BloodHelperClient.config().screenshotActions) {
                original.accept((Component)message);
                return;
            }
            Path file = ScreenshotActions.findSavedFile(message);
            if (file == null) {
                original.accept((Component)message);
                return;
            }
            Minecraft.getInstance().execute(() -> original.accept((Component)message.copy().append((Component)Component.literal((String)"\n")).append(ScreenshotActions.buttons(file))));
        };
    }

    private static Path findSavedFile(Component message) {
        TranslatableContents text;
        Object[] objectArray = message.getContents();
        if (!(objectArray instanceof TranslatableContents) || !(text = (TranslatableContents)objectArray).getKey().equals("screenshot.success")) {
            return null;
        }
        for (Object arg : text.getArgs()) {
            Component component;
            ClickEvent clickEvent;
            if (!(arg instanceof Component) || !((clickEvent = (component = (Component)arg).getStyle().getClickEvent()) instanceof ClickEvent.OpenFile)) continue;
            ClickEvent.OpenFile open = (ClickEvent.OpenFile)clickEvent;
            try {
                return ScreenshotActions.allowed(Path.of(open.path(), new String[0]));
            }
            catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static Path allowed(Path file) throws IOException {
        Path root = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots").toRealPath(new LinkOption[0]);
        Path normalized = file.toAbsolutePath().normalize();
        if (!normalized.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png") || Files.isSymbolicLink(normalized) || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Invalid screenshot");
        }
        Path real = normalized.toRealPath(new LinkOption[0]);
        if (!real.startsWith(root)) {
            throw new IOException("Outside screenshot directory");
        }
        return real;
    }

    public static Component buttons(Path file) {
        MutableComponent line = Component.empty();
        ScreenshotActions.add(line, file, "copy", HudStyle.tr("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c", "Copy"));
        ScreenshotActions.add(line, file, "crop", HudStyle.tr("\u041e\u0431\u0440\u0435\u0437\u0430\u0442\u044c", "Crop"));
        ScreenshotActions.add(line, file, "redact", HudStyle.tr("\u0417\u0430\u043c\u0430\u0437\u0430\u0442\u044c", "Redact"));
        ScreenshotActions.add(line, file, "arrow", HudStyle.tr("\u0421\u0442\u0440\u0435\u043b\u043a\u0430", "Arrow"));
        return line;
    }

    private static void add(MutableComponent line, Path file, String kind, String label) {
        ClickEvent.Custom event = new ClickEvent.Custom(EVENT, Optional.empty());
        ACTIONS.put((ClickEvent)event, new Action(file, kind));
        ORDER.add((ClickEvent)event);
        while (ORDER.size() > 512) {
            ACTIONS.remove(ORDER.removeFirst());
        }
        line.append((Component)Component.literal((String)("[" + label + "] ")).withStyle(arg_0 -> ScreenshotActions.lambda$add$0((ClickEvent)event, arg_0)));
    }

    public static boolean clicked(Style style, boolean insertion, Screen parent) {
        ClickEvent.Custom event;
        ClickEvent clickEvent;
        if (style == null || !((clickEvent = style.getClickEvent()) instanceof ClickEvent.Custom) || !EVENT.equals((Object)(event = (ClickEvent.Custom)clickEvent).id())) {
            return false;
        }
        if (insertion) {
            return true;
        }
        Action action = ACTIONS.get(event);
        if (action == null || !BloodHelperClient.config().screenshotActions) {
            return true;
        }
        try {
            Path file = ScreenshotActions.allowed(action.file);
            if (action.kind.equals("copy")) {
                ScreenshotActions.copyFile(file);
            } else {
                Gui gui = Minecraft.getInstance().gui;
                gui.setScreen((Screen)new ScreenshotEditorScreen(parent, file, switch (action.kind) {
                    case "redact" -> ScreenshotImages.Tool.REDACT;
                    case "arrow" -> ScreenshotImages.Tool.ARROW;
                    default -> ScreenshotImages.Tool.CROP;
                }));
            }
        }
        catch (Exception failure) {
            ScreenshotActions.message(HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442", "Could not open screenshot"));
        }
        return true;
    }

    public static void copyFile(Path file) {
        if (!COPYING.compareAndSet(false, true)) {
            return;
        }
        Util.ioPool().execute(() -> {
            try {
                ImageClipboard.copy(ScreenshotImages.read(ScreenshotActions.allowed(file)));
                ScreenshotActions.message(HudStyle.tr("\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u0432 \u0431\u0443\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430", "Image copied to clipboard"));
            }
            catch (Exception failure) {
                ScreenshotActions.message(HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Could not copy image"));
            }
            finally {
                COPYING.set(false);
            }
        });
    }

    public static void message(String text) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)text)));
    }

    public static void saved(Path file) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)(HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e: ", "Saved: ") + String.valueOf(file.getFileName()) + "\n")).append(ScreenshotActions.buttons(file))));
    }

    private static /* synthetic */ Style lambda$add$0(ClickEvent event, Style style) {
        return style.withColor(9101504).withClickEvent(event);
    }

    private record Action(Path file, String kind) {
    }
}

