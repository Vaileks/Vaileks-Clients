package pyz1k.extensions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import pyz1k.Pyz1k;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.*;
import pyz1k.utilites.color.*;
import java.util.*;

/** Additional pages use the original palette and real item renderer for all thumbnails. */
public final class ExtensionScreen extends Screen {
    private enum Page {SKINS,LAYOUT,PRESETS,PROFILES}
    private record Hit(float x,float y,float w,float h,Runnable action){boolean contains(double mx,double my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}}
    private final Screen parent;
    private final List<Hit> hits=new ArrayList<>();
    private Page page=Page.SKINS;
    private Item target=Items.DIAMOND_SWORD;
    private TextFieldWidget search,presetInput;
    private boolean favoritesOnly;
    private String status="";
    private List<SkinCatalog.Skin> filtered=List.of();
    private int lastGeneration=-1,scroll;
    private String lastQuery="";
    private float x,y,w,h;
    private HudElement dragging;
    private float dragX,dragY;
    public ExtensionScreen(Screen parent){super(Text.literal("Pyz1k · ItemSkin"));this.parent=parent;}
    public boolean layout(){return page==Page.LAYOUT;}
    @Override protected void init(){
        w=Math.min(540,width-16);h=Math.min(350,height-16);x=(width-w)/2;y=(height-h)/2;
        String query=search==null?"":search.getText();
        search=addDrawableChild(new TextFieldWidget(textRenderer,(int)x+14,(int)y+114,(int)w-28,18,Text.literal("Поиск скина")));
        search.setMaxLength(120);search.setDrawsBackground(false);search.setPlaceholder(Text.literal("Поиск по модели…"));search.setText(query);
        search.setEditableColor(ClientColors.TEXT_PRIMARY);
        presetInput=addDrawableChild(new TextFieldWidget(textRenderer,(int)x+18,(int)y+83,(int)w-150,18,Text.literal("Имя набора")));
        presetInput.setMaxLength(40);presetInput.setDrawsBackground(false);presetInput.setPlaceholder(Text.literal("Название набора…"));
        SkinCatalog.ensureLoaded();
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        ClientTheme.updateDynamicColors();hits.clear();
        search.setVisible(page==Page.SKINS);presetInput.setVisible(page==Page.PRESETS);
        if(page==Page.LAYOUT){renderLayout(ctx,mx,my);return;}
        HudVisualStyle.drawMenuPanel(ctx,x,y,w,h,w,1);
        text(ctx,"Pyz1k / ItemSkin",x+12,y+10,ClientColors.TEXT_PRIMARY,9);
        float tab=x+12;
        String[] labels={"Скины","Наборы","Серверы","HUD"};
        Page[] pages={Page.SKINS,Page.PRESETS,Page.PROFILES,Page.LAYOUT};
        int[] widths={55,56,60,35};
        for(int i=0;i<pages.length;i++){Page next=pages[i];button(ctx,labels[i],tab,y+29,widths[i],20,page==next,()->switchPage(next));tab+=widths[i]+3;}
        button(ctx,"X",x+w-28,y+6,20,20,false,this::close);
        y+=27;h-=27;
        switch(page){case SKINS->renderSkins(ctx,mx,my,delta);case PRESETS->renderPresets(ctx,mx,my,delta);case PROFILES->renderProfiles(ctx);default->{}}
        y-=27;h+=27;
    }
    private void switchPage(Page next){page=next;scroll=0;status="";search.setFocused(false);presetInput.setFocused(false);if(next==Page.SKINS)SkinCatalog.ensureLoaded();}
    private void renderSkins(DrawContext ctx,int mx,int my,float delta){
        if(!SkinCatalog.onServer()){
            text(ctx,"Зайди на сервер и прими его ресурспак.",x+16,y+58,ClientColors.TEXT_PRIMARY,9);
            text(ctx,"Каталог появится после загрузки моделей сервера.",x+16,y+78,ClientColors.CATEGORY_INACTIVE,8);return;
        }
        button(ctx,FeatureModules.skins.isEnable()?"Скины: ВКЛ":"Скины: ВЫКЛ",x+12,y+33,105,19,FeatureModules.skins.isEnable(),()->FeatureModules.skins.toggle());
        button(ctx,"Сбросить предмет",x+121,y+33,112,19,false,()->SkinCatalog.select(target,null));
        button(ctx,favoritesOnly?"Избранное: ДА":"Избранное",x+237,y+33,100,19,favoritesOnly,()->{favoritesOnly=!favoritesOnly;invalidate();});
        float iw=(w-24)/6;
        for(int i=0;i<SkinCatalog.TARGETS.size();i++){
            Item item=SkinCatalog.TARGETS.get(i);float bx=x+12+i*iw;
            button(ctx,"",bx,y+56,iw-4,25,target==item,()->{target=item;invalidate();});
            ItemStack stack=new ItemStack(item);float iconX=bx+(iw-20)*.5f;
            icon(ctx,stack,iconX,y+60,1);
            if(inside(mx,my,bx,y+56,iw-4,25))ctx.drawTooltip(textRenderer,stack.getName(),mx,my);
        }
        RenderUtil.drawRoundRect(ctx,x+12,y+84,w-24,21,4,ClientColors.SETTINGS_BACKGROUND);
        search.render(ctx,mx,my,delta);
        updateFilter();
        int columns=Math.max(2,(int)(w-24)/100);float cardW=(w-24)/columns;float top=y+111;
        int visibleRows=Math.max(1,(int)(h-140)/66);int maxScroll=Math.max(0,(filtered.size()+columns-1)/columns-visibleRows);
        scroll=Math.max(0,Math.min(scroll,maxScroll));
        SkinCatalog.Skin chosen=SkinCatalog.chosen(target);
        for(int r=0;r<visibleRows;r++)for(int col=0;col<columns;col++){
            int index=(r+scroll)*columns+col;if(index>=filtered.size())break;
            SkinCatalog.Skin skin=filtered.get(index);float bx=x+12+col*cardW,by=top+r*66;
            boolean active=chosen!=null&&chosen.key().equals(skin.key());
            button(ctx,"",bx,by,cardW-5,61,active,()->SkinCatalog.select(target,skin));
            button(ctx,SkinCatalog.favorite(skin)?"*":"+",bx+cardW-26,by+3,18,17,SkinCatalog.favorite(skin),()->{SkinCatalog.toggleFavorite(skin);invalidate();});
            button(ctx,"3D",bx+cardW-29,by+43,21,15,false,()->MinecraftClient.getInstance().setScreen(new SkinPreviewScreen(this,target,skin)));
            icon(ctx,skin.preview(target),bx+(cardW-32)*.5f,by+4,1.65f);
            text(ctx,trim(shortName(skin.name()),cardW-13,7),bx+5,by+37,ClientColors.TEXT_PRIMARY,7);
            text(ctx,active?"Выбрано":"Выбрать",bx+5,by+49,active?ClientColors.CATEGORY_ACCENT_SECOND:ClientColors.CATEGORY_INACTIVE,6);
        }
        if(filtered.isEmpty()){
            text(ctx,SkinCatalog.entries().isEmpty()?"В серверном РП не найдены item definitions.":"Нет моделей для выбранного материала.",x+16,top+15,ClientColors.CATEGORY_INACTIVE,8);
        }
        text(ctx,"Моделей: "+filtered.size()+" / "+SkinCatalog.entries().size()+" · Только у тебя · Колесо: прокрутка",x+12,y+h-16,ClientColors.CATEGORY_INACTIVE,7);
    }
    private void updateFilter(){
        String query=search.getText().toLowerCase(Locale.ROOT);
        if(lastGeneration==SkinCatalog.generation()&&lastQuery.equals(query))return;
        filtered=SkinCatalog.entries().stream().filter(s->SkinCatalog.compatible(s,target))
            .filter(s->!favoritesOnly||SkinCatalog.favorite(s))
            .filter(s->query.isBlank()||s.name().toLowerCase(Locale.ROOT).contains(query)||s.model().toString().contains(query)).toList();
        lastGeneration=SkinCatalog.generation();lastQuery=query;scroll=0;
    }
    private void invalidate(){lastGeneration=-1;scroll=0;}
    private void renderPresets(DrawContext ctx,int mx,int my,float delta){
        text(ctx,"Набор сохраняет скины всех шести предметов",x+12,y+36,ClientColors.CATEGORY_INACTIVE,8);
        RenderUtil.drawRoundRect(ctx,x+12,y+51,w-140,25,4,ClientColors.SETTINGS_BACKGROUND);presetInput.render(ctx,mx,my,delta);
        button(ctx,"Сохранить",x+w-121,y+51,109,25,false,()->status=SkinCatalog.savePreset(presetInput.getText())?"Набор сохранён":"Зайди на сервер и укажи имя (до 40 символов)");
        var names=SkinCatalog.presetNames();int visible=Math.max(1,(int)(h-115)/30);scroll=Math.clamp(scroll,0,Math.max(0,names.size()-visible));
        for(int i=0;i<visible&&scroll+i<names.size();i++){
            String name=names.get(scroll+i);float by=y+85+i*30;
            button(ctx,trim(name,w-190,8),x+12,by,w-190,25,false,()->presetInput.setText(name));
            button(ctx,"Применить",x+w-172,by,105,25,false,()->status="Применено скинов: "+SkinCatalog.applyPreset(name)+" / 6. Недоступные сброшены.");
            button(ctx,"X",x+w-61,by,49,25,false,()->{SkinCatalog.deletePreset(name);status="Набор удалён";});
        }
        text(ctx,trim(status.isEmpty()?"Наборы используют модели активного РП; несовместимые пропускаются":status,w-24,7),x+12,y+h-16,ClientColors.CATEGORY_INACTIVE,7);
    }
    private void renderProfiles(DrawContext ctx){
        text(ctx,trim("Функции, настройки, бинды и HUD отдельно для серверов",w-24,8),x+12,y+34,ClientColors.TEXT_PRIMARY,8);
        button(ctx,ExtensionConfig.get().automaticProfiles?"Автопрофили: ВКЛ":"Автопрофили: ВЫКЛ",x+12,y+49,177,25,ExtensionConfig.get().automaticProfiles,()->ServerProfiles.automatic(!ExtensionConfig.get().automaticProfiles));
        button(ctx,"Сохранить сервер",x+12,y+83,155,25,false,()->status=ServerProfiles.save()?"Профиль сервера сохранён":"Зайди на сервер");
        button(ctx,"Загрузить",x+174,y+83,112,25,false,()->status=ServerProfiles.restore()?"Профиль загружен":"Нет сохранённого профиля этого сервера");
        text(ctx,trim("Автопрофили сохраняются при выходе с сервера",w-24,7),x+12,y+119,ClientColors.CATEGORY_INACTIVE,7);
        button(ctx,"Режим скриншота",x+12,y+140,155,25,ScreenshotMode.active(),ScreenshotMode::toggle);
        text(ctx,"F8 · Бинд: ScreenshotMode",x+175,y+150,ClientColors.TEXT_PRIMARY,7);
        text(ctx,trim(status,w-24,7),x+12,y+h-16,ClientColors.CATEGORY_INACTIVE,7);
    }
    private void renderLayout(DrawContext ctx,int mx,int my){
        HudVisualStyle.drawPanel(ctx,8,8,Math.min(width-16,410),54,1);
        text(ctx,"Расположение HUD · Перетаскивай рамки мышью",17,17,ClientColors.HUD_TEXT,9);
        button(ctx,"Готово",17,35,58,19,true,()->switchPage(Page.SKINS));
        text(ctx,"Также доступно в открытом чате",192,41,ClientColors.HUD_SECONDARY_TEXT,7);
        Interface module=Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
        for(HudElement e:module.getHudElements()){
            float ew=Math.max(75,e.getWidth()),eh=Math.max(20,e.getHeight());
            float ex=Math.max(0,Math.min(width-ew,e.getX())),ey=Math.max(65,Math.min(height-eh,e.getY()));
            RenderUtil.drawBorder(ctx,ex,ey,ew,eh,4,ClientColors.CATEGORY_ACCENT,1,0,0.5f);
            text(ctx,e.getDisplayName(),ex+4,ey+5,ClientColors.CATEGORY_ACCENT_SECOND,7);
            hits.add(new Hit(ex,ey,ew,eh,()->{dragging=e;dragX=mx-ex;dragY=my-ey;}));
        }
    }
    private void button(DrawContext ctx,String label,float bx,float by,float bw,float bh,boolean active,Runnable run){
        RenderUtil.drawRoundRect(ctx,bx,by,bw,bh,4,active?ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND,ClientColors.CATEGORY_ACCENT,.38f):ClientColors.SETTINGS_BACKGROUND);
        RenderUtil.drawBorder(ctx,bx,by,bw,bh,4,active?ClientColors.CATEGORY_ACCENT:ClientColors.SETTINGS_OUTLINE,.65f,0,.5f);
        if(!label.isEmpty())text(ctx,label,bx+6,by+(bh-7)/2,active?ClientColors.CATEGORY_ACCENT_SECOND:ClientColors.TEXT_PRIMARY,7);
        hits.add(new Hit(bx,by,bw,bh,run));
    }
    private static void text(DrawContext ctx,String label,float x,float y,int color,float size){RenderUtil.drawText(ctx,label,x,y,color,GFonts.MEDIUM,size);}
    private static void icon(DrawContext ctx,ItemStack stack,float x,float y,float scale){
        SkinCatalog.drawOriginal(()->{var m=ctx.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(scale,scale);ctx.drawItem(stack,0,0);m.popMatrix();});
    }
    private static String shortName(String value){String[] parts=value.split("[:/]");return parts.length==0?value:parts[parts.length-1].replace('_',' ');}
    private static String trim(String s,float width,float size){
        if(RenderUtil.getTextWidth(s,GFonts.MEDIUM,size)<=width)return s;
        while(!s.isEmpty()&&RenderUtil.getTextWidth(s+"…",GFonts.MEDIUM,size)>width)s=s.substring(0,s.length()-1);return s+"…";
    }
    private static boolean inside(double mx,double my,float x,float y,float w,float h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(click.button()==0){for(int i=hits.size()-1;i>=0;i--){Hit hit=hits.get(i);if(hit.contains(click.x(),click.y())){hit.action.run();return true;}}}
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){
        if(dragging!=null){
            dragging.setPosition((float)Math.max(0,Math.min(width-Math.max(30,dragging.getWidth()),click.x()-dragX)),(float)Math.max(65,Math.min(height-Math.max(20,dragging.getHeight()),click.y()-dragY)));return true;
        }return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseReleased(Click click){if(dragging!=null){dragging=null;Pyz1k.getInstance().getConfigManager().flushAutoSave();return true;}return super.mouseReleased(click);}
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){scroll-=Math.round((float)vertical);return true;}
    @Override public boolean keyPressed(KeyInput key){if(key.key()==256){if(page==Page.LAYOUT)switchPage(Page.SKINS);else close();return true;}return super.keyPressed(key);}
    @Override public void close(){Pyz1k.getInstance().getConfigManager().flushAutoSave();MinecraftClient.getInstance().setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
