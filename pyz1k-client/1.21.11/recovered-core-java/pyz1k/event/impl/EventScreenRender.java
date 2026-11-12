/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 */
package pyz1k.event.impl;

import lombok.Generated;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import pyz1k.event.Event;

public class EventScreenRender
extends Event {
    private final class_332 context;
    private final class_9779 tickCounter;
    private final Stage stage;

    public EventScreenRender(class_332 context, class_9779 tickCounter) {
        this(context, tickCounter, Stage.HUD);
    }

    public EventScreenRender(class_332 context, class_9779 tickCounter, Stage stage) {
        this.context = context;
        this.tickCounter = tickCounter;
        this.stage = stage == null ? Stage.HUD : stage;
    }

    @Generated
    public class_332 getContext() {
        return this.context;
    }

    @Generated
    public class_9779 getTickCounter() {
        return this.tickCounter;
    }

    @Generated
    public Stage getStage() {
        return this.stage;
    }

    public static enum Stage {
        ESP,
        HUD;

    }
}

