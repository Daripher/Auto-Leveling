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
		
		public Common(ForgeConfigSpec.Builder builder)
		{
			movementSpeedBonus = builder.defineInRange("movement_speed_bonus", 0.0001D, 0.0D, 1.0D);
			flyingSpeedBonus = builder.defineInRange("flying_speed_bonus", 0.0001D, 0.0D, 1.0D);
			attackDamageBonus = builder.defineInRange("attack_damage_bonus", 0.001D, 0.0D, 1.0D);
			armorBonus = builder.defineInRange("armor_bonus", 0.001D, 0.0D, 1.0D);
			healthBonus = builder.defineInRange("health_bonus", 0.001D, 0.0D, 1.0D);
		}
	}
	
	static
	{
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}
