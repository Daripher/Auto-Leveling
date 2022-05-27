package daripher.autoleveling.network;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;

import java.util.Optional;

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
public class NetworkDispatcher
{
	public static SimpleChannel networkChannel;
	
	@SubscribeEvent
	public static void onCommonSetupEvent(FMLCommonSetupEvent event)
	{
		networkChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(AutoLevelingMod.MOD_ID, "channel"), () -> "1.0", s -> true, s -> true);
		networkChannel.registerMessage(1, SyncLevelingData.class, SyncLevelingData::encode, SyncLevelingData::decode, SyncLevelingData::receive, Optional.of(PLAY_TO_CLIENT));
	}
}
