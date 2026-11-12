/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.vl;

public final class PersonalPerformance {
    private PersonalPerformance() {
    }

    private static Object field(Object object, String string) throws ReflectiveOperationException {
        return object.getClass().getField(string).get(object);
    }

    private static void set(Object object, String string, Object object2) throws ReflectiveOperationException {
        object.getClass().getField(string).set(object, object2);
    }

    public static void apply(Object object) {
        try {
            Object object2;
            PersonalPerformance.set(object, "hudMenuEffects", false);
            Object object3 = PersonalPerformance.field(object, "renderQuality");
            if (object3 != null) {
                PersonalPerformance.set(object3, "glassQuality", "LOW");
                PersonalPerformance.set(object3, "shadows", false);
            }
            if ((object2 = PersonalPerformance.field(object, "optimizer")) != null) {
                PersonalPerformance.set(object2, "enabled", true);
                PersonalPerformance.set(object2, "hideGrass", false);
                PersonalPerformance.set(object2, "disableEntityShadows", true);
                PersonalPerformance.set(object2, "decorativeParticles", true);
                PersonalPerformance.set(object2, "signs", true);
                PersonalPerformance.set(object2, "signDistance", 24);
                PersonalPerformance.set(object2, "signTextDistance", 12);
                Object object4 = PersonalPerformance.field(object2, "droppedItems");
                if (object4 != null) {
                    PersonalPerformance.set(object4, "enabled", false);
                }
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException(reflectiveOperationException);
        }
    }
}

