package daripher.autoleveling.api;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class LevelingApi
{
	public static final Capability<ILevelingData> CAPABILITY = CapabilityManager.get(new CapabilityToken<ILevelingData>() {});
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "leveling");
	
	public static boolean canHaveLevel(Entity entity)
	{
		return entity instanceof LivingEntity && ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE) != null;
	}
}
