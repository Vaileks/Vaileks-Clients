/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ChatHistory;
import ru.bloodhelper.client.modules.ChatTools;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.StreamerMode;

public final class ChatHistoryScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 23, "");
    private final List<Hit> visible = new ArrayList<Hit>();
    private long copiedUntil;
    private String copiedText = "";
    private long selectedId;
    private String cachedQuery = "";
    private String cachedLanguage = "";
    private int cachedWidth = -1;
    private List<ChatHistory.Row<Component>> cachedSource = List.of();
    private List<DisplayRow> cachedRows = List.of();

    @Override
    protected int preferredPanelHeight() {
        return ChatTools.size() == 0 || StreamerMode.hideChat() ? 170 : 410;
    }

    public ChatHistoryScreen(Screen parent) {
        super(parent, HudStyle.tr("\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0447\u0430\u0442\u0430", "Chat History"));
        this.search.setTextSize(11.0f);
        this.search.setMaxLength(256);
        this.search.setResponder(value -> {
            this.scroll = 0;
        });
        this.search.setFocused(true);
    }

    protected void init() {
        this.search.setFocused(this.search.isFocused() && !StreamerMode.hideChat());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        this.frame(g, HudStyle.tr("\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0447\u0430\u0442\u0430", "Chat History"), mouseX, mouseY);
        if (StreamerMode.hideChat()) {
            this.search.setFocused(false);
            this.visible.clear();
            this.maximumScroll = 0;
            String label = HudStyle.tr("\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0447\u0430\u0442\u0430 \u0441\u043a\u0440\u044b\u0442\u0430 \u0432 Streamer Mode.", "Chat history is hidden by Streamer Mode.");
            int y = this.top + 60;
            for (String line : ChatHistoryScreen.wrap(label, this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, y, -7105633);
                y += 15;
            }
            this.footer(g, HudStyle.tr("\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u044b \u0438 \u043f\u043e\u044f\u0432\u044f\u0442\u0441\u044f \u043f\u043e\u0441\u043b\u0435 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u0441\u043a\u0440\u044b\u0442\u0438\u044f \u0447\u0430\u0442\u0430.", "Messages are preserved and return when chat hiding is disabled."));
            return;
        }
        int inputWidth = Math.max(20, this.panelWidth - 132);
        this.search.setX(this.left + 22);
        this.search.setY(this.top + 46);
        this.search.setWidth(inputWidth - 16);
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u0432 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u043d\u044b\u0445 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f\u0445\u2026", "Search received messages\u2026"));
        UiDraw.border(g, this.left + 14, this.top + 44, inputWidth, 27, 3, -15592939, this.search.isFocused() ? UiDraw.alpha(BloodHelperClient.config().accent, 160) : -13027008);
        this.search.render(g, mouseX, mouseY, delta);
        this.button(g, this.left + this.panelWidth - 104, this.top + 47, 90, HudStyle.tr("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Clear"), mouseX, mouseY, () -> {
            ChatTools.clear();
            this.scroll = 0;
            this.selectedId = 0L;
        });
        this.bodyTop = this.top + 80;
        this.visible.clear();
        List<ChatHistory.Row<Component>> source = ChatTools.rows(this.search.getValue());
        if (!(source.equals(this.cachedSource) && this.cachedQuery.equals(this.search.getValue()) && this.cachedWidth == this.panelWidth && this.cachedLanguage.equals(BloodHelperClient.config().language))) {
            this.cachedSource = source;
            this.cachedQuery = this.search.getValue();
            this.cachedWidth = this.panelWidth;
            this.cachedLanguage = BloodHelperClient.config().language;
            this.cachedRows = source.reversed().stream().map(row -> {
                List<String> lines = ChatHistoryScreen.wrap(row.latest().text(), Math.max(1, this.panelWidth - 46));
                return new DisplayRow((ChatHistory.Row<Component>)row, lines, 23 + lines.size() * 14);
            }).toList();
        }
        int totalHeight = this.cachedRows.stream().mapToInt(DisplayRow::height).sum();
        this.maximumScroll = Math.max(0, totalHeight - Math.max(1, this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int y = this.bodyTop - this.scroll;
            for (DisplayRow display : this.cachedRows) {
                int end = y + display.height;
                if (end >= this.bodyTop && y < this.bodyBottom) {
                    Object metadata;
                    boolean selected;
                    boolean hovered = mouseX >= this.left + 12 && mouseX < this.left + this.panelWidth - 12 && mouseY >= Math.max(y, this.bodyTop) && mouseY < Math.min(end, this.bodyBottom);
                    boolean bl = selected = display.row.latest().id() == this.selectedId;
                    if (hovered || selected) {
                        UiDraw.rounded(g, this.left + 12, y, this.panelWidth - 24, display.height - 3, 3, selected ? -13621974 : -14540248);
                    }
                    Object object = metadata = BloodHelperClient.config().chatTools.timestamps ? ChatTools.time(display.row.latest().receivedAt()) : "";
                    if (display.row.count() > 1) {
                        metadata = (String)metadata + (((String)metadata).isEmpty() ? "" : "  \u00b7  ") + "\u00d7" + display.row.count();
                    }
                    CustomFont.regular().draw(g, (String)metadata, this.left + 20, y + 4, 9.0f, -7105633);
                    if (hovered) {
                        String copy = HudStyle.tr("\u041a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c", "Copy");
                        CustomFont.regular().draw(g, copy, (float)(this.left + this.panelWidth - 20) - CustomFont.regular().width(copy, 9.0f), y + 4, 9.0f, -1381908);
                    }
                    int lineY = y + 17;
                    for (String line : display.lines) {
                        if (lineY + 13 >= this.bodyTop && lineY < this.bodyBottom) {
                            this.text(g, line, this.left + 20, lineY, -1381908);
                        }
                        lineY += 14;
                    }
                    this.visible.add(new Hit(Math.max(y, this.bodyTop), Math.min(end, this.bodyBottom), display.row));
                }
                y = end;
            }
            if (this.cachedRows.isEmpty()) {
                String label = ChatTools.size() == 0 ? HudStyle.tr("\u041f\u043e\u043b\u0443\u0447\u0435\u043d\u043d\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u043f\u043e\u044f\u0432\u044f\u0442\u0441\u044f \u0437\u0434\u0435\u0441\u044c.", "Received messages will appear here.") : HudStyle.tr("\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b.", "No messages found.");
                this.text(g, CustomFont.regular().ellipsize(label, 11.0f, this.panelWidth - 40), this.left + 20, this.bodyTop + 18, -7105633);
            }
        }
        finally {
            g.disableScissor();
        }
        this.scrollbar(g, totalHeight);
        String footer = System.nanoTime() < this.copiedUntil ? HudStyle.tr("\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043e", "Message copied") : ChatTools.size() + " / " + BloodHelperClient.config().chatTools.maxMessages + "  \u00b7  " + HudStyle.tr("\u041d\u0430\u0436\u043c\u0438 \u0441\u0442\u0440\u043e\u043a\u0443, \u0447\u0442\u043e\u0431\u044b \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u00b7 Ctrl+F: \u043f\u043e\u0438\u0441\u043a", "Click a row to copy \u00b7 Ctrl+F: search");
        this.footer(g, footer);
    }

    private void copy(ChatHistory.Row<Component> row) {
        this.copiedText = row.latest().text();
        this.selectedId = row.latest().id();
        this.minecraft.keyboardHandler.setClipboard(this.copiedText);
        this.copiedUntil = System.nanoTime() + 1500000000L;
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent event, boolean doubled) {
        if (StreamerMode.hideChat()) {
            return super.panelMouseClicked(event, doubled);
        }
        if (this.search.mouseClicked(event, doubled)) {
            return true;
        }
        if (event.button() == 1 && event.x() >= (double)(this.left + 12) && event.x() < (double)(this.left + this.panelWidth - 12)) {
            for (Hit row : this.visible) {
                if (!(event.y() >= (double)row.top) || !(event.y() < (double)row.bottom)) continue;
                this.copy(row.row);
                return true;
            }
        }
        return super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (StreamerMode.hideChat()) {
            return super.panelMouseDragged(event, dx, dy);
        }
        return this.search.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent event) {
        if (StreamerMode.hideChat()) {
            return super.panelMouseReleased(event);
        }
        return this.search.mouseReleased(event) || super.panelMouseReleased(event);
    }

    public boolean keyPressed(KeyEvent event) {
        if (StreamerMode.hideChat()) {
            return super.keyPressed(event);
        }
        if (event.hasControlDown() && event.key() == 9) {
            this.search.setFocused(true);
            return true;
        }
        if (event.hasControlDown() && event.key() == 6 && !this.search.isFocused() && this.selectedId != 0L) {
            this.minecraft.keyboardHandler.setClipboard(this.copiedText);
            this.copiedUntil = System.nanoTime() + 1500000000L;
            return true;
        }
        return this.search.keyPressed(event) || super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return !StreamerMode.hideChat() && this.search.charTyped(event) || super.charTyped(event);
    }

    private record DisplayRow(ChatHistory.Row<Component> row, List<String> lines, int height) {
    }

    private record Hit(int top, int bottom, ChatHistory.Row<Component> row) {
    }
}

