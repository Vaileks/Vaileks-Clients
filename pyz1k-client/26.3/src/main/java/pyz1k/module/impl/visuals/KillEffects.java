/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.impl.visuals;

import java.util.List;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.SliderSetting;

@ModuleInfo(name="KillEffects", desc="Client-side animated kill effects", category=Category.VISUALS)
public final class KillEffects
extends Module {
    private final SelectSetting effect = this.registerSetting(new SelectSetting("Эффект", "Случайно", List.of("Случайно", "Молния", "Летучие мыши", "Орбитальный удар", "Люк", "Поезд", "Красная бомба", "Маленький медный голем", "Медный голем", "Терминатор", "Мухоловка")));
    private final SliderSetting scale = this.registerSetting(new SliderSetting("Размер", 1.0f, 0.5f, 2.0f, 0.1f));
    private final BooleanSetting hideServerEffects = this.registerSetting(new BooleanSetting("Скрывать эффекты сервера", false));
    private final BooleanSetting muteServerSounds = this.registerSetting(new BooleanSetting("Скрывать звуки сервера", true));
    private final BooleanSetting hideOtherServerEffects = this.registerSetting(new BooleanSetting("Скрывать чужие эффекты сервера", false));
    private final BooleanSetting muteOtherServerSounds = this.registerSetting(new BooleanSetting("Скрывать чужие звуки сервера", false));

    public boolean hideServerEffects() {
        return this.hideServerEffects.isValue();
    }

    public boolean muteServerSounds() {
        return this.muteServerSounds.isValue();
    }

    public boolean hideOtherServerEffects() {
        return this.hideOtherServerEffects.isValue();
    }

    public boolean muteOtherServerSounds() {
        return this.muteOtherServerSounds.isValue();
    }

    public int variant(int counter) {
        return switch (this.effect.getValue()) {
            case "Молния" -> 0;
            case "Летучие мыши" -> 1;
            case "Орбитальный удар" -> 2;
            case "Люк" -> 3;
            case "Поезд" -> 4;
            case "Красная бомба" -> 5;
            case "Маленький медный голем" -> 6;
            case "Медный голем" -> 7;
            case "Терминатор" -> 8;
            case "Мухоловка" -> 9;
            default -> counter % 10;
        };
    }

    public float scale() {
        return ((Float)this.scale.getValue()).floatValue();
    }
}

