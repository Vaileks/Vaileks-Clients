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
    private final SelectSetting effect = this.registerSetting(new SelectSetting("\u042d\u0444\u0444\u0435\u043a\u0442", "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u043e", List.of("\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u043e", "\u041c\u043e\u043b\u043d\u0438\u044f", "\u041b\u0435\u0442\u0443\u0447\u0438\u0435 \u043c\u044b\u0448\u0438", "\u041e\u0440\u0431\u0438\u0442\u0430\u043b\u044c\u043d\u044b\u0439 \u0443\u0434\u0430\u0440", "\u041b\u044e\u043a", "\u041f\u043e\u0435\u0437\u0434", "\u041a\u0440\u0430\u0441\u043d\u0430\u044f \u0431\u043e\u043c\u0431\u0430", "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u0438\u0439 \u043c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c", "\u041c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c", "\u0422\u0435\u0440\u043c\u0438\u043d\u0430\u0442\u043e\u0440", "\u041c\u0443\u0445\u043e\u043b\u043e\u0432\u043a\u0430")));
    private final SliderSetting scale = this.registerSetting(new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 1.0f, 0.5f, 2.0f, 0.1f));
    private final BooleanSetting hideServerEffects = this.registerSetting(new BooleanSetting("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u0441\u0435\u0440\u0432\u0435\u0440\u0430", false));
    private final BooleanSetting muteServerSounds = this.registerSetting(new BooleanSetting("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u0437\u0432\u0443\u043a\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u0430", true));
    private final BooleanSetting hideOtherServerEffects = this.registerSetting(new BooleanSetting("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u0447\u0443\u0436\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u0441\u0435\u0440\u0432\u0435\u0440\u0430", false));
    private final BooleanSetting muteOtherServerSounds = this.registerSetting(new BooleanSetting("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u0447\u0443\u0436\u0438\u0435 \u0437\u0432\u0443\u043a\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u0430", false));

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
            case "\u041c\u043e\u043b\u043d\u0438\u044f" -> 0;
            case "\u041b\u0435\u0442\u0443\u0447\u0438\u0435 \u043c\u044b\u0448\u0438" -> 1;
            case "\u041e\u0440\u0431\u0438\u0442\u0430\u043b\u044c\u043d\u044b\u0439 \u0443\u0434\u0430\u0440" -> 2;
            case "\u041b\u044e\u043a" -> 3;
            case "\u041f\u043e\u0435\u0437\u0434" -> 4;
            case "\u041a\u0440\u0430\u0441\u043d\u0430\u044f \u0431\u043e\u043c\u0431\u0430" -> 5;
            case "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u0438\u0439 \u043c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c" -> 6;
            case "\u041c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c" -> 7;
            case "\u0422\u0435\u0440\u043c\u0438\u043d\u0430\u0442\u043e\u0440" -> 8;
            case "\u041c\u0443\u0445\u043e\u043b\u043e\u0432\u043a\u0430" -> 9;
            default -> counter % 10;
        };
    }

    public float scale() {
        return ((Float)this.scale.getValue()).floatValue();
    }
}

