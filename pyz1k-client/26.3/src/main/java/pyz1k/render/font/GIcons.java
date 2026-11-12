/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.render.font;

public enum GIcons {
    FRAME(''),
    KEYBOARD(''),
    CHECKMARK(''),
    LOGO('');

    private final char glyph;

    private GIcons(char glyph) {
        this.glyph = glyph;
    }

    public char glyph() {
        return this.glyph;
    }
}

