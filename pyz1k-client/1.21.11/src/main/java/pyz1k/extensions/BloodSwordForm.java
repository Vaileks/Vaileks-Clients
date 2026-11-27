package pyz1k.extensions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
public enum BloodSwordForm {
    SWORD("Меч",0xffe8b84a),TRANSITION("Переход",0xfff06b61),MACE("Булава",0xffe53935),HARPOON("Гарпун",0xffff785e);
    public final String label;public final int color;
    BloodSwordForm(String label,int color){this.label=label;this.color=color;}
    public static BloodSwordForm from(ItemStack stack){
        if(stack==null||stack.isEmpty())return null;var model=stack.get(DataComponentTypes.ITEM_MODEL);
        if(model==null||!model.getNamespace().equals("bloodstone")||!model.getPath().matches("tools/bloodsword/[^/]+/(sword|block|mace|harpoon)"))return null;
        return switch(model.getPath().substring(model.getPath().lastIndexOf('/')+1)){case "sword"->SWORD;case "block"->TRANSITION;case "mace"->MACE;case "harpoon"->HARPOON;default->null;};
    }
}
