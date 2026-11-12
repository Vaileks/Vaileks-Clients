/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.render.font;

public enum GFonts {
    REGULAR("inter-regular"),
    MEDIUM("inter-medium"),
    SEMIBOLD("inter-semibold"),
    BOLD("inter-bold"),
    OPAL_MEDIUM("productsans-medium"),
    OPAL_BOLD("productsans-bold"),
    OPAL_ICONS("materialicons-regular"),
    NOTIFICATION_ICONS("pyz1k-notification-icons");

    private final String family;

    private GFonts(String family) {
        this.family = family;
    }

    public String family() {
        return this.family;
    }
}

