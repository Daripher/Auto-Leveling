package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.loot.condition.LevelCheck;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AutoLevelingLootItemConditions {
  public static final DeferredRegister<LootItemConditionType> REGISTRY =
      DeferredRegister.create(Registry.LOOT_CONDITION_TYPE.key(), AutoLevelingMod.MOD_ID);

  public static final RegistryObject<LootItemConditionType> LEVEL_CHECK =
      REGISTRY.register(
          "level_check", () -> new LootItemConditionType(new LevelCheck.Serializer()));
}
