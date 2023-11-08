package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.loot.LevelCondition;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class AutoLevelingLootConditions {
  public static final LootConditionType LEVEL_CHECK =
      registerCondition("level_check", new LevelCondition.Serializer());

  public static void init() {}

  private static <T extends ILootCondition> LootConditionType registerCondition(
      String name, ILootSerializer<T> serializer) {
    return Registry.register(
        Registry.LOOT_CONDITION_TYPE,
        new ResourceLocation(AutoLevelingMod.MOD_ID, name),
        new LootConditionType(serializer));
  }
}
