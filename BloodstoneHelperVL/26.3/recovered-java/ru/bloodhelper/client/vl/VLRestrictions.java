/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.vl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class VLRestrictions {
    private static final Map<Object, Boolean> HIDDEN = Collections.synchronizedMap(new WeakHashMap());

    private VLRestrictions() {
    }

    public static Object[] visibleModules(Object[] objectArray) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Object object : objectArray) {
            if (((Enum)object).name().equals("LEGENDARY_SIZE")) continue;
            arrayList.add(object);
        }
        return arrayList.toArray((Object[])Array.newInstance(objectArray.getClass().getComponentType(), arrayList.size()));
    }

    public static boolean hideButton(String string) {
        return string.equals("about.boosty") || string.equals("policy.releases");
    }

    public static boolean blockedOption(String string) {
        String string2 = string.startsWith("search.") ? string.substring(7) : string;
        return string2.startsWith("legendarySize.") || string2.startsWith("legendaryVisuals.loot") || Set.of("legendaryVisuals.beams", "legendaryVisuals.names", "legendaryVisuals.legendary", "legendaryVisuals.legendaryColor", "legendaryVisuals.totems", "legendaryVisuals.totemColor", "legendaryVisuals.enchantedApples", "legendaryVisuals.appleColor").contains(string2);
    }

    public static Object markRow(Object object, String string) {
        if (VLRestrictions.blockedOption(string)) {
            HIDDEN.put(object, true);
        }
        return object;
    }

    public static List<?> cleanRows(List<?> list) {
        ArrayList arrayList = new ArrayList(list.size());
        Iterator<?> iterator = list.iterator();
        while (iterator.hasNext()) {
            Object obj;
            Object object = obj = iterator.next();
            if (obj.getClass().getSimpleName().equals("SettingHit")) {
                try {
                    Method method = obj.getClass().getDeclaredMethod("row", new Class[0]);
                    method.setAccessible(true);
                    object = method.invoke(obj, new Object[0]);
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    throw new IllegalStateException(reflectiveOperationException);
                }
            }
            if (HIDDEN.containsKey(object)) continue;
            arrayList.add(obj);
        }
        return arrayList;
    }

    public static float safeScale(float f, String string) {
        return 1.0f;
    }

    public static void enforce(Object object) {
        try {
            Object object2;
            Object object3 = object.getClass().getField("legendaryVisuals").get(object);
            if (object3 != null) {
                object3.getClass().getField("loot").setBoolean(object3, false);
            }
            if ((object2 = object.getClass().getField("legendarySize").get(object)) != null) {
                for (String string : List.of("enabled", "enlargeDropped", "scaleAllDropped")) {
                    object2.getClass().getField(string).setBoolean(object2, false);
                }
                object2.getClass().getField("droppedScale").setDouble(object2, 1.0);
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException(reflectiveOperationException);
        }
    }
}

