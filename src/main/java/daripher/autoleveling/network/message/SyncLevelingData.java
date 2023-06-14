package daripher.autoleveling.network.message;

import java.util.function.Supplier;

import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SyncLevelingData {
	private int entityId;
	private int level;

	private SyncLevelingData() {
	}

	public SyncLevelingData(LivingEntity entity) {
		entityId = entity.getId();
		level = MobsLevelingEvents.getLevel(entity);
	}

	public static SyncLevelingData decode(PacketBuffer buf) {
		SyncLevelingData result = new SyncLevelingData();
		result.entityId = buf.readInt();
		result.level = buf.readInt();
		return result;
	}

	public void encode(PacketBuffer buf) {
		buf.writeInt(entityId);
		buf.writeInt(level);
	}

	public static void receive(SyncLevelingData message, Supplier<NetworkEvent.Context> ctxSupplier) {
		Context ctx = ctxSupplier.get();
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(message, ctx)));
	}

	@OnlyIn(value = Dist.CLIENT)
	private static void handlePacket(SyncLevelingData message, NetworkEvent.Context ctx) {
		Minecraft minecraft = Minecraft.getInstance();
		Entity entity = minecraft.level.getEntity(message.entityId);
		MobsLevelingEvents.setLevel((LivingEntity) entity, message.level);
	}
}
