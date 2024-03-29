package daripher.autoleveling.network;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.network.message.SyncLevelingData;
import java.util.Optional;
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
  public static SimpleChannel networkChannel;

  @SubscribeEvent
  public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
    networkChannel =
        NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AutoLevelingMod.MOD_ID, "channel"),
            () -> "1.0",
            s -> true,
            s -> true);
    Optional<NetworkDirection> toClient = Optional.of(NetworkDirection.PLAY_TO_CLIENT);
    networkChannel.registerMessage(
        1,
        SyncLevelingData.class,
        SyncLevelingData::encode,
        SyncLevelingData::decode,
        SyncLevelingData::receive,
        toClient);
  }
}
