/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package pyz1k.extensions;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import pyz1k.extensions.BloodCooldownProfiles;
import pyz1k.extensions.ExtensionClient;
import pyz1k.extensions.ExtensionScreen;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.SliderSetting;

public final class FeatureModules {
    public static LootGloow loot;
    public static OldMirrorSword oldMirror;
    public static BetterNames names;
    public static Skins skins;
    public static Durability durability;
    public static BloodSwordCooldown blood;

    private FeatureModules() {
    }

    public static void register(List<Module> modules) {
        loot = new LootGloow(); oldMirror = new OldMirrorSword();
        names = new BetterNames();
        skins = new Skins();
        durability = new Durability();
        blood = new BloodSwordCooldown();
        modules.addAll(List.of(loot, oldMirror, names, skins, new Extras(), durability, new Screenshot(), blood));
        pyz1k.extensions.pvp.PvpModules.register(modules);
        Interface.registerExternalHudElement(ExtensionClient.BLOOD_HUD);
    }

    @ModuleInfo(name="LootGloow", desc="Обычный лут и эпики из белого списка; легендарки исключены", category=Category.VISUALS)
    public static final class LootGloow extends Module {
        public final BooleanSetting glow=registerSetting(new BooleanSetting("Подсветка",true));
        public final SliderSetting scale=registerSetting(new SliderSetting("Размер лута",1,0.25f,5,0.05f));
        public final SliderSetting distance=registerSetting(new SliderSetting("Дистанция",64,8,128,4));
        public final ColorSetting color=registerSetting(new ColorSetting("Обычный лут",0xff85c9ff));
        public final ColorSetting epicColor=registerSetting(new ColorSetting("Эпики",0xffc48bff));
        public final BooleanSetting totems=registerSetting(new BooleanSetting("Вайтлист: Кастомные тотемы",true));
        public final ColorSetting totemColor=registerSetting(new ColorSetting("Тотемы",0xffffc75a));
        public final BooleanSetting ballista=registerSetting(new BooleanSetting("Вайтлист: Балиста",true));
        public final BooleanSetting shield=registerSetting(new BooleanSetting("Вайтлист: Незеритовый щит",true));
        public final BooleanSetting flaming=registerSetting(new BooleanSetting("Вайтлист: Флейм арбалет",true));
    }
    @ModuleInfo(name="OldMirrorSword", desc="Старая текстура Зеркального Меча и его копий", category=Category.VISUALS)
    public static final class OldMirrorSword extends Module {}

    @ModuleInfo(name="BetterNameVisibility", desc="Читаемые ники, в том числе сквозь стены", category=Category.VISUALS)
    public static final class BetterNames
    extends Module {
        public final BooleanSetting throughWalls = this.registerSetting(new BooleanSetting("Сквозь стены", true));
        public final BooleanSetting background = this.registerSetting(new BooleanSetting("Фон", true));
        public final SliderSetting scale = this.registerSetting(new SliderSetting("Размер", 1.15f, 0.75f, 2.0f, 0.05f));
        public final SliderSetting distance = this.registerSetting(new SliderSetting("Дистанция", 64.0f, 8.0f, 128.0f, 4.0f));
        public final ColorSetting color = this.registerSetting(new ColorSetting("Цвет текста", -723720));
    }

    @ModuleInfo(name="ItemSkin", desc="Локальные скины из активного серверного ресурспака", category=Category.VISUALS)
    public static final class Skins
    extends Module {
        public final BooleanSetting inventory = this.registerSetting(new BooleanSetting("В инвентаре", true));
    }

    @ModuleInfo(name="DurabilityAlert", desc="Предупреждение о низкой прочности снаряжения", category=Category.VISUALS)
    public static final class Durability
    extends Module {
        public final SliderSetting threshold = this.registerSetting(new SliderSetting("Осталось процентов", 15.0f, 1.0f, 50.0f, 1.0f));
        public final BooleanSetting armor = this.registerSetting(new BooleanSetting("Броня", true));
        public final BooleanSetting sound = this.registerSetting(new BooleanSetting("Звук", true));
    }

    @ModuleInfo(name="BloodSwordCooldown", desc="Кулдауны форм кровавого меча и пресеты режимов", category=Category.VISUALS)
    public static final class BloodSwordCooldown
    extends Module {
        public final SelectSetting mode = this.registerSetting(new SelectSetting("Режим", "Митапы", BloodCooldownProfiles.ALL.stream().map(BloodCooldownProfiles.Mode::label).toList()));
        public final SliderSetting scale = this.registerSetting(new SliderSetting("Размер", 1.0f, 0.5f, 2.0f, 0.05f));
        public final BooleanSetting precise = this.registerSetting(new BooleanSetting("Точное время", true));
        public final BooleanSetting showIdle = this.registerSetting(new BooleanSetting("Показывать без КД", true));
        public final BooleanSetting itemData = this.registerSetting(new BooleanSetting("Таймеры из предмета", true));
    }

    @ModuleInfo(name="ClientTools", desc="Каталог скинов и расположение HUD", category=Category.VISUALS)
    public static final class Extras
    extends Module {
        @Override
        public void toggle() {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen((Screen)new ExtensionScreen(mc.gui.screen()));
        }
    }

    @ModuleInfo(name="ScreenshotMode", desc="Скрыть HUD одной кнопкой. По умолчанию F8", category=Category.VISUALS)
    public static final class Screenshot
    extends Module {
        public Screenshot() {
            this.setKey(297);
        }

        @Override
        public void toggle() {
            ScreenshotMode.toggle();
        }
    }
}

