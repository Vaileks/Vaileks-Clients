/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.assistant;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import ru.bloodhelper.client.assistant.AssistantKnowledge;
import ru.bloodhelper.client.policy.ModulePolicy;

public final class AssistantActions {
    private static final Pattern ENABLE = AssistantActions.words("\u0432\u043a\u043b\u044e\u0447\u0438|\u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c|\u0430\u043a\u0442\u0438\u0432\u0438\u0440\u0443\u0439|enable|turn on|switch on");
    private static final Pattern DISABLE = AssistantActions.words("\u0432\u044b\u043a\u043b\u044e\u0447\u0438|\u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u044c|\u043e\u0442\u043a\u043b\u044e\u0447\u0438|\u043e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c|disable|turn off|switch off");
    private static final Pattern OPEN = AssistantActions.words("\u043e\u0442\u043a\u0440\u043e\u0439|\u043e\u0442\u043a\u0440\u044b\u0442\u044c|\u043f\u043e\u043a\u0430\u0436\u0438|open|show");
    private static final Set<String> SCREENS = Set.of("_menu", "_hud", "_themes", "_profiles", "_about");

    private AssistantActions() {
    }

    private static Pattern words(String value) {
        return Pattern.compile("(?U)\\b(?:" + value + ")\\b");
    }

    public static Intent parse(String text, List<AssistantKnowledge.Entry> previous) {
        Kind kind;
        boolean open;
        boolean off;
        String q = text.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        if (AssistantActions.words("\u043a\u0430\u043a|\u043f\u043e\u0447\u0435\u043c\u0443|\u0437\u0430\u0447\u0435\u043c|\u0435\u0441\u043b\u0438|\u043d\u0435\u043b\u044c\u0437\u044f|\u043d\u0435|\u043f\u0438\u0448\u0435\u0442|\u0441\u043a\u0430\u0437\u0430\u043b|\u0441\u043a\u0430\u0436\u0438|\u043f\u043e\u043f\u0440\u043e\u0441\u0438\u043b|\u043f\u0440\u043e\u0441\u0438\u043b|\u043d\u0430\u043f\u0438\u0448\u0438|\u043e\u0431\u044a\u044f\u0441\u043d\u0438|\u0440\u0430\u0441\u0441\u043a\u0430\u0436\u0438|\u043f\u0440\u0438\u043c\u0435\u0440|\u0446\u0438\u0442\u0430\u0442\u0430|write|explain|example|asked|told|how|why|if|not|don't|says|said|quote").matcher(q).find() || q.indexOf(34) >= 0 || q.indexOf(171) >= 0 || q.indexOf(96) >= 0) {
            return new Intent(false, null, List.of());
        }
        boolean on = ENABLE.matcher(q).find();
        int verbs = (on ? 1 : 0) + ((off = DISABLE.matcher(q).find()) ? 1 : 0) + ((open = OPEN.matcher(q).find()) ? 1 : 0);
        if (verbs == 0) {
            return new Intent(false, null, List.of());
        }
        List<AssistantKnowledge.Entry> entries = AssistantKnowledge.candidates(q, true);
        if (entries.isEmpty() && previous.size() == 1 && AssistantActions.words("\u0435\u0433\u043e|\u044d\u0442\u043e|\u0435\u0435|it|that").matcher(q).find()) {
            entries = previous;
        }
        if (verbs != 1 || entries.size() != 1) {
            return new Intent(true, null, entries.stream().limit(2L).toList());
        }
        String id = entries.getFirst().target();
        Kind kind2 = on ? Kind.ENABLE : (kind = off ? Kind.DISABLE : Kind.OPEN);
        if (!(ModulePolicy.MODULES.contains(id) || kind == Kind.OPEN && SCREENS.contains(id))) {
            return new Intent(true, null, entries);
        }
        return new Intent(true, new Request(kind, id), entries);
    }

    public static Result execute(Request request, Controls controls, boolean ru) {
        boolean changed;
        boolean desired;
        if (request == null || request.kind() == null || request.target() == null || !ModulePolicy.MODULES.contains(request.target()) && (request.kind() != Kind.OPEN || !SCREENS.contains(request.target()))) {
            return new Result(false, ru ? "\u041d\u0430\u0437\u043e\u0432\u0438 \u043e\u0434\u0438\u043d \u043c\u043e\u0434\u0443\u043b\u044c \u0438 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435: \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c, \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0438\u043b\u0438 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438." : "Name one module and an action: enable, disable or open settings.");
        }
        String name = AssistantKnowledge.entries().stream().filter(e -> e.id().equals(request.target())).findFirst().map(e -> AssistantKnowledge.title(e, ru)).orElse(request.target());
        if (request.kind() == Kind.OPEN) {
            boolean opened;
            return new Result(opened, (opened = controls.open(request.target())) ? (ru ? "\u041e\u0442\u043a\u0440\u044b\u043b \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438: " : "Opened settings: ") + name : (ru ? "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438: " : "Could not open settings: ") + name);
        }
        boolean bl = desired = request.kind() == Kind.ENABLE;
        if (desired && !controls.allowed(request.target())) {
            return new Result(false, ru ? name + " \u0441\u0435\u0439\u0447\u0430\u0441 \u0437\u0430\u043f\u0440\u0435\u0449\u0451\u043d \u043f\u0440\u0430\u0432\u0438\u043b\u0430\u043c\u0438 \u043a\u043b\u0438\u0435\u043d\u0442\u0430 \u0438\u043b\u0438 Safe Mode. \u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0435\u0433\u043e \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c." : name + " is currently blocked by client policy or Safe Mode. It was not enabled.");
        }
        if (controls.enabled(request.target()) != desired) {
            controls.setEnabled(request.target(), desired);
        }
        boolean bl2 = changed = controls.enabled(request.target()) == desired;
        return new Result(changed, changed ? (ru ? (desired ? "\u0412\u043a\u043b\u044e\u0447\u0438\u043b: " : "\u0412\u044b\u043a\u043b\u044e\u0447\u0438\u043b: ") : (desired ? "Enabled: " : "Disabled: ")) + name : (ru ? "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0438\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443: " : "Could not change the setting: ") + name);
    }

    public record Intent(boolean command, Request request, List<AssistantKnowledge.Entry> related) {
    }

    public record Request(Kind kind, String target) {
    }

    public static enum Kind {
        ENABLE,
        DISABLE,
        OPEN;

    }

    public record Result(boolean success, String text) {
    }

    public static interface Controls {
        public boolean allowed(String var1);

        public boolean enabled(String var1);

        public void setEnabled(String var1, boolean var2);

        public boolean open(String var1);
    }
}

