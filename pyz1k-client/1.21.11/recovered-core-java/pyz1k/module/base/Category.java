/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.module.base;

import lombok.Generated;

public enum Category {
    VISUALS("Visuals"),
    ITEM_RENDER("ItemRender"),
    HAND_RENDER("HandRender");

    final String name;

    @Generated
    private Category(String name) {
        this.name = name;
    }

    @Generated
    public String getName() {
        return this.name;
    }
}

