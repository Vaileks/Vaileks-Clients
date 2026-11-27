package pyz1k.extensions.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
import pyz1k.module.base.Module;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.menu.sections.ModuleGridSection;
@Mixin(value=MenuScreen.class,remap=false)
public interface UnifiedMenuAccess {
 @Invoker("focusSearchResult") void pyz1k$focus(Module module);
 @Accessor("moduleGridSection") ModuleGridSection pyz1k$grid();
}
