package pyz1k.cosmetics;

import net.minecraft.resources.Identifier;

public record CosmeticHat(String id, String name, Source source, Identifier itemModel) {

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

