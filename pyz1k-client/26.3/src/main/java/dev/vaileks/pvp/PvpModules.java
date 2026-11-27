/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  pyz1k.module.base.Category
 *  pyz1k.module.base.Module
 *  pyz1k.module.base.ModuleInfo
 */
package pyz1k.extensions.pvp;

import pyz1k.extensions.pvp.Feature;
import pyz1k.extensions.pvp.HubScreen;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;

public final class PvpModules {
    private static final EnumMap<Feature, Module> MODULES = new EnumMap(Feature.class);
    private static int enabledCount;

    public static boolean enabled(Feature f) {
        Module m = MODULES.get((Object)f);
        return m != null && m.isEnable();
    }

    public static boolean any() {
        return enabledCount > 0;
    }

    public static void enable(Feature f, boolean value) {
        Module m = MODULES.get((Object)f);
        if (m != null) {
            m.setEnable(value);
        }
    }

    public static void register(List<Module> modules) {
        MODULES.put(Feature.READY_AGAIN, new ReadyAgain());
        MODULES.put(Feature.EFFECT_ALARM, new EffectAlarm());
        MODULES.put(Feature.REFILL_REMINDER, new RefillReminder());
        MODULES.put(Feature.ARMOR_PANIC, new ArmorPanic());
        MODULES.put(Feature.KIT_CHECK, new KitCheck());
        MODULES.put(Feature.FIGHT_RECAP, new FightRecap());
        MODULES.put(Feature.COMBAT_FOCUS, new CombatFocus());
        MODULES.put(Feature.TOTEM_PULSE, new TotemPulse());
        MODULES.put(Feature.OFFHAND_ALERT, new OffhandAlert());
        MODULES.put(Feature.PEARL_LANDING_TIMER, new PearlLandingTimer());
        MODULES.put(Feature.HOTBAR_MEMORY, new HotbarMemory());
        MODULES.put(Feature.POTION_IDENTITY, new PotionIdentity());
        MODULES.put(Feature.RESUPPLY_LIST, new ResupplyList());
        MODULES.put(Feature.DUEL_NOTES, new DuelNotes());
        MODULES.put(Feature.HUD_ON_DEMAND, new HudOnDemand());
        MODULES.put(Feature.FIGHT_TIMELINE, new FightTimeline());
        MODULES.put(Feature.INVENTORY_SEARCH_FLASH, new InventorySearchFlash());
        modules.addAll(MODULES.values());
        modules.add(new Settings());
    }

    @ModuleInfo(name="ReadyAgain", desc="\u0421\u0438\u0433\u043d\u0430\u043b, \u043a\u043e\u0433\u0434\u0430 \u0437\u0430\u043a\u0430\u043d\u0447\u0438\u0432\u0430\u0435\u0442\u0441\u044f \u043a\u0443\u043b\u0434\u0430\u0443\u043d \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430.", category=Category.VISUALS)
    public static final class ReadyAgain
    extends FeatureModule {
    }

    @ModuleInfo(name="EffectAlarm", desc="\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435 \u043f\u0435\u0440\u0435\u0434 \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435\u043c \u0432\u0430\u0448\u0438\u0445 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432.", category=Category.VISUALS)
    public static final class EffectAlarm
    extends FeatureModule {
    }

    @ModuleInfo(name="RefillReminder", desc="\u041c\u0430\u043b\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435, \u043d\u043e \u0435\u0441\u0442\u044c \u0437\u0430\u043f\u0430\u0441 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435.", category=Category.VISUALS)
    public static final class RefillReminder
    extends FeatureModule {
    }

    @ModuleInfo(name="ArmorPanic", desc="\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435 \u043e \u043d\u0438\u0437\u043a\u043e\u0439 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438 \u0441\u0432\u043e\u0435\u0439 \u0431\u0440\u043e\u043d\u0438.", category=Category.VISUALS)
    public static final class ArmorPanic
    extends FeatureModule {
    }

    @ModuleInfo(name="KitCheck", desc="\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442 \u0438 \u043f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0435\u0433\u043e \u043f\u0435\u0440\u0435\u0434 \u0431\u043e\u0435\u043c.", category=Category.VISUALS)
    public static final class KitCheck
    extends FeatureModule {
    }

    @ModuleInfo(name="FightRecap", desc="\u0418\u0442\u043e\u0433\u0438 \u0431\u043e\u044f: \u0434\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c, \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0438 \u0432\u0430\u0448\u0438 \u0442\u043e\u0442\u0435\u043c\u044b.", category=Category.VISUALS)
    public static final class FightRecap
    extends FeatureModule {
    }

    @ModuleInfo(name="CombatFocus", desc="\u0412\u043e \u0432\u0440\u0435\u043c\u044f PvP \u0441\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0432\u0442\u043e\u0440\u043e\u0441\u0442\u0435\u043f\u0435\u043d\u043d\u044b\u0435 \u043f\u0430\u043d\u0435\u043b\u0438 \u044d\u0442\u0438\u0445 \u043c\u043e\u0434\u043e\u0432.", category=Category.VISUALS)
    public static final class CombatFocus
    extends FeatureModule {
    }

    @ModuleInfo(name="TotemPulse", desc="\u041f\u043e\u0441\u043b\u0435 \u0441\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u044f \u0432\u0430\u0448\u0435\u0433\u043e \u0442\u043e\u0442\u0435\u043c\u0430 \u043f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0437\u0430\u043f\u0430\u0441.", category=Category.VISUALS)
    public static final class TotemPulse
    extends FeatureModule {
    }

    @ModuleInfo(name="OffhandAlert", desc="\u041f\u0440\u043e\u0432\u0435\u0440\u044f\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u043b\u0435\u0432\u043e\u0439 \u0440\u0443\u043a\u0435 \u043f\u043e \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u043c\u0443 \u043f\u0440\u043e\u0444\u0438\u043b\u044e.", category=Category.VISUALS)
    public static final class OffhandAlert
    extends FeatureModule {
    }

    @ModuleInfo(name="PearlLandingTimer", desc="\u0412 \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u043e\u043c \u043c\u0438\u0440\u0435 \u0438\u0437\u043c\u0435\u0440\u044f\u0435\u0442 \u043f\u043e\u043b\u0451\u0442 \u0441\u043e\u0431\u0441\u0442\u0432\u0435\u043d\u043d\u043e\u0433\u043e \u0436\u0435\u043c\u0447\u0443\u0433\u0430.", category=Category.VISUALS)
    public static final class PearlLandingTimer
    extends FeatureModule {
    }

    @ModuleInfo(name="HotbarMemory", desc="\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u0441\u043b\u043e\u0442\u044b, \u043e\u0442\u043b\u0438\u0447\u0430\u044e\u0449\u0438\u0435\u0441\u044f \u043e\u0442 \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u043e\u0439 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438.", category=Category.VISUALS)
    public static final class HotbarMemory
    extends FeatureModule {
    }

    @ModuleInfo(name="PotionIdentity", desc="\u041a\u043e\u0440\u043e\u0442\u043a\u0438\u0435 \u043e\u0431\u043e\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432 \u043d\u0430 \u0432\u0430\u0448\u0438\u0445 \u0437\u0435\u043b\u044c\u044f\u0445.", category=Category.VISUALS)
    public static final class PotionIdentity
    extends FeatureModule {
    }

    @ModuleInfo(name="ResupplyList", desc="\u041f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u0440\u0442\u0438 \u043f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u044e\u0449\u0438\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442\u0430.", category=Category.VISUALS)
    public static final class ResupplyList
    extends FeatureModule {
    }

    @ModuleInfo(name="DuelNotes", desc="\u0417\u0430\u043c\u0435\u0442\u043a\u0438 \u043f\u043e\u0441\u043b\u0435 \u0431\u043e\u044f \u0438 \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u044e\u0449\u0438\u0445\u0441\u044f \u043e\u0448\u0438\u0431\u043e\u043a.", category=Category.VISUALS)
    public static final class DuelNotes
    extends FeatureModule {
    }

    @ModuleInfo(name="HudOnDemand", desc="\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u043f\u0430\u043d\u0435\u043b\u0438 \u044d\u0442\u0438\u0445 \u043c\u043e\u0434\u043e\u0432 \u043f\u043e \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u044e \u043a\u043b\u0430\u0432\u0438\u0448\u0438.", category=Category.VISUALS)
    public static final class HudOnDemand
    extends FeatureModule {
    }

    @ModuleInfo(name="FightTimeline", desc="\u041b\u0435\u043d\u0442\u0430 \u0432\u0430\u0448\u0438\u0445 \u0441\u043e\u0431\u044b\u0442\u0438\u0439 \u0432 \u0431\u043e\u044e, \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435 \u0432 \u0444\u0430\u0439\u043b.", category=Category.VISUALS)
    public static final class FightTimeline
    extends FeatureModule {
    }

    @ModuleInfo(name="InventorySearchFlash", desc="\u041f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0439 \u043a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u0438 \u043f\u0440\u0438 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f.", category=Category.VISUALS)
    public static final class InventorySearchFlash
    extends FeatureModule {
    }

    @ModuleInfo(name="PvPTools", desc="\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 PvP-\u043c\u043e\u0434\u0443\u043b\u0435\u0439, \u043a\u043e\u043c\u043f\u043b\u0435\u043a\u0442\u044b, \u043e\u0442\u0447\u0451\u0442\u044b \u0438 \u0437\u0430\u043c\u0435\u0442\u043a\u0438", category=Category.VISUALS)
    public static final class Settings
    extends Module {
        public void toggle() {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen((Screen)new HubScreen(mc.gui.screen()));
        }
    }

    public static abstract class FeatureModule
    extends Module {
        public void onEnable() {
            super.onEnable();
            ++enabledCount;
        }

        public void onDisable() {for(var entry:MODULES.entrySet())if(entry.getValue()==this){Core.disabled(entry.getKey());break;}

            super.onDisable();
            enabledCount = Math.max(0, enabledCount - 1);
        }
    }
}

