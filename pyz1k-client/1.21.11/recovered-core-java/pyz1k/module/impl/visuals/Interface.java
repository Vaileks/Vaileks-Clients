/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.eventbus.Subscribe
 *  net.minecraft.class_11908
 *  net.minecraft.class_310
 *  net.minecraft.class_408
 */
package pyz1k.module.impl.visuals;

import com.google.common.eventbus.Subscribe;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_408;
import pyz1k.Pyz1k;
import pyz1k.event.impl.EventScreenRender;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.impl.visuals.ItemCooldownsModule;
import pyz1k.module.impl.visuals.PotionList;
import pyz1k.module.impl.visuals.PvPCooldown;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.screen.HudElement;
import pyz1k.screen.screen.HudElementManager;
import pyz1k.screen.screen.elements.WatermarkHudElement;
import pyz1k.utilites.color.ClientTheme;

@ModuleInfo(name="Interface", desc="Watermark and HUD editor", category=Category.VISUALS, enabledByDefault=true)
public class Interface
extends Module {
    private static Interface INSTANCE;
    private final MultiSelectSetting watermarkContent = this.registerSetting(new MultiSelectSetting("\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0430", List.of("\u0412\u0440\u0435\u043c\u044f", "\u041f\u0438\u043d\u0433", "\u0424\u043f\u0441", "Ip"), new LinkedHashSet<String>(List.of("\u0412\u0440\u0435\u043c\u044f", "\u041f\u0438\u043d\u0433", "\u0424\u043f\u0441", "Ip"))));
    private final SelectSetting style = this.registerSetting(new SelectSetting("\u0421\u0442\u0438\u043b\u044c", "\u041a\u043b\u0438\u0435\u043d\u0442", List.of("\u041a\u043b\u0438\u0435\u043d\u0442", "\u0412\u0430\u043d\u0438\u043b\u043b\u0430")));
    private final BooleanSetting background = this.registerSetting(new BooleanSetting("\u0424\u043e\u043d", true));
    private final ColorSetting backgroundColor = this.registerSetting(new ColorSetting("\u0426\u0432\u0435\u0442 \u0444\u043e\u043d\u0430", -1509488629));
    private final HudElementManager hudElementManager = new HudElementManager();
    private boolean hudRendererRegistered;

    public Interface() {
        INSTANCE = this;
        this.hudElementManager.register(new WatermarkHudElement(this));
        Pyz1k pyz1k = Pyz1k.getInstance();
        if (pyz1k != null) {
            pyz1k.getEventBus().register((Object)this);
            this.hudRendererRegistered = true;
        }
    }

    @Override
    public void onEnable() {
        if (!this.hudRendererRegistered) {
            super.onEnable();
            this.hudRendererRegistered = true;
        }
    }

    @Override
    public void onDisable() {
    }

    public Set<String> getWatermarkContent() {
        LinkedHashSet<String> canonical = new LinkedHashSet<String>();
        Iterator<String> iterator = this.watermarkContent.getSelectedValues().iterator();
        block17: while (iterator.hasNext()) {
            String value;
            switch (value = iterator.next()) {
                case "\u0412\u0440\u0435\u043c\u044f": 
                case "Time": {
                    canonical.add("Time");
                    continue block17;
                }
                case "\u041f\u0438\u043d\u0433": 
                case "Ping": {
                    canonical.add("Ping");
                    continue block17;
                }
                case "\u0424\u043f\u0441": 
                case "FPS": {
                    canonical.add("FPS");
                    continue block17;
                }
                case "Ip": 
                case "IP": 
                case "Server IP": {
                    canonical.add("Server IP");
                    continue block17;
                }
            }
            canonical.add(value);
        }
        return canonical;
    }

    public boolean isClientStyle() {
        return "\u041a\u043b\u0438\u0435\u043d\u0442".equals(this.style.getValue());
    }

    public boolean isBackgroundEnabled() {
        return this.background.isValue();
    }

    public int getBackgroundColor() {
        return this.backgroundColor.getColor();
    }

    public List<HudElement> getHudElements() {
        return this.hudElementManager.getElements();
    }

    public static void registerExternalHudElement(HudElement element) {
        if (INSTANCE != null && element != null) {
            Interface.INSTANCE.hudElementManager.register(element);
        }
    }

    public static boolean isHudShadowEnabled() {
        return true;
    }

    public static float getHudShadowOpacity() {
        return 0.65f;
    }

    public static float getHudShadowSize() {
        return 7.0f;
    }

    public boolean isHudElementEnabled(String displayName) {
        return this.getVisibleElements().contains(displayName);
    }

    public boolean handleHudEditorKeyPressed(class_11908 key) {
        return this.hudElementManager.handleKeyPressed(key);
    }

    public boolean handleHudEditorCharTyped(int codePoint) {
        return this.hudElementManager.handleCharTyped(codePoint);
    }

    @Subscribe
    public void onRenderHud(EventScreenRender event) {
        if (event.getStage() != EventScreenRender.Stage.HUD) {
            return;
        }
        ClientTheme.updateDynamicColors();
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1755 instanceof MenuScreen) {
            return;
        }
        boolean editorEnabled = mc != null && mc.field_1755 instanceof class_408;
        this.hudElementManager.render(event.getContext(), event.getTickCounter().method_60637(false), this.getVisibleElements(), editorEnabled);
    }

    private Set<String> getVisibleElements() {
        Pyz1k pyz1k;
        LinkedHashSet<String> visible = new LinkedHashSet<String>();
        if (this.isEnable()) {
            visible.add("Watermark");
        }
        if ((pyz1k = Pyz1k.getInstance()) != null) {
            PotionList potionList;
            PvPCooldown pvpCooldown;
            ItemCooldownsModule itemCooldowns = pyz1k.getModuleManager().getModule(ItemCooldownsModule.class);
            if (itemCooldowns != null && itemCooldowns.isEnable()) {
                visible.add("Item Cooldowns");
            }
            if ((pvpCooldown = pyz1k.getModuleManager().getModule(PvPCooldown.class)) != null && pvpCooldown.isEnable()) {
                visible.add("PvP Cooldown");
            }
            if ((potionList = pyz1k.getModuleManager().getModule(PotionList.class)) != null && potionList.isEnable()) {
                visible.add("PotionList");
            }
        }
        return visible;
    }
}

