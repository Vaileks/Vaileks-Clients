/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.render.renderers.pipeline;

import lombok.Generated;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public final class PipelineInitializer {
    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            initialized = true;
            PipelineRegistry.init();
        }
    }

    private PipelineInitializer() {
    }

    @Generated
    public static boolean isInitialized() {
        return initialized;
    }
}

