package daripher.autoleveling.network.message;

import java.util.function.Supplier;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncLevelingData {
	private CompoundTag compoundTag;
	private int entityId;

	private SyncLevelingData() {
	}

	public SyncLevelingData(LivingEntity entity, ILevelingData levelingData) {
		compoundTag = levelingData.serializeNBT();
		entityId = entity.getId();
	}

	public static SyncLevelingData decode(FriendlyByteBuf buf) {
		var result = new SyncLevelingData();
		result.compoundTag = buf.readAnySizeNbt();
		result.entityId = buf.readInt();
		return result;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt(compoundTag);
		buf.writeInt(entityId);
	}

	public static void receive(SyncLevelingData message, Supplier<NetworkEvent.Context> ctxSupplier) {
		var ctx = ctxSupplier.get();
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(message, ctx)));
	}

	@OnlyIn(value = Dist.CLIENT)
	private static void handlePacket(SyncLevelingData message, NetworkEvent.Context ctx) {
		var minecraft = Minecraft.getInstance();
		var entity = minecraft.level.getEntity(message.entityId);

		if (entity instanceof LivingEntity) {
			LevelingDataProvider.get((LivingEntity) entity).ifPresent(levelingData -> {
				levelingData.deserializeNBT(message.compoundTag);
			});
		}
	}
}
