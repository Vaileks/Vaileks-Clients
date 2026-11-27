package pyz1k.extensions.mixin;
import java.util.Map;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.advancement.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ClientAdvancementManager.class)
public interface AdvancementProgressAccessor {
 @Accessor("advancementProgresses") Map<AdvancementEntry,AdvancementProgress> pyz1k$progress();
}
