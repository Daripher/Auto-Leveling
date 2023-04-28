package daripher.autoleveling;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.init.AutoLevelingItems;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod {
	public static final String MOD_ID = "autoleveling";

	public AutoLevelingMod() {
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AutoLevelingLootItemConditions.REGISTRY.register(modEventBus);
		AutoLevelingItems.REGISTRY.register(modEventBus);
		AutoLevelingAttributes.REGISTRY.register(modEventBus);
		Config.registerCommonConfig();
	}
}
