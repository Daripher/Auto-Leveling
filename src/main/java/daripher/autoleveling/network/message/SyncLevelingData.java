package daripher.autoleveling.network.message;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.network.INetworkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SyncLevelingData implements INetworkMessage<SyncLevelingData> {
	private CompoundNBT nbt;
	private int entityId;

	public SyncLevelingData(LivingEntity entity, ILevelingData levelingData) {
		nbt = (CompoundNBT) LevelingApi.CAPABILITY.writeNBT(levelingData, null);
		entityId = entity.getId();
	}

	public SyncLevelingData() {
	}

	@Override
	public Function<PacketBuffer, SyncLevelingData> getDecoder() {
		return buf -> {
			SyncLevelingData result = new SyncLevelingData();
			result.nbt = buf.readAnySizeNbt();
			result.entityId = buf.readInt();
			return result;
		};
	}

	@Override
	public BiConsumer<SyncLevelingData, PacketBuffer> getEncoder() {
		return (msg, buf) -> {
			buf.writeNbt(msg.nbt);
			buf.writeInt(msg.entityId);
		};
	}

	@Override
	public BiConsumer<SyncLevelingData, Supplier<Context>> getConsumer() {
		return (msg, ctxSupplier) -> {
			NetworkEvent.Context ctx = ctxSupplier.get();
			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacketOnClient(msg, ctx)));
		};
	}

	@Override
	public Class<SyncLevelingData> getType() {
		return SyncLevelingData.class;
	}

	@Override
	public Optional<NetworkDirection> getDirection() {
		return Optional.of(NetworkDirection.PLAY_TO_CLIENT);
	}

	@OnlyIn(value = Dist.CLIENT)
	private static void handlePacketOnClient(SyncLevelingData message, Context ctx) {
		Minecraft client = Minecraft.getInstance();
		Entity entity = client.level.getEntity(message.entityId);

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		LevelingDataProvider.getLevelingData((LivingEntity) entity).ifPresent(levelingData -> {
			LevelingApi.CAPABILITY.readNBT(levelingData, null, message.nbt);
		});
	}
}
