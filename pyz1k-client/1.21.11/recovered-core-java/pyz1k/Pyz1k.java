/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.eventbus.EventBus
 *  com.google.common.eventbus.Subscribe
 *  lombok.Generated
 *  net.fabricmc.api.ModInitializer
 *  net.minecraft.class_310
 */
package pyz1k;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Generated;
import net.fabricmc.api.ModInitializer;
import net.minecraft.class_310;
import pyz1k.config.ConfigManager;
import pyz1k.event.impl.EventKeyPress;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleManager;
import pyz1k.module.bind.BindData;
import pyz1k.module.bind.BindMode;
import pyz1k.module.impl.visuals.Menu;
import pyz1k.module.setting.AbstractSetting;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.InputSetting;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.Setting;
import pyz1k.module.setting.SliderSetting;
import pyz1k.render.renderers.pipeline.PipelineInitializer;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.utilites.color.ClientTheme;

public class Pyz1k
implements ModInitializer {
    public static final int MOUSE_BIND_CODE_BASE = 1000;
    private static Pyz1k instance;
    private final EventBus eventBus;
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final Set<Integer> pressedKeys = new HashSet<Integer>();
    private final IdentityHashMap<BindData, Boolean> comboState = new IdentityHashMap();
    private final IdentityHashMap<BindData, Boolean> holdOffRestoreState = new IdentityHashMap();
    private final IdentityHashMap<BindData, Float> sliderRestoreState = new IdentityHashMap();
    private final IdentityHashMap<BindData, String> selectRestoreState = new IdentityHashMap();
    private final IdentityHashMap<BindData, Set<String>> multiRestoreState = new IdentityHashMap();
    private final IdentityHashMap<BindData, String> inputRestoreState = new IdentityHashMap();
    private final IdentityHashMap<BindData, Integer> colorRestoreState = new IdentityHashMap();
    private final Set<Integer> menuScreenPressedKeys = new HashSet<Integer>();
    private boolean menuScreenBindDown;

    public void onInitialize() {
        PipelineInitializer.init();
        KillEffectEngine.init();
    }

    public Pyz1k() {
        instance = this;
        this.eventBus = new EventBus();
        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager(this.moduleManager);
        ClientTheme.syncFromSetting();
        this.eventBus.register((Object)this);
    }

    @Subscribe
    public void onPress(EventKeyPress e) {
        class_310 client = class_310.method_1551();
        if (client != null && client.field_1755 != null) {
            if (client.field_1755 instanceof MenuScreen) {
                this.processMenuBindWhileScreenOpen(e);
            } else {
                this.resetMenuScreenBindState();
            }
            this.pressedKeys.clear();
            this.comboState.clear();
            this.configManager.flushAutoSave();
        } else {
            this.resetMenuScreenBindState();
            if (e.getAction() == 0) {
                this.pressedKeys.remove(e.getKey());
            } else if (e.getAction() == 1 || e.getAction() == 2) {
                this.pressedKeys.add(e.getKey());
            }
            if (e.getAction() == 1) {
                for (Module module : this.moduleManager.getModules()) {
                    if (module.getBind().hasKeys() || module.getKey() != e.getKey() || module instanceof Menu && this.isAltDown()) continue;
                    module.toggle();
                }
            }
            this.processModuleBinds();
            this.processSettingBinds();
            this.configManager.flushAutoSave();
        }
    }

    private void processMenuBindWhileScreenOpen(EventKeyPress e) {
        Menu menuModule = this.moduleManager.getModule(Menu.class);
        if (menuModule != null) {
            if (e.getAction() == 0) {
                this.menuScreenPressedKeys.remove(e.getKey());
            } else if (e.getAction() == 1 || e.getAction() == 2) {
                this.menuScreenPressedKeys.add(e.getKey());
            }
            BindData bind = menuModule.getBind();
            if (!bind.hasKeys()) {
                if (e.getAction() == 1 && menuModule.getKey() == e.getKey()) {
                    ((Module)menuModule).toggle();
                }
            } else {
                boolean down = this.menuScreenPressedKeys.containsAll(bind.getKeys());
                if (down && !this.menuScreenBindDown && (e.getAction() == 1 || e.getAction() == 2)) {
                    ((Module)menuModule).toggle();
                }
                this.menuScreenBindDown = down;
            }
        }
    }

    private void resetMenuScreenBindState() {
        if (!this.menuScreenPressedKeys.isEmpty() || this.menuScreenBindDown) {
            this.menuScreenPressedKeys.clear();
            this.menuScreenBindDown = false;
        }
    }

    public void markConfigDirty() {
        if (this.configManager != null) {
            this.configManager.markDirty();
        }
    }

    public static int mouseButtonToBindCode(int mouseButton) {
        return 1000 + mouseButton;
    }

    public static boolean isMouseBindCode(int code) {
        return code >= 1000;
    }

    public static int bindCodeToMouseButton(int code) {
        return code - 1000;
    }

    private boolean isAltDown() {
        return this.pressedKeys.contains(342) || this.pressedKeys.contains(346);
    }

    private void processModuleBinds() {
        for (Module module : this.moduleManager.getModules()) {
            BindData bind = module.getBind();
            if (!bind.hasKeys()) {
                this.comboState.remove(bind);
                this.holdOffRestoreState.remove(bind);
                this.sliderRestoreState.remove(bind);
                this.selectRestoreState.remove(bind);
                this.multiRestoreState.remove(bind);
                this.inputRestoreState.remove(bind);
                this.colorRestoreState.remove(bind);
                continue;
            }
            boolean down = this.pressedKeys.containsAll(bind.getKeys());
            boolean prevDown = this.comboState.getOrDefault(bind, false);
            if (bind.getMode() == BindMode.HOLD) {
                if (down && !prevDown) {
                    boolean wasEnabled = module.isEnable();
                    this.holdOffRestoreState.put(bind, wasEnabled);
                    module.setEnable(!wasEnabled);
                    bind.setActive(true);
                } else if (!down && prevDown) {
                    Boolean restore = this.holdOffRestoreState.get(bind);
                    if (restore != null && module.isEnable() != restore.booleanValue()) {
                        module.setEnable(restore);
                    }
                    this.holdOffRestoreState.remove(bind);
                    bind.setActive(false);
                } else {
                    bind.setActive(down);
                }
            } else if (down && !prevDown) {
                module.toggle();
                bind.setActive(module.isEnable());
            }
            this.comboState.put(bind, down);
        }
    }

    private void processSettingBinds() {
        for (Module module : this.moduleManager.getModules()) {
            for (Setting rawSetting : module.getSettings()) {
                if (!(rawSetting instanceof AbstractSetting)) continue;
                AbstractSetting setting = (AbstractSetting)rawSetting;
                BindData bind = setting.getBind();
                if (!bind.hasKeys()) {
                    this.comboState.remove(bind);
                    this.holdOffRestoreState.remove(bind);
                    continue;
                }
                boolean down = this.pressedKeys.containsAll(bind.getKeys());
                boolean prevDown = this.comboState.getOrDefault(bind, false);
                if (!(setting instanceof BooleanSetting)) {
                    Object restore;
                    Object target;
                    if (setting instanceof SliderSetting) {
                        SliderSetting sliderSetting = (SliderSetting)setting;
                        if (bind.getMode() == BindMode.HOLD) {
                            Float target2;
                            Float restore2;
                            if (down && !prevDown) {
                                this.sliderRestoreState.put(bind, (Float)sliderSetting.getValue());
                            }
                            if (!down && prevDown && (restore2 = this.sliderRestoreState.get(bind)) != null && sliderSetting.getValue() != restore2) {
                                sliderSetting.setValue(restore2);
                            }
                            bind.setActive(down);
                            if (down && (target2 = (Float)sliderSetting.getBindOnValue()) != null && sliderSetting.getValue() != target2) {
                                sliderSetting.setValue(target2);
                            }
                        } else if (down && !prevDown) {
                            boolean newActive;
                            boolean bl = newActive = !bind.isActive();
                            if (newActive) {
                                this.sliderRestoreState.put(bind, (Float)sliderSetting.getValue());
                                bind.setActive(true);
                                target = (Float)sliderSetting.getBindOnValue();
                                if (target != null && sliderSetting.getValue() != target) {
                                    sliderSetting.setValue(target);
                                }
                            } else {
                                bind.setActive(false);
                                restore = this.sliderRestoreState.get(bind);
                                if (restore != null && sliderSetting.getValue() != restore) {
                                    sliderSetting.setValue(restore);
                                }
                            }
                        }
                        this.comboState.put(bind, down);
                        continue;
                    }
                    if (setting instanceof SelectSetting) {
                        SelectSetting selectSetting = (SelectSetting)setting;
                        if (bind.getMode() == BindMode.HOLD) {
                            String target3;
                            String restore3;
                            if (down && !prevDown) {
                                this.selectRestoreState.put(bind, selectSetting.getValue());
                            }
                            if (!down && prevDown && (restore3 = this.selectRestoreState.get(bind)) != null && !restore3.equals(selectSetting.getValue())) {
                                selectSetting.setValue(restore3);
                            }
                            bind.setActive(down);
                            if (down && (target3 = (String)selectSetting.getBindOnValue()) != null && !target3.equals(selectSetting.getValue())) {
                                selectSetting.setValue(target3);
                            }
                        } else if (down && !prevDown) {
                            boolean newActive;
                            boolean bl = newActive = !bind.isActive();
                            if (newActive) {
                                this.selectRestoreState.put(bind, selectSetting.getValue());
                                bind.setActive(true);
                                target = (String)selectSetting.getBindOnValue();
                                if (target != null && !((String)target).equals(selectSetting.getValue())) {
                                    selectSetting.setValue((String)target);
                                }
                            } else {
                                bind.setActive(false);
                                restore = this.selectRestoreState.get(bind);
                                if (restore != null && !((String)restore).equals(selectSetting.getValue())) {
                                    selectSetting.setValue((String)restore);
                                }
                            }
                        }
                        this.comboState.put(bind, down);
                        continue;
                    }
                    if (setting instanceof MultiSelectSetting) {
                        MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                        if (bind.getMode() == BindMode.HOLD) {
                            Set target4;
                            Set<String> restore4;
                            if (down && !prevDown) {
                                this.multiRestoreState.put(bind, new LinkedHashSet<String>(multiSelectSetting.getSelectedValues()));
                            }
                            if (!down && prevDown && (restore4 = this.multiRestoreState.get(bind)) != null && !restore4.equals(multiSelectSetting.getSelectedValues())) {
                                multiSelectSetting.setValue(restore4);
                            }
                            bind.setActive(down);
                            if (down && (target4 = (Set)multiSelectSetting.getBindOnValue()) != null && !target4.equals(multiSelectSetting.getSelectedValues())) {
                                multiSelectSetting.setValue(target4);
                            }
                        } else if (down && !prevDown) {
                            boolean newActive;
                            boolean bl = newActive = !bind.isActive();
                            if (newActive) {
                                this.multiRestoreState.put(bind, new LinkedHashSet<String>(multiSelectSetting.getSelectedValues()));
                                bind.setActive(true);
                                target = (Set)multiSelectSetting.getBindOnValue();
                                if (target != null && !target.equals(multiSelectSetting.getSelectedValues())) {
                                    multiSelectSetting.setValue((Set<String>)target);
                                }
                            } else {
                                bind.setActive(false);
                                restore = this.multiRestoreState.get(bind);
                                if (restore != null && !restore.equals(multiSelectSetting.getSelectedValues())) {
                                    multiSelectSetting.setValue((Set<String>)restore);
                                }
                            }
                        }
                        this.comboState.put(bind, down);
                        continue;
                    }
                    if (setting instanceof InputSetting) {
                        InputSetting inputSetting = (InputSetting)setting;
                        if (bind.getMode() == BindMode.HOLD) {
                            String target5;
                            String restore5;
                            if (down && !prevDown) {
                                this.inputRestoreState.put(bind, inputSetting.getValue());
                            }
                            if (!down && prevDown && (restore5 = this.inputRestoreState.get(bind)) != null && !restore5.equals(inputSetting.getValue())) {
                                inputSetting.setValue(restore5);
                            }
                            bind.setActive(down);
                            if (down && (target5 = (String)inputSetting.getBindOnValue()) != null && !target5.equals(inputSetting.getValue())) {
                                inputSetting.setValue(target5);
                            }
                        } else if (down && !prevDown) {
                            boolean newActive;
                            boolean bl = newActive = !bind.isActive();
                            if (newActive) {
                                this.inputRestoreState.put(bind, inputSetting.getValue());
                                bind.setActive(true);
                                target = (String)inputSetting.getBindOnValue();
                                if (target != null && !((String)target).equals(inputSetting.getValue())) {
                                    inputSetting.setValue((String)target);
                                }
                            } else {
                                bind.setActive(false);
                                restore = this.inputRestoreState.get(bind);
                                if (restore != null && !((String)restore).equals(inputSetting.getValue())) {
                                    inputSetting.setValue((String)restore);
                                }
                            }
                        }
                        this.comboState.put(bind, down);
                        continue;
                    }
                    if (setting instanceof ColorSetting) {
                        ColorSetting colorSetting = (ColorSetting)setting;
                        if (bind.getMode() == BindMode.HOLD) {
                            Integer target6;
                            Integer restore6;
                            if (down && !prevDown) {
                                this.colorRestoreState.put(bind, colorSetting.getColor());
                            }
                            if (!down && prevDown && (restore6 = this.colorRestoreState.get(bind)) != null && restore6.intValue() != colorSetting.getColor()) {
                                colorSetting.setColor(restore6);
                            }
                            bind.setActive(down);
                            if (down && (target6 = (Integer)colorSetting.getBindOnValue()) != null && target6.intValue() != colorSetting.getColor()) {
                                colorSetting.setColor(target6);
                            }
                        } else if (down && !prevDown) {
                            boolean newActive;
                            boolean bl = newActive = !bind.isActive();
                            if (newActive) {
                                this.colorRestoreState.put(bind, colorSetting.getColor());
                                bind.setActive(true);
                                target = (Integer)colorSetting.getBindOnValue();
                                if (target != null && ((Integer)target).intValue() != colorSetting.getColor()) {
                                    colorSetting.setColor((Integer)target);
                                }
                            } else {
                                bind.setActive(false);
                                restore = this.colorRestoreState.get(bind);
                                if (restore != null && ((Integer)restore).intValue() != colorSetting.getColor()) {
                                    colorSetting.setColor((Integer)restore);
                                }
                            }
                        }
                        this.comboState.put(bind, down);
                        continue;
                    }
                    if (bind.getMode() == BindMode.HOLD) {
                        if (bind.isActive() != down) {
                            setting.applyBindState(down);
                        }
                    } else if (down && !prevDown) {
                        setting.applyBindState(!bind.isActive());
                    }
                    this.comboState.put(bind, down);
                    continue;
                }
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                if (booleanSetting.isBindOnly()) {
                    if (down && !prevDown) {
                        booleanSetting.setValue(true);
                        bind.setActive(true);
                    } else if (!down && prevDown) {
                        bind.setActive(false);
                    }
                    this.comboState.put(bind, down);
                    continue;
                }
                if (bind.getMode() == BindMode.HOLD) {
                    if (down && !prevDown) {
                        boolean wasValue = booleanSetting.isValue();
                        this.holdOffRestoreState.put(bind, wasValue);
                        booleanSetting.setValue(!wasValue);
                        bind.setActive(true);
                        this.notifySettingBind(module, booleanSetting);
                    } else if (!down && prevDown) {
                        Boolean restore = this.holdOffRestoreState.get(bind);
                        if (restore != null && booleanSetting.isValue() != restore.booleanValue()) {
                            booleanSetting.setValue(restore);
                            this.notifySettingBind(module, booleanSetting);
                        }
                        this.holdOffRestoreState.remove(bind);
                        bind.setActive(false);
                    } else {
                        bind.setActive(down);
                    }
                } else if (down && !prevDown) {
                    booleanSetting.toggle();
                    bind.setActive(booleanSetting.isValue());
                    this.notifySettingBind(module, booleanSetting);
                }
                this.comboState.put(bind, down);
            }
        }
    }

    private void notifySettingBind(Module module, BooleanSetting setting) {
    }

    @Generated
    public static Pyz1k getInstance() {
        return instance;
    }

    @Generated
    public EventBus getEventBus() {
        return this.eventBus;
    }

    @Generated
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Generated
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
}

