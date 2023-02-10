package daripher.autoleveling.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	public static class Common {
		public final ConfigValue<List<List<Object>>> attributesBonuses;
		public final ConfigValue<Double> expMultiplier;
		public final ConfigValue<Boolean> showLevel;
		public final ConfigValue<List<String>> blacklistedMobs;
		public final ConfigValue<List<String>> whitelistedMobs;
		public final ConfigValue<Integer> defaultStartingLevel;
		public final ConfigValue<Integer> defaultMaxLevel;
		public final ConfigValue<Double> defaultLevelsPerDistance;
		public final ConfigValue<Double> defaultLevelsPerDeepness;
		public final ConfigValue<Integer> defaultRandomLevelBonus;

		public Common(ForgeConfigSpec.Builder builder) {
			Predicate<Object> positiveOrZeroDouble = o -> o instanceof Double && (Double) o >= 0;
			Predicate<Object> positiveDouble = o -> o instanceof Double && (Double) o > 0;
			Predicate<Object> positiveOrZeroInteger = o -> o instanceof Integer && (Integer) o >= 0;
			Predicate<Object> positiveInteger = o -> o instanceof Integer && (Integer) o > 0;
			builder.push("mobs");
			blacklistedMobs = builder.define("blacklist", new ArrayList<String>());
			whitelistedMobs = builder.define("whitelist", new ArrayList<String>());
			showLevel = builder.define("show_level", true);
			expMultiplier = builder.define("exp_bonus_per_level", 0.1D);
			builder.pop();
			builder.push("attributes");
			attributesBonuses = builder.define("attribute_bonuses",
					ImmutableList.of(
							ImmutableList.of("minecraft:generic.movement_speed", 0.001),
							ImmutableList.of("minecraft:generic.flying_speed", 0.001),
							ImmutableList.of("minecraft:generic.attack_damage", 0.1),
							ImmutableList.of("minecraft:generic.armor", 0.1),
							ImmutableList.of("minecraft:generic.max_health", 0.1),
							ImmutableList.of("autoleveling:monster.projectile_damage_bonus", 0.1),
							ImmutableList.of("autoleveling:monster.explosion_damage_bonus", 0.1)),
					Config::isValidAttributeBonusConfig);
			builder.pop();
			builder.push("default_leveling_settings");
			defaultStartingLevel = builder.define("starting_level", 1, positiveInteger);
			defaultMaxLevel = builder.define("max_level", 0, positiveOrZeroInteger);
			defaultLevelsPerDistance = builder.define("levels_per_distance", 0.01D, positiveDouble);
			defaultLevelsPerDeepness = builder.define("levels_per_deepness", 0D, positiveOrZeroDouble);
			defaultRandomLevelBonus = builder.define("random_level_bonus", 0, positiveOrZeroInteger);
			builder.pop();
		}
	}

	private static boolean isValidAttributeBonusConfig(Object object) {
		if (object == null) {
			return false;
		}

		if (!(object instanceof List)) {
			return false;
		}
		
		List<?> list = (List<?>) object;

		for (Object listObject : list) {
			if (!(listObject instanceof List)) {
				return false;
			}
			
			List<?> innerList = (List<?>) listObject;

			if (innerList.size() != 2) {
				return false;
			}

			if (!(innerList.get(0) instanceof String)) {
				return false;
			}

			if (innerList.get(1) instanceof Double) {
				return false;
			}
		}

		return true;
	}

	static {
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
