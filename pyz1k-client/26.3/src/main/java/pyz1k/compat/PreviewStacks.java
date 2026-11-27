package pyz1k.compat;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
/** Item defaults in 26.3 are supplied by the world registry, after login. */
public final class PreviewStacks {
 public static ItemStack of(ItemLike item) {
  if (item == null) return ItemStack.EMPTY;
  try { item.asItem().builtInRegistryHolder().components(); }
  catch (NullPointerException unbound) { return ItemStack.EMPTY; }
  return new ItemStack(item);
 }
}
