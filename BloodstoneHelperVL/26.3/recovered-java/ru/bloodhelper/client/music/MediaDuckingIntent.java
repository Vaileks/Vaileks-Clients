/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

record MediaDuckingIntent(long epoch, boolean active, int remainingThousandths) {
    static MediaDuckingIntent create(long epoch, boolean active, double reductionPercent) {
        double reduction = Double.isFinite(reductionPercent) ? Math.clamp(reductionPercent, 0.0, 100.0) : 60.0;
        return new MediaDuckingIntent(Math.max(1L, epoch), active, (int)Math.round((100.0 - reduction) * 10.0));
    }

    String encode() {
        return "duck:" + this.epoch + ":" + (this.active ? 1 : 0) + ":" + this.remainingThousandths;
    }
}

