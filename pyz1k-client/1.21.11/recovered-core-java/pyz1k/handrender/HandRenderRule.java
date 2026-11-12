/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.handrender;

import java.util.UUID;

public final class HandRenderRule {
    public String id = UUID.randomUUID().toString();
    public String itemId = "minecraft:diamond_sword";
    public boolean checkId = true;
    public boolean checkName;
    public String nameContains = "";
    public float handScale = 1.0f;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
}

