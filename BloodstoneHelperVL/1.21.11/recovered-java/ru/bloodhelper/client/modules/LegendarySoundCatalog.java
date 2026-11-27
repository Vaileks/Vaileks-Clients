/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LegendarySoundCatalog {
    private static final List<Weapon> WEAPONS = List.of(new Weapon("gamble", "\u041b\u0443\u0434\u043e-\u043c\u0435\u0447", "Gamble Sword", List.of(LegendarySoundCatalog.entry("gamble.jackpot_start", "\u0414\u0436\u0435\u043a\u043f\u043e\u0442: \u043d\u0430\u0447\u0430\u043b\u043e", "Jackpot: start", "\u0417\u0432\u0443\u043a \u043d\u0430\u0447\u0430\u043b\u0430 \u0434\u0436\u0435\u043a\u043f\u043e\u0442\u0430", "Jackpot activation sound", "bloodstone:gamble_sword.gamble_sword2"), LegendarySoundCatalog.entry("gamble.jackpot_loop1", "\u0414\u0436\u0435\u043a\u043f\u043e\u0442: \u043c\u0443\u0437\u044b\u043a\u0430 1", "Jackpot: music 1", "\u041f\u0435\u0440\u0432\u044b\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 \u043c\u0443\u0437\u044b\u043a\u0438 \u0434\u0436\u0435\u043a\u043f\u043e\u0442\u0430", "First jackpot music variant", "bloodstone:gamble_sword.gamble_sword3"), LegendarySoundCatalog.entry("gamble.jackpot_loop2", "\u0414\u0436\u0435\u043a\u043f\u043e\u0442: \u043c\u0443\u0437\u044b\u043a\u0430 2", "Jackpot: music 2", "\u0412\u0442\u043e\u0440\u043e\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 \u043c\u0443\u0437\u044b\u043a\u0438 \u0434\u0436\u0435\u043a\u043f\u043e\u0442\u0430", "Second jackpot music variant", "bloodstone:gamble_sword.gamble_sword4"))), new Weapon("laser", "\u041b\u0430\u0437\u0435\u0440", "Laser", List.of(LegendarySoundCatalog.entry("laser.start", "\u041b\u0443\u0447: \u043d\u0430\u0447\u0430\u043b\u043e", "Beam: start", "\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043b\u0443\u0447\u0430", "Beam activation", "bloodstone:laser.laser_start"), LegendarySoundCatalog.entry("laser.loop1", "\u041b\u0443\u0447: \u0437\u0432\u0443\u043a 1", "Beam: sound 1", "\u041f\u0435\u0440\u0432\u044b\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 \u0437\u0432\u0443\u043a\u0430 \u043b\u0443\u0447\u0430", "First beam sound variant", "bloodstone:laser.laser1"), LegendarySoundCatalog.entry("laser.loop2", "\u041b\u0443\u0447: \u0437\u0432\u0443\u043a 2", "Beam: sound 2", "\u0412\u0442\u043e\u0440\u043e\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 \u0437\u0432\u0443\u043a\u0430 \u043b\u0443\u0447\u0430", "Second beam sound variant", "bloodstone:laser.laser2"), LegendarySoundCatalog.entry("laser.finish", "\u041b\u0443\u0447: \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435", "Beam: end", "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043b\u0443\u0447\u0430", "Beam deactivation", "bloodstone:laser.laser_finish"))), new Weapon("bloodsword", "\u041a\u0440\u043e\u0432\u0430\u0432\u044b\u0439 \u043c\u0435\u0447", "Blood Sword", List.of(LegendarySoundCatalog.entry("bloodsword.transition", "\u0421\u043c\u0435\u043d\u0430 \u0438 \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435 \u0444\u043e\u0440\u043c", "Form change / end", "\u041e\u0431\u0449\u0438\u0439 \u0437\u0432\u0443\u043a \u0442\u0440\u0451\u0445 \u0441\u0442\u0430\u0434\u0438\u0439 \u0444\u043e\u0440\u043c\u044b", "Shared by three form transitions", "bloodstone:bloodsword.transition"))), new Weapon("glove", "\u041f\u0435\u0440\u0447\u0430\u0442\u043a\u0430 \u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438", "Resilience Glove", List.of(LegendarySoundCatalog.entry("glove.hit", "\u0423\u0434\u0430\u0440 \u043f\u0435\u0440\u0447\u0430\u0442\u043a\u043e\u0439", "Glove hit", "\u0417\u0432\u0443\u043a \u0443\u0434\u0430\u0440\u0430", "Impact sound", "bloodstone:glove.hit"), LegendarySoundCatalog.shared("glove.totem_break", "\u0420\u0430\u0437\u0440\u0443\u0448\u0435\u043d\u0438\u0435 / \u0438\u0437\u0432\u043b\u0435\u0447\u0435\u043d\u0438\u0435 \u0442\u043e\u0442\u0435\u043c\u0430", "Totem break / removal", "minecraft:block.respawn_anchor.deplete"), LegendarySoundCatalog.shared("glove.totem_insert", "\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u0442\u043e\u0442\u0435\u043c\u0430", "Totem insertion", "minecraft:block.beacon.power_select"))), new Weapon("railgun", "\u0420\u0435\u043b\u044c\u0441\u0430\u0442\u0440\u043e\u043d", "Railgun", List.of(LegendarySoundCatalog.entry("railgun.shot", "\u0412\u044b\u0441\u0442\u0440\u0435\u043b", "Shot", "\u0417\u0432\u0443\u043a \u0432\u044b\u0441\u0442\u0440\u0435\u043b\u0430 \u0440\u0435\u043b\u044c\u0441\u0430\u0442\u0440\u043e\u043d\u0430", "Railgun firing sound", "bloodstone:rail_gun.shot"), LegendarySoundCatalog.entry("railgun.headshot", "\u041f\u043e\u043f\u0430\u0434\u0430\u043d\u0438\u0435 \u0432 \u0433\u043e\u043b\u043e\u0432\u0443", "Headshot", "\u0417\u0432\u0443\u043a\u043e\u0432\u043e\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u0435 \u043f\u043e\u043f\u0430\u0434\u0430\u043d\u0438\u044f", "Headshot sound feedback", "bloodstone:rail_gun.headshot"), LegendarySoundCatalog.shared("railgun.reload", "\u041f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0430: \u043d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u044f", "Reload: anvil", "minecraft:block.anvil.land", "minecraft:block.anvil_land"))), new Weapon("unicorn", "\u0420\u043e\u0433 \u0415\u0434\u0438\u043d\u043e\u0440\u043e\u0433\u0430", "Unicorn Horn", List.of(LegendarySoundCatalog.entry("unicorn.presence", "\u041f\u0440\u0438\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u0435 \u0435\u0434\u0438\u043d\u043e\u0440\u043e\u0433\u0430", "Unicorn presence", "\u0417\u0432\u0443\u043a \u043f\u0440\u0438\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u044f", "Presence sound", "bloodstone:unicorn_horn.presence"), LegendarySoundCatalog.shared("unicorn.summon", "\u041f\u0440\u0438\u0437\u044b\u0432 \u0435\u0434\u0438\u043d\u043e\u0440\u043e\u0433\u0430", "Unicorn summon", "minecraft:entity.horse.angry"))), new Weapon("sculk", "\u0421\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442", "Sculk Crossbow", List.of(LegendarySoundCatalog.shared("sculk.sonic", "\u0417\u0432\u0443\u043a\u043e\u0432\u043e\u0439 \u0443\u0434\u0430\u0440 \u0438 \u0437\u0430\u0440\u044f\u0434", "Sonic boom and charge", "minecraft:entity.warden.sonic_boom", "minecraft:entity.warden.sonic_charge"), LegendarySoundCatalog.shared("sculk.infection", "\u0421\u043a\u0430\u043b\u043a: \u043a\u0440\u0438\u043a \u0438 \u0440\u0430\u0441\u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0435\u043d\u0438\u0435", "Sculk: shriek and spread", "minecraft:block.sculk_shrieker.shriek", "minecraft:block.sculk_catalyst.bloom", "minecraft:block.sculk.spread"), LegendarySoundCatalog.shared("sculk.crossbow", "\u0417\u0430\u0440\u044f\u0434\u043a\u0430 \u0438 \u0432\u044b\u0441\u0442\u0440\u0435\u043b \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430", "Crossbow loading and firing", "minecraft:item.crossbow.shoot", "minecraft:item.crossbow.loading_start", "minecraft:item.crossbow.loading_middle", "minecraft:item.crossbow.loading_end", "minecraft:item.crossbow.quick_charge_1", "minecraft:item.crossbow.quick_charge_2", "minecraft:item.crossbow.quick_charge_3"))), new Weapon("cloud", "\u041d\u0435\u0431\u0435\u0441\u043d\u044b\u0439 \u043c\u0435\u0447", "Cloud Sword", List.of(LegendarySoundCatalog.shared("cloud.activation", "\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435", "Activation", "minecraft:block.beacon.activate"), LegendarySoundCatalog.shared("cloud.deactivation", "\u041e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435", "Deactivation", "minecraft:block.beacon.deactivate"), LegendarySoundCatalog.shared("cloud.trail", "\u0421\u043d\u0435\u0436\u043d\u044b\u0439 \u0441\u043b\u0435\u0434", "Snow trail", "minecraft:block.snow.place"), LegendarySoundCatalog.shared("cloud.overheat", "\u041f\u0435\u0440\u0435\u0433\u0440\u0435\u0432", "Overheat", "minecraft:entity.blaze.hurt"))), new Weapon("spider", "\u041f\u0430\u0443\u0447\u0438\u0439 \u041a\u043b\u0438\u043d\u043e\u043a", "Spider Blade", List.of(LegendarySoundCatalog.shared("spider.ability", "\u0421\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c", "Ability", "minecraft:entity.spider.death"))), new Weapon("light_mace", "\u041b\u0451\u0433\u043a\u0430\u044f \u0411\u0443\u043b\u0430\u0432\u0430", "Light Mace", List.of(LegendarySoundCatalog.shared("light_mace.burst", "\u041f\u043e\u0440\u044b\u0432 \u0432\u0435\u0442\u0440\u0430", "Wind burst", "minecraft:entity.wind_charge.wind_burst"))), new Weapon("orbital", "\u041e\u0440\u0431\u0438\u0442\u0430\u043b\u044c\u043d\u0430\u044f \u041f\u0443\u0448\u043a\u0430", "Orbital Cannon", List.of(LegendarySoundCatalog.entry("orbital.nuclear", "\u042f\u0434\u0435\u0440\u043d\u044b\u0439 \u0432\u0437\u0440\u044b\u0432", "Nuclear explosion", "\u0417\u0432\u0443\u043a \u044f\u0434\u0435\u0440\u043d\u043e\u0433\u043e \u0432\u0437\u0440\u044b\u0432\u0430 \u0438\u0437 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0433\u043e \u043f\u0430\u043a\u0430", "Server-pack nuclear explosion", "minecraft:custom.nuclear_explosion"), LegendarySoundCatalog.entry("orbital.radiation", "\u0420\u0430\u0434\u0438\u0430\u0446\u0438\u044f", "Radiation", "\u0417\u0432\u0443\u043a \u0440\u0430\u0434\u0438\u0430\u0446\u0438\u0438 \u0438\u0437 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0433\u043e \u043f\u0430\u043a\u0430", "Server-pack radiation sound", "minecraft:custom.radiation"))), new Weapon("mirror", "\u0417\u0435\u0440\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u041c\u0435\u0447", "Mirror Sword", List.of(LegendarySoundCatalog.shared("mirror.activation", "\u0421\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c", "Ability", "minecraft:block.beacon.activate"))), new Weapon("death_scythe", "\u041a\u043e\u0441\u0430 \u0421\u043c\u0435\u0440\u0442\u0438", "Death Scythe", List.of(LegendarySoundCatalog.shared("death_scythe.ability", "\u0421\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c", "Ability", "minecraft:entity.wither.hurt", "minecraft:entity_wither_hurt"))));
    private static final List<Entry> ENTRIES;
    private static final Map<String, String> EVENT_TO_ABILITY;
    private static final String SEARCH_TERMS;

    private static Entry entry(String id, String ru, String en, String noteRu, String noteEn, String event) {
        return new Entry(id, ru, en, noteRu, noteEn, List.of(event));
    }

    private static Entry shared(String id, String ru, String en, String ... events) {
        return new Entry(id, ru, en, "\u041e\u0431\u0449\u0438\u0439 \u0437\u0432\u0443\u043a Minecraft: \u0442\u0438\u0448\u0435 \u0438 \u0432\u043d\u0435 \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u0438", "Shared Minecraft sound: also quieter outside this ability", List.of(events));
    }

    private LegendarySoundCatalog() {
    }

    public static List<Weapon> weapons() {
        return WEAPONS;
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Map<String, String> eventToAbility() {
        return EVENT_TO_ABILITY;
    }

    public static String searchTerms() {
        return SEARCH_TERMS;
    }

    static {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        LinkedHashMap<String, String> events = new LinkedHashMap<String, String>();
        StringBuilder search = new StringBuilder();
        HashSet<String> ids = new HashSet<String>();
        for (Weapon weapon : WEAPONS) {
            search.append(weapon.ru).append(' ').append(weapon.en).append(' ');
            for (Entry entry : weapon.entries) {
                if (!ids.add(entry.id)) {
                    throw new IllegalStateException("Duplicate ability " + entry.id);
                }
                entries.add(entry);
                search.append(entry.abilityRu).append(' ').append(entry.abilityEn).append(' ');
                for (String event : entry.events) {
                    events.putIfAbsent(event, entry.id);
                }
            }
        }
        ENTRIES = List.copyOf(entries);
        EVENT_TO_ABILITY = Map.copyOf(events);
        SEARCH_TERMS = search.toString().toLowerCase(Locale.ROOT);
    }

    public record Entry(String id, String abilityRu, String abilityEn, String noteRu, String noteEn, List<String> events) {
        public Entry {
            events = List.copyOf(events);
        }
    }

    public record Weapon(String id, String ru, String en, List<Entry> entries) {
        public Weapon {
            entries = List.copyOf(entries);
        }

        public boolean matches(String query) {
            if (query.isBlank()) {
                return false;
            }
            if ((this.ru + " " + this.en).toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
            return this.entries.stream().anyMatch(e -> (e.abilityRu + " " + e.abilityEn).toLowerCase(Locale.ROOT).contains(query));
        }
    }
}

