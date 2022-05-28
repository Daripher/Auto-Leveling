package daripher.autoleveling.config;

import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config
{
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	
	public static class Common
	{
		public final ConfigValue<Boolean> showLevel;
		public final ConfigValue<Double> levelBonus;
		public final ConfigValue<Double> movementSpeedBonus;
		public final ConfigValue<Double> flyingSpeedBonus;
		public final ConfigValue<Double> attackDamageBonus;
		public final ConfigValue<Double> armorBonus;
		public final ConfigValue<Double> healthBonus;
		
		public final ConfigValue<Boolean> lootDropEnabled;
		public final ConfigValue<Boolean> showItemLevel;
		public final ConfigValue<Boolean> showItemRarity;
		public final ConfigValue<Double> lootDropChance;
		public final ConfigValue<Double> lootDropChancePerLevel;
		public final ConfigValue<Double> lootDropChancePerLuck;
		public final ConfigValue<Double> uncommonDropChance;
		public final ConfigValue<Double> rareDropChance;
		public final ConfigValue<Double> epicDropChance;
		public final ConfigValue<Double> legendaryDropChance;
		public final ConfigValue<Double> uncommonDropChancePerLuck;
		public final ConfigValue<Double> rareDropChancePerLuck;
		public final ConfigValue<Double> epicDropChancePerLuck;
		public final ConfigValue<Double> legendaryDropChancePerLuck;
		
		public Common(ForgeConfigSpec.Builder builder)
		{
			Predicate<Object> positiveOrZeroDoublePredicate = d -> d instanceof Double && (Double) d >= 0;
			builder.push("mobs");
			levelBonus = builder.define("levels_per_distance", 0.01D, positiveOrZeroDoublePredicate);
			showLevel = builder.define("show_level", true);
			builder.push("attributes");
			movementSpeedBonus = builder.define("movement_speed_per_level", 0.001D, positiveOrZeroDoublePredicate);
			flyingSpeedBonus = builder.define("flying_speed_per_level", 0.001D, positiveOrZeroDoublePredicate);
			attackDamageBonus = builder.define("attack_damage_per_level", 0.1D, positiveOrZeroDoublePredicate);
			armorBonus = builder.define("armor_per_level", 0.1D, positiveOrZeroDoublePredicate);
			healthBonus = builder.define("health_per_level", 0.1D, positiveOrZeroDoublePredicate);
			builder.pop(2);
			builder.push("loot");
			lootDropEnabled = builder.define("drop_enabled", true);
			showItemLevel = builder.define("show_item_level", true);
			showItemRarity = builder.define("show_item_rarity", true);
			lootDropChance = builder.define("drop_chance", 0.2D, positiveOrZeroDoublePredicate);
			lootDropChancePerLevel = builder.define("drop_chance_per_level", 0.05D, positiveOrZeroDoublePredicate);
			lootDropChancePerLuck = builder.define("drop_chance_per_luck", 0.1D, positiveOrZeroDoublePredicate);
			uncommonDropChance = builder.define("uncommon_loot_drop_chance", 0.75D, positiveOrZeroDoublePredicate);
			rareDropChance = builder.define("rare_loot_drop_chance", 0.3D, positiveOrZeroDoublePredicate);
			epicDropChance = builder.define("epic_loot_drop_chance", 0.05D, positiveOrZeroDoublePredicate);
			legendaryDropChance = builder.define("legendary_loot_drop_chance", 0.01D, positiveOrZeroDoublePredicate);
			uncommonDropChancePerLuck = builder.define("uncommon_loot_luck_bonus", 0.1D, positiveOrZeroDoublePredicate);
			rareDropChancePerLuck = builder.define("rare_loot_luck_bonus", 0.02D, positiveOrZeroDoublePredicate);
			epicDropChancePerLuck = builder.define("epic_loot_luck_bonus", 0.01D, positiveOrZeroDoublePredicate);
			legendaryDropChancePerLuck = builder.define("legendary_loot_luck_bonus", 0.005D, positiveOrZeroDoublePredicate);
			builder.pop(1);
		}
	}
	
	static
	{
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
