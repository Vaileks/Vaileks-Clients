/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2371
 *  net.minecraft.class_2480
 *  net.minecraft.class_5632
 *  net.minecraft.class_9288
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_2480;
import net.minecraft.class_5632;
import net.minecraft.class_9288;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ShulkerPreviewSettings;
import ru.bloodhelper.client.modules.ShulkerPreviewLayout;

public final class ShulkerPreview {
    private static class_9288 cachedContents;
    private static Payload cachedPayload;
    private static Optional<class_5632> cachedTooltip;

    private ShulkerPreview() {
    }

    public static Optional<class_5632> forTooltip(class_1799 stack, Optional<class_5632> original, int screenWidth, int screenHeight) {
        List<Object> slots;
        class_1747 item;
        class_1792 class_17922;
        ShulkerPreviewSettings settings = BloodHelperClient.config().shulkerPreview;
        if (!(settings.enabled && original.isEmpty() && !stack.method_7960() && (class_17922 = stack.method_7909()) instanceof class_1747 && (item = (class_1747)class_17922).method_7711() instanceof class_2480)) {
            return original;
        }
        class_9288 contents = (class_9288)stack.method_58695(class_9334.field_49622, (Object)class_9288.field_49334);
        ShulkerPreviewLayout layout = ShulkerPreviewLayout.fit(settings.scale, screenWidth, screenHeight);
        if (contents == cachedContents && cachedPayload != null && cachedPayload.layout().equals(layout)) {
            return cachedTooltip;
        }
        if (contents == cachedContents && cachedPayload != null) {
            slots = cachedPayload.slots();
        } else {
            class_2371 copied = class_2371.method_10213((int)27, (Object)class_1799.field_8037);
            contents.method_57492(copied);
            slots = List.copyOf(copied);
        }
        cachedContents = contents;
        cachedPayload = new Payload(slots, layout);
        cachedTooltip = Optional.of(cachedPayload);
        return cachedTooltip;
    }

    static {
        cachedTooltip = Optional.empty();
    }

    public record Payload(List<class_1799> slots, ShulkerPreviewLayout layout) implements class_5632
    {
    }
}

