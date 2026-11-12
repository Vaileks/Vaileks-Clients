package pyz1k.extensions.pvp;
import java.util.*;
import pyz1k.module.base.Module;
import pyz1k.module.base.ModuleInfo;
import pyz1k.module.base.Category;
import net.minecraft.client.MinecraftClient;
public final class PvpModules {
 private static final EnumMap<Feature,Module> MODULES=new EnumMap<>(Feature.class);
 private static int enabledCount;
 public static boolean enabled(Feature f){Module m=MODULES.get(f);return m!=null&&m.isEnable();}
 public static boolean any(){return enabledCount>0;}
 public static void enable(Feature f,boolean value){Module m=MODULES.get(f);if(m!=null)m.setEnable(value);}
 public static void register(List<Module> modules){
  MODULES.put(Feature.READY_AGAIN,new ReadyAgain());
  MODULES.put(Feature.EFFECT_ALARM,new EffectAlarm());
  MODULES.put(Feature.REFILL_REMINDER,new RefillReminder());
  MODULES.put(Feature.ARMOR_PANIC,new ArmorPanic());
  MODULES.put(Feature.KIT_CHECK,new KitCheck());
  MODULES.put(Feature.FIGHT_RECAP,new FightRecap());
  MODULES.put(Feature.COMBAT_FOCUS,new CombatFocus());
  MODULES.put(Feature.TOTEM_PULSE,new TotemPulse());
  MODULES.put(Feature.OFFHAND_ALERT,new OffhandAlert());
  MODULES.put(Feature.PEARL_LANDING_TIMER,new PearlLandingTimer());
  MODULES.put(Feature.HOTBAR_MEMORY,new HotbarMemory());
  MODULES.put(Feature.POTION_IDENTITY,new PotionIdentity());
  MODULES.put(Feature.RESUPPLY_LIST,new ResupplyList());
  MODULES.put(Feature.DUEL_NOTES,new DuelNotes());
  MODULES.put(Feature.HUD_ON_DEMAND,new HudOnDemand());
  MODULES.put(Feature.FIGHT_TIMELINE,new FightTimeline());
  MODULES.put(Feature.INVENTORY_SEARCH_FLASH,new InventorySearchFlash());
  modules.addAll(MODULES.values());modules.add(new Settings());
 }
 public abstract static class FeatureModule extends Module {
  @Override public void onEnable(){super.onEnable();enabledCount++;}
  @Override public void onDisable(){for(var entry:MODULES.entrySet())if(entry.getValue()==this){Core.disabled(entry.getKey());break;}
super.onDisable();enabledCount=Math.max(0,enabledCount-1);}
 }
 @ModuleInfo(name="ReadyAgain",desc="Сигнал, когда заканчивается кулдаун предмета.",category=Category.VISUALS)
 public static final class ReadyAgain extends FeatureModule {}
 @ModuleInfo(name="EffectAlarm",desc="Предупреждение перед окончанием ваших эффектов.",category=Category.VISUALS)
 public static final class EffectAlarm extends FeatureModule {}
 @ModuleInfo(name="RefillReminder",desc="Мало предметов в хотбаре, но есть запас в инвентаре.",category=Category.VISUALS)
 public static final class RefillReminder extends FeatureModule {}
 @ModuleInfo(name="ArmorPanic",desc="Предупреждение о низкой прочности своей брони.",category=Category.VISUALS)
 public static final class ArmorPanic extends FeatureModule {}
 @ModuleInfo(name="KitCheck",desc="Сохраните комплект и проверьте его перед боем.",category=Category.VISUALS)
 public static final class KitCheck extends FeatureModule {}
 @ModuleInfo(name="FightRecap",desc="Итоги боя: длительность, предметы и ваши тотемы.",category=Category.VISUALS)
 public static final class FightRecap extends FeatureModule {}
 @ModuleInfo(name="CombatFocus",desc="Во время PvP скрывает второстепенные панели этих модов.",category=Category.VISUALS)
 public static final class CombatFocus extends FeatureModule {}
 @ModuleInfo(name="TotemPulse",desc="После срабатывания вашего тотема показывает запас.",category=Category.VISUALS)
 public static final class TotemPulse extends FeatureModule {}
 @ModuleInfo(name="OffhandAlert",desc="Проверяет предмет в левой руке по выбранному профилю.",category=Category.VISUALS)
 public static final class OffhandAlert extends FeatureModule {}
 @ModuleInfo(name="PearlLandingTimer",desc="В одиночном мире измеряет полёт собственного жемчуга.",category=Category.VISUALS)
 public static final class PearlLandingTimer extends FeatureModule {}
 @ModuleInfo(name="HotbarMemory",desc="Подсвечивает слоты, отличающиеся от сохранённой раскладки.",category=Category.VISUALS)
 public static final class HotbarMemory extends FeatureModule {}
 @ModuleInfo(name="PotionIdentity",desc="Короткие обозначения эффектов на ваших зельях.",category=Category.VISUALS)
 public static final class PotionIdentity extends FeatureModule {}
 @ModuleInfo(name="ResupplyList",desc="После смерти показывает недостающие предметы комплекта.",category=Category.VISUALS)
 public static final class ResupplyList extends FeatureModule {}
 @ModuleInfo(name="DuelNotes",desc="Заметки после боя и статистика повторяющихся ошибок.",category=Category.VISUALS)
 public static final class DuelNotes extends FeatureModule {}
 @ModuleInfo(name="HudOnDemand",desc="Показывает панели этих модов по удержанию клавиши.",category=Category.VISUALS)
 public static final class HudOnDemand extends FeatureModule {}
 @ModuleInfo(name="FightTimeline",desc="Лента ваших событий в бою, сохранение в файл.",category=Category.VISUALS)
 public static final class FightTimeline extends FeatureModule {}
 @ModuleInfo(name="InventorySearchFlash",desc="Подсветка выбранной категории при открытии инвентаря.",category=Category.VISUALS)
 public static final class InventorySearchFlash extends FeatureModule {}
 @ModuleInfo(name="PvPTools",desc="Настройки PvP-модулей, комплекты, отчёты и заметки",category=Category.VISUALS)
 public static final class Settings extends Module {
  @Override public void toggle(){var mc=MinecraftClient.getInstance();mc.setScreen(new HubScreen(mc.currentScreen));}
 }
}
