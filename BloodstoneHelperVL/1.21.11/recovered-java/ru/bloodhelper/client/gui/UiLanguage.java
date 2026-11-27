/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  net.fabricmc.fabric.api.resource.v1.ResourceLoader
 *  net.minecraft.class_2561
 *  net.minecraft.class_2588
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3264
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_3302
 *  net.minecraft.class_4013
 *  net.minecraft.class_7417
 *  net.minecraft.class_8828
 */
package ru.bloodhelper.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3302;
import net.minecraft.class_4013;
import net.minecraft.class_7417;
import net.minecraft.class_8828;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.ReGlassResources;

public final class UiLanguage {
    private static final Pattern NUMBER_FORMAT = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    private static final Pattern ARGUMENT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private static volatile Tables cached = new Tables(null, Map.of(), Map.of());
    private static boolean registered;

    private UiLanguage() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ResourceLoader.get((class_3264)class_3264.field_14188).registerReloader(class_2960.method_60655((String)"bloodhelper", (String)"ui_languages"), (class_3302)((class_4013)UiLanguage::reload));
        registered = true;
    }

    public static Object cacheToken() {
        return UiLanguage.tables();
    }

    public static String text(class_2561 component) {
        if (component == null) {
            return "";
        }
        Tables tables = UiLanguage.tables();
        boolean russian = "ru".equals(BloodHelperClient.config().language);
        return UiLanguage.resolve(component, russian ? tables.russian : tables.english, tables.english, 0);
    }

    private static Tables tables() {
        class_310 client = class_310.method_1551();
        class_3300 manager = client == null ? null : client.method_1478();
        Tables current = cached;
        if (manager != null && current.manager != manager) {
            UiLanguage.reload(manager);
            current = cached;
        }
        return current;
    }

    private static synchronized void reload(class_3300 manager) {
        Map<String, String> english = UiLanguage.load(manager, "en_us");
        Map<String, String> russian = UiLanguage.load(manager, "ru_ru");
        Tables next = new Tables((class_3300)(english.isEmpty() && russian.isEmpty() ? null : manager), english, russian);
        ReGlassResources.prepare(manager, next);
        cached = next;
    }

    private static Map<String, String> load(class_3300 manager, String language) {
        HashMap<String, String> entries = new HashMap<String, String>();
        if (manager == null) {
            return Map.of();
        }
        List namespaces = manager.method_14487().stream().sorted().toList();
        for (String namespace : namespaces) {
            class_2960 path = class_2960.method_60655((String)namespace, (String)("lang/" + language + ".json"));
            for (class_3298 resource : manager.method_14489(path)) {
                try {
                    BufferedReader reader = resource.method_43039();
                    try {
                        JsonElement root = JsonParser.parseReader((Reader)reader);
                        if (!root.isJsonObject()) continue;
                        for (Map.Entry entry : root.getAsJsonObject().entrySet()) {
                            if (!((JsonElement)entry.getValue()).isJsonPrimitive() || !((JsonElement)entry.getValue()).getAsJsonPrimitive().isString()) continue;
                            entries.put((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
                        }
                    }
                    finally {
                        if (reader == null) continue;
                        reader.close();
                    }
                }
                catch (IOException | RuntimeException exception) {}
            }
        }
        return Map.copyOf(entries);
    }

    static String resolve(class_2561 component, Map<String, String> selected, Map<String, String> fallback, int depth) {
        if (component == null || depth > 64) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        class_7417 contents = component.method_10851();
        if (contents instanceof class_8828) {
            class_8828 literal = (class_8828)contents;
            result.append(literal.comp_737());
        } else if (contents instanceof class_2588) {
            class_2588 translated = (class_2588)contents;
            String template = selected.get(translated.method_11022());
            if (template == null) {
                template = fallback.get(translated.method_11022());
            }
            if (template == null) {
                template = translated.method_48323();
            }
            if (template == null) {
                result.append(component.method_27662().getString());
            } else {
                Object[] arguments = translated.method_11023();
                String[] values = new String[arguments.length];
                for (int i = 0; i < arguments.length; ++i) {
                    String string;
                    Object object = arguments[i];
                    if (object instanceof class_2561) {
                        class_2561 child = (class_2561)object;
                        string = UiLanguage.resolve(child, selected, fallback, depth + 1);
                    } else {
                        string = String.valueOf(arguments[i]);
                    }
                    values[i] = string;
                }
                result.append(UiLanguage.format(template, values));
            }
        } else {
            result.append(component.method_27662().getString());
        }
        for (class_2561 sibling : component.method_10855()) {
            result.append(UiLanguage.resolve(sibling, selected, fallback, depth + 1));
        }
        return result.toString();
    }

    private static String format(String source, String[] arguments) {
        String template = NUMBER_FORMAT.matcher(source).replaceAll("%$1s");
        Matcher matcher = ARGUMENT.matcher(template);
        StringBuilder result = new StringBuilder();
        int cursor = 0;
        int sequentialIndex = 0;
        while (matcher.find()) {
            result.append(template, cursor, matcher.start());
            String type = matcher.group(2);
            if ("%".equals(type) && matcher.group(1) == null) {
                result.append('%');
            } else if ("s".equals(type)) {
                int index;
                try {
                    index = matcher.group(1) == null ? sequentialIndex++ : Integer.parseInt(matcher.group(1)) - 1;
                }
                catch (NumberFormatException ignored) {
                    return source;
                }
                if (index < 0 || index >= arguments.length) {
                    return source;
                }
                result.append(arguments[index]);
            } else {
                return source;
            }
            cursor = matcher.end();
        }
        return result.append(template, cursor, template.length()).toString();
    }

    private record Tables(class_3300 manager, Map<String, String> english, Map<String, String> russian) {
    }
}

