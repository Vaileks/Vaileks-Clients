/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.base;


public enum Category {
    VISUALS("Visuals"),
    ITEM_RENDER("ItemRender"),
    HAND_RENDER("HandRender");

    final String name;

    private Category(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

