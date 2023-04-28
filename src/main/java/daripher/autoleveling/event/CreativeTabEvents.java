package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
public class CreativeTabEvents {
	@SubscribeEvent
	public static void registerCreativeTab(CreativeModeTabEvent.Register event) {
		var creativeTabId = new ResourceLocation(AutoLevelingMod.MOD_ID, "tools");
		event.registerCreativeModeTab(creativeTabId, CreativeTabEvents::createToolsCreativeTab);
	}

	private static void createToolsCreativeTab(CreativeModeTab.Builder builder) {
		builder.title(Component.translatable("itemGroup.autoleveling"));
		builder.icon(() -> new ItemStack(AutoLevelingItems.BLACKLIST_TOOL.get()));
		builder.displayItems(CreativeTabEvents::addToolsCreativeTabItems);
	}

	private static void addToolsCreativeTabItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
		output.accept(AutoLevelingItems.BLACKLIST_TOOL.get());
		output.accept(AutoLevelingItems.WHITELIST_TOOL.get());
	}
}
