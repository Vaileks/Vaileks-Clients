/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.config.HudEdgeAnchor;
import ru.bloodhelper.client.config.HudEditorSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudEditHistory;
import ru.bloodhelper.client.gui.HudResize;
import ru.bloodhelper.client.gui.HudSnap;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.AdaptiveHudLayout;
import ru.bloodhelper.client.hud.HudLayout;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudWidgets;
import ru.bloodhelper.client.modules.CombatHudModule;

public final class HudInteraction {
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int TOP = 4;
    private static final int BOTTOM = 8;
    private final LinkedHashSet<String> selection = new LinkedHashSet();
    private final HudEditHistory<Map<String, Pose>> history = new HudEditHistory(50);
    private ClientConfig context;
    private Object combatSettings;
    private Object editorSettings;
    private boolean combat;
    private String contextArmorLayout;
    private String contextItemLayout;
    private List<String> contextItems = List.of();
    private int contextWidth;
    private int contextHeight;
    private Drag drag;
    private Marquee marquee;
    private List<HudSnap.Guide> guides = List.of();

    public String selected() {
        return this.selection.isEmpty() ? null : (String)this.selection.iterator().next();
    }

    public Set<String> selectedKeys() {
        return Collections.unmodifiableSet(this.selection);
    }

    public int selectedCount() {
        return this.selection.size();
    }

    public boolean active() {
        return this.drag != null || this.marquee != null;
    }

    public boolean canUndo() {
        return this.history.canUndo();
    }

    public boolean canRedo() {
        return this.history.canRedo();
    }

    public boolean allSelectedLocked() {
        if (this.selection.isEmpty()) {
            return false;
        }
        for (String key : this.selection) {
            if (this.locked(key)) continue;
            return false;
        }
        return true;
    }

    public boolean locked(String key) {
        return BloodHelperClient.config().hudEditor.locks(CombatHudModule.combatLayout(BloodHelperClient.config())).contains(key);
    }

    public void reset() {
        this.cancel();
        this.selection.clear();
        this.history.clear();
        this.context = null;
        this.contextItems = List.of();
    }

    public String widgetAt(double x, double y) {
        String found = null;
        for (Map.Entry<String, HudRenderer.Bounds> entry : HudRenderer.bounds().entrySet()) {
            HudRenderer.Bounds b = entry.getValue();
            if (!(x >= (double)(b.x() - 3)) || !(y >= (double)(b.y() - 3)) || !(x <= (double)(b.x() + b.width() + 3)) || !(y <= (double)(b.y() + b.height() + 3))) continue;
            found = entry.getKey();
        }
        return found;
    }

    private static int edges(HudRenderer.Bounds b, double x, double y) {
        int result = 0;
        if (Math.abs(x - (double)b.x()) <= 3.0) {
            result |= 1;
        } else if (Math.abs(x - (double)b.x() - (double)b.width()) <= 3.0) {
            result |= 2;
        }
        if (Math.abs(y - (double)b.y()) <= 3.0) {
            result |= 4;
        } else if (Math.abs(y - (double)b.y() - (double)b.height()) <= 3.0) {
            result |= 8;
        }
        return result;
    }

    public boolean click(MouseButtonEvent event, boolean musicControls) {
        boolean musicCenter;
        if (event.button() != 1 || !this.sync()) {
            return false;
        }
        this.finish();
        String key = this.widgetAt(event.x(), event.y());
        if (key == null) {
            if (!event.hasShiftDown()) {
                this.selection.clear();
            }
            if (musicControls && event.y() > (double)(Minecraft.getInstance().getWindow().getGuiScaledHeight() - 32)) {
                return false;
            }
            this.marquee = new Marquee(event.x(), event.y(), event.x(), event.y(), Set.copyOf(this.selection));
            return true;
        }
        HudRenderer.Bounds b = HudRenderer.bounds().get(key);
        int edge = HudInteraction.edges(b, event.x(), event.y());
        if (event.hasShiftDown()) {
            if (!this.selection.remove(key)) {
                this.selection.add(key);
            }
            return true;
        }
        double musicSide = Math.min((double)b.width() * 0.28, Math.max(14.0, (double)b.height() * 0.65));
        boolean bl = musicCenter = event.x() >= (double)b.x() + musicSide && event.x() < (double)(b.x() + b.width()) - musicSide;
        if (edge == 0 && musicControls && "music".equals(key) && musicCenter && HudRenderer.clickMusic(event.x(), event.y(), event.button())) {
            return true;
        }
        if (!this.selection.contains(key)) {
            this.selection.clear();
            this.selection.add(key);
        }
        if (this.locked(key)) {
            return true;
        }
        LinkedHashMap<String, HudRenderer.Bounds> boxes = new LinkedHashMap<String, HudRenderer.Bounds>();
        if (edge != 0) {
            boxes.put(key, b);
        } else {
            for (String selected : this.selection) {
                if (this.locked(selected) || !HudRenderer.bounds().containsKey(selected)) continue;
                boxes.put(selected, HudRenderer.bounds().get(selected));
            }
        }
        if (boxes.isEmpty()) {
            return true;
        }
        this.materialize(boxes.keySet());
        LinkedHashMap<String, ClientConfig.HudPosition> targets = new LinkedHashMap<String, ClientConfig.HudPosition>();
        for (String target : boxes.keySet()) {
            targets.put(target, this.widget(target).position());
        }
        this.drag = new Drag(key, Map.copyOf(boxes), HudInteraction.union(boxes.values()), this.snapshot(boxes.keySet()), Map.copyOf(targets), event.x(), event.y(), edge);
        return true;
    }

    public boolean drag(MouseButtonEvent event, int screenWidth, int screenHeight) {
        if (!this.active() || event.button() != 1) {
            return false;
        }
        if (!this.sync()) {
            return true;
        }
        if (this.marquee != null) {
            this.marquee = new Marquee(this.marquee.x, this.marquee.y, event.x(), event.y(), this.marquee.before);
            this.selection.clear();
            this.selection.addAll(this.marquee.before);
            double x = Math.min(this.marquee.x, event.x());
            double y = Math.min(this.marquee.y, event.y());
            double right = Math.max(this.marquee.x, event.x());
            double bottom = Math.max(this.marquee.y, event.y());
            for (Map.Entry<String, HudRenderer.Bounds> e : HudRenderer.bounds().entrySet()) {
                HudRenderer.Bounds b = e.getValue();
                if (!((double)b.x() < right) || !((double)(b.x() + b.width()) > x) || !((double)b.y() < bottom) || !((double)(b.y() + b.height()) > y)) continue;
                this.selection.add(e.getKey());
            }
            return true;
        }
        if (this.drag == null) {
            return true;
        }
        if (this.drag.edges == 0) {
            ArrayList<HudSnap.Box> neighbors = new ArrayList<HudSnap.Box>();
            for (Map.Entry<String, HudRenderer.Bounds> e : HudRenderer.bounds().entrySet()) {
                if (this.drag.boxes.containsKey(e.getKey())) continue;
                neighbors.add(HudInteraction.box(e.getValue()));
            }
            HudEditorSettings settings = this.context.hudEditor;
            HudSnap.Result result = HudSnap.move(this.drag.group, event.x() - this.drag.x, event.y() - this.drag.y, neighbors, screenWidth, screenHeight, settings.snapping && !event.hasAltDown(), settings.gap);
            this.guides = result.guides();
            for (Map.Entry<String, HudRenderer.Bounds> e : this.drag.boxes.entrySet()) {
                HudRenderer.Bounds b = e.getValue();
                this.anchor(e.getKey(), (double)b.x() + result.dx(), (double)b.y() + result.dy(), b.width(), b.height(), screenWidth, screenHeight);
            }
        } else {
            HudRenderer.Bounds b = this.drag.boxes.get(this.drag.key);
            Pose initial = this.drag.before.get(this.drag.key);
            HudResize.Result result = HudResize.resize(b.x(), b.y(), b.width(), b.height(), initial.scale, this.drag.edges, event.x() - this.drag.x, event.y() - this.drag.y, screenWidth, screenHeight);
            this.widget((String)this.drag.key).position().scale = result.scale();
            this.anchor(this.drag.key, result.x(), result.y(), result.width(), result.height(), screenWidth, screenHeight);
        }
        return true;
    }

    public boolean release(MouseButtonEvent event) {
        if (!this.active() || event.button() != 1) {
            return false;
        }
        this.finish();
        return true;
    }

    public void finish() {
        if (this.drag != null) {
            if (this.contextMatches()) {
                this.commit(this.drag.before);
            } else {
                this.rollbackDrag();
            }
        }
        this.drag = null;
        this.marquee = null;
        this.guides = List.of();
    }

    private void cancel() {
        this.rollbackDrag();
        this.drag = null;
        this.marquee = null;
        this.guides = List.of();
    }

    private void rollbackDrag() {
        if (this.drag != null) {
            for (Map.Entry<String, Pose> e : this.drag.before.entrySet()) {
                ClientConfig.HudPosition p = this.drag.targets.get(e.getKey());
                Pose before = e.getValue();
                p.x = before.x;
                p.y = before.y;
                p.scale = before.scale;
                HudInteraction.restoreAnchor(e.getKey(), before.anchor, this.context, this.combat);
            }
        }
    }

    public boolean scroll(double x, double y, double amount) {
        if (this.active() || amount == 0.0 || !this.sync()) {
            return false;
        }
        String key = this.widgetAt(x, y);
        if (key == null) {
            return false;
        }
        if (!this.selection.contains(key)) {
            this.selection.clear();
            this.selection.add(key);
        }
        if (this.locked(key)) {
            return true;
        }
        this.materialize(Set.of(key));
        Map<String, Pose> before = this.snapshot(Set.of(key));
        ClientConfig.HudPosition p = this.widget(key).position();
        p.scale = Math.clamp(p.scale + amount * 0.05 * 0.65, 0.35, 2.0);
        this.commit(before);
        return true;
    }

    public void toggleLock() {
        boolean lock;
        if (!this.sync() || this.selection.isEmpty()) {
            return;
        }
        this.finish();
        Map<String, Pose> before = this.snapshot(this.selection);
        boolean bl = lock = !this.allSelectedLocked();
        if (lock) {
            this.context.hudEditor.locks(this.combat).addAll(this.selection);
        } else {
            this.context.hudEditor.locks(this.combat).removeAll(this.selection);
        }
        this.commit(before);
    }

    public void toggleSnapping() {
        HudEditorSettings s = BloodHelperClient.config().hudEditor;
        s.snapping = !s.snapping;
        BloodHelperClient.saveConfig();
    }

    public void toggleGuides() {
        HudEditorSettings s = BloodHelperClient.config().hudEditor;
        s.guides = !s.guides;
        BloodHelperClient.saveConfig();
    }

    public void toggleAdaptive() {
        this.finish();
        HudEditorSettings s = BloodHelperClient.config().hudEditor;
        s.adaptive = !s.adaptive;
        s.edgeAnchors.clear();
        BloodHelperClient.saveConfig();
    }

    public void cycleGap() {
        HudEditorSettings s = BloodHelperClient.config().hudEditor;
        s.gap = s.gap >= 16 ? 0 : s.gap + 4;
        BloodHelperClient.saveConfig();
    }

    public void undo() {
        if (!this.sync()) {
            return;
        }
        this.finish();
        Map<String, Pose> state = this.history.undo();
        if (state != null) {
            HudInteraction.restore(state, this.context, this.combat);
            BloodHelperClient.saveConfig();
        }
    }

    public void redo() {
        if (!this.sync()) {
            return;
        }
        this.finish();
        Map<String, Pose> state = this.history.redo();
        if (state != null) {
            HudInteraction.restore(state, this.context, this.combat);
            BloodHelperClient.saveConfig();
        }
    }

    public boolean disableSelected() {
        if (!this.sync() || this.selection.isEmpty()) {
            return false;
        }
        this.finish();
        LinkedHashSet<String> keys = new LinkedHashSet<String>(this.selection);
        keys.removeIf(this::locked);
        if (keys.isEmpty()) {
            return true;
        }
        Map<String, Pose> before = this.snapshot(keys);
        for (String key : keys) {
            HudWidgets.setEnabled(key, this.context, false);
        }
        this.commit(before);
        this.selection.clear();
        return true;
    }

    public boolean keyPressed(KeyEvent event, boolean chat) {
        if (!this.sync()) {
            return false;
        }
        if (event.key() == 76 && !event.hasControlDown() && !this.selection.isEmpty()) {
            return this.disableSelected();
        }
        boolean control = event.hasControlDown();
        if (control && event.key() == 29) {
            if (event.hasShiftDown()) {
                this.redo();
            } else {
                this.undo();
            }
            return true;
        }
        if (control && event.key() == 28) {
            this.redo();
            return true;
        }
        if (event.key() == 15 && (!chat || control)) {
            this.toggleLock();
            return true;
        }
        if (chat) {
            return false;
        }
        if (control && event.key() == 4) {
            this.finish();
            this.selection.clear();
            this.selection.addAll(HudRenderer.bounds().keySet());
            return true;
        }
        if (event.key() == 21) {
            this.resetPositions();
            return true;
        }
        if (event.key() == 80 || event.key() == 79 || event.key() == 82 || event.key() == 81) {
            int step;
            int n = step = event.hasShiftDown() ? 10 : 1;
            this.nudge(event.key() == 80 ? -step : (event.key() == 79 ? step : 0), event.key() == 82 ? -step : (event.key() == 81 ? step : 0));
            return true;
        }
        return false;
    }

    private void nudge(int dx, int dy) {
        this.finish();
        LinkedHashMap<String, HudRenderer.Bounds> boxes = new LinkedHashMap<String, HudRenderer.Bounds>();
        for (String key : this.selection) {
            if (this.locked(key)) continue;
            boxes.put(key, HudRenderer.bounds().get(key));
        }
        if (boxes.isEmpty()) {
            return;
        }
        this.materialize(boxes.keySet());
        Map<String, Pose> before = this.snapshot(boxes.keySet());
        HudSnap.Result result = HudSnap.move(HudInteraction.union(boxes.values()), dx, dy, List.of(), this.contextWidth, this.contextHeight, false, 0);
        for (Map.Entry e : boxes.entrySet()) {
            HudRenderer.Bounds b = (HudRenderer.Bounds)e.getValue();
            this.anchor((String)e.getKey(), (double)b.x() + result.dx(), (double)b.y() + result.dy(), b.width(), b.height(), this.contextWidth, this.contextHeight);
        }
        this.commit(before);
    }

    public void resetPositions() {
        if (!this.sync()) {
            return;
        }
        this.finish();
        LinkedHashSet<String> keys = new LinkedHashSet<String>(this.selection.isEmpty() ? HudRenderer.bounds().keySet() : this.selection);
        keys.removeIf(this::locked);
        if (keys.isEmpty()) {
            return;
        }
        Map<String, Pose> before = this.snapshot(keys);
        ClientConfig defaults = new ClientConfig();
        defaults.armor.layout = this.context.armor.layout;
        defaults.itemCounter.layout = this.context.itemCounter.layout;
        defaults.itemCounter.items = new ArrayList<String>(this.context.itemCounter.items);
        for (String key : keys) {
            this.context.hudEditor.edgeAnchors.remove(AdaptiveHudLayout.key(key, this.combat));
            HudWidgets.Widget normal = HudWidgets.get(key, defaults);
            ClientConfig.HudPosition p = normal.position();
            String alignment = normal.alignment();
            boolean bottom = normal.bottom();
            if (this.combat) {
                CombatHudSettings.Pose pose = CombatHudSettings.initialPose(key, p, alignment, bottom);
                p = pose.position;
                alignment = pose.alignment;
                bottom = pose.bottom;
            }
            HudInteraction.apply(key, new Pose(p.x, p.y, p.scale, alignment, bottom, false, HudWidgets.activation(key, this.context), null), this.context, this.combat);
        }
        this.commit(before);
    }

    public void recoverPositions() {
        if (!this.sync()) {
            return;
        }
        this.finish();
        LinkedHashSet<String> keys = new LinkedHashSet<String>(this.selection.isEmpty() ? HudRenderer.bounds().keySet() : this.selection);
        keys.removeIf(this::locked);
        Map<String, Pose> before = this.snapshot(keys);
        for (String key : keys) {
            HudRenderer.Bounds b = HudRenderer.bounds().get(key);
            if (b == null) continue;
            this.anchor(key, Math.clamp((long)b.x(), 0, Math.max(0, this.contextWidth - b.width())), Math.clamp((long)b.y(), 0, Math.max(0, this.contextHeight - b.height())), b.width(), b.height(), this.contextWidth, this.contextHeight);
            this.context.hudEditor.edgeAnchors.remove(AdaptiveHudLayout.key(key, this.combat));
        }
        this.commit(before);
        BloodHelperClient.saveConfig();
    }

    private void materialize(Set<String> keys) {
        if (!this.context.hudEditor.adaptive) {
            return;
        }
        for (String key : keys) {
            HudRenderer.Bounds b = HudRenderer.bounds().get(key);
            if (b == null) continue;
            this.anchor(key, b.x(), b.y(), b.width(), b.height(), this.contextWidth, this.contextHeight);
            HudWidgets.Widget w = this.widget(key);
            ClientConfig.HudPosition p = w.position();
            this.context.hudEditor.edgeAnchors.remove(AdaptiveHudLayout.key(key, this.combat));
            AdaptiveHudLayout.place(this.context.hudEditor, key, this.combat, p, w.alignment(), w.bottom(), new HudLayout.Placement(b.x(), b.y(), b.width(), b.height(), p.scale), this.contextWidth, this.contextHeight);
        }
    }

    private void commit(Map<String, Pose> before) {
        Map<String, Pose> after = this.snapshot(before.keySet());
        if (!before.equals(after)) {
            this.history.record(before, after);
            BloodHelperClient.saveConfig();
        }
    }

    private HudWidgets.Widget widget(String key) {
        return HudInteraction.widget(key, this.context, this.combat);
    }

    private static HudWidgets.Widget widget(String key, ClientConfig c, boolean combat) {
        HudWidgets.Widget normal = HudWidgets.get(key, c);
        if (!combat) {
            return normal;
        }
        CombatHudSettings.Pose pose = c.combatHud.ensure(key, normal.position(), normal.alignment(), normal.bottom());
        return new HudWidgets.Widget(key, normal.title(), pose.position, pose.alignment, pose.bottom);
    }

    private Map<String, Pose> snapshot(Set<String> keys) {
        LinkedHashMap<String, Pose> result = new LinkedHashMap<String, Pose>();
        for (String key : keys) {
            HudWidgets.Widget w = this.widget(key);
            ClientConfig.HudPosition p = w.position();
            HudEdgeAnchor a = this.context.hudEditor.edgeAnchors.get(AdaptiveHudLayout.key(key, this.combat));
            result.put(key, new Pose(p.x, p.y, p.scale, w.alignment(), w.bottom(), this.context.hudEditor.locks(this.combat).contains(key), HudWidgets.activation(key, this.context), a == null ? null : a.copy()));
        }
        return Map.copyOf(result);
    }

    private static void restore(Map<String, Pose> values, ClientConfig c, boolean combat) {
        LinkedHashSet<String> available = new LinkedHashSet<String>();
        for (HudWidgets.Widget widget : HudWidgets.all(c)) {
            available.add(widget.key());
        }
        for (Map.Entry entry : values.entrySet()) {
            if (!available.contains(entry.getKey())) continue;
            HudInteraction.apply((String)entry.getKey(), (Pose)entry.getValue(), c, combat);
        }
    }

    private static void apply(String key, Pose value, ClientConfig c, boolean combat) {
        HudWidgets.restoreActivation(key, c, value.activation);
        HudWidgets.Widget w = HudInteraction.widget(key, c, combat);
        ClientConfig.HudPosition p = w.position();
        p.x = value.x;
        p.y = value.y;
        p.scale = value.scale;
        HudInteraction.restoreAnchor(key, value.anchor, c, combat);
        if (combat) {
            CombatHudSettings.Pose pose = c.combatHud.layout.get(key);
            pose.alignment = value.alignment;
            pose.bottom = value.bottom;
        } else if (key.equals("cooldowns")) {
            c.cooldowns.alignment = value.alignment;
            c.cooldowns.bottom = value.bottom;
        } else if (key.equals("pvp")) {
            c.pvp.alignment = value.alignment;
            c.pvp.bottom = value.bottom;
        } else {
            ClientConfig.WidgetSettings settings;
            switch (key) {
                case "armor": {
                    ClientConfig.WidgetSettings widgetSettings = c.armor;
                    break;
                }
                case "itemCounter": {
                    ClientConfig.WidgetSettings widgetSettings = c.itemCounter;
                    break;
                }
                case "notifications": {
                    ClientConfig.WidgetSettings widgetSettings = c.notifications;
                    break;
                }
                case "effects": {
                    ClientConfig.WidgetSettings widgetSettings = c.effects;
                    break;
                }
                case "watermark": {
                    ClientConfig.WidgetSettings widgetSettings = c.watermark;
                    break;
                }
                case "performance": {
                    ClientConfig.WidgetSettings widgetSettings = c.performance;
                    break;
                }
                case "session": {
                    ClientConfig.WidgetSettings widgetSettings = c.session;
                    break;
                }
                case "spectatorTimer": {
                    ClientConfig.WidgetSettings widgetSettings = c.spectatorTimer;
                    break;
                }
                case "staffStats": {
                    ClientConfig.WidgetSettings widgetSettings = c.staffStats;
                    break;
                }
                case "waypoints": {
                    ClientConfig.WidgetSettings widgetSettings = c.waypoints;
                    break;
                }
                default: {
                    ClientConfig.WidgetSettings widgetSettings = settings = null;
                }
            }
            if (settings != null) {
                settings.alignment = value.alignment;
                settings.bottom = value.bottom;
            }
        }
        if (value.locked) {
            c.hudEditor.locks(combat).add(key);
        } else {
            c.hudEditor.locks(combat).remove(key);
        }
    }

    private static void restoreAnchor(String key, HudEdgeAnchor anchor, ClientConfig c, boolean combat) {
        String id = AdaptiveHudLayout.key(key, combat);
        if (anchor == null) {
            c.hudEditor.edgeAnchors.remove(id);
        } else {
            c.hudEditor.edgeAnchors.put(id, anchor.copy());
        }
    }

    private void anchor(String key, double x, double y, double w, double h, int sw, int sh) {
        HudWidgets.Widget widget = this.widget(key);
        ClientConfig.HudPosition p = widget.position();
        p.x = (x + ("CENTER".equals(widget.alignment()) ? w / 2.0 : ("RIGHT".equals(widget.alignment()) ? w : 0.0))) / (double)Math.max(1, sw);
        p.y = widget.bottom() ? 1.0 - (y + h) / (double)Math.max(1, sh) : y / (double)Math.max(1, sh);
    }

    private boolean contextMatches() {
        ClientConfig c = BloodHelperClient.config();
        Window window = Minecraft.getInstance().getWindow();
        return this.context == c && this.combatSettings == c.combatHud && this.editorSettings == c.hudEditor && this.combat == CombatHudModule.combatLayout(c) && Objects.equals(this.contextArmorLayout, c.armor.layout) && Objects.equals(this.contextItemLayout, c.itemCounter.layout) && this.contextItems.equals(c.itemCounter.items) && this.contextWidth == window.getGuiScaledWidth() && this.contextHeight == window.getGuiScaledHeight();
    }

    private boolean sync() {
        ClientConfig c = BloodHelperClient.config();
        boolean activeCombat = CombatHudModule.combatLayout(c);
        boolean matches = this.contextMatches();
        if (!matches) {
            this.cancel();
            this.selection.clear();
            this.history.clear();
            this.context = c;
            this.combatSettings = c.combatHud;
            this.editorSettings = c.hudEditor;
            this.combat = activeCombat;
            this.contextArmorLayout = c.armor.layout;
            this.contextItemLayout = c.itemCounter.layout;
            this.contextItems = List.copyOf(c.itemCounter.items);
            Window window = Minecraft.getInstance().getWindow();
            this.contextWidth = window.getGuiScaledWidth();
            this.contextHeight = window.getGuiScaledHeight();
        }
        if (this.drag != null && !HudRenderer.bounds().keySet().containsAll(this.drag.boxes.keySet())) {
            this.cancel();
        }
        this.selection.removeIf(key -> !HudRenderer.bounds().containsKey(key) || !HudWidgets.enabled(key, c));
        return HudRenderer.renderedCombatLayout() == activeCombat;
    }

    private static HudSnap.Box box(HudRenderer.Bounds b) {
        return new HudSnap.Box(b.x(), b.y(), b.width(), b.height());
    }

    private static HudSnap.Box union(Iterable<HudRenderer.Bounds> boxes) {
        double x;
        double y = x = Double.MAX_VALUE;
        double right = -x;
        double bottom = -x;
        for (HudRenderer.Bounds b : boxes) {
            x = Math.min(x, (double)b.x());
            y = Math.min(y, (double)b.y());
            right = Math.max(right, (double)(b.x() + b.width()));
            bottom = Math.max(bottom, (double)(b.y() + b.height()));
        }
        return new HudSnap.Box(x, y, right - x, bottom - y);
    }

    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean chat) {
        if (!this.sync()) {
            return;
        }
        String hot = this.widgetAt(mouseX, mouseY);
        int accent = ThemeColors.icon(BloodHelperClient.config().accent);
        if (this.context.hudEditor.guides) {
            for (HudSnap.Guide guide : this.guides) {
                int p = (int)Math.round(guide.coordinate());
                int from = (int)Math.round(guide.from());
                int to = (int)Math.round(guide.to());
                int color = UiDraw.alpha(accent, 150);
                if (guide.vertical()) {
                    g.fill(p, from, p + 1, to, color);
                } else {
                    g.fill(from, p, to, p + 1, color);
                }
                if (!(guide.gap() >= 0.0)) continue;
                String label = Math.round(guide.gap()) + " px";
                CustomFont.medium().draw(g, label, guide.vertical() ? (float)(p + 4) : (float)(from + 4), guide.vertical() ? (float)(from + 2) : (float)(p + 3), 7.0f, accent);
            }
        }
        for (Map.Entry entry : HudRenderer.bounds().entrySet()) {
            int color;
            boolean selected = this.selection.contains(entry.getKey());
            boolean hovered = ((String)entry.getKey()).equals(hot);
            boolean locked = this.locked((String)entry.getKey());
            if (chat && !selected && !hovered) continue;
            HudRenderer.Bounds b = (HudRenderer.Bounds)entry.getValue();
            int n = selected ? UiDraw.alpha(accent, 220) : (color = hovered ? -808595238 : 1718383232);
            if (selected || hovered) {
                g.outline(b.x() - 1, b.y() - 1, b.width() + 2, b.height() + 2, color);
            }
            if (locked) {
                HudInteraction.drawLock(g, b.x() + b.width() - 10, b.y() + 2, color);
                continue;
            }
            for (int dx : new int[]{0, b.width()}) {
                for (int dy : new int[]{0, b.height()}) {
                    if (selected || hovered) {
                        UiDraw.rounded(g, b.x() + dx - 2, b.y() + dy - 2, 4, 4, 1, color);
                        continue;
                    }
                    g.fill(b.x() + dx - 2, b.y() + dy, b.x() + dx + 3, b.y() + dy + 1, color);
                    g.fill(b.x() + dx, b.y() + dy - 2, b.x() + dx + 1, b.y() + dy + 3, color);
                }
            }
        }
        if (this.marquee != null) {
            int x = (int)Math.min(this.marquee.x, this.marquee.endX);
            int n = (int)Math.min(this.marquee.y, this.marquee.endY);
            int w = (int)Math.abs(this.marquee.endX - this.marquee.x);
            int h = (int)Math.abs(this.marquee.endY - this.marquee.y);
            g.fill(x, n, x + w, n + h, UiDraw.alpha(accent, 22));
            g.outline(x, n, w, h, UiDraw.alpha(accent, 180));
        }
    }

    private static void drawLock(GuiGraphicsExtractor g, int x, int y, int color) {
        UiDraw.outline(g, x + 2, y, 5, 7, 2, color);
        UiDraw.rounded(g, x, y + 5, 9, 6, 2, color);
    }

    private record Drag(String key, Map<String, HudRenderer.Bounds> boxes, HudSnap.Box group, Map<String, Pose> before, Map<String, ClientConfig.HudPosition> targets, double x, double y, int edges) {
    }

    private record Marquee(double x, double y, double endX, double endY, Set<String> before) {
    }

    private record Pose(double x, double y, double scale, String alignment, boolean bottom, boolean locked, HudWidgets.Activation activation, HudEdgeAnchor anchor) {
    }
}

