package daripher.autoleveling.network;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public interface INetworkMessage<T> {
	Class<T> getType();

	BiConsumer<T, PacketBuffer> getEncoder();

	Function<PacketBuffer, T> getDecoder();

	BiConsumer<T, Supplier<NetworkEvent.Context>> getConsumer();

	Optional<NetworkDirection> getDirection();
}
