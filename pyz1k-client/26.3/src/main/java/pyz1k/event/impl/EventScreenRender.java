/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package pyz1k.event.impl;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.event.Event;

public class EventScreenRender
extends Event {
    private final GuiGraphicsExtractor context;
    private final DeltaTracker tickCounter;
    private final Stage stage;

    public EventScreenRender(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        this(context, tickCounter, Stage.HUD);
    }

    public EventScreenRender(GuiGraphicsExtractor context, DeltaTracker tickCounter, Stage stage) {
        this.context = context;
        this.tickCounter = tickCounter;
        this.stage = stage == null ? Stage.HUD : stage;
    }

    public GuiGraphicsExtractor getContext() {
        return this.context;
    }

    public DeltaTracker getTickCounter() {
        return this.tickCounter;
    }

    public Stage getStage() {
        return this.stage;
    }

    public static enum Stage {
        ESP,
        HUD;

    }
}

