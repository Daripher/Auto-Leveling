package daripher.autoleveling.config;

import java.util.ArrayList;
import java.util.List;
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
		public final ConfigValue<Double> movementSpeedBonus;
		public final ConfigValue<Double> flyingSpeedBonus;
		public final ConfigValue<Double> attackDamageBonus;
		public final ConfigValue<Double> armorBonus;
		public final ConfigValue<Double> healthBonus;
		public final ConfigValue<Double> levelBonus;
		public final ConfigValue<Double> expBonus;
		public final ConfigValue<Boolean> showLevel;
		public final ConfigValue<Integer> maxLevel;
		public final ConfigValue<Integer> randomLevelBonus;
		public final ConfigValue<List<String>> blacklistedMobs;
		public final ConfigValue<Integer> overworldStartingLevel;
		public final ConfigValue<Integer> theNetherStartingLevel;
		public final ConfigValue<Integer> theEndStartingLevel;

		public Common(ForgeConfigSpec.Builder builder)
		{
			Predicate<Object> positiveOrZeroDouble = o -> o instanceof Double && (Double) o >= 0;
			Predicate<Object> positiveOrZeroInteger = o -> o instanceof Integer && (Integer) o >= 0;
			Predicate<Object> positiveInteger = o -> o instanceof Integer && (Integer) o > 0;
			builder.push("mobs");
			blacklistedMobs = builder.define("blacklist", new ArrayList<String>());
			showLevel = builder.define("show_level", true);
			expBonus = builder.define("exp_bonus_per_level", 0.1D);
			builder.push("level");
			levelBonus = builder.define("levels_per_distance", 0.01D, positiveOrZeroDouble);
			maxLevel = builder.define("max_level", 0, positiveOrZeroInteger);
			randomLevelBonus = builder.define("random_level_bonus", 0, positiveOrZeroInteger);
			builder.push("attributes");
			movementSpeedBonus = builder.define("movement_speed_per_level", 0.001D, positiveOrZeroDouble);
			flyingSpeedBonus = builder.define("flying_speed_per_level", 0.001D, positiveOrZeroDouble);
			attackDamageBonus = builder.define("attack_damage_per_level", 0.1D, positiveOrZeroDouble);
			armorBonus = builder.define("armor_per_level", 0.1D, positiveOrZeroDouble);
			healthBonus = builder.define("health_per_level", 0.1D, positiveOrZeroDouble);
			builder.push("dimensions");
			overworldStartingLevel = builder.define("overworld_starting_level", 1, positiveInteger);
			theNetherStartingLevel = builder.define("the_nether_starting_level", 1, positiveInteger);
			theEndStartingLevel = builder.define("the_end_starting_level", 1, positiveInteger);
			builder.pop(2);
		}
	}
	
	static
	{
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
