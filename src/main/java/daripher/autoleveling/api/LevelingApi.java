package daripher.autoleveling.api;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class LevelingApi {
	public static final Capability<ILevelingData> CAPABILITY = CapabilityManager.get(new CapabilityToken<ILevelingData>() {});
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "leveling");
}
