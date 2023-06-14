package daripher.autoleveling;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.init.AutoLevelingItems;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MOD_ID = "autoleveling";

	public AutoLevelingMod() {
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AutoLevelingLootItemConditions.REGISTRY.register(modEventBus);
		AutoLevelingItems.REGISTRY.register(modEventBus);
		AutoLevelingAttributes.REGISTRY.register(modEventBus);
		Config.registerCommonConfig();
	}
	
	@SubscribeEvent
	public static void attachMobAttributes(EntityAttributeModificationEvent event) {
		event.getTypes().forEach(entityType -> {
			event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER.get());
			event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER.get());
		});
	}
	
	@SubscribeEvent
	public static void registerCreativeTab(CreativeModeTabEvent.Register event) {
		var creativeTabId = new ResourceLocation(AutoLevelingMod.MOD_ID, "tools");
		event.registerCreativeModeTab(creativeTabId, AutoLevelingMod::createToolsCreativeTab);
	}

	private static void createToolsCreativeTab(CreativeModeTab.Builder builder) {
		builder.title(Component.translatable("itemGroup.autoleveling"));
		builder.icon(() -> new ItemStack(AutoLevelingItems.BLACKLIST_TOOL.get()));
		builder.displayItems(AutoLevelingMod::addToolsCreativeTabItems);
	}

	private static void addToolsCreativeTabItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
		output.accept(AutoLevelingItems.BLACKLIST_TOOL.get());
		output.accept(AutoLevelingItems.WHITELIST_TOOL.get());
	}
}
