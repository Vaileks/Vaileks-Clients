/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.assistant.AnswerReveal;
import ru.bloodhelper.client.assistant.AssistantActions;
import ru.bloodhelper.client.assistant.AssistantAssets;
import ru.bloodhelper.client.assistant.AssistantKnowledge;
import ru.bloodhelper.client.assistant.AssistantService;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.OutlineSurface;
import ru.bloodhelper.client.gui.ScrollBarDrag;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;

public final class AiAssistantPanel {
    public static final int WIDTH = 320;
    public static final int GAP = 12;
    private static final int TOP = 80;
    private static final int BOTTOM = 402;
    private final GuiTextField input = new GuiTextField(0, 0, 220, 20, "");
    private final Consumer<String> navigate;
    private final Function<AssistantActions.Request, AssistantActions.Result> execute;
    private final Function<String, String> catalog;
    private final List<Action> actions = new ArrayList<Action>();
    private final List<Card> cards = new ArrayList<Card>();
    private List<AssistantService.Message> measuredMessages = List.of();
    private ClientConfig config;
    private int left;
    private int contentHeight;
    private double scroll;
    private final ScrollBarDrag scrollBar = new ScrollBarDrag();
    private final AnswerReveal reveal = new AnswerReveal();
    private AssistantService.Message revealing;
    private boolean followBottom = true;
    private boolean opened;
    private boolean confirmInstall;
    private boolean measuredRu = true;

    public AiAssistantPanel(Consumer<String> navigate, Function<String, String> catalog, Function<AssistantActions.Request, AssistantActions.Result> execute) {
        this.navigate = navigate;
        this.catalog = catalog;
        this.execute = execute;
        this.input.setMaxLength(400);
        this.input.setTextSize(12.0f);
    }

    private boolean ru() {
        return this.config == null || !"en".equals(this.config.language);
    }

    private String tr(String russian, String english) {
        return this.ru() ? russian : english;
    }

    public boolean focused() {
        return this.opened && this.input.isFocused();
    }

    public void blur() {
        this.input.setFocused(false);
    }

    public void close() {
        if (!this.opened) {
            return;
        }
        this.opened = false;
        this.scrollBar.release();
        this.scrollBar.hide();
        this.confirmInstall = false;
        this.input.setFocused(false);
        this.actions.clear();
        this.reveal.finish();
        AssistantService.get().closePanel();
    }

    public void render(GuiGraphicsExtractor g, int x, ClientConfig config, double mx, double my) {
        boolean setup;
        this.left = x;
        this.config = config;
        this.opened = true;
        AssistantService service = AssistantService.get();
        service.open();
        AssistantService.Snapshot state = service.snapshot();
        this.actions.clear();
        this.scrollBar.hide();
        if ("LIQUID_GLASS".equals(config.surfaceStyle)) {
            LiquidGlass.surfaceMenu(g, x, 0, 320, 500, 14, ThemeColors.surface(-233564902), config);
        } else if ("OUTLINE".equals(config.surfaceStyle)) {
            OutlineSurface.draw(g, x, 0, 320, 500, 8, 255, config, true);
        } else {
            UiDraw.border(g, x, 0, 320, 500, 8, ThemeColors.surface(-15461096), ThemeColors.line());
        }
        AiAssistantPanel.inset(g, x + 10, 10, 300, 58);
        AiAssistantPanel.inset(g, x + 10, 76, 300, 330);
        AiAssistantPanel.inset(g, x + 10, 414, 300, 76);
        UiDraw.rounded(g, x + 10, 28, 3, 21, 1, config.accent);
        AiAssistantPanel.text(g, this.tr("\u0418\u0418-\u043f\u043e\u043c\u043e\u0449\u043d\u0438\u043a", "AI assistant"), x + 22, 19, 14, -2039324);
        AiAssistantPanel.text(g, "Qwen2.5 \u00b7 0.49B \u00b7 CPU", x + 22, 42, 10, -6775902);
        this.button(g, x + 320 - 80, 20, 58, 22, this.tr("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Clear"), mx, my, () -> {
            service.clearConversation();
            this.scroll = 0.0;
        });
        String status = switch (state.stage()) {
            default -> throw new MatchException(null, null);
            case AssistantService.Stage.CHECKING -> this.tr("\u041f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043a\u0430\u2026", "Preparing\u2026");
            case AssistantService.Stage.MISSING -> this.tr("\u041c\u043e\u0434\u0435\u043b\u044c \u043d\u0435 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430", "Model not installed");
            case AssistantService.Stage.INSTALLING -> this.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430: ", "Downloading: ") + Math.round(state.progress() * 100.0) + "%";
            case AssistantService.Stage.STARTING -> this.tr("\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u044e \u043c\u043e\u0434\u0435\u043b\u044c \u0432 \u043f\u0430\u043c\u044f\u0442\u044c\u2026", "Loading model into memory\u2026");
            case AssistantService.Stage.ANSWERING -> this.tr("\u0414\u0443\u043c\u0430\u044e \u043d\u0430\u0434 \u043e\u0442\u0432\u0435\u0442\u043e\u043c\u2026", "Thinking about your message\u2026");
            case AssistantService.Stage.READY -> {
                if (this.reveal.active(System.nanoTime())) {
                    yield this.tr("\u041f\u0438\u0448\u0443 \u043e\u0442\u0432\u0435\u0442\u2026", "Writing the answer\u2026");
                }
                if (service.resident()) {
                    yield this.tr("\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u043e \u00b7 \u043c\u043e\u0434\u0435\u043b\u044c \u0432 \u043f\u0430\u043c\u044f\u0442\u0438", "Local \u00b7 model in memory");
                }
                yield this.tr("\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u0430\u044f \u0441\u043f\u0440\u0430\u0432\u043a\u0430", "Local help");
            }
            case AssistantService.Stage.UNSUPPORTED -> this.tr("\u0418\u0418: Windows/Linux x64 \u00b7 \u0441\u043f\u0440\u0430\u0432\u043a\u0430 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430", "AI: Windows/Linux x64 \u00b7 help available");
            case AssistantService.Stage.ERROR -> this.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0432\u0435\u0440\u0448\u0438\u0442\u044c \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435", "Could not finish the action");
        };
        AiAssistantPanel.text(g, status, x + 20, 390, 10, state.stage() == AssistantService.Stage.ERROR ? -19574 : -6775902);
        boolean bl = setup = this.confirmInstall || !state.installed() && state.messages().isEmpty();
        if (setup) {
            this.setup(g, state, mx, my);
        } else {
            this.conversation(g, state, mx, my);
        }
        if (state.stage() == AssistantService.Stage.INSTALLING) {
            UiDraw.rounded(g, x + 20, 382, 280, 3, 1, ThemeColors.line());
            UiDraw.rounded(g, x + 20, 382, (int)(280.0 * state.progress()), 3, 1, config.accent);
        }
        UiDraw.border(g, x + 20, 423, 280, 25, 5, ThemeColors.surface(-15855853), this.input.isFocused() ? config.accent : ThemeColors.line());
        this.input.setX(x + 27);
        this.input.setY(426);
        this.input.setWidth(266);
        this.input.setHint(this.tr("\u041d\u0430\u043f\u0438\u0448\u0438 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0438\u043b\u0438 \u043f\u0440\u043e\u0441\u044c\u0431\u0443\u2026", "Chat or request a setting change\u2026"));
        this.input.setTextColor(ThemeColors.text(-2039324));
        this.input.setHintColor(ThemeColors.text(-9275778));
        this.input.render(g, (int)mx, (int)my, 0.0f);
        if (state.busy()) {
            this.button(g, x + 20, 456, 134, 25, this.tr("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c", "Stop"), mx, my, service::cancel);
        } else if (state.installed()) {
            this.button(g, x + 20, 456, 134, 25, this.tr("\u0421\u043f\u0440\u043e\u0441\u0438\u0442\u044c \u0418\u0418", "Ask AI"), mx, my, () -> this.submit(true));
        } else if (AssistantAssets.supported()) {
            this.button(g, x + 20, 456, 134, 25, this.tr("\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0418\u0418", "Install AI"), mx, my, () -> {
                this.confirmInstall = true;
            });
        } else {
            AiAssistantPanel.text(g, this.tr("\u0418\u0418 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 \u044d\u0442\u043e\u0439 \u041e\u0421", "AI unavailable on this OS"), x + 20, 463, 10, -6775902);
        }
        if (!state.busy()) {
            this.button(g, x + 162, 456, 138, 25, this.tr("\u041d\u0430\u0439\u0442\u0438 \u0432 \u0441\u043f\u0440\u0430\u0432\u043a\u0435", "Search help"), mx, my, () -> this.submit(false));
        }
    }

    private void setup(GuiGraphicsExtractor g, AssistantService.Snapshot state, double mx, double my) {
        int y = 94;
        AiAssistantPanel.text(g, this.tr("\u041f\u043e\u043c\u043e\u0449\u044c \u043f\u043e BloodHelper", "BloodHelper help"), this.left + 22, y, 14, -2039324);
        y += 28;
        String description = this.tr("\u041c\u043e\u0436\u043d\u043e \u043e\u0431\u0449\u0430\u0442\u044c\u0441\u044f, \u0438\u0441\u043a\u0430\u0442\u044c \u0441\u043f\u0440\u0430\u0432\u043a\u0443 \u0438 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044f\u043c\u0438: \u00ab\u0432\u043a\u043b\u044e\u0447\u0438 Crosshair\u00bb, \u00ab\u0432\u044b\u043a\u043b\u044e\u0447\u0438 Motion Blur\u00bb, \u00ab\u043e\u0442\u043a\u0440\u043e\u0439 \u0445\u0438\u0442\u0431\u043e\u043a\u0441\u044b\u00bb.", "Chat, search help or control modules: enable Crosshair, disable Motion Blur, open hitboxes.");
        y = this.paragraph(g, description, this.left + 22, y, 276, 12) + 18;
        y = this.paragraph(g, AssistantAssets.requirements(this.ru()), this.left + 22, y, 276, 11) + 12;
        y = this.paragraph(g, this.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0438\u0442\u0441\u044f \u0432\u043d\u0435 \u043c\u0435\u043d\u044e. \u041c\u043e\u0434\u0435\u043b\u044c \u043e\u0441\u0442\u0430\u0451\u0442\u0441\u044f \u0432 \u043f\u0430\u043c\u044f\u0442\u0438 \u0434\u043e \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u043c\u043e\u0434\u0443\u043b\u044f. \u0412\u043e\u043f\u0440\u043e\u0441\u044b \u043e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u044e\u0442\u0441\u044f \u043b\u043e\u043a\u0430\u043b\u044c\u043d\u043e.", "Downloads continue outside the menu. The model stays resident until disabled. Questions are processed locally."), this.left + 22, y, 276, 11) + 12;
        if (this.confirmInstall && !state.busy()) {
            this.button(g, this.left + 22, 342, 180, 28, this.tr("\u0421\u043a\u0430\u0447\u0430\u0442\u044c \u0438 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c", "Download and install"), mx, my, () -> {
                this.confirmInstall = false;
                AssistantService.get().install();
            });
            this.button(g, this.left + 210, 342, 88, 28, this.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my, () -> {
                this.confirmInstall = false;
            });
        } else if (!state.busy()) {
            AiAssistantPanel.text(g, this.tr("\u0421\u043f\u0440\u0430\u0432\u043a\u0430 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0431\u0435\u0437 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0418\u0418.", "Help works without installing AI."), this.left + 22, 348, 11, -6775902);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void conversation(GuiGraphicsExtractor g, AssistantService.Snapshot state, double mx, double my) {
        if (!this.measuredMessages.equals(state.messages()) || this.measuredRu != this.ru()) {
            Object last;
            if (!(state.messages().isEmpty() || ((AssistantService.Message)(last = state.messages().getLast())).user() || last == this.revealing || this.measuredMessages.contains(last))) {
                this.revealing = last;
                this.reveal.begin(((AssistantService.Message)last).text(), System.nanoTime(), this.config.animations);
            }
            this.cards.clear();
            for (AssistantService.Message message : state.messages()) {
                ArrayList<String> lines = new ArrayList<String>();
                for (String string : message.text().split("\n", -1)) {
                    if (string.isBlank()) {
                        lines.add("");
                        continue;
                    }
                    lines.addAll(CustomFont.regular().wrap(string, 12.0f, 252.0f));
                }
                int height = 30 + lines.size() * 16 + (!message.user() && !message.related().isEmpty() ? 29 : 0) + 8;
                this.cards.add(new Card(message, List.copyOf(lines), height));
            }
            this.measuredMessages = state.messages();
            this.measuredRu = this.ru();
        }
        int viewBottom = state.stage() == AssistantService.Stage.ERROR ? 330 : 382;
        int viewport = viewBottom - 80;
        long now = System.nanoTime();
        if (!this.config.animations) {
            this.reveal.finish();
        }
        boolean thinking = state.stage() == AssistantService.Stage.STARTING || state.stage() == AssistantService.Stage.ANSWERING;
        this.contentHeight = 8;
        for (Card card : this.cards) {
            this.contentHeight += this.liveHeight(card, now) + 8;
        }
        if (thinking) {
            this.contentHeight += 66;
        }
        if (this.followBottom) {
            this.scroll = Math.max(0, this.contentHeight - viewport);
        }
        this.scroll = Math.clamp(this.scroll, 0.0, (double)Math.max(0, this.contentHeight - viewport));
        if (this.cards.isEmpty() && !thinking) {
            this.paragraph(g, this.tr("\u0421\u043f\u0440\u043e\u0441\u0438, \u043a\u0430\u043a \u043d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c \u0440\u0430\u0441\u0442\u044f\u0436\u0435\u043d\u0438\u0435, \u043f\u0435\u0440\u0435\u0434\u0430\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433 \u0438\u043b\u0438 \u0438\u0437\u043c\u0435\u043d\u0438\u0442\u044c HUD.", "Ask how to stretch the view, share a config or arrange the HUD."), this.left + 22, 100, 276, 12);
        }
        g.enableScissor(this.left + 14, 80, this.left + 320 - 14, viewBottom);
        int yy = 88 - (int)this.scroll;
        try {
            for (Card card : this.cards) {
                int height = this.liveHeight(card, now);
                int y = yy;
                yy += height + 8;
                if (y + height < 80 || y > viewBottom) continue;
                AssistantService.Message message = card.message();
                UiDraw.rounded(g, this.left + 18, y, 284, height, 7, ThemeColors.surface(message.user() ? -14342094 : -15131613));
                String author = message.user() ? this.tr("\u0422\u044b", "You") : (message.generated() ? this.tr("\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a BloodHelper", "BloodHelper assistant") : this.tr("\u0421\u043f\u0440\u0430\u0432\u043a\u0430 BloodHelper", "BloodHelper help"));
                AiAssistantPanel.text(g, author, this.left + 28, y + 8, 10, message.user() ? -6775902 : this.config.accent);
                if (!message.user()) {
                    this.clippedButton(g, this.left + 320 - 72, y + 5, 43, 18, this.tr("\u041a\u043e\u043f\u0438\u044f", "Copy"), mx, my, viewBottom, () -> Minecraft.getInstance().keyboardHandler.setClipboard(message.text()));
                }
                int ty = y + 28;
                int remaining = message == this.revealing && this.reveal.active(now) ? this.reveal.visible(now) : Integer.MAX_VALUE;
                for (String line : card.lines()) {
                    int points = line.codePointCount(0, line.length());
                    if (remaining <= 0) break;
                    String visible = AnswerReveal.prefix(line, remaining);
                    if (ty + 16 >= 80 && ty < viewBottom) {
                        AiAssistantPanel.text(g, visible, this.left + 28, ty, 12, -2039324);
                    }
                    remaining -= Math.max(1, points);
                    ty += 16;
                }
                if (message.user() || message.related().isEmpty() || message == this.revealing && this.reveal.active(now)) continue;
                AssistantKnowledge.Entry entry = message.related().getFirst();
                String title = this.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c: ", "Open: ") + AssistantKnowledge.title(entry, this.ru());
                this.clippedButton(g, this.left + 28, ty + 3, 264, 23, title, mx, my, viewBottom, () -> this.navigate.accept(entry.target()));
            }
            if (thinking) {
                UiDraw.rounded(g, this.left + 18, yy, 284, 58, 7, ThemeColors.surface(-15131613));
                this.thinkingFace(g, this.left + 28, yy + 14, now);
                AiAssistantPanel.text(g, this.tr("\u0414\u0443\u043c\u0430\u044e", "Thinking") + ".".repeat(this.config.animations ? (int)(now / 350000000L % 3L) + 1 : 3), this.left + 56, yy + 10, 12, -2039324);
                AiAssistantPanel.text(g, state.stage() == AssistantService.Stage.STARTING ? this.tr("\u041f\u043e\u0434\u0433\u043e\u0442\u0430\u0432\u043b\u0438\u0432\u0430\u044e \u043c\u043e\u0434\u0435\u043b\u044c", "Preparing the model") : this.tr("\u0418\u0449\u0443 \u043f\u043e\u0434\u0445\u043e\u0434\u044f\u0449\u0443\u044e \u0441\u0442\u0430\u0442\u044c\u044e", "Searching the documentation"), this.left + 56, yy + 30, 10, -6775902);
            }
        }
        finally {
            g.disableScissor();
        }
        int n = Math.max(0, this.contentHeight - viewport);
        this.scrollBar.configure(this.left + 320 - 17, 80.0, 3.0, viewport, 22.0, this.scroll, n);
        if (n > 0) {
            int thumb = Math.max(22, viewport * viewport / Math.max(1, this.contentHeight));
            int sy = 80 + (int)((double)(viewport - thumb) * this.scroll / (double)n);
            UiDraw.rounded(g, this.left + 320 - 17, 80, 3, viewport, 1, ThemeColors.line());
            UiDraw.rounded(g, this.left + 320 - 17, sy, 3, thumb, 1, ThemeColors.text(-6775902));
        }
        if (state.stage() == AssistantService.Stage.ERROR) {
            String error = switch (state.issue()) {
                case "linux_engine" -> this.tr("\u041d\u0435 \u0437\u0430\u043f\u0443\u0441\u0442\u0438\u043b\u0441\u044f \u0434\u0432\u0438\u0436\u043e\u043a Linux. \u041d\u0443\u0436\u043d\u044b glibc 2.34+, libstdc++, OpenSSL 3, libgomp, zlib \u0438 zstd.", "Linux engine failed to start. Requires glibc 2.34+, libstdc++, OpenSSL 3, libgomp, zlib and zstd.");
                case "linux_execute" -> this.tr("\u041d\u0435\u0442 \u043f\u0440\u0430\u0432\u0430 \u0437\u0430\u043f\u0443\u0441\u043a\u0430 \u0434\u0432\u0438\u0436\u043a\u0430. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u043f\u0440\u0430\u0432\u0430 \u0438 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440 noexec \u0443 \u043f\u0430\u043f\u043a\u0438 \u0438\u0433\u0440\u044b.", "Engine cannot execute. Check permissions and the game folder mount noexec option.");
                case "download" -> this.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043d\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0441\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435 \u0438 \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0435 \u043c\u0435\u0441\u0442\u043e, \u0437\u0430\u0442\u0435\u043c \u043f\u043e\u0432\u0442\u043e\u0440\u0438 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0443.", "Download did not finish. Check your connection and disk space, then retry setup.");
                case "timeout" -> this.tr("\u041e\u0442\u0432\u0435\u0442 \u0437\u0430\u043d\u044f\u043b \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0434\u043e\u043b\u0433\u043e. \u041f\u0440\u043e\u0446\u0435\u0441\u0441 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d; \u043f\u043e\u043f\u0440\u043e\u0431\u0443\u0439 \u0431\u043e\u043b\u0435\u0435 \u043a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u0432\u043e\u043f\u0440\u043e\u0441.", "The answer took too long. The process was stopped; try a shorter question.");
                default -> this.tr("\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u0434\u0432\u0438\u0436\u043e\u043a \u043d\u0435 \u043e\u0442\u0432\u0435\u0442\u0438\u043b. \u041c\u043e\u0436\u043d\u043e \u043f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c \u0437\u0430\u043f\u0440\u043e\u0441 \u0438\u043b\u0438 \u043f\u0435\u0440\u0435\u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u043a\u043e\u043c\u043f\u043e\u043d\u0435\u043d\u0442\u044b.", "The local engine did not respond. Retry the question or reinstall the components.");
            };
            this.paragraph(g, error, this.left + 22, 332, 276, 10);
            if (AssistantAssets.supported()) {
                this.button(g, this.left + 22, 365, 125, 20, this.tr("\u041f\u0435\u0440\u0435\u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c", "Reinstall"), mx, my, () -> {
                    this.confirmInstall = true;
                });
            }
        }
    }

    private int liveHeight(Card card, long now) {
        if (card.message() != this.revealing || !this.reveal.active(now)) {
            return card.height();
        }
        int remaining = this.reveal.visible(now);
        int lines = 0;
        for (String line : card.lines()) {
            if (remaining <= 0) break;
            remaining -= Math.max(1, line.codePointCount(0, line.length()));
            ++lines;
        }
        return 38 + Math.max(1, lines) * 16;
    }

    private void thinkingFace(GuiGraphicsExtractor g, int x, int y, long now) {
        int bob = this.config.animations ? (int)Math.round(Math.sin((double)now / 2.5E8)) : 0;
        UiDraw.rounded(g, x, y += bob, 20, 20, 10, -12458);
        g.fill(x + 5, y + 6, x + 7, y + 8, -10864866);
        g.fill(x + 13, y + 7, x + 15, y + 9, -10864866);
        g.fill(x + 4, y + 3, x + 8, y + 4, -8631006);
        g.fill(x + 12, y + 4, x + 16, y + 5, -8631006);
        g.fill(x + 7, y + 13, x + 13, y + 14, -8631006);
        UiDraw.rounded(g, x + 11, y + 13, 7, 9, 3, -18114);
        UiDraw.rounded(g, x + 8, y + 16, 8, 4, 2, -11156);
    }

    private void submit(boolean ai) {
        String question = this.input.getValue().strip();
        if (question.isEmpty()) {
            return;
        }
        AssistantService service = AssistantService.get();
        AssistantService.Snapshot state = service.snapshot();
        if (state.busy() || ai && !state.installed()) {
            return;
        }
        AssistantKnowledge.Context context = AssistantKnowledge.find(question, this.ru(), this.catalog.apply(question));
        if (!service.handleCommand(question, this.ru(), this.execute)) {
            if (ai) {
                service.ask(question, context, this.ru(), this.config.aiAssistant.threads);
            } else {
                service.help(question, context, this.ru());
            }
        }
        this.input.setValue("");
        this.confirmInstall = false;
        this.followBottom = true;
        this.scroll = Double.MAX_VALUE;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (!this.opened) {
            return false;
        }
        if (event.x() < (double)this.left || event.x() >= (double)(this.left + 320) || event.y() < 0.0 || event.y() >= 500.0) {
            this.blur();
            return false;
        }
        if (event.button() != 1) {
            return true;
        }
        if (this.scrollBar.press(event.x(), event.y())) {
            this.scroll = this.scrollBar.value();
            this.followBottom = false;
            this.blur();
            return true;
        }
        if (this.input.mouseClicked(event, doubled)) {
            return true;
        }
        for (int i = this.actions.size() - 1; i >= 0; --i) {
            if (!this.actions.get(i).contains(event.x(), event.y())) continue;
            this.actions.get(i).run().run();
            return true;
        }
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.opened && this.scrollBar.drag(event.y())) {
            this.scroll = this.scrollBar.value();
            this.followBottom = false;
            return true;
        }
        return this.opened && this.input.mouseDragged(event, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.scrollBar.release()) {
            return true;
        }
        return this.opened && this.input.mouseReleased(event);
    }

    public boolean mouseScrolled(double x, double y, double amount) {
        if (!this.opened || x < (double)this.left || x >= (double)(this.left + 320) || y < 80.0 || y >= 402.0) {
            return false;
        }
        this.scroll = Math.clamp(this.scroll - amount * 30.0, 0.0, (double)Math.max(0, this.contentHeight - 300));
        this.followBottom = this.scroll >= (double)(Math.max(0, this.contentHeight - 300) - 8);
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (!this.focused()) {
            return false;
        }
        if (event.key() == 258) {
            this.blur();
            return true;
        }
        if (event.key() == 257 || event.key() == 335) {
            this.submit(AssistantService.get().snapshot().installed());
            return true;
        }
        return this.input.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return this.focused() && this.input.charTyped(event);
    }

    private static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        UiDraw.rounded(g, x, y, w, h, 9, ThemeColors.surface(-988934123));
        UiDraw.outline(g, x, y, w, h, 9, ThemeColors.line());
    }

    private static void text(GuiGraphicsExtractor g, String value, int x, int y, int size, int color) {
        CustomFont.regular().draw(g, value, x, y, size, ThemeColors.text(color));
    }

    private int paragraph(GuiGraphicsExtractor g, String value, int x, int y, int width, int size) {
        for (String line : CustomFont.regular().wrap(value, size, width)) {
            AiAssistantPanel.text(g, line, x, y, size, -6775902);
            y += size + 4;
        }
        return y;
    }

    private void button(GuiGraphicsExtractor g, int x, int y, int w, int h, String title, double mx, double my, Runnable run) {
        Action action = new Action(x, y, w, h, run);
        boolean hot = action.contains(mx, my);
        UiDraw.border(g, x, y, w, h, 5, ThemeColors.surface(hot ? -13354683 : -14407630), hot ? this.config.accent : ThemeColors.line());
        AiAssistantPanel.text(g, CustomFont.regular().ellipsize(title, 10.0f, w - 14), x + 7, y + (h - 10) / 2, 10, -2039324);
        this.actions.add(action);
    }

    private void clippedButton(GuiGraphicsExtractor g, int x, int y, int w, int h, String title, double mx, double my, int bottom, Runnable run) {
        int previous = this.actions.size();
        this.button(g, x, y, w, h, title, mx, my, run);
        if (y < 80 || y + h > bottom) {
            this.actions.remove(previous);
        }
    }

    private record Card(AssistantService.Message message, List<String> lines, int height) {
    }

    private record Action(int x, int y, int w, int h, Runnable run) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }
}

