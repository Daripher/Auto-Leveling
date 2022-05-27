package daripher.autoleveling.network.message;

import java.util.function.Supplier;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SyncLevelingData
{
	private CompoundNBT nbt;
	private int entityId;
	
	private SyncLevelingData()
	{
	}
	
	public SyncLevelingData(LivingEntity entity, ILevelingData levelingData)
	{
		nbt = (CompoundNBT) LevelingApi.CAPABILITY.writeNBT(levelingData, null);
		entityId = entity.getId();
	}
	
	public static SyncLevelingData decode(PacketBuffer buf)
	{
		SyncLevelingData result = new SyncLevelingData();
		result.nbt = buf.readAnySizeNbt();
		result.entityId = buf.readInt();
		return result;
	}
	
	public void encode(PacketBuffer buf)
	{
		buf.writeNbt(nbt);
		buf.writeInt(entityId);
	}
	
	public static void receive(SyncLevelingData message, Supplier<NetworkEvent.Context> ctxSupplier)
	{
		NetworkEvent.Context ctx = ctxSupplier.get();
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(message, ctx)));
	}
	
	@OnlyIn(value = Dist.CLIENT)
	private static void handlePacket(SyncLevelingData message, Context ctx)
	{
		Minecraft client = Minecraft.getInstance();
		Entity entity = client.level.getEntity(message.entityId);
		
		if (entity instanceof LivingEntity)
		{
			LevelingDataProvider.get((LivingEntity) entity).ifPresent(levelingData ->
			{
				LevelingApi.CAPABILITY.readNBT(levelingData, null, message.nbt);
			});
		}
	}
}
