/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.tiers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TierIcons {
    private static final Map<String, String> GLYPHS = new HashMap<String, String>();
    private static final Map<String, Set<String>> MODES;

    private TierIcons() {
    }

    public static Set<String> modes(String provider) {
        return MODES.getOrDefault(provider, Set.of());
    }

    public static String glyph(String provider, String mode) {
        if ("MC".equals(provider) && "nethpot".equals(mode)) {
            mode = "netherop";
        }
        return GLYPHS.getOrDefault(provider + ":" + mode, "");
    }

    static {
        GLYPHS.put("CIS:vanilla", "\ue500");
        GLYPHS.put("CIS:sword", "\ue501");
        GLYPHS.put("CIS:nethpot", "\ue502");
        GLYPHS.put("CIS:pot", "\ue503");
        GLYPHS.put("CIS:uhc", "\ue504");
        GLYPHS.put("CIS:smp", "\ue505");
        GLYPHS.put("CIS:netherop", "\ue506");
        GLYPHS.put("CIS:mace", "\ue507");
        GLYPHS.put("CIS:axe", "\ue508");
        GLYPHS.put("CIS:spearmace", "\ue509");
        GLYPHS.put("MY:sword", "\ue50a");
        GLYPHS.put("MY:netherop", "\ue50b");
        GLYPHS.put("MY:nethpot", "\ue50c");
        GLYPHS.put("MY:pot", "\ue50d");
        GLYPHS.put("MY:vanilla", "\ue50e");
        GLYPHS.put("MY:uhc", "\ue50f");
        GLYPHS.put("MY:mace", "\ue510");
        GLYPHS.put("MY:diaop", "\ue511");
        GLYPHS.put("MY:axe", "\ue512");
        GLYPHS.put("MY:smp", "\ue513");
        GLYPHS.put("A:axe", "\ue514");
        GLYPHS.put("A:cart", "\ue515");
        GLYPHS.put("A:creeper", "\ue516");
        GLYPHS.put("A:dsmp", "\ue517");
        GLYPHS.put("A:mace", "\ue518");
        GLYPHS.put("A:nethpot", "\ue519");
        GLYPHS.put("A:netherop", "\ue51a");
        GLYPHS.put("A:smp", "\ue51b");
        GLYPHS.put("A:smace", "\ue51c");
        GLYPHS.put("A:suhc", "\ue51d");
        GLYPHS.put("A:sword", "\ue51e");
        GLYPHS.put("A:uhc", "\ue51f");
        GLYPHS.put("A:vanilla", "\ue520");
        GLYPHS.put("MC:axe", "\ue521");
        GLYPHS.put("MC:mace", "\ue522");
        GLYPHS.put("MC:netherop", "\ue523");
        GLYPHS.put("MC:pot", "\ue524");
        GLYPHS.put("MC:smp", "\ue525");
        GLYPHS.put("MC:sword", "\ue526");
        GLYPHS.put("MC:uhc", "\ue527");
        GLYPHS.put("MC:vanilla", "\ue528");
        GLYPHS.put("PVP:axe", "\ue529");
        GLYPHS.put("PVP:vanilla", "\ue52a");
        GLYPHS.put("PVP:mace", "\ue52b");
        GLYPHS.put("PVP:nethpot", "\ue52c");
        GLYPHS.put("PVP:pot", "\ue52d");
        GLYPHS.put("PVP:smp", "\ue52e");
        GLYPHS.put("PVP:sword", "\ue52f");
        GLYPHS.put("PVP:uhc", "\ue530");
        MODES = new HashMap<String, Set<String>>();
        for (String key : GLYPHS.keySet()) {
            String[] parts = key.split(":", 2);
            MODES.computeIfAbsent(parts[0], ignored -> new HashSet()).add(parts[1]);
        }
        MODES.replaceAll((provider, modes) -> Set.copyOf(modes));
    }
}

