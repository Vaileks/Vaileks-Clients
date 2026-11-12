/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BindManagerModel {
    private static final Set<String> DEBUG_CHORDS = Set.of("key.debug.clearChat", "key.debug.copyLocation", "key.debug.copyRecreateCommand", "key.debug.crash", "key.debug.debugOptions", "key.debug.dumpDynamicTextures", "key.debug.dumpVersion", "key.debug.focusPause", "key.debug.fpsCharts", "key.debug.networkCharts", "key.debug.profiling", "key.debug.profilingChart", "key.debug.reloadChunk", "key.debug.reloadResourcePacks", "key.debug.showAdvancedTooltips", "key.debug.showChunkBorders", "key.debug.showHitboxes", "key.debug.spectate", "key.debug.switchGameMode");

    private BindManagerModel() {
    }

    public static boolean debugChord(String id) {
        return DEBUG_CHORDS.contains(id);
    }

    public static Map<String, List<String>> conflicts(List<Binding> bindings) {
        Binding modifier = bindings.stream().filter(binding -> binding.id().equals("key.debug.modifier")).findFirst().orElse(null);
        HashMap<String, List> byKey = new HashMap<String, List>();
        for (Binding binding2 : bindings) {
            if (binding2.unbound() || BindManagerModel.debugChord(binding2.id()) && modifier != null && modifier.unbound()) continue;
            String key = BindManagerModel.debugChord(binding2.id()) && modifier != null ? modifier.key() + "+" + binding2.key() : binding2.key();
            byKey.computeIfAbsent(key, ignored -> new ArrayList()).add(binding2);
        }
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        for (List group : byKey.values()) {
            for (Binding binding3 : group) {
                List<String> others = group.stream().filter(other -> !other.id().equals(binding3.id()) && !BindManagerModel.intentionalNativePair(binding3, other)).map(Binding::id).toList();
                if (others.isEmpty()) continue;
                result.put(binding3.id(), others);
            }
        }
        return Map.copyOf(result);
    }

    private static boolean intentionalNativePair(Binding first, Binding second) {
        if (Set.of("key.debug.modifier", "key.debug.overlay").contains(first.id()) && Set.of("key.debug.modifier", "key.debug.overlay").contains(second.id())) {
            return true;
        }
        return first.isDefault() && second.isDefault() && Set.of("key.debug.copyLocation", "key.debug.crash").contains(first.id()) && Set.of("key.debug.copyLocation", "key.debug.crash").contains(second.id());
    }

    public static List<Binding> filter(List<Binding> all, Map<String, List<String>> conflicts, Filter filter, String query) {
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return all.stream().filter(binding -> switch (filter.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> binding.bloodhelper();
            case 1 -> true;
            case 2 -> conflicts.containsKey(binding.id());
        }).filter(binding -> needle.isEmpty() || BindManagerModel.contains(binding.label(), needle) || BindManagerModel.contains(binding.category(), needle) || BindManagerModel.contains(binding.keyLabel(), needle) || BindManagerModel.contains(binding.id(), needle)).toList();
    }

    private static boolean contains(String value, String needle) {
        return value.toLowerCase(Locale.ROOT).contains(needle);
    }

    public record Binding(String id, String label, String category, String key, String defaultKey, String keyLabel, boolean bloodhelper, boolean unbound) {
        public boolean isDefault() {
            return this.key.equals(this.defaultKey);
        }
    }

    public static enum Filter {
        BLOODHELPER,
        ALL,
        CONFLICTS;

    }
}

