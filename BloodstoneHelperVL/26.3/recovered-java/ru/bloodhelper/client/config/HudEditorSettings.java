/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.config.HudEdgeAnchor;
import ru.bloodhelper.client.config.InventoryUiSettings;

public final class HudEditorSettings {
    public boolean showChatEditor = true;
    public boolean chatEditingEnabled = false;
    public InventoryUiSettings.Position chatPanel = new InventoryUiSettings.Position();
    public boolean snapping = true;
    public boolean guides = true;
    public int gap = 8;
    public boolean adaptive = false;
    public Map<String, HudEdgeAnchor> edgeAnchors = new LinkedHashMap<String, HudEdgeAnchor>();
    public Set<String> normalLocks = new LinkedHashSet<String>();
    public Set<String> combatLocks = new LinkedHashSet<String>();

    public Set<String> locks(boolean combat) {
        return combat ? this.combatLocks : this.normalLocks;
    }

    public void sanitize() {
        if (this.chatPanel == null) {
            this.chatPanel = new InventoryUiSettings.Position();
        }
        this.chatPanel.sanitize();
        this.gap = Math.clamp((long)this.gap, 0, 32);
        this.normalLocks = HudEditorSettings.clean(this.normalLocks);
        this.combatLocks = HudEditorSettings.clean(this.combatLocks);
        LinkedHashMap<String, HudEdgeAnchor> anchors = new LinkedHashMap<String, HudEdgeAnchor>();
        if (this.edgeAnchors != null) {
            for (Map.Entry<String, HudEdgeAnchor> entry : this.edgeAnchors.entrySet()) {
                if (anchors.size() >= 576) break;
                if (entry.getKey() == null || !entry.getKey().matches("(?:normal|combat):[A-Za-z0-9_.:/-]{1,160}") || entry.getValue() == null || !entry.getValue().valid()) continue;
                anchors.put(entry.getKey(), entry.getValue());
            }
        }
        this.edgeAnchors = anchors;
    }

    private static Set<String> clean(Set<String> keys) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if (keys != null) {
            for (String key : keys) {
                if (result.size() == 288) break;
                if (!CombatHudSettings.validKey(key)) continue;
                result.add(key);
            }
        }
        return result;
    }
}

