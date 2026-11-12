/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  net.fabricmc.fabric.api.resource.v1.ResourceLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.contents.PlainTextContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
        ResourceLoader.get((PackType)PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"ui_languages"), (PreparableReloadListener)((ResourceManagerReloadListener)UiLanguage::reload));
        registered = true;
    }

    public static Object cacheToken() {
        return UiLanguage.tables();
    }

    public static String text(Component component) {
        if (component == null) {
            return "";
        }
        Tables tables = UiLanguage.tables();
        boolean russian = "ru".equals(BloodHelperClient.config().language);
        return UiLanguage.resolve(component, russian ? tables.russian : tables.english, tables.english, 0);
    }

    private static Tables tables() {
        Minecraft client = Minecraft.getInstance();
        ResourceManager manager = client == null ? null : client.getResourceManager();
        Tables current = cached;
        if (manager != null && current.manager != manager) {
            UiLanguage.reload(manager);
            current = cached;
        }
        return current;
    }

    private static synchronized void reload(ResourceManager manager) {
        Map<String, String> english = UiLanguage.load(manager, "en_us");
        Map<String, String> russian = UiLanguage.load(manager, "ru_ru");
        Tables next = new Tables((ResourceManager)(english.isEmpty() && russian.isEmpty() ? null : manager), english, russian);
        ReGlassResources.prepare(manager, next);
        cached = next;
    }

    private static Map<String, String> load(ResourceManager manager, String language) {
        HashMap<String, String> entries = new HashMap<String, String>();
        if (manager == null) {
            return Map.of();
        }
        List namespaces = manager.getNamespaces().stream().sorted().toList();
        for (String namespace : namespaces) {
            Identifier path = Identifier.fromNamespaceAndPath((String)namespace, (String)("lang/" + language + ".json"));
            for (Resource resource : manager.getResourceStack(path)) {
                try {
                    BufferedReader reader = resource.openAsReader();
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

    static String resolve(Component component, Map<String, String> selected, Map<String, String> fallback, int depth) {
        if (component == null || depth > 64) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        ComponentContents contents = component.getContents();
        if (contents instanceof PlainTextContents) {
            PlainTextContents literal = (PlainTextContents)contents;
            result.append(literal.text());
        } else if (contents instanceof TranslatableContents) {
            TranslatableContents translated = (TranslatableContents)contents;
            String template = selected.get(translated.getKey());
            if (template == null) {
                template = fallback.get(translated.getKey());
            }
            if (template == null) {
                template = translated.getFallback();
            }
            if (template == null) {
                result.append(component.plainCopy().getString());
            } else {
                Object[] arguments = translated.getArgs();
                String[] values = new String[arguments.length];
                for (int i = 0; i < arguments.length; ++i) {
                    String string;
                    Object object = arguments[i];
                    if (object instanceof Component) {
                        Component child = (Component)object;
                        string = UiLanguage.resolve(child, selected, fallback, depth + 1);
                    } else {
                        string = String.valueOf(arguments[i]);
                    }
                    values[i] = string;
                }
                result.append(UiLanguage.format(template, values));
            }
        } else {
            result.append(component.plainCopy().getString());
        }
        for (Component sibling : component.getSiblings()) {
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

    private record Tables(ResourceManager manager, Map<String, String> english, Map<String, String> russian) {
    }
}

