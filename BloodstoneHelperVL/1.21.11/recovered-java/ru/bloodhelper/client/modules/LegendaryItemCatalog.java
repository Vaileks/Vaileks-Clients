/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LegendaryItemCatalog {
    private static final List<Weapon> WEAPONS = List.of(new Weapon("netherite_shield", "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0439 \u0449\u0438\u0442", "Netherite Shield"), new Weapon("artemis_bow", "\u041b\u0443\u043a \u0410\u0440\u0442\u0435\u043c\u0438\u0434\u044b", "Artemis Bow"), new Weapon("bloodsword", "\u041a\u0440\u043e\u0432\u0430\u0432\u043e\u0435 \u043e\u0440\u0443\u0436\u0438\u0435", "Blood Weapon"), new Weapon("cloud_sword", "\u041d\u0435\u0431\u0435\u0441\u043d\u044b\u0439 \u043c\u0435\u0447", "Cloud Sword"), new Weapon("death_scythe", "\u041a\u043e\u0441\u0430 \u0421\u043c\u0435\u0440\u0442\u0438", "Death Scythe"), new Weapon("dragon_katana", "\u041a\u0430\u0442\u0430\u043d\u0430 \u0414\u0440\u0430\u043a\u043e\u043d\u0430", "Dragon Katana"), new Weapon("emerald_blade", "\u0418\u0437\u0443\u043c\u0440\u0443\u0434\u043d\u044b\u0439 \u043a\u043b\u0438\u043d\u043e\u043a", "Emerald Blade"), new Weapon("excalibur", "\u042d\u043a\u0441\u043a\u0430\u043b\u0438\u0431\u0443\u0440", "Excalibur"), new Weapon("frost_sword", "\u041c\u043e\u0440\u043e\u0437\u043d\u044b\u0439 \u043c\u0435\u0447", "Frost Sword"), new Weapon("gamble_sword", "\u041b\u0443\u0434\u043e-\u043c\u0435\u0447", "Gamble Sword"), new Weapon("glove", "\u041f\u0435\u0440\u0447\u0430\u0442\u043a\u0430 \u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438", "Gauntlet of Fortitude"), new Weapon("hypnosis_staff", "\u0416\u0435\u0437\u043b \u0413\u0438\u043f\u043d\u043e\u0437\u0430", "Hypnosis Staff"), new Weapon("judge_sword", "\u0421\u0443\u0434\u043d\u044b\u0439 \u043c\u0435\u0447", "Judge Sword"), new Weapon("laser", "\u041b\u0430\u0437\u0435\u0440", "Laser"), new Weapon("lite_mace", "\u041b\u0451\u0433\u043a\u0430\u044f \u0431\u0443\u043b\u0430\u0432\u0430", "Light Mace"), new Weapon("midas_sword", "\u041c\u0435\u0447 \u041c\u0438\u0434\u0430\u0441\u0430", "Midas Sword"), new Weapon("mirror_blade", "\u0417\u0435\u0440\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u043c\u0435\u0447", "Mirror Blade"), new Weapon("mjolnir", "\u041c\u044c\u0451\u043b\u044c\u043d\u0438\u0440", "Mjolnir"), new Weapon("orbital_nuke", "\u041e\u0440\u0431\u0438\u0442\u0430\u043b\u044c\u043d\u0430\u044f \u043f\u0443\u0448\u043a\u0430", "Orbital Cannon"), new Weapon("poseidon_trident", "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446 \u041f\u043e\u0441\u0435\u0439\u0434\u043e\u043d\u0430", "Poseidon Trident"), new Weapon("rail_gun", "\u0420\u0435\u043b\u044c\u0441\u0430\u0442\u0440\u043e\u043d", "Railgun"), new Weapon("ravager_horn", "\u0420\u043e\u0433 \u0420\u0430\u0437\u043e\u0440\u0438\u0442\u0435\u043b\u044f", "Ravager Horn"), new Weapon("reaper_scythe", "\u041a\u043e\u0441\u0430 \u0416\u043d\u0435\u0446\u0430", "Reaper Scythe"), new Weapon("sculk_crossbow", "\u0421\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442", "Sculk Crossbow"), new Weapon("shadow_blade", "\u0422\u0435\u043d\u0435\u0432\u043e\u0439 \u043a\u043b\u0438\u043d\u043e\u043a", "Shadow Blade"), new Weapon("shrink_ray", "\u0423\u043c\u0435\u043d\u044c\u0448\u0438\u0442\u0435\u043b\u044c", "Shrink Ray"), new Weapon("spider_sword", "\u041f\u0430\u0443\u0447\u0438\u0439 \u043a\u043b\u0438\u043d\u043e\u043a", "Spider Blade"), new Weapon("sword_of_nature", "\u041c\u0435\u0447 \u0436\u0438\u0437\u043d\u0438", "Sword of Life"), new Weapon("time_bow", "\u041b\u0443\u043a \u0412\u0440\u0435\u043c\u0435\u043d\u0438", "Time Bow"), new Weapon("unicorn_horn", "\u0420\u043e\u0433 \u0415\u0434\u0438\u043d\u043e\u0440\u043e\u0433\u0430", "Unicorn Horn"), new Weapon("villager_staff", "\u041f\u043e\u0441\u043e\u0445 \u0416\u0438\u0442\u0435\u043b\u044f", "Villager Staff"));
    private static final Map<String, String> ALIASES = new LinkedHashMap<String, String>();

    private LegendaryItemCatalog() {
    }

    private static void aliases(String id, String ... values) {
        for (String value : values) {
            ALIASES.put(LegendaryItemCatalog.normalize(value), id);
        }
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace("_", "");
    }

    public static List<Weapon> weapons() {
        return WEAPONS;
    }

    public static String type(String value) {
        return value == null || value.length() > 80 ? null : ALIASES.get(LegendaryItemCatalog.normalize(value));
    }

    public static boolean contains(String id) {
        return id != null && WEAPONS.stream().anyMatch(w -> w.id().equals(id));
    }

    public static String model(String value) {
        String tail;
        if (value == null || value.length() > 256) {
            return null;
        }
        if (value.startsWith("bloodstone:tools/")) {
            tail = value.substring("bloodstone:tools/".length());
        } else if (value.startsWith("bloodstone:item/tools/")) {
            tail = value.substring("bloodstone:item/tools/".length());
        } else {
            return null;
        }
        int slash = tail.indexOf(47);
        if (slash < 1) {
            return null;
        }
        if (tail.startsWith("reaper_scythe/death/")) {
            return "death_scythe";
        }
        return LegendaryItemCatalog.type(tail.substring(0, slash));
    }

    public static String translation(String key) {
        String tail;
        if (key == null || !key.endsWith(".name")) {
            return null;
        }
        if (key.startsWith("bloodstone.legendary.")) {
            tail = key.substring("bloodstone.legendary.".length());
        } else if (key.startsWith("legendary.")) {
            tail = key.substring("legendary.".length());
        } else if (key.startsWith("bloodstone.uwlegendary.item.")) {
            tail = key.substring("bloodstone.uwlegendary.item.".length());
        } else {
            return null;
        }
        if (tail.startsWith("uwlegendary.bloodsword.")) {
            return "bloodsword";
        }
        tail = tail.substring(0, tail.length() - ".name".length());
        return LegendaryItemCatalog.type(tail.substring(tail.lastIndexOf(46) + 1));
    }

    static {
        for (Weapon weapon : WEAPONS) {
            ALIASES.put(LegendaryItemCatalog.normalize(weapon.id()), weapon.id());
        }
        LegendaryItemCatalog.aliases("bloodsword", "blood_sword", "blood_mace", "blood_harpoon", "blood_clot");
        LegendaryItemCatalog.aliases("lite_mace", "light_mace", "lyogkayabulava");
        LegendaryItemCatalog.aliases("glove", "gauntlet_of_fortitude", "endurance_glove");
        LegendaryItemCatalog.aliases("orbital_nuke", "orbital_cannon");
        LegendaryItemCatalog.aliases("rail_gun", "railgun", "relsatron");
        LegendaryItemCatalog.aliases("spider_sword", "spider_blade");
        LegendaryItemCatalog.aliases("sword_of_nature", "sword_of_life");
        LegendaryItemCatalog.aliases("emerald_blade", "emerald_sword");
        LegendaryItemCatalog.aliases("excalibur", "excalibur_sword");
        LegendaryItemCatalog.aliases("villager_staff", "villager_wand");
    }

    public record Weapon(String id, String ru, String en) {
    }
}

