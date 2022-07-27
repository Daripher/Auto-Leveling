package daripher.autoleveling;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod
{
	public static final String MOD_ID = "autoleveling";
	
	public AutoLevelingMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AutoLevelingLootItemConditions.REGISTRY.register(modEventBus);
	}
	
	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(ILevelingData.class);
	}
}
