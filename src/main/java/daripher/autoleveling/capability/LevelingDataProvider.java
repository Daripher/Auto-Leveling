package daripher.autoleveling.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber(bus = Bus.FORGE, modid = AutoLevelingMod.MOD_ID)
public class LevelingDataProvider implements ICapabilitySerializable<CompoundNBT>
{
	private ILevelingData instance = LevelingApi.CAPABILITY.getDefaultInstance();
	
	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof LivingEntity)
		{
			event.addCapability(LevelingApi.CAPABILITY_ID, new LevelingDataProvider());
		}
	}
	
	@SubscribeEvent
	public static void onPlayerStartTracking(PlayerEvent.StartTracking event)
	{
		if (event.getTarget() instanceof LivingEntity)
		{
			LivingEntity livingEntity = (LivingEntity) event.getTarget();
			
			LevelingDataProvider.get(livingEntity).ifPresent(levelingData ->
			{
				LevelingDataProvider.syncWith((ServerPlayerEntity) event.getPlayer(), livingEntity, levelingData);
			});
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if (LevelingApi.CAPABILITY == capability)
		{
			return (LazyOptional<T>) LazyOptional.of(() -> instance);
		}
		
		return LazyOptional.empty();
	}
	
	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT) LevelingApi.CAPABILITY.writeNBT(instance, null);
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		LevelingApi.CAPABILITY.readNBT(instance, null, nbt);
	}
	
	public static LazyOptional<ILevelingData> get(LivingEntity entity)
	{
		return entity.getCapability(LevelingApi.CAPABILITY);
	}
	
	public static void syncWith(ServerPlayerEntity player, LivingEntity entity, ILevelingData levelingData)
	{
		NetworkDispatcher.networkChannel.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelingData(entity, levelingData));
	}
}
