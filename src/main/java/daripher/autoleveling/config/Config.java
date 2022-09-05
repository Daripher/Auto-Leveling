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
		public final ConfigValue<Double> expBonus;
		public final ConfigValue<Boolean> showLevel;
		public final ConfigValue<List<String>> blacklistedMobs;
		public final ConfigValue<List<String>> whitelistedMobs;
		
		public Common(ForgeConfigSpec.Builder builder)
		{
			Predicate<Object> positiveOrZeroDouble = o -> o instanceof Double && (Double) o >= 0;
			builder.push("mobs");
			blacklistedMobs = builder.define("blacklist", new ArrayList<String>());
			whitelistedMobs = builder.define("whitelist", new ArrayList<String>());
			showLevel = builder.define("show_level", true);
			expBonus = builder.define("exp_bonus_per_level", 0.1D);
			builder.pop();
			builder.push("attributes");
			movementSpeedBonus = builder.define("movement_speed_per_level", 0.001D, positiveOrZeroDouble);
			flyingSpeedBonus = builder.define("flying_speed_per_level", 0.001D, positiveOrZeroDouble);
			attackDamageBonus = builder.define("attack_damage_per_level", 0.1D, positiveOrZeroDouble);
			armorBonus = builder.define("armor_per_level", 0.1D, positiveOrZeroDouble);
			healthBonus = builder.define("health_per_level", 0.1D, positiveOrZeroDouble);
			builder.pop();
		}
	}
	
	static
	{
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
