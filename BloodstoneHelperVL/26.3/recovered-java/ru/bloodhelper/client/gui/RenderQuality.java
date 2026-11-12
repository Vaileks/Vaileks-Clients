/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.RenderQualitySettings;

public final class RenderQuality {
    private static final RenderQualitySettings DEFAULTS = new RenderQualitySettings();

    private RenderQuality() {
    }

    public static RenderQualitySettings settings() {
        ClientConfig config = BloodHelperClient.config();
        return config == null || config.renderQuality == null ? DEFAULTS : config.renderQuality;
    }

    public static boolean shadows() {
        return RenderQuality.settings().shadows;
    }

    public static boolean diagnostics() {
        return RenderQuality.settings().diagnostics;
    }

    public static double animationMillis(double normalMillis) {
        return RenderQuality.settings().animationMillis(normalMillis);
    }
}

