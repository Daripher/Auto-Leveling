package daripher.autoleveling.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	public static class Common {
		public final ConfigValue<List<? extends List<Object>>> attributesBonuses;
		public final ConfigValue<Double> expBonus;
		public final ConfigValue<Boolean> alwaysShowLevel;
		public final ConfigValue<Boolean> showLevelWhenLookingAt;
		public final ConfigValue<List<String>> blacklistedMobs;
		public final ConfigValue<List<String>> whitelistedMobs;
		public final ConfigValue<List<String>> blacklistedShownLevels;
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
			alwaysShowLevel = builder.define("always_show_level", false);
			showLevelWhenLookingAt = builder.define("show_level_when_looking_at", true);
			expBonus = builder.define("exp_bonus_per_level", 0.1D);
			blacklistedShownLevels = builder.define("hidden_levels", new ArrayList<String>());
			builder.pop();
			builder.push("attributes");
			attributesBonuses = builder.defineList("attribute_bonuses", Config::getDefaultAttributeBonuses, Config::isValidAttributeBonus);
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

	private static List<List<Object>> getDefaultAttributeBonuses() {
		List<List<Object>> attributeBonusesList = new ArrayList<>();
		attributeBonusesList.add(Arrays.asList("minecraft:generic.movement_speed", 0.001));
		attributeBonusesList.add(Arrays.asList("minecraft:generic.flying_speed", 0.001));
		attributeBonusesList.add(Arrays.asList("minecraft:generic.attack_damage", 0.1));
		attributeBonusesList.add(Arrays.asList("minecraft:generic.armor", 0.1));
		attributeBonusesList.add(Arrays.asList("minecraft:generic.max_health", 0.1));
		attributeBonusesList.add(Arrays.asList("autoleveling:monster.projectile_damage_bonus", 0.1));
		attributeBonusesList.add(Arrays.asList("autoleveling:monster.explosion_damage_bonus", 0.1));
		return attributeBonusesList;
	}

	private static <T> boolean isValidAttributeBonus(T object) {
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			return list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Double;
		}

		return false;
	}

	static {
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
