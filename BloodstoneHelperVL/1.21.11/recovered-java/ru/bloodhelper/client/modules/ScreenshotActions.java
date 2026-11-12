/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_10607
 *  net.minecraft.class_2558$class_11506
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2588
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
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
import net.minecraft.class_156;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ImageClipboard;
import ru.bloodhelper.client.modules.ScreenshotEditorScreen;
import ru.bloodhelper.client.modules.ScreenshotImages;

public final class ScreenshotActions {
    public static final class_2960 EVENT = class_2960.method_60655((String)"bloodhelper", (String)"screenshot_action");
    private static final IdentityHashMap<class_2558, Action> ACTIONS = new IdentityHashMap();
    private static final ArrayDeque<class_2558> ORDER = new ArrayDeque();
    private static final AtomicBoolean COPYING = new AtomicBoolean();

    private ScreenshotActions() {
    }

    public static Consumer<class_2561> decorate(Consumer<class_2561> original) {
        return message -> {
            if (!BloodHelperClient.config().screenshotActions) {
                original.accept((class_2561)message);
                return;
            }
            Path file = ScreenshotActions.findSavedFile(message);
            if (file == null) {
                original.accept((class_2561)message);
                return;
            }
            class_310.method_1551().execute(() -> original.accept((class_2561)message.method_27661().method_10852((class_2561)class_2561.method_43470((String)"\n")).method_10852(ScreenshotActions.buttons(file))));
        };
    }

    private static Path findSavedFile(class_2561 message) {
        class_2588 text;
        Object[] objectArray = message.method_10851();
        if (!(objectArray instanceof class_2588) || !(text = (class_2588)objectArray).method_11022().equals("screenshot.success")) {
            return null;
        }
        for (Object arg : text.method_11023()) {
            class_2561 component;
            class_2558 class_25582;
            if (!(arg instanceof class_2561) || !((class_25582 = (component = (class_2561)arg).method_10866().method_10970()) instanceof class_2558.class_10607)) continue;
            class_2558.class_10607 open = (class_2558.class_10607)class_25582;
            try {
                return ScreenshotActions.allowed(Path.of(open.comp_3504(), new String[0]));
            }
            catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static Path allowed(Path file) throws IOException {
        Path root = class_310.method_1551().field_1697.toPath().resolve("screenshots").toRealPath(new LinkOption[0]);
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

    public static class_2561 buttons(Path file) {
        class_5250 line = class_2561.method_43473();
        ScreenshotActions.add(line, file, "copy", HudStyle.tr("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c", "Copy"));
        ScreenshotActions.add(line, file, "crop", HudStyle.tr("\u041e\u0431\u0440\u0435\u0437\u0430\u0442\u044c", "Crop"));
        ScreenshotActions.add(line, file, "redact", HudStyle.tr("\u0417\u0430\u043c\u0430\u0437\u0430\u0442\u044c", "Redact"));
        ScreenshotActions.add(line, file, "arrow", HudStyle.tr("\u0421\u0442\u0440\u0435\u043b\u043a\u0430", "Arrow"));
        return line;
    }

    private static void add(class_5250 line, Path file, String kind, String label) {
        class_2558.class_11506 event = new class_2558.class_11506(EVENT, Optional.empty());
        ACTIONS.put((class_2558)event, new Action(file, kind));
        ORDER.add((class_2558)event);
        while (ORDER.size() > 512) {
            ACTIONS.remove(ORDER.removeFirst());
        }
        line.method_10852((class_2561)class_2561.method_43470((String)("[" + label + "] ")).method_27694(arg_0 -> ScreenshotActions.lambda$add$0((class_2558)event, arg_0)));
    }

    public static boolean clicked(class_2583 style, boolean insertion, class_437 parent) {
        class_2558.class_11506 event;
        class_2558 class_25582;
        if (style == null || !((class_25582 = style.method_10970()) instanceof class_2558.class_11506) || !EVENT.equals((Object)(event = (class_2558.class_11506)class_25582).comp_4378())) {
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
                class_310 class_3102 = class_310.method_1551();
                class_3102.method_1507((class_437)new ScreenshotEditorScreen(parent, file, switch (action.kind) {
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
        class_156.method_27958().execute(() -> {
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
        class_310.method_1551().execute(() -> class_310.method_1551().field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)text)));
    }

    public static void saved(Path file) {
        class_310.method_1551().execute(() -> class_310.method_1551().field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)(HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e: ", "Saved: ") + String.valueOf(file.getFileName()) + "\n")).method_10852(ScreenshotActions.buttons(file))));
    }

    private static /* synthetic */ class_2583 lambda$add$0(class_2558 event, class_2583 style) {
        return style.method_36139(9101504).method_10958(event);
    }

    private record Action(Path file, String kind) {
    }
}

