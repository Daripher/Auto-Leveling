package daripher.autoleveling.api;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class LevelingApi
{
	@CapabilityInject(ILevelingData.class)
	public static final Capability<ILevelingData> CAPABILITY = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "leveling");
}
