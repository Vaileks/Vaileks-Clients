/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LegendaryCooldownData {
    static final int MAX_METADATA_KEYS = 128;
    private static final Pattern COOLDOWN_KEY = Pattern.compile("bloodstone:[a-z0-9_]{1,112}cooldown");
    private static final Pattern WEAPON_TYPE = Pattern.compile("[a-z0-9_]{1,64}");
    private static final Pattern MODEL = Pattern.compile("bloodstone:tools/([a-z0-9_]{1,64})/[a-z0-9_./-]{1,128}");
    private static final Map<String, Meaning> KNOWN = Map.of("bloodstone:uwlegendary_lyogkayabulava_primarycooldown", new Meaning("lyogkayabulava", "BOOST"), "bloodstone:teleportcooldown", new Meaning("dragonkatana", "TELEPORT"), "bloodstone:shadowcooldown", new Meaning("shadowblade", "SHADOW"), "bloodstone:shieldcooldown", new Meaning("excalibur", "SHIELD"), "bloodstone:hornandmore_shrink_ray_abilitycooldown", new Meaning("shrinkray", "SHRINK"), "bloodstone:reaperscythemeleecooldown", new Meaning("reaperscythe", "REAP"), "bloodstone:hornandmore_poseidon_abilitycooldown", new Meaning("poseidontrident", "WAVE"));

    private LegendaryCooldownData() {
    }

    static Optional<Weapon> parse(Map<String, ?> pdc, String itemModel, String baseItem) {
        String itemKey;
        byte[] uuid;
        Object stamp;
        if (pdc == null || pdc.size() > 128 || !Byte.valueOf((byte)1).equals(pdc.get("bloodstone:islegendaryweapon"))) {
            return Optional.empty();
        }
        ArrayList<Timer> timers = new ArrayList<Timer>();
        for (Map.Entry<String, ?> entry : pdc.entrySet()) {
            Object obj;
            String key = entry.getKey();
            if (key == null || !COOLDOWN_KEY.matcher(key).matches() || !((obj = entry.getValue()) instanceof Long) || (Long)(stamp = (Long)obj) <= 0L) continue;
            Meaning known = KNOWN.get(key);
            timers.add(new Timer(key, (Long)stamp, known == null ? "ABILITY" : known.icon()));
        }
        if (timers.isEmpty()) {
            return Optional.empty();
        }
        timers.sort(Comparator.comparing(Timer::abilityKey));
        String weapon = LegendaryCooldownData.weaponKey(pdc, itemModel, timers);
        stamp = pdc.get("bloodstone:uuid");
        if (stamp instanceof byte[] && (uuid = (byte[])stamp).length > 0 && uuid.length <= 256) {
            itemKey = "uuid:" + String.valueOf(UUID.nameUUIDFromBytes(uuid));
        } else {
            String base = baseItem != null && baseItem.length() <= 128 ? baseItem : "unknown";
            itemKey = weapon + "/" + base;
        }
        return Optional.of(new Weapon(weapon, itemKey, List.copyOf(timers)));
    }

    private static String weaponKey(Map<String, ?> pdc, String itemModel, List<Timer> timers) {
        Matcher matcher;
        Object type;
        Object obj = pdc.get("ulegendary:weapon_type");
        if (obj instanceof String && WEAPON_TYPE.matcher((CharSequence)(type = (String)obj)).matches()) {
            return "bloodstone:" + (String)type;
        }
        for (Timer timer : timers) {
            Meaning known = KNOWN.get(timer.abilityKey());
            if (known == null) continue;
            return "bloodstone:" + known.weapon();
        }
        if (itemModel != null && (matcher = MODEL.matcher(itemModel)).matches()) {
            return "bloodstone:" + matcher.group(1);
        }
        return "bloodstone:legendary";
    }

    private record Meaning(String weapon, String icon) {
    }

    record Timer(String abilityKey, long deadlineEpochTicks, String icon) {
    }

    record Weapon(String weaponKey, String itemKey, List<Timer> timers) {
    }
}

