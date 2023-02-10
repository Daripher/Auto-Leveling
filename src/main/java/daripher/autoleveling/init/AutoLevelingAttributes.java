package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoLevelingAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AutoLevelingMod.MOD_ID);

	public static final RegistryObject<Attribute> PROJECTILE_DAMAGE_BONUS = rangedAttribute("monster", "projectile_damage_bonus", 1, 1, 1000);
	public static final RegistryObject<Attribute> EXPLOSION_DAMAGE_BONUS = rangedAttribute("monster", "explosion_damage_bonus", 1, 1, 1000);

	private static RegistryObject<Attribute> rangedAttribute(String category, String name, double defaultValue, double minValue, double maxValue) {
		return REGISTRY.register(category + "." + name, () -> new RangedAttribute(category + "." + name, defaultValue, minValue, maxValue).setSyncable(true));
	}
}
