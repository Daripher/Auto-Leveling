package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.loot.LevelCheck;
import net.minecraft.loot.LootConditionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class AutoLevelingLootConditions
{
	public static final LootConditionType LEVEL_CHECK = Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(AutoLevelingMod.MOD_ID, "level_check"),
			new LootConditionType(new LevelCheck.Serializer()));
}
