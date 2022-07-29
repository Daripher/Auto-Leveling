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
		public final ConfigValue<Boolean> showLevel;
		public final ConfigValue<List<String>> blacklistedMobs;
		
		public Common(ForgeConfigSpec.Builder builder)
		{
			Predicate<Object> positiveOrZeroDoublePredicate = d -> d instanceof Double && (Double) d >= 0;
			builder.push("mobs");
			levelBonus = builder.define("levels_per_distance", 0.01D, positiveOrZeroDoublePredicate);
			showLevel = builder.define("show_level", true);
			blacklistedMobs = builder.define("blacklist", new ArrayList<String>());
			builder.push("attributes");
			movementSpeedBonus = builder.define("movement_speed_per_level", 0.001D, positiveOrZeroDoublePredicate);
			flyingSpeedBonus = builder.define("flying_speed_per_level", 0.001D, positiveOrZeroDoublePredicate);
			attackDamageBonus = builder.define("attack_damage_per_level", 0.1D, positiveOrZeroDoublePredicate);
			armorBonus = builder.define("armor_per_level", 0.1D, positiveOrZeroDoublePredicate);
			healthBonus = builder.define("health_per_level", 0.1D, positiveOrZeroDoublePredicate);
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
