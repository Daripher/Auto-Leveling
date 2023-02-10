package daripher.autoleveling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.capability.LevelingData;
import daripher.autoleveling.capability.LevelingDataStorage;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.init.AutoLevelingItems;
import daripher.autoleveling.init.AutoLevelingLootConditions;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod {
	public static final String MOD_ID = "autoleveling";
	public static final Logger LOGGER = LogManager.getLogger(AutoLevelingMod.MOD_ID);

	public AutoLevelingMod() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		// Just to call initialization
		AutoLevelingLootConditions.LEVEL_CHECK.getClass();
		AutoLevelingItems.REGISTRY.register(modEventBus);
		AutoLevelingAttributes.REGISTRY.register(modEventBus);
	}

	@SubscribeEvent
	public static void setupMod(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(ILevelingData.class, new LevelingDataStorage(), LevelingData::new);
	}
}
