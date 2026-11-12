/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.tiers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudMotion;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.tiers.TierIcons;
import ru.bloodhelper.client.tiers.TierService;

public final class TierLookupScreen
extends ModuleDetailScreen {
    private static final List<String> PROVIDERS = List.of("CIS", "MC", "PVP", "A", "MY");
    private static final List<String> ORDER = List.of("vanilla", "sword", "axe", "pot", "nethpot", "smp", "uhc", "mace", "netherop", "diaop", "dsmp", "suhc", "creeper", "cart", "spearmace", "smace", "spear");
    private final String player;
    private final long opened = System.nanoTime();
    private final HudMotion.Value dropdown = new HudMotion.Value();
    private String provider = "CIS";
    private boolean expanded;
    private int menuX;
    private int menuY;
    private int menuWidth;
    private int menuHeight;
    private int menuScroll;
    private int maximumMenuScroll;
    private int headerY;

    public TierLookupScreen(String player) {
        super(null, HudStyle.tr("\u0422\u0438\u0440\u044b \u00b7 ", "Tiers \u00b7 ") + player);
        this.player = player;
        TierService.openLookup(this, this.provider, player);
    }

    @Override
    protected double appearance(long now) {
        return BloodHelperClient.config().animations ? 1.0 - Math.pow(1.0 - Math.clamp((double)(now - this.opened) / 2.2E8, 0.0, 1.0), 3.0) : 1.0;
    }

    public void method_25432() {
        TierService.closeLookup(this);
        super.method_25432();
    }

    public void method_25393() {
        super.method_25393();
        if (BloodHelperClient.config().serverAllowsModule("otherTiers")) {
            TierService.openLookup(this, this.provider, this.player);
        } else {
            TierService.closeLookup(this);
        }
    }

    @Override
    protected int preferredPanelWidth() {
        return 590;
    }

    @Override
    protected int preferredPanelHeight() {
        return 420;
    }

    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0422\u0438\u0440\u044b \u00b7 ", "Tiers \u00b7 ") + this.player, mx, my);
        boolean allowed = BloodHelperClient.config().serverAllowsModule("otherTiers");
        TierService.LookupView view = TierService.lookupView(this.provider, this.player);
        this.headerY = this.bodyTop;
        this.menuX = this.left + 14;
        this.menuWidth = Math.max(1, Math.min(230, this.panelWidth - 28));
        this.menuY = this.headerY + 25;
        this.button(g, this.menuX, this.headerY, this.menuWidth, TierLookupScreen.providerName(this.provider) + (this.expanded ? "  \u25b4" : "  \u25be"), mx, my, () -> {
            this.expanded = !this.expanded;
        });
        String status = !allowed ? HudStyle.tr("\u041f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d \u043f\u0440\u0430\u0432\u0438\u043b\u0430\u043c\u0438 \u043c\u043e\u0434\u0430", "Disabled by module policy") : (!view.error().isEmpty() ? HudStyle.tr("\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u00b7 ", "Source unavailable \u00b7 ") + view.error() : (!view.ready() ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u043c \u0442\u0438\u0440\u044b\u2026", "Loading tiers\u2026") : (view.profile().ranks().isEmpty() ? HudStyle.tr("\u0414\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u043d\u0438\u043a\u0430 \u043d\u0435\u0442 \u043e\u043f\u0443\u0431\u043b\u0438\u043a\u043e\u0432\u0430\u043d\u043d\u044b\u0445 \u0442\u0438\u0440\u043e\u0432", "No published tiers for this name") : HudStyle.tr("\u0420\u0435\u0436\u0438\u043c\u043e\u0432 \u0441 \u0442\u0438\u0440\u043e\u043c: ", "Ranked modes: ") + view.profile().ranks().size())));
        this.text(g, CustomFont.regular().ellipsize(status, 11.0f, Math.max(1, this.panelWidth - 28)), this.left + 14, this.headerY + 31, !view.error().isEmpty() ? -1664136 : -7105633);
        LinkedHashSet<String> modes = new LinkedHashSet<String>();
        for (String mode : ORDER) {
            if (!TierIcons.modes(this.provider).contains(mode)) continue;
            modes.add(mode);
        }
        view.profile().ranks().keySet().stream().sorted().forEach(modes::add);
        int columns = this.panelWidth >= 500 ? 3 : (this.panelWidth >= 320 ? 2 : 1);
        int cellWidth = Math.max(1, (this.panelWidth - 36) / columns);
        int originalTop = this.bodyTop;
        this.bodyTop = this.headerY + 57;
        this.scrollbar(g, (modes.size() + columns - 1) / columns * 58);
        if (this.bodyBottom > this.bodyTop) {
            g.method_44379(this.left + 8, this.bodyTop, this.left + this.panelWidth - 8, this.bodyBottom);
            int i = 0;
            for (String mode : modes) {
                int x = this.left + 14 + i % columns * cellWidth;
                int y = this.bodyTop + i / columns * 58 - this.scroll;
                if (y + 52 >= this.bodyTop && y < this.bodyBottom) {
                    this.kit(g, mode, allowed && view.ready() ? view.profile().ranks().get(mode) : null, x, y, cellWidth - 6);
                }
                ++i;
            }
            g.method_44380();
        }
        this.bodyTop = originalTop;
        this.footer(g, HudStyle.tr("\u2014 \u043d\u0435\u0442 \u043e\u043f\u0443\u0431\u043b\u0438\u043a\u043e\u0432\u0430\u043d\u043d\u043e\u0433\u043e \u0442\u0438\u0440\u0430 \u00b7 \u0434\u0430\u043d\u043d\u044b\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u0441\u0430\u0439\u0442\u0430", "\u2014 no published tier \u00b7 data from the selected website"));
        this.drawProviders(g, mx, my);
    }

    private void kit(class_332 g, String mode, String tier, int x, int y, int width) {
        int n;
        UiDraw.rounded(g, x, y, width, 51, 8, ThemeColors.surface(-1255461579));
        UiDraw.outline(g, x, y, width, 51, 8, ThemeColors.line());
        if (!TierIcons.glyph(this.provider, mode).isEmpty()) {
            String iconMode = this.provider.equals("MC") && mode.equals("nethpot") ? "netherop" : mode;
            class_2960 texture = class_2960.method_60655((String)"bloodhelper", (String)("textures/tiers/" + this.provider.toLowerCase(Locale.ROOT) + "-" + iconMode + ".png"));
            g.method_25293(class_10799.field_56883, texture, x + 8, y + 10, 0.0f, 0.0f, 30, 30, 32, 32, 32, 32, -1);
        }
        CustomFont font = CustomFont.medium();
        font.draw(g, font.ellipsize(TierLookupScreen.modeName(mode), 10.0f, Math.max(1, width - 48)), x + 44, y + 9, 10.0f, -1381908);
        if (tier == null) {
            n = -7105633;
        } else {
            switch (tier.charAt(2)) {
                case '1': {
                    n = -10918;
                    break;
                }
                case '2': {
                    n = -2761755;
                    break;
                }
                case '3': {
                    n = -2385564;
                    break;
                }
                case '4': {
                    n = -7545944;
                    break;
                }
                default: {
                    n = -7882520;
                }
            }
        }
        int color = n;
        font.draw(g, tier == null ? "\u2014" : tier, x + 44, y + 27, 13.0f, color);
    }

    private void drawProviders(class_332 g, int mx, int my) {
        double amount = this.dropdown.to(this.expanded ? 1.0 : 0.0, System.nanoTime(), BloodHelperClient.config().animations, 190.0);
        int fullHeight = Math.min(120, Math.max(0, this.bodyBottom - this.menuY));
        this.maximumMenuScroll = Math.max(0, 120 - fullHeight);
        this.menuScroll = Math.clamp((long)this.menuScroll, 0, this.maximumMenuScroll);
        this.menuHeight = (int)Math.round((double)fullHeight * amount);
        if (this.menuHeight < 1) {
            return;
        }
        LiquidGlass.surfaceMenu(g, this.menuX, this.menuY, this.menuWidth, this.menuHeight, 8, ThemeColors.surface(-64937173), BloodHelperClient.config());
        g.method_44379(this.menuX, this.menuY, this.menuX + this.menuWidth, this.menuY + this.menuHeight);
        int row = 0;
        for (String source : PROVIDERS) {
            int y;
            if (source.equals(this.provider) || (y = this.menuY + 6 + row++ * 27 - this.menuScroll - (int)((double)(fullHeight - this.menuHeight) * 0.12)) < this.menuY || y + 21 > this.menuY + this.menuHeight) continue;
            this.button(g, this.menuX + 5, y, this.menuWidth - 10, TierLookupScreen.providerName(source), mx, my, () -> {
                this.provider = source;
                this.expanded = false;
                this.scroll = 0;
                this.menuScroll = 0;
                TierService.openLookup(this, source, this.player);
            });
        }
        g.method_44380();
    }

    private boolean inMenu(double x, double y) {
        return x >= (double)this.menuX && x < (double)(this.menuX + this.menuWidth) && y >= (double)this.menuY && y < (double)(this.menuY + this.menuHeight);
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        boolean header;
        if (this.inMenu(event.comp_4798(), event.comp_4799())) {
            super.panelMouseClicked(event, doubled);
            return true;
        }
        boolean bl = header = event.comp_4798() >= (double)this.menuX && event.comp_4798() < (double)(this.menuX + this.menuWidth) && event.comp_4799() >= (double)this.headerY && event.comp_4799() < (double)(this.headerY + 21);
        if (this.expanded && !header) {
            this.expanded = false;
            return true;
        }
        return super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        if (this.inMenu(x, y)) {
            this.menuScroll = Math.clamp((long)(this.menuScroll - (int)Math.round(vertical * 27.0)), 0, this.maximumMenuScroll);
            return true;
        }
        return super.panelMouseScrolled(x, y, horizontal, vertical);
    }

    private static String providerName(String source) {
        return switch (source) {
            case "MC" -> "MCTiers";
            case "PVP" -> "PvPTiers";
            case "A" -> "ATiers";
            case "MY" -> "MyTiers";
            default -> "CIS Tiers";
        };
    }

    private static String modeName(String mode) {
        return switch (mode) {
            case "vanilla" -> "Crystal";
            case "sword" -> "Sword";
            case "axe" -> "Axe";
            case "pot" -> "Diamond Pot";
            case "nethpot" -> "Netherite Pot";
            case "netherop" -> "Nether OP";
            case "uhc" -> "UHC";
            case "smp" -> "SMP";
            case "mace" -> "Mace";
            case "diaop" -> "Diamond OP";
            case "dsmp" -> "Diamond SMP";
            case "suhc" -> "Shield UHC";
            case "creeper" -> "Creeper";
            case "cart" -> "Minecart";
            case "spearmace" -> "Spear Mace";
            case "smace" -> "Shield Mace";
            case "spear" -> "Spear";
            default -> mode;
        };
    }
}

