package daripher.autoleveling;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.capability.LevelingData;
import daripher.autoleveling.config.Config;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod
{
	public static final String MOD_ID = "autoleveling";
	
	public AutoLevelingMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onCommonSetup);
	}
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		CapabilityManager.INSTANCE.register(ILevelingData.class, new LevelingData.Storage(), LevelingData::new);
	}
}
