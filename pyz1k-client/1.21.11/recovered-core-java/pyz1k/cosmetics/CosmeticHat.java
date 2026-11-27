/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package pyz1k.cosmetics;

import net.minecraft.class_2960;

public record CosmeticHat(String id, String name, Source source, class_2960 itemModel) {

    public static enum Source {
        HOPLITE("Hoplite"),
        BLOODSTONE("Bloodstone");

        private final String label;

        private Source(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }
}

