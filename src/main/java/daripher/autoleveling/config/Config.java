package daripher.autoleveling.config;

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

		public Common(ForgeConfigSpec.Builder builder)
		{
			builder.push("mobs");
			movementSpeedBonus = builder.defineInRange("movement_speed_per_level", 0.001D, 0.0D, 1.0D);
			flyingSpeedBonus = builder.defineInRange("flying_speed_per_level", 0.001D, 0.0D, 1.0D);
			attackDamageBonus = builder.defineInRange("attack_damage_per_level", 0.1D, 0.0D, 1.0D);
			armorBonus = builder.defineInRange("armor_per_level", 0.1D, 0.0D, 1.0D);
			healthBonus = builder.defineInRange("health_per_level", 0.1D, 0.0D, 1.0D);
			levelBonus = builder.defineInRange("levels_per_distance", 0.01D, 0.0D, 1.0D);
			showLevel = builder.define("show_level", true);
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
