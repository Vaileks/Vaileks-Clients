/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class KillEffectSoundCatalog {
    private static final List<Entry> ENTRIES = List.of(new Entry("airship", "\u0414\u0438\u0440\u0438\u0436\u0430\u0431\u043b\u044c", "Airship"), new Entry("anvil", "\u041d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u044f", "Anvil"), new Entry("apple", "\u042f\u0431\u043b\u043e\u043a\u043e", "Apple"), new Entry("arrows", "\u0421\u0442\u0440\u0435\u043b\u044b", "Arrows"), new Entry("bamboo", "\u0411\u0430\u043c\u0431\u0443\u043a", "Bamboo"), new Entry("banana", "\u0411\u0430\u043d\u0430\u043d", "Banana"), new Entry("bats", "\u041b\u0435\u0442\u0443\u0447\u0438\u0435 \u043c\u044b\u0448\u0438", "Bats"), new Entry("bee", "\u041f\u0447\u0435\u043b\u0430", "Bee"), new Entry("big_ufo", "\u0411\u043e\u043b\u044c\u0448\u043e\u0435 \u041d\u041b\u041e", "Big Ufo"), new Entry("bubble_burst", "\u041b\u043e\u043f\u043d\u0443\u0432\u0448\u0438\u0439 \u043f\u0443\u0437\u044b\u0440\u044c", "Bubble Burst"), new Entry("butterfly", "\u0411\u0430\u0431\u043e\u0447\u043a\u0430", "Butterfly"), new Entry("candy", "\u041a\u043e\u043d\u0444\u0435\u0442\u044b", "Candy"), new Entry("chili", "\u041f\u0435\u0440\u0435\u0446 \u0447\u0438\u043b\u0438", "Chili"), new Entry("clouds", "\u041e\u0431\u043b\u0430\u043a\u0430", "Clouds"), new Entry("copper_chains", "\u041c\u0435\u0434\u043d\u044b\u0435 \u0446\u0435\u043f\u0438", "Copper Chains"), new Entry("copper_gears", "\u041c\u0435\u0434\u043d\u044b\u0435 \u0448\u0435\u0441\u0442\u0435\u0440\u0451\u043d\u043a\u0438", "Copper Gears"), new Entry("copper_golem", "\u041c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c", "Copper Golem"), new Entry("dripstone", "\u041a\u0430\u043f\u0435\u043b\u044c\u043d\u0438\u043a", "Dripstone"), new Entry("egg", "\u042f\u0439\u0446\u043e", "Egg"), new Entry("enderman", "\u042d\u043d\u0434\u0435\u0440\u043c\u0435\u043d", "Enderman"), new Entry("flat_ground_trap", "\u041b\u043e\u0432\u0443\u0448\u043a\u0430 \u0432 \u043f\u043e\u043b\u0443", "Flat Ground Trap"), new Entry("flowers", "\u0426\u0432\u0435\u0442\u044b", "Flowers"), new Entry("gravestone", "\u041d\u0430\u0434\u0433\u0440\u043e\u0431\u0438\u0435", "Gravestone"), new Entry("grim_reaper", "\u0416\u043d\u0435\u0446", "Grim Reaper"), new Entry("hammer", "\u041c\u043e\u043b\u043e\u0442", "Hammer"), new Entry("hearts", "\u0421\u0435\u0440\u0434\u0446\u0430", "Hearts"), new Entry("heaven", "\u0420\u0430\u0439", "Heaven"), new Entry("hell", "\u0410\u0434", "Hell"), new Entry("ice_spikes", "\u041b\u0435\u0434\u044f\u043d\u044b\u0435 \u0448\u0438\u043f\u044b", "Ice Spikes"), new Entry("imposter", "\u0418\u043c\u043f\u043e\u0441\u0442\u0435\u0440", "Imposter"), new Entry("katana", "\u041a\u0430\u0442\u0430\u043d\u0430", "Katana"), new Entry("lightning", "\u041c\u043e\u043b\u043d\u0438\u044f", "Lightning"), new Entry("mini_ufo", "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u043e\u0435 \u041d\u041b\u041e", "Mini Ufo"), new Entry("mini_warden", "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u0438\u0439 \u0432\u0430\u0440\u0434\u0435\u043d", "Mini Warden"), new Entry("orbital_strike", "\u041e\u0440\u0431\u0438\u0442\u0430\u043b\u044c\u043d\u044b\u0439 \u0443\u0434\u0430\u0440", "Orbital Strike"), new Entry("pacman", "\u041f\u0430\u043a\u043c\u0430\u043d", "Pacman"), new Entry("paper", "\u0411\u0443\u043c\u0430\u0433\u0430", "Paper"), new Entry("phoenix", "\u0424\u0435\u043d\u0438\u043a\u0441", "Phoenix"), new Entry("pig", "\u0421\u0432\u0438\u043d\u044c\u044f", "Pig"), new Entry("piston", "\u041f\u043e\u0440\u0448\u0435\u043d\u044c", "Piston"), new Entry("plantfood_feasting", "\u041f\u043b\u043e\u0442\u043e\u044f\u0434\u043d\u043e\u0435 \u0440\u0430\u0441\u0442\u0435\u043d\u0438\u0435", "Plantfood Feasting"), new Entry("portal", "\u041f\u043e\u0440\u0442\u0430\u043b", "Portal"), new Entry("rabbit", "\u041a\u0440\u043e\u043b\u0438\u043a", "Rabbit"), new Entry("ragdoll", "\u0422\u0440\u044f\u043f\u0438\u0447\u043d\u0430\u044f \u043a\u0443\u043a\u043b\u0430", "Ragdoll"), new Entry("red_bomb", "\u041a\u0440\u0430\u0441\u043d\u0430\u044f \u0431\u043e\u043c\u0431\u0430", "Red Bomb"), new Entry("robot_terminator", "\u0422\u0435\u0440\u043c\u0438\u043d\u0430\u0442\u043e\u0440", "Robot Terminator"), new Entry("sakura", "\u0421\u0430\u043a\u0443\u0440\u0430", "Sakura"), new Entry("scarecrow", "\u041f\u0443\u0433\u0430\u043b\u043e", "Scarecrow"), new Entry("sculk", "\u0421\u043a\u0430\u043b\u043a", "Sculk"), new Entry("shadow_merge", "\u0421\u043b\u0438\u044f\u043d\u0438\u0435 \u0441 \u0442\u0435\u043d\u044c\u044e", "Shadow Merge"), new Entry("snowman", "\u0421\u043d\u0435\u0433\u043e\u0432\u0438\u043a", "Snowman"), new Entry("star", "\u0417\u0432\u0435\u0437\u0434\u0430", "Star"), new Entry("sun", "\u0421\u043e\u043b\u043d\u0446\u0435", "Sun"), new Entry("tentacle_grasp", "\u0425\u0432\u0430\u0442\u043a\u0430 \u0449\u0443\u043f\u0430\u043b\u0435\u0446", "Tentacle Grasp"), new Entry("time_warp", "\u0418\u0441\u043a\u0430\u0436\u0435\u043d\u0438\u0435 \u0432\u0440\u0435\u043c\u0435\u043d\u0438", "Time Warp"), new Entry("tiny_copper_golem", "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u0438\u0439 \u043c\u0435\u0434\u043d\u044b\u0439 \u0433\u043e\u043b\u0435\u043c", "Tiny Copper Golem"), new Entry("tornado", "\u0422\u043e\u0440\u043d\u0430\u0434\u043e", "Tornado"), new Entry("train", "\u041f\u043e\u0435\u0437\u0434", "Train"), new Entry("trapdoor", "\u041b\u044e\u043a", "Trapdoor"), new Entry("vines", "\u041b\u0438\u0430\u043d\u044b", "Vines"), new Entry("warp_into_unknown", "\u041f\u0435\u0440\u0435\u0445\u043e\u0434 \u0432 \u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e\u0441\u0442\u044c", "Warp Into Unknown"), new Entry("wave", "\u0412\u043e\u043b\u043d\u0430", "Wave"), new Entry("white_arm", "\u0411\u0435\u043b\u0430\u044f \u0440\u0443\u043a\u0430", "White Arm"));
    private static final Set<String> IDS = ENTRIES.stream().map(Entry::id).collect(Collectors.toUnmodifiableSet());
    private static final String SEARCH = ENTRIES.stream().map(e -> e.ru() + " " + e.en()).collect(Collectors.joining(" ")).toLowerCase(Locale.ROOT);

    private KillEffectSoundCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static boolean contains(String id) {
        return id != null && IDS.contains(id);
    }

    public static String searchTerms() {
        return SEARCH;
    }

    public record Entry(String id, String ru, String en) {
        public String event() {
            return "blooddonate:custom." + this.id;
        }

        public List<String> events() {
            return switch (this.id) {
                case "big_ufo", "mini_ufo" -> List.of(this.event(), "minecraft:custom.ufo", "minecraft:custom.ufo_laser");
                case "heaven", "hell", "portal", "pacman", "white_arm", "hammer" -> List.of(this.event(), "minecraft:custom." + this.id);
                case "ragdoll" -> List.of(this.event(), "minecraft:custom.body_hit", "minecraft:custom.squish");
                default -> List.of(this.event());
            };
        }

        public boolean matches(String query) {
            return (this.id + " " + this.ru + " " + this.en).toLowerCase(Locale.ROOT).contains(query);
        }
    }
}

