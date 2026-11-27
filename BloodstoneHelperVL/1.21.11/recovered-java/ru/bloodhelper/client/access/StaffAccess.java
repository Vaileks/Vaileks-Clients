/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_7469
 */
package ru.bloodhelper.client.access;

import java.nio.file.Path;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7469;
import ru.bloodhelper.client.access.ClientControlService;
import ru.bloodhelper.client.access.ControlStatusScreen;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.policy.ClientControlPolicy;

public final class StaffAccess {
    private StaffAccess() {
    }

    public static void initialize(Path directory) {
    }

    public static void register(Path directory) {
    }

    public static boolean active() {
        return false;
    }

    public static boolean activeFor(Class<?> caller) {
        return false;
    }

    public static String uid() {
        return ClientControlPolicy.publicUid();
    }

    public static String status() {
        return "REMOVED";
    }

    static String detail() {
        return "";
    }

    public static void retry() {
        ClientControlService.retry();
    }

    public static void tick(class_310 client) {
    }

    public static void disconnected() {
    }

    public static class_2561 display(class_2561 value, class_7469 signature) {
        return value;
    }

    public static class_2561 decorateNicknames(class_2561 value) {
        return value;
    }

    public static boolean clicked(class_2583 style, boolean insertion) {
        return false;
    }

    public static void commandSent(String command) {
    }

    public static boolean interceptChat(String message) {
        return false;
    }

    public static void render(String widget, class_332 graphics, ClientConfig config, boolean preview) {
    }

    public static boolean hasFeature(String feature) {
        return false;
    }

    public static boolean hudClick(double x, double y, int button) {
        return false;
    }

    public static void resetTimer() {
    }

    public static void resetCounters() {
    }

    public static void openScreen(String section, class_437 parent) {
    }

    public static void openAccessScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new ControlStatusScreen());
    }

    public static void close() {
    }
}

