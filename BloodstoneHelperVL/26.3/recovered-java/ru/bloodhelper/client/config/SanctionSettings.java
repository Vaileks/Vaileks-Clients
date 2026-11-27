/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SanctionSettings {
    public boolean enabled;
    public boolean quickSanction;
    public boolean previewBan;
    public boolean previewMute;
    public boolean previewVoiceMute;
    public boolean duplicateGuard;
    public boolean commandResults;
    public int duplicateWindowMinutes = 60;
    public int confirmationTimeoutSeconds = 15;
    public String durationFormat = "COMPACT";
    public String permanentToken = "";
    public Map<String, String> commands = new LinkedHashMap<String, String>();
    public Map<String, String> removals = new LinkedHashMap<String, String>();
    public List<ReasonTemplate> reasons = new ArrayList<ReasonTemplate>();

    public void sanitize() {
        this.duplicateWindowMinutes = Math.clamp((long)this.duplicateWindowMinutes, 1, 10080);
        this.confirmationTimeoutSeconds = Math.clamp((long)this.confirmationTimeoutSeconds, 5, 60);
        if (!Set.of("COMPACT", "SECONDS", "SECONDS_SUFFIX", "MINUTES").contains(this.durationFormat == null ? "" : this.durationFormat)) {
            this.durationFormat = "COMPACT";
        }
        this.permanentToken = SanctionSettings.safe(this.permanentToken, 32);
        this.commands = SanctionSettings.cleanMap(this.commands);
        this.removals = SanctionSettings.cleanMap(this.removals);
        if (this.reasons == null) {
            this.reasons = new ArrayList<ReasonTemplate>();
        }
        ArrayList<ReasonTemplate> cleaned = new ArrayList<ReasonTemplate>();
        for (ReasonTemplate r : this.reasons) {
            if (r == null || cleaned.size() >= 100 || !Set.of("BAN", "MUTE", "VOICE_MUTE").contains(r.kind == null ? "" : r.kind)) continue;
            r.title = SanctionSettings.safe(r.title, 64);
            r.duration = SanctionSettings.safe(r.duration, 64);
            r.reason = SanctionSettings.safe(r.reason, 180);
            if (r.title.isEmpty()) continue;
            cleaned.add(r);
        }
        this.reasons = cleaned;
    }

    private static Map<String, String> cleanMap(Map<String, String> source) {
        LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
        if (source != null) {
            for (String key : List.of("BAN", "MUTE", "VOICE_MUTE")) {
                String value = SanctionSettings.safe(source.get(key), 256);
                if (value.isEmpty()) continue;
                out.put(key, value);
            }
        }
        return out;
    }

    private static String safe(String value, int length) {
        if (value == null || value.length() > length || value.codePoints().anyMatch(c -> Character.isISOControl(c) || Character.getType(c) == 16)) {
            return "";
        }
        return value.strip();
    }

    public static final class ReasonTemplate {
        public String title = "";
        public String kind = "BAN";
        public String duration = "";
        public String reason = "";

        public ReasonTemplate() {
        }

        public ReasonTemplate(String title, String kind, String duration, String reason) {
            this.title = title;
            this.kind = kind;
            this.duration = duration;
            this.reason = reason;
        }
    }
}

