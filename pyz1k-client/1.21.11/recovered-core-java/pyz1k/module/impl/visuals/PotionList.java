/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.impl.visuals;

import java.util.List;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.SliderSetting;
import pyz1k.screen.screen.elements.PotionListHudElement;

@ModuleInfo(name="PotionList", desc="", category=Category.VISUALS, enabledByDefault=true)
public final class PotionList
extends Module {
    private final SliderSetting scale = this.registerSetting(new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 1.0f, 0.5f, 2.0f, 0.05f));
    private final SelectSetting style = this.registerSetting(new SelectSetting("\u0421\u0442\u0438\u043b\u044c", "\u041e\u0431\u044b\u0447\u043d\u044b\u0439", List.of("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "\u041c\u0438\u043d\u0438")));
    private final SelectSetting fontStyle = this.registerSetting(new SelectSetting("\u0428\u0440\u0438\u0444\u0442", "\u041a\u043b\u0438\u0435\u043d\u0442", List.of("\u041a\u043b\u0438\u0435\u043d\u0442", "\u0412\u0430\u043d\u0438\u043b\u043b\u0430")));
    private final BooleanSetting background = this.registerSetting(new BooleanSetting("\u0424\u043e\u043d", true));
    private final ColorSetting backgroundColor = this.registerSetting(new ColorSetting("\u0426\u0432\u0435\u0442 \u0444\u043e\u043d\u0430", -1509488629));

    public PotionList() {
        Interface.registerExternalHudElement(new PotionListHudElement(this));
    }

    public float getScale() {
        return ((Float)this.scale.getValue()).floatValue();
    }

    public String getStyle() {
        return this.style.getValue();
    }

    public boolean isCompactStyle() {
        return "\u041c\u0438\u043d\u0438".equals(this.style.getValue());
    }

    public boolean isClientStyle() {
        return "\u041a\u043b\u0438\u0435\u043d\u0442".equals(this.fontStyle.getValue());
    }

    public boolean isBackgroundEnabled() {
        return this.background.isValue();
    }

    public int getBackgroundColor() {
        return this.backgroundColor.getColor();
    }
}

