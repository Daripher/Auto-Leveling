package daripher.autoleveling.network;

import java.util.Optional;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@EventBusSubscriber(bus = Bus.MOD, modid = AutoLevelingMod.MOD_ID)
public class NetworkDispatcher {
	public static SimpleChannel network_channel;

	@SubscribeEvent
	public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
		var networkChannelId = new ResourceLocation(AutoLevelingMod.MOD_ID, "channel");
		network_channel = NetworkRegistry.newSimpleChannel(networkChannelId, () -> "1.0", s -> true, s -> true);
		network_channel.registerMessage(1, SyncLevelingData.class, SyncLevelingData::encode, SyncLevelingData::decode, SyncLevelingData::receive, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}
}
