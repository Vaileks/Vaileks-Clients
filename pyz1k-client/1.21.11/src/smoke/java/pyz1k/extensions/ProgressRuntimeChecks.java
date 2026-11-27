package pyz1k.extensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import pyz1k.config.ConfigManager;
import java.nio.file.*;
import java.util.*;
public final class ProgressRuntimeChecks {
 static String persistedKey;
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void prepare(MinecraftClient mc)throws Exception {
  String a=UUID.randomUUID().toString(),b=UUID.randomUUID().toString();persistedKey=a+"\nsmoke.test";long n=System.nanoTime();
  ServerPlaytime.observe("SMOKE.TEST:25565",a,"Test",n);ServerPlaytime.observe("smoke.test",a,"Test",n+2_000_000_000L);ServerPlaytime.observe("second.test",a,"Test",n+3_000_000_000L);ServerPlaytime.observe("second.test",a,"Test",n+5_000_000_000L);ServerPlaytime.observe("smoke.test",b,"Other",n+6_000_000_000L);ServerPlaytime.observe("smoke.test",b,"Other",n+7_000_000_000L);ServerPlaytime.observe("","","",n+8_000_000_000L);ServerPlaytime.save();
  var entries=ServerPlaytime.entries();check(entries.stream().anyMatch(e->e.key().equals(a+"\nsmoke.test")&&e.millis()==2000),"server total");check(entries.stream().anyMatch(e->e.key().equals(b+"\nsmoke.test")&&e.millis()==1000),"account isolation");
  var chest=new ItemStack(Items.DIAMOND_CHESTPLATE);chest.addEnchantment(mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.BLAST_PROTECTION),4);mc.player.equipStack(EquipmentSlot.CHEST,chest);mc.player.equipStack(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));FeatureModules.blast.setEnable(true);
  mc.getServer().execute(()->{var player=mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());var manager=mc.getServer().getAdvancementLoader();for(String[] pair:new String[][]{{"adventure/adventuring_time","minecraft:plains"},{"husbandry/balanced_diet","bread"},{"husbandry/bred_all_animals","minecraft:cow"},{"adventure/kill_all_mobs","minecraft:zombie"}}){var entry=manager.get(Identifier.of("minecraft:"+pair[0]));check(entry!=null,"server fixture advancement");var ancestor=entry.value().parent();while(ancestor.isPresent()){var parent=manager.get(ancestor.get());for(String criterion:parent.value().requirements().getNames())player.getAdvancementTracker().grantCriterion(parent,criterion);ancestor=parent.value().parent();}player.getAdvancementTracker().grantCriterion(entry,pair[1]);}});
 }
 public static void verifyAdvancements()throws Exception {
  var entries=AdvancementData.read();System.out.println("PROGRESS_SMOKE: received achievements "+entries.stream().map(AdvancementData.Entry::id).toList());for(String[] pair:new String[][]{{"adventure/adventuring_time","minecraft:plains"},{"husbandry/balanced_diet","bread"},{"husbandry/bred_all_animals","minecraft:cow"},{"adventure/kill_all_mobs","minecraft:zombie"}}){var e=entries.stream().filter(v->v.id().equals("minecraft:"+pair[0])).findFirst().orElseThrow();check(e.checklist().state(pair[1])==ProgressLogic.State.DONE,"received criterion missing: "+pair[0]);check(e.checklist().names().size()>1,"remaining checklist missing");}
  var path=ConfigManager.getRootDir().resolve("config/server-playtime.json");check(Files.exists(path),"playtime not persisted");var json=com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();check(json.getAsJsonObject("totals").get(persistedKey).getAsLong()==2000,"saved server total incorrect");
  System.out.println("PROGRESS_SMOKE: server-supplied biome, food, breed and kill criteria plus remaining lists and persisted per-account time passed");
 }
 public static void verifyRevocation(){var e=AdvancementData.read().stream().filter(v->v.id().equals("minecraft:husbandry/balanced_diet")).findFirst().orElseThrow();check(e.checklist().state("bread")==ProgressLogic.State.REMAINING,"revoked progress not refreshed");}
}
