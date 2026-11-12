/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package pyz1k.killeffects;

import java.util.Set;
import net.minecraft.class_2960;
import pyz1k.Pyz1k;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.module.impl.visuals.KillEffects;

public final class ServerKillSoundFilter {
    private static final Set<String> SOUNDS = Set.of("blooddonate:custom.airship", "blooddonate:custom.anvil", "blooddonate:custom.apple", "blooddonate:custom.arrows", "blooddonate:custom.bamboo", "blooddonate:custom.banana", "blooddonate:custom.bats", "blooddonate:custom.bee", "blooddonate:custom.big_ufo", "blooddonate:custom.bubble_burst", "blooddonate:custom.butterfly", "blooddonate:custom.candy", "blooddonate:custom.chili", "blooddonate:custom.clouds", "blooddonate:custom.copper_chains", "blooddonate:custom.copper_gears", "blooddonate:custom.copper_golem", "blooddonate:custom.dripstone", "blooddonate:custom.egg", "blooddonate:custom.enderman", "blooddonate:custom.flat_ground_trap", "blooddonate:custom.flowers", "blooddonate:custom.gravestone", "blooddonate:custom.grim_reaper", "blooddonate:custom.hammer", "blooddonate:custom.hearts", "blooddonate:custom.heaven", "blooddonate:custom.hell", "blooddonate:custom.ice_spikes", "blooddonate:custom.imposter", "blooddonate:custom.katana", "blooddonate:custom.lightning", "blooddonate:custom.mini_ufo", "blooddonate:custom.mini_warden", "blooddonate:custom.orbital_strike", "blooddonate:custom.pacman", "blooddonate:custom.paper", "blooddonate:custom.phoenix", "blooddonate:custom.pig", "blooddonate:custom.piston", "blooddonate:custom.plantfood_feasting", "blooddonate:custom.portal", "blooddonate:custom.rabbit", "blooddonate:custom.ragdoll", "blooddonate:custom.red_bomb", "blooddonate:custom.robot_terminator", "blooddonate:custom.sakura", "blooddonate:custom.scarecrow", "blooddonate:custom.sculk", "blooddonate:custom.shadow_merge", "blooddonate:custom.snowman", "blooddonate:custom.star", "blooddonate:custom.sun", "blooddonate:custom.tentacle_grasp", "blooddonate:custom.time_warp", "blooddonate:custom.tiny_copper_golem", "blooddonate:custom.tornado", "blooddonate:custom.train", "blooddonate:custom.trapdoor", "blooddonate:custom.vines", "blooddonate:custom.warp_into_unknown", "blooddonate:custom.wave", "blooddonate:custom.white_arm", "minecraft:custom.body_hit", "minecraft:custom.hammer", "minecraft:custom.heaven", "minecraft:custom.helicopter", "minecraft:custom.hell", "minecraft:custom.nuclear_explosion", "minecraft:custom.pacman", "minecraft:custom.portal", "minecraft:custom.radiation", "minecraft:custom.squish", "minecraft:custom.ufo", "minecraft:custom.ufo_laser", "minecraft:custom.white_arm");

    private ServerKillSoundFilter() {
    }

    public static boolean shouldMute(class_2960 sound, double x, double y, double z) {
        Pyz1k client = Pyz1k.getInstance();
        if (client == null || sound == null || !SOUNDS.contains(sound.toString())) {
            return false;
        }
        KillEffects module = client.getModuleManager().getModule(KillEffects.class);
        if (module == null || !module.isEnable()) {
            return false;
        }
        if (KillEffectEngine.nearRecentKill(x, y, z)) {
            return module.muteServerSounds();
        }
        return module.muteOtherServerSounds() && KillEffectEngine.nearOtherKill(x, y, z, 64.0);
    }
}

