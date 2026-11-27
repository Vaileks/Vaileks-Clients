/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 *  net.minecraft.network.chat.Style
 */
package ru.bloodhelper.client.access;

import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
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

    public static void tick(Minecraft client) {
    }

    public static void disconnected() {
    }

    public static Component display(Component value, MessageSignature signature) {
        return value;
    }

    public static Component decorateNicknames(Component value) {
        return value;
    }

    public static boolean clicked(Style style, boolean insertion) {
        return false;
    }

    public static void commandSent(String command) {
    }

    public static boolean interceptChat(String message) {
        return false;
    }

    public static void render(String widget, GuiGraphicsExtractor graphics, ClientConfig config, boolean preview) {
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

    public static void openScreen(String section, Screen parent) {
    }

    public static void openAccessScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new ControlStatusScreen());
    }

    public static void close() {
    }
}

