/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.skins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.SkinsSettings;
import ru.bloodhelper.client.skins.BlockbenchModel;
import ru.bloodhelper.client.skins.SkinMesh;

public final class CustomModels {
    private static volatile Map<String, Descriptor> catalog = Map.of();
    private static final Map<String, SkinMesh> loaded = new ConcurrentHashMap<String, SkinMesh>();
    private static final Set<String> pending = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> retryAt = new ConcurrentHashMap<String, Long>();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BloodHelper local models");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean started = new AtomicBoolean();
    private static final AtomicBoolean choosing = new AtomicBoolean();
    private static volatile String notice = "";

    private CustomModels() {
    }

    public static boolean validId(String id) {
        return id != null && id.matches("CUSTOM_[a-f0-9]{64}");
    }

    private static Path directory() {
        return FabricLoader.getInstance().getConfigDir().resolve("bloodhelper/local-models");
    }

    public static void initialize() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        IO.execute(() -> {
            try {
                Files.createDirectories(CustomModels.directory(), new FileAttribute[0]);
                try (Stream<Path> files = Files.list(CustomModels.directory());){
                    for (Path path : files.filter(p -> p.getFileName().toString().endsWith(".bbmodel")).sorted().limit(16L).toList()) {
                        try {
                            CustomModels.importFile(path, false);
                        }
                        catch (Exception invalid) {
                            notice = "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c / Cannot read model";
                        }
                    }
                }
            }
            catch (IOException error) {
                notice = "\u041f\u0430\u043f\u043a\u0430 \u043c\u043e\u0434\u0435\u043b\u0435\u0439 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 / Model folder unavailable";
            }
        });
    }

    public static String notice() {
        return notice;
    }

    public static List<Descriptor> available() {
        return List.copyOf(catalog.values());
    }

    public static String[] ids() {
        ArrayList<String> result = new ArrayList<String>(List.of("RUDY", "VERITI", "JENNY", "MIMI_TYPH"));
        result.addAll(catalog.keySet());
        return (String[])result.toArray(String[]::new);
    }

    public static String[] names(boolean ru) {
        ArrayList<String> result = new ArrayList<String>(List.of(ru ? "\u0420\u0443\u0434\u0438" : "Rudy", ru ? "\u0412\u0435\u0440\u0438\u0442\u0438" : "Verity", "Jenny", "Mimi Typh"));
        for (Descriptor item : catalog.values()) {
            result.add(item.name());
        }
        return (String[])result.toArray(String[]::new);
    }

    public static SkinMesh get(String id) {
        return CustomModels.validId(id) ? loaded.get(id) : null;
    }

    public static void choose() {
        if (!choosing.compareAndSet(false, true)) {
            return;
        }
        Thread picker = new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("BloodHelper \u00b7 Blockbench");
                    chooser.setFileFilter(new FileNameExtensionFilter("Blockbench (.bbmodel)", "bbmodel"));
                    if (chooser.showOpenDialog(null) == 0) {
                        Path path = chooser.getSelectedFile().toPath();
                        IO.execute(() -> {
                            try {
                                CustomModels.importFile(path, true);
                            }
                            catch (Exception invalid) {
                                notice = "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c .bbmodel: \u043f\u0440\u043e\u0432\u0435\u0440\u044c \u0444\u043e\u0440\u043c\u0430\u0442 \u0438 \u0432\u0441\u0442\u0440\u043e\u0435\u043d\u043d\u044b\u0435 PNG / Invalid model or embedded PNG";
                            }
                        });
                    }
                });
            }
            catch (Exception | LinkageError error) {
                notice = "\u0412\u044b\u0431\u043e\u0440 \u0444\u0430\u0439\u043b\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d / File picker unavailable";
            }
            finally {
                choosing.set(false);
            }
        }, "BloodHelper model picker");
        picker.setDaemon(true);
        picker.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void importFile(Path path, boolean select) throws Exception {
        Object name;
        byte[] bytes;
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".bbmodel")) {
            throw new IOException("Expected .bbmodel");
        }
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            bytes = in.readNBytes(0x800001);
        }
        if (bytes.length > 0x800000) {
            throw new IOException("Model too large");
        }
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        String id = "CUSTOM_" + digest;
        if (catalog.size() >= 16 && !catalog.containsKey(id)) {
            throw new IOException("Maximum 16 local models");
        }
        BlockbenchModel.Result result = BlockbenchModel.parse(id, bytes);
        Files.createDirectories(CustomModels.directory(), new FileAttribute[0]);
        Path target = CustomModels.directory().resolve(digest + ".bbmodel");
        if (!path.toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) {
            Path tmp = Files.createTempFile(CustomModels.directory(), "import-", ".tmp", new FileAttribute[0]);
            try {
                Files.write(tmp, bytes, new OpenOption[0]);
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            finally {
                Files.deleteIfExists(tmp);
            }
        }
        if (((String)(name = path.getFileName().toString().replaceFirst("(?i)\\.bbmodel$", ""))).equals(digest)) {
            name = "Model " + digest.substring(0, 8);
        }
        if (((String)name).length() > 60) {
            name = ((String)name).substring(0, 60);
        }
        LinkedHashMap<String, Descriptor> next = new LinkedHashMap<String, Descriptor>(catalog);
        next.put(id, new Descriptor(id, (String)name, bytes.length));
        catalog = Collections.unmodifiableMap(next);
        CustomModels.install(id, result);
        if (select) {
            class_310.method_1551().execute(() -> {
                SkinsSettings settings = ConfigStore.get().skins;
                settings.character = id;
                settings.enabled = true;
                ConfigStore.requestSave();
            });
        }
        notice = "\u041c\u043e\u0434\u0435\u043b\u044c \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u0430 \u043b\u043e\u043a\u0430\u043b\u044c\u043d\u043e \u00b7 \u0432\u0438\u0434\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0442\u0435\u0431\u0435 / Local model \u00b7 only visible to you";
    }

    public static void install(String id, BlockbenchModel.Result result) throws IOException {
        ArrayList<class_1011> images = new ArrayList<class_1011>();
        try {
            for (byte[] png : result.textures()) {
                class_1011 image = class_1011.method_4309((InputStream)new ByteArrayInputStream(png));
                if (image.method_4307() > 512 || image.method_4323() > 512) {
                    image.close();
                    throw new IOException("Texture too large");
                }
                images.add(image);
            }
        }
        catch (Exception error) {
            images.forEach(class_1011::close);
            throw new IOException("Invalid custom texture", error);
        }
        class_310.method_1551().execute(() -> {
            try {
                for (int i = 0; i < images.size(); ++i) {
                    class_2960 texture = class_2960.method_60655((String)"bloodhelper", (String)result.mesh().textures()[i]);
                    class_310.method_1551().method_1531().method_4616(texture, (class_1044)new class_1043(() -> "BloodHelper custom costume", (class_1011)images.get(i)));
                }
                loaded.put(id, result.mesh());
                retryAt.remove(id);
                pending.remove(id);
            }
            catch (RuntimeException failure) {
                images.forEach(class_1011::close);
                pending.remove(id);
                retryAt.put(id, System.nanoTime() + 30000000000L);
            }
        });
    }

    public static void close() {
        IO.shutdownNow();
    }

    public record Descriptor(String id, String name, int bytes) {
    }
}

