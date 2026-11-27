/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.modules.LocalMusicScreen;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.music.MediaService;
import ru.bloodhelper.client.music.MediaSources;

public final class MediaSourceScreen
extends ModuleDetailScreen {
    public MediaSourceScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u041c\u0443\u0437\u044b\u043a\u0430\u043b\u044c\u043d\u043e\u0435 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435", "Music application"));
    }

    @Override
    protected int preferredPanelHeight() {
        return 330;
    }

    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u041c\u0443\u0437\u044b\u043a\u0430\u043b\u044c\u043d\u043e\u0435 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435", "Music application"), mx, my);
        MediaService service = BloodHelperClient.media();
        MediaSources catalog = service.sources();
        String selected = BloodHelperClient.config().music.preferredSource;
        String description = selected.isEmpty() ? HudStyle.tr("\u0410\u0432\u0442\u043e \u2014 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0441\u0438\u0441\u0442\u0435\u043c\u043d\u044b\u0439 \u043f\u043b\u0435\u0435\u0440", "Auto \u2014 active system player") : HudStyle.tr("\u0417\u0430\u043a\u0440\u0435\u043f\u043b\u0451\u043d: ", "Pinned: ") + MediaSources.labelFor(selected);
        this.text(g, CustomFont.regular().ellipsize(description, 11.0f, this.panelWidth - 28), this.left + 14, this.bodyTop, -1381908);
        int listTop = this.bodyTop + 26;
        int savedTop = this.bodyTop;
        this.bodyTop = listTop;
        this.scrollbar(g, 36 * (catalog.apps().size() + 1));
        g.method_44379(this.left + 8, this.bodyTop, this.left + this.panelWidth - 8, this.bodyBottom);
        int y = this.bodyTop - this.scroll;
        this.sourceRow(g, "", HudStyle.tr("\u0410\u0432\u0442\u043e", "Auto"), HudStyle.tr("\u0421\u043b\u0435\u0434\u043e\u0432\u0430\u0442\u044c \u0437\u0430 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u043c \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435\u043c", "Follow the active application"), y, selected, mx, my);
        y += 36;
        for (MediaSources.Source source : catalog.apps()) {
            Object label = "bloodhelper:local".equals(source.id()) ? HudStyle.tr("\u041c\u043e\u044f \u043c\u0443\u0437\u044b\u043a\u0430", "My music") : source.label();
            long duplicates = catalog.apps().stream().filter(other -> other.label().equals(source.label())).count();
            if (duplicates > 1L) {
                label = (String)label + " \u00b7 " + (catalog.apps().indexOf(source) + 1);
            }
            this.sourceRow(g, source.id(), (String)label, source.playing() ? HudStyle.tr("\u0418\u0433\u0440\u0430\u0435\u0442", "Playing") : HudStyle.tr("\u041d\u0430 \u043f\u0430\u0443\u0437\u0435", "Paused"), y, selected, mx, my);
            y += 36;
        }
        g.method_44380();
        this.bodyTop = savedTop;
        String hint = !service.isSupported() ? HudStyle.tr("\u0412\u044b\u0431\u043e\u0440 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0439 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 Windows \u0438 Linux", "Application selection requires Windows or Linux") : (!selected.isEmpty() && catalog.apps().stream().noneMatch(app -> app.id().equals(selected)) ? HudStyle.tr("\u041e\u0436\u0438\u0434\u0430\u0435\u043c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u043f\u043b\u0435\u0435\u0440. \u0417\u0430\u043f\u0443\u0441\u0442\u0438 \u0432 \u043d\u0451\u043c \u0442\u0440\u0435\u043a.", "Waiting for the pinned player. Start a track in it.") : HudStyle.tr("\u041d\u0435\u0442 \u043d\u0443\u0436\u043d\u043e\u0433\u043e \u043f\u043b\u0435\u0435\u0440\u0430? \u041e\u0442\u043a\u0440\u043e\u0439 \u0435\u0433\u043e \u0438 \u0437\u0430\u043f\u0443\u0441\u0442\u0438 \u0442\u0440\u0435\u043a.", "Missing a player? Open it and start a track."));
        this.footer(g, hint);
    }

    private void sourceRow(class_332 g, String id, String label, String detail, int y, String selected, int mx, int my) {
        if (y < this.bodyTop || y + 30 > this.bodyBottom) {
            return;
        }
        this.button(g, this.left + 14, y, this.panelWidth - 28, (id.equals(selected) ? "\u2713  " : "") + label + "  \u00b7  " + detail, mx, my, () -> this.choose(id));
    }

    private void choose(String id) {
        BloodHelperClient.config().music.preferredSource = MediaSources.cleanId(id);
        ConfigStore.requestSave();
        BloodHelperClient.media().setPreferredSource(id);
        MusicWidget.resetMotion();
        if ("bloodhelper:local".equals(id)) {
            this.field_22787.method_1507((class_437)new LocalMusicScreen(this.parent));
        } else {
            this.method_25419();
        }
    }
}

