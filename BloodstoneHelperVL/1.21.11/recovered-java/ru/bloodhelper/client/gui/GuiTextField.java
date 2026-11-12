/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;

public final class GuiTextField {
    private static final long BLINK_NANOS = 500000000L;
    private static final float CARET_WIDTH = 1.0f;
    private int x;
    private int y;
    private int width;
    private int height;
    private String value = "";
    private String hint;
    private int maxLength = 128;
    private int cursor;
    private int anchor;
    private boolean focused;
    private boolean dragging;
    private boolean draggingWords;
    private int dragWordStart;
    private int dragWordEnd;
    private double dragMouseX;
    private long dragUpdatedAt;
    private long blinkStartedAt = System.nanoTime();
    private float textSize = 14.0f;
    private int textColor = -658446;
    private int hintColor = -7305590;
    private float scrollX;
    private Predicate<String> filter = text -> true;
    private Consumer<String> responder = text -> {};
    private boolean metricsDirty = true;
    private CustomFont measuredFont;
    private float[] advances = new float[]{0.0f};

    public GuiTextField(int x, int y, int width, int height, String hint) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.setHint(hint);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        String next = GuiTextField.cleanText(Objects.requireNonNull(value, "value"), this.maxLength);
        if (this.filter.test(next)) {
            this.changeValue(next, next.length(), next.length());
        }
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        if (this.value.length() > this.maxLength) {
            int end = GuiTextField.boundaryAtOrBefore(this.value, this.maxLength);
            this.changeValue(this.value.substring(0, end), Math.min(this.cursor, end), Math.min(this.anchor, end));
        }
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = Objects.requireNonNull(responder, "responder");
    }

    public void setHint(String hint) {
        this.hint = GuiTextField.cleanText(hint == null ? "" : hint, Integer.MAX_VALUE);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = Math.max(0, width);
    }

    public void setHeight(int height) {
        this.height = Math.max(0, height);
    }

    public void setTextSize(float textSize) {
        if (!Float.isFinite(textSize) || textSize <= 0.0f) {
            throw new IllegalArgumentException("Text size must be finite and positive");
        }
        if (this.textSize != textSize) {
            this.textSize = textSize;
            this.metricsDirty = true;
        }
    }

    public void setTextColor(int argb) {
        this.textColor = argb;
    }

    public void setHintColor(int argb) {
        this.hintColor = argb;
    }

    public void setFocused(boolean focused) {
        if (this.focused != focused) {
            this.resetBlink();
        }
        this.focused = focused;
        if (!focused) {
            this.stopDragging();
        }
    }

    public boolean isFocused() {
        return this.focused;
    }

    public boolean canConsumeInput() {
        return this.focused;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(class_332 graphics, int mouseX, int mouseY, float partialTick) {
        if (this.width <= 0 || this.height <= 0) {
            return;
        }
        CustomFont font = CustomFont.regular();
        this.measure(font);
        this.keepCursorVisible();
        long now = System.nanoTime();
        if (this.focused && this.dragging) {
            this.updateDrag(now);
        }
        float lineHeight = font.lineHeight(this.textSize);
        float textY = (float)this.y + ((float)this.height - lineHeight) * 0.5f;
        float textX = (float)this.x - this.scrollX;
        graphics.method_44379(this.x, this.y, this.x + this.width, this.y + this.height);
        try {
            if (this.focused && this.cursor != this.anchor) {
                GuiTextField.fill(graphics, textX + this.advances[Math.min(this.cursor, this.anchor)], textY, textX + this.advances[Math.max(this.cursor, this.anchor)], textY + lineHeight, -2141428852);
            }
            if (this.value.isEmpty()) {
                font.draw(graphics, this.hint, this.x, textY, this.textSize, this.hintColor);
            } else {
                font.draw(graphics, this.value, textX, textY, this.textSize, this.textColor);
            }
            if (this.focused && (now - this.blinkStartedAt) / 500000000L % 2L == 0L) {
                float caretX = textX + this.advances[this.cursor];
                GuiTextField.fill(graphics, caretX, textY, caretX + 1.0f, textY + lineHeight, ThemeColors.text(this.textColor));
            }
        }
        finally {
            graphics.method_44380();
        }
    }

    public boolean mouseClicked(class_11909 event, boolean doubled) {
        if (event.method_74245() != 0) {
            return false;
        }
        if (!this.contains(event.comp_4798(), event.comp_4799())) {
            this.setFocused(false);
            return false;
        }
        this.setFocused(true);
        this.measure(CustomFont.regular());
        this.keepCursorVisible();
        this.dragging = true;
        this.dragMouseX = event.comp_4798();
        this.dragUpdatedAt = System.nanoTime();
        boolean bl = this.draggingWords = doubled && !event.method_74239() && !this.value.isEmpty();
        if (this.draggingWords) {
            int character = this.characterAt(event.comp_4798());
            this.dragWordStart = this.runStart(character);
            this.dragWordEnd = this.runEnd(character);
            this.anchor = this.dragWordStart;
            this.cursor = this.dragWordEnd;
            this.resetBlink();
        } else {
            this.moveCursor(this.cursorAt(event.comp_4798()), event.method_74239());
        }
        this.keepCursorVisible();
        return true;
    }

    public boolean mouseDragged(class_11909 event, double dx, double dy) {
        if (!this.focused || !this.dragging || event.method_74245() != 0) {
            return false;
        }
        this.measure(CustomFont.regular());
        this.keepCursorVisible();
        this.dragMouseX = event.comp_4798();
        this.updateDrag(System.nanoTime());
        return true;
    }

    public boolean mouseReleased(class_11909 event) {
        if (!this.dragging || event.method_74245() != 0) {
            return false;
        }
        this.stopDragging();
        return true;
    }

    public boolean keyPressed(class_11908 event) {
        if (!this.canConsumeInput()) {
            return false;
        }
        if (event.method_74241()) {
            this.stopDragging();
            this.anchor = 0;
            this.cursor = this.value.length();
            this.resetBlink();
            return true;
        }
        if (event.method_74242()) {
            this.copySelection();
            return true;
        }
        if (event.method_74244()) {
            if (this.cursor != this.anchor) {
                this.copySelection();
                this.replaceRange(Math.min(this.cursor, this.anchor), Math.max(this.cursor, this.anchor), "");
            }
            return true;
        }
        if (event.method_74243()) {
            this.insertText(class_310.method_1551().field_1774.method_1460());
            return true;
        }
        boolean selecting = event.method_74239();
        boolean byWord = event.method_75377();
        switch (event.comp_4795()) {
            case 263: {
                this.stopDragging();
                int next = !selecting && !byWord && this.cursor != this.anchor ? Math.min(this.cursor, this.anchor) : (byWord ? this.previousWord(this.cursor) : this.previousCodepoint(this.cursor));
                this.moveCursor(next, selecting);
                break;
            }
            case 262: {
                this.stopDragging();
                int next = !selecting && !byWord && this.cursor != this.anchor ? Math.max(this.cursor, this.anchor) : (byWord ? this.nextWord(this.cursor) : this.nextCodepoint(this.cursor));
                this.moveCursor(next, selecting);
                break;
            }
            case 268: {
                this.stopDragging();
                this.moveCursor(0, selecting);
                break;
            }
            case 269: {
                this.stopDragging();
                this.moveCursor(this.value.length(), selecting);
                break;
            }
            case 259: {
                this.delete(true, byWord);
                break;
            }
            case 261: {
                this.delete(false, byWord);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    public boolean charTyped(class_11905 event) {
        if (!this.canConsumeInput() || !GuiTextField.allowedCodepoint(event.comp_4793())) {
            return false;
        }
        int modifiers = event.comp_4794();
        if ((modifiers & 8) != 0 || (modifiers & 2) != 0 && (modifiers & 4) == 0) {
            return false;
        }
        this.insertText(event.method_74226());
        return true;
    }

    private void insertText(String text) {
        int start = Math.min(this.cursor, this.anchor);
        int end = Math.max(this.cursor, this.anchor);
        int available = this.maxLength - (this.value.length() - (end - start));
        String insertion = GuiTextField.cleanText(text, available);
        if (!insertion.isEmpty()) {
            this.replaceRange(start, end, insertion);
        }
    }

    private void delete(boolean backwards, boolean byWord) {
        this.stopDragging();
        if (this.cursor != this.anchor) {
            this.replaceRange(Math.min(this.cursor, this.anchor), Math.max(this.cursor, this.anchor), "");
        } else {
            int other = backwards ? (byWord ? this.previousWord(this.cursor) : this.previousCodepoint(this.cursor)) : (byWord ? this.nextWord(this.cursor) : this.nextCodepoint(this.cursor));
            this.replaceRange(Math.min(this.cursor, other), Math.max(this.cursor, other), "");
        }
    }

    private void replaceRange(int start, int end, String insertion) {
        String next = this.value.substring(0, start) + insertion + this.value.substring(end);
        if (this.filter.test(next)) {
            int nextCursor = start + insertion.length();
            this.changeValue(next, nextCursor, nextCursor);
        }
    }

    private void changeValue(String next, int nextCursor, int nextAnchor) {
        boolean changed = !this.value.equals(next);
        this.value = next;
        this.cursor = nextCursor;
        this.anchor = nextAnchor;
        this.metricsDirty |= changed;
        this.stopDragging();
        this.resetBlink();
        if (changed) {
            this.responder.accept(this.value);
        }
    }

    private void copySelection() {
        if (this.cursor != this.anchor) {
            class_310.method_1551().field_1774.method_1455(this.value.substring(Math.min(this.cursor, this.anchor), Math.max(this.cursor, this.anchor)));
        }
    }

    private void moveCursor(int position, boolean selecting) {
        this.cursor = position;
        if (!selecting) {
            this.anchor = position;
        }
        this.resetBlink();
    }

    private int previousCodepoint(int position) {
        return position == 0 ? 0 : this.value.offsetByCodePoints(position, -1);
    }

    private int nextCodepoint(int position) {
        return position == this.value.length() ? position : this.value.offsetByCodePoints(position, 1);
    }

    private int previousWord(int position) {
        while (position > 0 && GuiTextField.wordKind(this.value.codePointBefore(position)) == 0) {
            position = this.previousCodepoint(position);
        }
        return position == 0 ? 0 : this.runStart(this.previousCodepoint(position));
    }

    private int nextWord(int position) {
        if (position < this.value.length()) {
            position = this.runEnd(position);
        }
        while (position < this.value.length() && GuiTextField.wordKind(this.value.codePointAt(position)) == 0) {
            position = this.nextCodepoint(position);
        }
        return position;
    }

    private int runStart(int position) {
        int kind = GuiTextField.wordKind(this.value.codePointAt(position));
        while (position > 0 && GuiTextField.wordKind(this.value.codePointBefore(position)) == kind) {
            position = this.previousCodepoint(position);
        }
        return position;
    }

    private int runEnd(int position) {
        int kind = GuiTextField.wordKind(this.value.codePointAt(position));
        while ((position = this.nextCodepoint(position)) < this.value.length() && GuiTextField.wordKind(this.value.codePointAt(position)) == kind) {
        }
        return position;
    }

    private static int wordKind(int codepoint) {
        if (Character.isWhitespace(codepoint) || Character.isSpaceChar(codepoint)) {
            return 0;
        }
        int type = Character.getType(codepoint);
        return Character.isLetterOrDigit(codepoint) || codepoint == 95 || type == 6 || type == 8 || type == 7 ? 1 : 2;
    }

    private void measure(CustomFont font) {
        if (!this.metricsDirty && this.measuredFont == font) {
            return;
        }
        this.advances = font.caretAdvances(this.value, this.textSize);
        this.measuredFont = font;
        this.metricsDirty = false;
    }

    private float viewportWidth() {
        return Math.max(0.0f, (float)this.width - 1.0f);
    }

    private float maxScroll() {
        return Math.max(0.0f, this.advances[this.value.length()] - this.viewportWidth());
    }

    private void keepCursorVisible() {
        float caretX = this.advances[this.cursor];
        if (caretX < this.scrollX) {
            this.scrollX = caretX;
        }
        if (caretX > this.scrollX + this.viewportWidth()) {
            this.scrollX = caretX - this.viewportWidth();
        }
        this.scrollX = Math.clamp(this.scrollX, 0.0f, this.maxScroll());
    }

    private int cursorAt(double mouseX) {
        double target = mouseX - (double)this.x + (double)this.scrollX;
        int position = 0;
        while (position < this.value.length()) {
            int next = this.nextCodepoint(position);
            if (target < (double)((this.advances[position] + this.advances[next]) * 0.5f)) {
                return position;
            }
            position = next;
        }
        return this.value.length();
    }

    private int characterAt(double mouseX) {
        double target = mouseX - (double)this.x + (double)this.scrollX;
        int position = 0;
        while (position < this.value.length()) {
            int next = this.nextCodepoint(position);
            if (target < (double)this.advances[next]) {
                return position;
            }
            position = next;
        }
        return this.previousCodepoint(this.value.length());
    }

    private void updateDrag(long now) {
        double outside;
        double seconds = Math.clamp((double)(now - this.dragUpdatedAt) / 1.0E9, 0.0, 0.05);
        this.dragUpdatedAt = now;
        double d = this.dragMouseX < (double)this.x ? this.dragMouseX - (double)this.x : (outside = this.dragMouseX >= (double)(this.x + this.width) ? this.dragMouseX - (double)(this.x + this.width) + 1.0 : 0.0);
        if (outside != 0.0) {
            double speed = Math.copySign((double)this.textSize * 6.0 + Math.min(Math.abs(outside), 120.0) * 8.0, outside);
            this.scrollX = (float)Math.clamp((double)this.scrollX + speed * seconds, 0.0, (double)this.maxScroll());
        }
        double pointerX = Math.clamp(this.dragMouseX, (double)this.x, (double)((float)this.x + this.viewportWidth()));
        int next = this.cursorAt(pointerX);
        int nextAnchor = this.anchor;
        if (this.draggingWords && !this.value.isEmpty()) {
            int character = this.characterAt(pointerX);
            if (next < this.dragWordStart) {
                nextAnchor = this.dragWordEnd;
                next = this.runStart(character);
            } else if (next > this.dragWordEnd) {
                nextAnchor = this.dragWordStart;
                next = this.runEnd(character);
            } else {
                nextAnchor = this.dragWordStart;
                next = this.dragWordEnd;
            }
        }
        if (this.cursor != next || this.anchor != nextAnchor) {
            this.cursor = next;
            this.anchor = nextAnchor;
            this.resetBlink();
        }
        this.keepCursorVisible();
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
    }

    private void stopDragging() {
        this.dragging = false;
        this.draggingWords = false;
    }

    private void resetBlink() {
        this.blinkStartedAt = System.nanoTime();
    }

    private static boolean allowedCodepoint(int codepoint) {
        if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint) || codepoint >= 55296 && codepoint <= 57343) {
            return false;
        }
        int type = Character.getType(codepoint);
        return type != 13 && type != 14;
    }

    private static String cleanText(String text, int limit) {
        int length;
        StringBuilder result = new StringBuilder(Math.min(text.length(), Math.max(0, limit)));
        for (int offset = 0; offset < text.length(); offset += length) {
            int codepoint = text.codePointAt(offset);
            length = Character.charCount(codepoint);
            if (!GuiTextField.allowedCodepoint(codepoint)) continue;
            if (length > limit - result.length()) break;
            result.appendCodePoint(codepoint);
        }
        return result.toString();
    }

    private static int boundaryAtOrBefore(String text, int position) {
        if (position > 0 && position < text.length() && Character.isHighSurrogate(text.charAt(position - 1)) && Character.isLowSurrogate(text.charAt(position))) {
            return position - 1;
        }
        return position;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void fill(class_332 graphics, float left, float top, float right, float bottom, int argb) {
        if (right <= left || bottom <= top) {
            return;
        }
        graphics.method_51448().pushMatrix();
        try {
            graphics.method_51448().translate(left, top);
            graphics.method_51448().scale(right - left, bottom - top);
            graphics.method_25294(0, 0, 1, 1, argb);
        }
        finally {
            graphics.method_51448().popMatrix();
        }
    }
}

