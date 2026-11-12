package pyz1k.extensions.pvp;
public enum Feature {
    READY_AGAIN("ready_again", "Ready Again", "Сигнал, когда заканчивается кулдаун предмета.", 3, "minecraft:ender_pearl"),
    EFFECT_ALARM("effect_alarm", "Effect Alarm", "Предупреждение перед окончанием ваших эффектов.", 10, "minecraft:potion"),
    REFILL_REMINDER("refill_reminder", "Refill Reminder", "Мало предметов в хотбаре, но есть запас в инвентаре.", 3, "minecraft:splash_potion"),
    ARMOR_PANIC("armor_panic", "Armor Panic", "Предупреждение о низкой прочности своей брони.", 15, "minecraft:diamond_chestplate"),
    KIT_CHECK("kit_check", "Kit Check", "Сохраните комплект и проверьте его перед боем.", 1, "minecraft:chest"),
    FIGHT_RECAP("fight_recap", "Fight Recap", "Итоги боя: длительность, предметы и ваши тотемы.", 15, "minecraft:paper"),
    COMBAT_FOCUS("combat_focus", "Combat Focus", "Во время PvP скрывает второстепенные панели этих модов.", 15, "minecraft:diamond_sword"),
    SERVER_PROFILES("server_profiles", "Server Profiles", "Отдельные настройки этих модов для каждого сервера.", 1, "minecraft:compass"),
    TOTEM_PULSE("totem_pulse", "Totem Pulse", "После срабатывания вашего тотема показывает запас.", 3, "minecraft:totem_of_undying"),
    OFFHAND_ALERT("offhand_alert", "Offhand Alert", "Проверяет предмет в левой руке по выбранному профилю.", 1, "minecraft:shield"),
    PEARL_LANDING_TIMER("pearl_landing_timer", "Pearl Landing Timer", "В одиночном мире измеряет полёт собственного жемчуга.", 1, "minecraft:ender_pearl"),
    HOTBAR_MEMORY("hotbar_memory", "Hotbar Memory", "Подсвечивает слоты, отличающиеся от сохранённой раскладки.", 1, "minecraft:chest"),
    POTION_IDENTITY("potion_identity", "Potion Identity", "Короткие обозначения эффектов на ваших зельях.", 1, "minecraft:potion"),
    RESUPPLY_LIST("resupply_list", "Resupply List", "После смерти показывает недостающие предметы комплекта.", 1, "minecraft:barrel"),
    DUEL_NOTES("duel_notes", "Duel Notes", "Заметки после боя и статистика повторяющихся ошибок.", 1, "minecraft:writable_book"),
    HUD_ON_DEMAND("hud_on_demand", "HUD On Demand", "Показывает панели этих модов по удержанию клавиши.", 1, "minecraft:spyglass"),
    FIGHT_TIMELINE("fight_timeline", "Fight Timeline", "Лента ваших событий в бою, сохранение в файл.", 15, "minecraft:clock"),
    INVENTORY_SEARCH_FLASH("inventory_search_flash", "Inventory Search Flash", "Подсветка выбранной категории при открытии инвентаря.", 3, "minecraft:glowstone_dust");
 public final String id, title, description, icon;
 public final int threshold;
 Feature(String id,String title,String description,int threshold,String icon) {this.id=id;this.title=title;this.description=description;this.threshold=threshold;this.icon=icon;}
}

