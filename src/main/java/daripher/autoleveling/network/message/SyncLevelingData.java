package daripher.autoleveling.network.message;

import java.util.function.Supplier;

import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncLevelingData {
	private int entityId;
	private int level;

	private SyncLevelingData() {
	}

	public SyncLevelingData(LivingEntity entity) {
		entityId = entity.getId();
		level = MobsLevelingEvents.getLevel(entity);
	}

	public static SyncLevelingData decode(FriendlyByteBuf buf) {
		var result = new SyncLevelingData();
		result.entityId = buf.readInt();
		result.level = buf.readInt();
		return result;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeInt(level);
	}

	public static void receive(SyncLevelingData message, Supplier<NetworkEvent.Context> ctxSupplier) {
		var ctx = ctxSupplier.get();
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(message, ctx)));
	}

	@OnlyIn(value = Dist.CLIENT)
	private static void handlePacket(SyncLevelingData message, NetworkEvent.Context ctx) {
		var client = Minecraft.getInstance();
		var entity = client.level.getEntity(message.entityId);
		MobsLevelingEvents.setLevel((LivingEntity) entity, message.level);
	}
}
