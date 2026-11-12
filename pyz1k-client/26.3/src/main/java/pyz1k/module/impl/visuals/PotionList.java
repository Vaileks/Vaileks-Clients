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
    private final SliderSetting scale = this.registerSetting(new SliderSetting("Размер", 1.0f, 0.5f, 2.0f, 0.05f));
    private final SelectSetting style = this.registerSetting(new SelectSetting("Стиль", "Обычный", List.of("Обычный", "Мини")));
    private final SelectSetting fontStyle = this.registerSetting(new SelectSetting("Шрифт", "Клиент", List.of("Клиент", "Ванилла")));
    private final BooleanSetting background = this.registerSetting(new BooleanSetting("Фон", true));
    private final ColorSetting backgroundColor = this.registerSetting(new ColorSetting("Цвет фона", -1509488629));

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
        return "Мини".equals(this.style.getValue());
    }

    public boolean isClientStyle() {
        return "Клиент".equals(this.fontStyle.getValue());
    }

    public boolean isBackgroundEnabled() {
        return this.background.isValue();
    }

    public int getBackgroundColor() {
        return this.backgroundColor.getColor();
    }
}

