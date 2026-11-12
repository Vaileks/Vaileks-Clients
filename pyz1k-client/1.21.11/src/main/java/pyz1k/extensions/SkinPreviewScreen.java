package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;

/** Native entity GUI rendering preserves real depth, special models and pack textures. */
public final class SkinPreviewScreen extends Screen {
    private final Screen parent;
    private final SkinCatalog.Skin skin;
    private final Item target;
    private float yaw=25,pitch=-15,zoom=1;
    private boolean rotating;
    private int x,y,w,h;
    public SkinPreviewScreen(Screen parent,Item target,SkinCatalog.Skin skin){super(Text.literal("ItemSkin · 3D"));this.parent=parent;this.target=target;this.skin=skin;}
    @Override protected void init(){w=Math.min(450,width-16);h=Math.min(340,height-16);x=(width-w)/2;y=(height-h)/2;}
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        HudVisualStyle.drawMenuPanel(ctx,x,y,w,h,w,1);
        RenderUtil.drawText(ctx,"ItemSkin · 3D",x+15,y+12,ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,10);
        ItemStack stack=skin.preview(target);
        var state=new ItemEntityRenderState();state.entityType=EntityType.ITEM;state.height=1;state.width=1;state.light=0xf000f0;state.renderedAmount=1;
        SkinCatalog.drawOriginal(()->MinecraftClient.getInstance().getItemModelManager().clearAndUpdate(state.itemRenderState,stack,ItemDisplayContext.FIXED,MinecraftClient.getInstance().world,null,0));
        // Render a fresh state so neither the world item scale nor its cached model is changed.
        var box=state.itemRenderState.getModelBoundingBox();
        float dx=(float)(box.maxX-box.minX),dy=(float)(box.maxY-box.minY),dz=(float)(box.maxZ-box.minZ);
        float diameter=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);if(!Float.isFinite(diameter)||diameter<.01f)diameter=1;
        var rotation=new Quaternionf().rotateZ((float)Math.PI).rotateX((float)Math.toRadians(pitch)).rotateY((float)Math.toRadians(yaw));
        var center=new Vector3f((float)(box.minX+box.maxX)*.5f,dy*.5f+.1625f,(float)(box.minZ+box.maxZ)*.5f).rotate(rotation).negate();
        ctx.addEntity(state,Math.min(w-30,h-100)*.75f*zoom/diameter,center,rotation,null,x+15,y+35,x+w-15,y+h-65);
        RenderUtil.drawText(ctx,"ЛКМ: вращение · Колесо: масштаб · ПКМ: сброс",x+15,y+h-58,ClientColors.CATEGORY_INACTIVE,GFonts.MEDIUM,7);
        ctx.drawText(textRenderer,textRenderer.trimToWidth(stack.getName().getString(),w-30),x+15,y+h-44,ClientColors.TEXT_PRIMARY,false);
        RenderUtil.drawRoundRect(ctx,x+15,y+h-27,100,19,4,ClientColors.CATEGORY_ACCENT);
        RenderUtil.drawText(ctx,"Применить",x+25,y+h-21,ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,8);
        RenderUtil.drawRoundRect(ctx,x+w-90,y+h-27,75,19,4,ClientColors.SETTINGS_BACKGROUND);
        RenderUtil.drawText(ctx,"Назад",x+w-78,y+h-21,ClientColors.TEXT_PRIMARY,GFonts.MEDIUM,8);
    }
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(click.button()==1){yaw=25;pitch=-15;zoom=1;return true;}
        if(click.button()==0){
            if(click.y()>=y+h-27&&click.y()<=y+h-8){if(click.x()>=x+15&&click.x()<=x+115){SkinCatalog.select(target,skin);close();return true;}if(click.x()>=x+w-90&&click.x()<=x+w-15){close();return true;}}
            rotating=click.x()>x+15&&click.x()<x+w-15&&click.y()>y+35&&click.y()<y+h-65;return rotating;
        }
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(rotating){yaw+=(float)dx;pitch=Math.clamp(pitch+(float)dy,-85,85);return true;}return super.mouseDragged(click,dx,dy);}
    @Override public boolean mouseReleased(Click click){rotating=false;return true;}
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){zoom=Math.clamp(zoom+(float)vertical*.1f,.4f,2.5f);return true;}
    @Override public boolean keyPressed(KeyInput key){if(key.key()==256){close();return true;}return super.keyPressed(key);}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
