package daripher.autoleveling.network;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@EventBusSubscriber(bus = Bus.MOD, modid = AutoLevelingMod.MOD_ID)
public class NetworkDispatcher {
	private static final ResourceLocation NETWORK_CHANNEL_ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "channel");
	private static int lastMessageId;
	public static SimpleChannel networkChannel;

	@SubscribeEvent
	public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
		networkChannel = NetworkRegistry.newSimpleChannel(NETWORK_CHANNEL_ID, () -> "1.0", s -> true, s -> true);
		registerMessage(new SyncLevelingData());
	}

	private static <T> void registerMessage(INetworkMessage<T> message) {
		networkChannel.registerMessage(++lastMessageId, message.getType(), message.getEncoder(), message.getDecoder(), message.getConsumer(), message.getDirection());
	}
}
