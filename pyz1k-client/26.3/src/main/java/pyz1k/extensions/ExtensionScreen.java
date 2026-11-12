/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package pyz1k.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;
import pyz1k.Pyz1k;
import pyz1k.extensions.ExtensionConfig;
import pyz1k.extensions.FeatureModules;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.extensions.ServerProfiles;
import pyz1k.extensions.SkinCatalog;
import pyz1k.extensions.SkinPreviewScreen;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ClientTheme;
import pyz1k.utilites.color.ColorUtil;

public final class ExtensionScreen
extends Screen {
    private final Screen parent;
    private final List<Hit> hits = new ArrayList<Hit>();
    private Page page = Page.SKINS;
    private Item target = Items.DIAMOND_SWORD;
    private EditBox search;
    private EditBox presetInput;
    private boolean favoritesOnly;
    private String status = "";
    private List<SkinCatalog.Skin> filtered = List.of();
    private int lastGeneration = -1;
    private int scroll;
    private String lastQuery = "";
    private float x;
    private float y;
    private float w;
    private float h;
    private HudElement dragging;
    private float dragX;
    private float dragY;

    public ExtensionScreen(Screen parent) {
        super((Component)Component.literal((String)"Pyz1k · ItemSkin"));
        this.parent = parent;
    }

    public boolean layout() {
        return this.page == Page.LAYOUT;
    }

    protected void init() {
        this.w = Math.min(540, this.width - 16);
        this.h = Math.min(350, this.height - 16);
        this.x = ((float)this.width - this.w) / 2.0f;
        this.y = ((float)this.height - this.h) / 2.0f;
        String query = this.search == null ? "" : this.search.getValue();
        this.search = (EditBox)this.addRenderableWidget(new EditBox(this.font, (int)this.x + 14, (int)this.y + 114, (int)this.w - 28, 18, (Component)Component.literal((String)"Поиск скина")));
        this.search.setMaxLength(120);
        this.search.setBordered(false);
        this.search.setHint((Component)Component.literal((String)"Поиск по модели…"));
        this.search.setValue(query);
        this.search.setTextColor(ClientColors.TEXT_PRIMARY);
        this.presetInput = (EditBox)this.addRenderableWidget(new EditBox(this.font, (int)this.x + 18, (int)this.y + 83, (int)this.w - 150, 18, (Component)Component.literal((String)"Имя набора")));
        this.presetInput.setMaxLength(40);
        this.presetInput.setBordered(false);
        this.presetInput.setHint((Component)Component.literal((String)"Название набора…"));
        SkinCatalog.ensureLoaded();
    }

    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        ClientTheme.updateDynamicColors();
        this.hits.clear();
        this.search.setVisible(this.page == Page.SKINS);
        this.presetInput.setVisible(this.page == Page.PRESETS);
        if (this.page == Page.LAYOUT) {
            this.renderLayout(ctx, mx, my);
            return;
        }
        HudVisualStyle.drawMenuPanel(ctx, this.x, this.y, this.w, this.h, this.w, 1.0f);
        ExtensionScreen.text(ctx, "Pyz1k / ItemSkin", this.x + 12.0f, this.y + 10.0f, ClientColors.TEXT_PRIMARY, 9.0f);
        float tab = this.x + 12.0f;
        String[] labels = new String[]{"Скины", "Наборы", "Серверы", "HUD"};
        Page[] pages = new Page[]{Page.SKINS, Page.PRESETS, Page.PROFILES, Page.LAYOUT};
        int[] widths = new int[]{55, 56, 60, 35};
        for (int i = 0; i < pages.length; ++i) {
            Page next = pages[i];
            this.button(ctx, labels[i], tab, this.y + 29.0f, widths[i], 20.0f, this.page == next, () -> this.switchPage(next));
            tab += (float)(widths[i] + 3);
        }
        this.button(ctx, "X", this.x + this.w - 28.0f, this.y + 6.0f, 20.0f, 20.0f, false, this::onClose);
        this.y += 27.0f;
        this.h -= 27.0f;
        switch (this.page.ordinal()) {
            case 0: {
                this.renderSkins(ctx, mx, my, delta);
                break;
            }
            case 2: {
                this.renderPresets(ctx, mx, my, delta);
                break;
            }
            case 3: {
                this.renderProfiles(ctx);
                break;
            }
        }
        this.y -= 27.0f;
        this.h += 27.0f;
    }

    private void switchPage(Page next) {
        this.page = next;
        this.scroll = 0;
        this.status = "";
        this.search.setFocused(false);
        this.presetInput.setFocused(false);
        if (next == Page.SKINS) {
            SkinCatalog.ensureLoaded();
        }
    }

    private void renderSkins(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        if (!SkinCatalog.onServer()) {
            ExtensionScreen.text(ctx, "Зайди на сервер и прими его ресурспак.", this.x + 16.0f, this.y + 58.0f, ClientColors.TEXT_PRIMARY, 9.0f);
            ExtensionScreen.text(ctx, "Каталог появится после загрузки моделей сервера.", this.x + 16.0f, this.y + 78.0f, ClientColors.CATEGORY_INACTIVE, 8.0f);
            return;
        }
        this.button(ctx, FeatureModules.skins.isEnable() ? "Скины: ВКЛ" : "Скины: ВЫКЛ", this.x + 12.0f, this.y + 33.0f, 105.0f, 19.0f, FeatureModules.skins.isEnable(), () -> FeatureModules.skins.toggle());
        this.button(ctx, "Сбросить предмет", this.x + 121.0f, this.y + 33.0f, 112.0f, 19.0f, false, () -> SkinCatalog.select(this.target, null));
        this.button(ctx, this.favoritesOnly ? "Избранное: ДА" : "Избранное", this.x + 237.0f, this.y + 33.0f, 100.0f, 19.0f, this.favoritesOnly, () -> {
            this.favoritesOnly = !this.favoritesOnly;
            this.invalidate();
        });
        float iw = (this.w - 24.0f) / 6.0f;
        for (int i = 0; i < SkinCatalog.TARGETS.size(); ++i) {
            Item item = SkinCatalog.TARGETS.get(i);
            float bx = this.x + 12.0f + (float)i * iw;
            this.button(ctx, "", bx, this.y + 56.0f, iw - 4.0f, 25.0f, this.target == item, () -> {
                this.target = item;
                this.invalidate();
            });
            ItemStack stack = pyz1k.compat.PreviewStacks.of((ItemLike)item);
            float iconX = bx + (iw - 20.0f) * 0.5f;
            ExtensionScreen.icon(ctx, stack, iconX, this.y + 60.0f, 1.0f);
            if (!ExtensionScreen.inside(mx, my, bx, this.y + 56.0f, iw - 4.0f, 25.0f)) continue;
            ctx.setTooltipForNextFrame(this.font, stack.getHoverName(), mx, my);
        }
        RenderUtil.drawRoundRect(ctx, this.x + 12.0f, this.y + 84.0f, this.w - 24.0f, 21.0f, 4.0f, ClientColors.SETTINGS_BACKGROUND);
        this.search.extractRenderState(ctx, mx, my, delta);
        this.updateFilter();
        int columns = Math.max(2, (int)(this.w - 24.0f) / 100);
        float cardW = (this.w - 24.0f) / (float)columns;
        float top = this.y + 111.0f;
        int visibleRows = Math.max(1, (int)(this.h - 140.0f) / 66);
        int maxScroll = Math.max(0, (this.filtered.size() + columns - 1) / columns - visibleRows);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
        SkinCatalog.Skin chosen = SkinCatalog.chosen(this.target);
        for (int r = 0; r < visibleRows; ++r) {
            int index;
            for (int col = 0; col < columns && (index = (r + this.scroll) * columns + col) < this.filtered.size(); ++col) {
                SkinCatalog.Skin skin = this.filtered.get(index);
                float bx = this.x + 12.0f + (float)col * cardW;
                float by = top + (float)(r * 66);
                boolean active = chosen != null && chosen.key().equals(skin.key());
                this.button(ctx, "", bx, by, cardW - 5.0f, 61.0f, active, () -> SkinCatalog.select(this.target, skin));
                this.button(ctx, SkinCatalog.favorite(skin) ? "*" : "+", bx + cardW - 26.0f, by + 3.0f, 18.0f, 17.0f, SkinCatalog.favorite(skin), () -> {
                    SkinCatalog.toggleFavorite(skin);
                    this.invalidate();
                });
                this.button(ctx, "3D", bx + cardW - 29.0f, by + 43.0f, 21.0f, 15.0f, false, () -> Minecraft.getInstance().gui.setScreen((Screen)new SkinPreviewScreen(this, this.target, skin)));
                ExtensionScreen.icon(ctx, skin.preview(this.target), bx + (cardW - 32.0f) * 0.5f, by + 4.0f, 1.65f);
                ExtensionScreen.text(ctx, ExtensionScreen.trim(ExtensionScreen.shortName(skin.name()), cardW - 13.0f, 7.0f), bx + 5.0f, by + 37.0f, ClientColors.TEXT_PRIMARY, 7.0f);
                ExtensionScreen.text(ctx, active ? "Выбрано" : "Выбрать", bx + 5.0f, by + 49.0f, active ? ClientColors.CATEGORY_ACCENT_SECOND : ClientColors.CATEGORY_INACTIVE, 6.0f);
            }
        }
        if (this.filtered.isEmpty()) {
            ExtensionScreen.text(ctx, SkinCatalog.entries().isEmpty() ? "В серверном РП не найдены item definitions." : "Нет моделей для выбранного материала.", this.x + 16.0f, top + 15.0f, ClientColors.CATEGORY_INACTIVE, 8.0f);
        }
        ExtensionScreen.text(ctx, "Моделей: " + this.filtered.size() + " / " + SkinCatalog.entries().size() + " · Только у тебя · Колесо: прокрутка", this.x + 12.0f, this.y + this.h - 16.0f, ClientColors.CATEGORY_INACTIVE, 7.0f);
    }

    private void updateFilter() {
        String query = this.search.getValue().toLowerCase(Locale.ROOT);
        if (this.lastGeneration == SkinCatalog.generation() && this.lastQuery.equals(query)) {
            return;
        }
        this.filtered = SkinCatalog.entries().stream().filter(s -> SkinCatalog.compatible(s, this.target)).filter(s -> !this.favoritesOnly || SkinCatalog.favorite(s)).filter(s -> query.isBlank() || s.name().toLowerCase(Locale.ROOT).contains(query) || s.model().toString().contains(query)).toList();
        this.lastGeneration = SkinCatalog.generation();
        this.lastQuery = query;
        this.scroll = 0;
    }

    private void invalidate() {
        this.lastGeneration = -1;
        this.scroll = 0;
    }

    private void renderPresets(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        ExtensionScreen.text(ctx, "Набор сохраняет скины всех шести предметов", this.x + 12.0f, this.y + 36.0f, ClientColors.CATEGORY_INACTIVE, 8.0f);
        RenderUtil.drawRoundRect(ctx, this.x + 12.0f, this.y + 51.0f, this.w - 140.0f, 25.0f, 4.0f, ClientColors.SETTINGS_BACKGROUND);
        this.presetInput.extractRenderState(ctx, mx, my, delta);
        this.button(ctx, "Сохранить", this.x + this.w - 121.0f, this.y + 51.0f, 109.0f, 25.0f, false, () -> {
            this.status = SkinCatalog.savePreset(this.presetInput.getValue()) ? "Набор сохранён" : "Зайди на сервер и укажи имя (до 40 символов)";
        });
        List<String> names = SkinCatalog.presetNames();
        int visible = Math.max(1, (int)(this.h - 115.0f) / 30);
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, names.size() - visible));
        for (int i = 0; i < visible && this.scroll + i < names.size(); ++i) {
            String name = names.get(this.scroll + i);
            float by = this.y + 85.0f + (float)(i * 30);
            this.button(ctx, ExtensionScreen.trim(name, this.w - 190.0f, 8.0f), this.x + 12.0f, by, this.w - 190.0f, 25.0f, false, () -> this.presetInput.setValue(name));
            this.button(ctx, "Применить", this.x + this.w - 172.0f, by, 105.0f, 25.0f, false, () -> {
                this.status = "Применено скинов: " + SkinCatalog.applyPreset(name) + " / 6. Недоступные сброшены.";
            });
            this.button(ctx, "X", this.x + this.w - 61.0f, by, 49.0f, 25.0f, false, () -> {
                SkinCatalog.deletePreset(name);
                this.status = "Набор удалён";
            });
        }
        ExtensionScreen.text(ctx, ExtensionScreen.trim(this.status.isEmpty() ? "Наборы используют модели активного РП; несовместимые пропускаются" : this.status, this.w - 24.0f, 7.0f), this.x + 12.0f, this.y + this.h - 16.0f, ClientColors.CATEGORY_INACTIVE, 7.0f);
    }

    private void renderProfiles(GuiGraphicsExtractor ctx) {
        ExtensionScreen.text(ctx, ExtensionScreen.trim("Функции, настройки, бинды и HUD отдельно для серверов", this.w - 24.0f, 8.0f), this.x + 12.0f, this.y + 34.0f, ClientColors.TEXT_PRIMARY, 8.0f);
        this.button(ctx, ExtensionConfig.get().automaticProfiles ? "Автопрофили: ВКЛ" : "Автопрофили: ВЫКЛ", this.x + 12.0f, this.y + 49.0f, 177.0f, 25.0f, ExtensionConfig.get().automaticProfiles, () -> ServerProfiles.automatic(!ExtensionConfig.get().automaticProfiles));
        this.button(ctx, "Сохранить сервер", this.x + 12.0f, this.y + 83.0f, 155.0f, 25.0f, false, () -> {
            this.status = ServerProfiles.save() ? "Профиль сервера сохранён" : "Зайди на сервер";
        });
        this.button(ctx, "Загрузить", this.x + 174.0f, this.y + 83.0f, 112.0f, 25.0f, false, () -> {
            this.status = ServerProfiles.restore() ? "Профиль загружен" : "Нет сохранённого профиля этого сервера";
        });
        ExtensionScreen.text(ctx, ExtensionScreen.trim("Автопрофили сохраняются при выходе с сервера", this.w - 24.0f, 7.0f), this.x + 12.0f, this.y + 119.0f, ClientColors.CATEGORY_INACTIVE, 7.0f);
        this.button(ctx, "Режим скриншота", this.x + 12.0f, this.y + 140.0f, 155.0f, 25.0f, ScreenshotMode.active(), ScreenshotMode::toggle);
        ExtensionScreen.text(ctx, "F8 · Бинд: ScreenshotMode", this.x + 175.0f, this.y + 150.0f, ClientColors.TEXT_PRIMARY, 7.0f);
        ExtensionScreen.text(ctx, ExtensionScreen.trim(this.status, this.w - 24.0f, 7.0f), this.x + 12.0f, this.y + this.h - 16.0f, ClientColors.CATEGORY_INACTIVE, 7.0f);
    }

    private void renderLayout(GuiGraphicsExtractor ctx, int mx, int my) {
        HudVisualStyle.drawPanel(ctx, 8.0f, 8.0f, Math.min(this.width - 16, 410), 54.0f, 1.0f);
        ExtensionScreen.text(ctx, "Расположение HUD · Перетаскивай рамки мышью", 17.0f, 17.0f, ClientColors.HUD_TEXT, 9.0f);
        this.button(ctx, "Готово", 17.0f, 35.0f, 58.0f, 19.0f, true, () -> this.switchPage(Page.SKINS));
        ExtensionScreen.text(ctx, "Также доступно в открытом чате", 192.0f, 41.0f, ClientColors.HUD_SECONDARY_TEXT, 7.0f);
        Interface module = Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
        for (HudElement e : module.getHudElements()) {
            float ew = Math.max(75.0f, e.getWidth());
            float eh = Math.max(20.0f, e.getHeight());
            float ex = Math.max(0.0f, Math.min((float)this.width - ew, e.getX()));
            float ey = Math.max(65.0f, Math.min((float)this.height - eh, e.getY()));
            RenderUtil.drawBorder(ctx, ex, ey, ew, eh, 4.0f, ClientColors.CATEGORY_ACCENT, 1.0f, 0.0f, 0.5f);
            ExtensionScreen.text(ctx, e.getDisplayName(), ex + 4.0f, ey + 5.0f, ClientColors.CATEGORY_ACCENT_SECOND, 7.0f);
            this.hits.add(new Hit(ex, ey, ew, eh, () -> {
                this.dragging = e;
                this.dragX = (float)mx - ex;
                this.dragY = (float)my - ey;
            }));
        }
    }

    private void button(GuiGraphicsExtractor ctx, String label, float bx, float by, float bw, float bh, boolean active, Runnable run) {
        RenderUtil.drawRoundRect(ctx, bx, by, bw, bh, 4.0f, active ? ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, 0.38f) : ClientColors.SETTINGS_BACKGROUND);
        RenderUtil.drawBorder(ctx, bx, by, bw, bh, 4.0f, active ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_OUTLINE, 0.65f, 0.0f, 0.5f);
        if (!label.isEmpty()) {
            ExtensionScreen.text(ctx, label, bx + 6.0f, by + (bh - 7.0f) / 2.0f, active ? ClientColors.CATEGORY_ACCENT_SECOND : ClientColors.TEXT_PRIMARY, 7.0f);
        }
        this.hits.add(new Hit(bx, by, bw, bh, run));
    }

    private static void text(GuiGraphicsExtractor ctx, String label, float x, float y, int color, float size) {
        RenderUtil.drawText(ctx, label, x, y, color, GFonts.MEDIUM, size);
    }

    private static void icon(GuiGraphicsExtractor ctx, ItemStack stack, float x, float y, float scale) {
        SkinCatalog.drawOriginal(() -> {
            Matrix3x2fStack m = ctx.pose();
            m.pushMatrix();
            m.translate(x, y);
            m.scale(scale, scale);
            ctx.item(stack, 0, 0);
            m.popMatrix();
        });
    }

    private static String shortName(String value) {
        String[] parts = value.split("[:/]");
        return parts.length == 0 ? value : parts[parts.length - 1].replace('_', ' ');
    }

    private static String trim(String s, float width, float size) {
        if (RenderUtil.getTextWidth(s, GFonts.MEDIUM, size) <= width) {
            return s;
        }
        while (!s.isEmpty() && RenderUtil.getTextWidth(s + "…", GFonts.MEDIUM, size) > width) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) == 0) {
            for (int i = this.hits.size() - 1; i >= 0; --i) {
                Hit hit = this.hits.get(i);
                if (!hit.contains(click.x(), click.y())) continue;
                hit.action.run();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (this.dragging != null) {
            this.dragging.setPosition((float)Math.max(0.0, Math.min((double)((float)this.width - Math.max(30.0f, this.dragging.getWidth())), click.x() - (double)this.dragX)), (float)Math.max(65.0, Math.min((double)((float)this.height - Math.max(20.0f, this.dragging.getHeight())), click.y() - (double)this.dragY)));
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.dragging != null) {
            this.dragging = null;
            Pyz1k.getInstance().getConfigManager().flushAutoSave();
            return true;
        }
        return super.mouseReleased(click);
    }

    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        this.scroll -= Math.round((float)vertical);
        return true;
    }

    public boolean keyPressed(KeyEvent key) {
        if (pyz1k.compat.LegacyInput.fromNativeKey(key.key()) == 256) {
            if (this.page == Page.LAYOUT) {
                this.switchPage(Page.SKINS);
            } else {
                this.onClose();
            }
            return true;
        }
        return super.keyPressed(key);
    }

    public void onClose() {
        Pyz1k.getInstance().getConfigManager().flushAutoSave();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static enum Page {
        SKINS,
        LAYOUT,
        PRESETS,
        PROFILES;

    }

    private record Hit(float x, float y, float w, float h, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }
}

