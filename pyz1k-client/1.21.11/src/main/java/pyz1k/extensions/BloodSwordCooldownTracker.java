package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import pyz1k.extensions.mixin.*;
import java.util.*;

/** Observes received inventory data; never starts a cooldown on a guessed click. */
public final class BloodSwordCooldownTracker {
    public record Timer(String key,String label,ItemStack stack,BloodSwordForm form,double remaining,double duration,String source){
        public float progress(){return (float)Math.clamp(remaining/Math.max(.001,duration),0,1);}
    }
    private static List<Timer> active=List.of();
    private static final Map<String,Double> observedTotals=new HashMap<>();
    private static boolean present;
    public static List<Timer> active(){return active;}
    public static boolean present(){return present;}
    public static void clear(){active=List.of();present=false;observedTotals.clear();}
    public static void tick(){
        if(FeatureModules.blood==null||!FeatureModules.blood.isEnable()){if(present||!active.isEmpty()||!observedTotals.isEmpty())clear();return;}
        var mc=MinecraftClient.getInstance();var module=FeatureModules.blood;
        if(mc.player==null||mc.world==null||module==null||!module.isEnable()){clear();return;}
        List<ItemStack> stacks=new ArrayList<>();stacks.add(mc.player.getOffHandStack());
        for(int slot=0;slot<mc.player.getInventory().size();slot++)stacks.add(mc.player.getInventory().getStack(slot));
        scan(stacks,mc.player.getItemCooldownManager(),BloodCooldownProfiles.mode(module.mode.getValue()),System.currentTimeMillis(),module.itemData.isValue());
    }
    public static List<Timer> scan(List<ItemStack> stacks,net.minecraft.entity.player.ItemCooldownManager manager,BloodCooldownProfiles.Mode mode,long now,boolean itemData){
        var access=(ItemCooldownManagerAccessor)manager;
        int nowTick=access.bloodSwordCooldown$getTick();
        LinkedHashMap<String,Timer> found=new LinkedHashMap<>();Set<Identifier> groups=new HashSet<>();present=false;
        for(ItemStack stack:stacks){
            BloodSwordForm form=BloodSwordForm.from(stack);if(form==null)continue;present=true;
            boolean receivedTimer=false;
            if(itemData){
                var component=stack.get(DataComponentTypes.CUSTOM_DATA);
                if(component!=null){
                    NbtCompound pdc=component.copyNbt().getCompoundOrEmpty("PublicBukkitValues");
                    for(String key:pdc.getKeys()){
                        String field=key.substring(key.indexOf(':')+1);String label,setting;
                        switch(field){
                            case "bloodsword_cooldown_until"->{label="КД булавы";setting="cooldown";}
                            case "bloodsword_bow_cooldown_until"->{label="КД гарпуна";setting="bow_cooldown";}
                            case "bloodsword_bow_pull_cooldown_until"->{label="КД притягивания";setting="bow_pull_cooldown";}
                            case "bloodsword_next_at"->{label="Форма: "+form.label;setting=switch(form){case MACE->"mace_duration";case HARPOON->"bow_duration";default->"transition_duration";};}
                            default->{continue;}
                        }
                        long end=pdc.getLong(key,0L);double remaining=BloodCooldownProfiles.remaining(end,now);if(remaining<=0)continue;
                        receivedTimer=true;
                        String timerKey=key+"|"+end;
                        double duration=mode.seconds(setting);
                        if(setting.equals("cooldown")&&form!=BloodSwordForm.SWORD)duration+=mode.seconds("mace_duration")+2*mode.seconds("transition_duration");
                        if(setting.equals("bow_cooldown")&&form==BloodSwordForm.HARPOON)duration+=mode.seconds("bow_duration")+2*mode.seconds("transition_duration");
                        double total=Math.max(duration,observedTotals.computeIfAbsent(timerKey,k->remaining));
                        found.putIfAbsent(timerKey,new Timer(timerKey,label,stack.copy(),form,remaining,total,"Предмет сервера"));
                    }
                }
            }
            Identifier group=manager.getGroup(stack);
            if(groups.add(group)&&!receivedTimer){
                Object raw=access.bloodSwordCooldown$getEntries().get(group);
                if(raw instanceof ItemCooldownEntryAccessor entry){
                    double remaining=(entry.bloodSwordCooldown$getEndTick()-nowTick)/20d;
                    if(remaining>0){String key="vanilla:"+group;found.put(key,new Timer(key,"КД: "+form.label,stack.copy(),form,remaining,(entry.bloodSwordCooldown$getEndTick()-entry.bloodSwordCooldown$getStartTick())/20d,"Сервер"));}
                }
            }
        }
        observedTotals.keySet().retainAll(found.keySet());active=List.copyOf(found.values());return active;
    }
}
