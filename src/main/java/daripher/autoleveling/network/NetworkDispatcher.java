package daripher.autoleveling.network;

import java.util.Optional;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@EventBusSubscriber(bus = Bus.MOD, modid = AutoLevelingMod.MOD_ID)
public class NetworkDispatcher {
	private static final ResourceLocation NETWORK_CHANNEL_ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "channel");
	public static SimpleChannel network_channel;

	@SubscribeEvent
	public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
		network_channel = NetworkRegistry.newSimpleChannel(NETWORK_CHANNEL_ID, () -> "1.0", s -> true, s -> true);
		network_channel.registerMessage(1, SyncLevelingData.class, SyncLevelingData::encode, SyncLevelingData::decode, SyncLevelingData::receive, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}
}
