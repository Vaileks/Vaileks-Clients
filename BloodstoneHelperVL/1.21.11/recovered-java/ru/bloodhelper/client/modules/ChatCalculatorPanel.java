/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ChatCalculator;

public final class ChatCalculatorPanel {
    private String previous;
    private ChatCalculator.Result result = new ChatCalculator.Result("", ChatCalculator.Error.EMPTY);
    private int originalY = Integer.MIN_VALUE;
    private int rowY;
    private int width;
    private long copiedUntil;

    public boolean active(class_342 input) {
        return input != null && BloodHelperClient.config().chatCalculator.enabled && ChatCalculator.isExpression(input.method_1882());
    }

    private void update(class_342 input) {
        if (!input.method_1882().equals(this.previous)) {
            this.previous = input.method_1882();
            this.result = ChatCalculator.calculate(this.previous);
            this.copiedUntil = 0L;
        }
    }

    public void prepare(class_342 input, int screenWidth, int screenHeight, class_332 graphics) {
        if (input == null) {
            return;
        }
        if (this.originalY == Integer.MIN_VALUE) {
            this.originalY = input.method_46427();
        }
        if (!this.active(input)) {
            input.method_46419(this.originalY);
            return;
        }
        this.update(input);
        this.rowY = Math.max(4, screenHeight - 23);
        this.width = Math.max(80, screenWidth - 4);
        input.method_46419(Math.max(2, this.rowY - 14));
        graphics.method_25294(2, input.method_46427() - 2, screenWidth - 2, this.rowY - 2, -1340465883);
    }

    public void resized() {
        this.originalY = Integer.MIN_VALUE;
    }

    public void render(class_342 input, class_332 g) {
        if (!this.active(input)) {
            return;
        }
        this.update(input);
        g.method_71048();
        g.method_25294(2, this.rowY, this.width + 2, this.rowY + 21, -367452889);
        Object message = this.result.valid() ? "= " + this.result.value() : ChatCalculatorPanel.error(this.result.error());
        int controlsWidth = this.width >= 280 ? 154 : 0;
        String fitted = ChatCalculatorPanel.fit((String)message, Math.max(20, this.width - controlsWidth - 12));
        CustomFont.regular().draw(g, fitted, 8.0f, this.rowY + 6, 10.0f, this.result.valid() ? -4655923 : -12660);
        if (controlsWidth > 0 && this.result.valid()) {
            ChatCalculatorPanel.button(g, this.width - 149, this.rowY + 2, 72, this.copiedUntil > System.nanoTime() ? HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u043e", "Copied") : HudStyle.tr("\u041a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c", "Copy"));
            ChatCalculatorPanel.button(g, this.width - 73, this.rowY + 2, 72, HudStyle.tr("\u0412\u0441\u0442\u0430\u0432\u0438\u0442\u044c", "Insert"));
        }
    }

    public boolean key(class_342 input, class_11908 event) {
        if (!this.active(input)) {
            return false;
        }
        this.update(input);
        if (event.method_74230()) {
            if (this.result.valid()) {
                this.copy();
            }
            return true;
        }
        if (event.method_74236()) {
            if (this.result.valid()) {
                this.insert(input);
            }
            return true;
        }
        return false;
    }

    public boolean click(class_342 input, class_11909 event) {
        if (!this.active(input) || event.method_74245() != 0 || event.comp_4799() < (double)this.rowY || event.comp_4799() >= (double)(this.rowY + 21)) {
            return false;
        }
        this.update(input);
        if (this.width >= 280 && this.result.valid()) {
            if (event.comp_4798() >= (double)(this.width - 149) && event.comp_4798() < (double)(this.width - 77)) {
                this.copy();
            }
            if (event.comp_4798() >= (double)(this.width - 73) && event.comp_4798() < (double)(this.width - 1)) {
                this.insert(input);
            }
        }
        return true;
    }

    private void insert(class_342 input) {
        input.method_1852(this.result.value());
        input.method_1872(false);
        input.method_25365(true);
    }

    private void copy() {
        class_310.method_1551().field_1774.method_1455(this.result.value());
        this.copiedUntil = System.nanoTime() + 1500000000L;
    }

    private static void button(class_332 g, int x, int y, int width, String label) {
        g.method_25294(x, y, x + width, y + 17, -13353648);
        CustomFont.regular().draw(g, label, x + 5, y + 4, 9.0f, -1577479);
    }

    private static String fit(String text, int width) {
        int end;
        if (CustomFont.regular().width(text, 10.0f) <= (float)width) {
            return text;
        }
        for (end = text.length(); end > 1 && CustomFont.regular().width(text.substring(0, end) + "\u2026", 10.0f) > (float)width; --end) {
        }
        return text.substring(0, end) + "\u2026";
    }

    private static String error(ChatCalculator.Error error) {
        return switch (error) {
            default -> throw new MatchException(null, null);
            case ChatCalculator.Error.NONE -> "";
            case ChatCalculator.Error.EMPTY -> HudStyle.tr("\u041f\u0440\u0438\u043c\u0435\u0440: = 64 * 230 \u00b7 Enter \u2014 \u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u00b7 Tab \u2014 \u0432\u0441\u0442\u0430\u0432\u0438\u0442\u044c", "Example: = 64 * 230 \u00b7 Enter copies \u00b7 Tab inserts");
            case ChatCalculator.Error.SYNTAX -> HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0438 \u0441\u043a\u043e\u0431\u043a\u0438", "Check the expression and parentheses");
            case ChatCalculator.Error.DIVISION_BY_ZERO -> HudStyle.tr("\u0414\u0435\u043b\u0435\u043d\u0438\u0435 \u043d\u0430 \u043d\u043e\u043b\u044c", "Division by zero");
            case ChatCalculator.Error.TOO_COMPLEX -> HudStyle.tr("\u0421\u043b\u0438\u0448\u043a\u043e\u043c \u0441\u043b\u043e\u0436\u043d\u043e\u0435 \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Expression is too complex");
            case ChatCalculator.Error.TOO_LARGE -> HudStyle.tr("\u0421\u043b\u0438\u0448\u043a\u043e\u043c \u0431\u043e\u043b\u044c\u0448\u043e\u0435 \u0447\u0438\u0441\u043b\u043e", "Number is too large");
        };
    }
}

