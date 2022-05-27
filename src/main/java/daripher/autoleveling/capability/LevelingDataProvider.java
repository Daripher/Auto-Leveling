package daripher.autoleveling.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(bus = Bus.FORGE, modid = AutoLevelingMod.MOD_ID)
public class LevelingDataProvider implements ICapabilitySerializable<CompoundTag>
{
	private LazyOptional<ILevelingData> lazyOptional = LazyOptional.of(LevelingData::new);
	
	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Enemy || event.getObject() instanceof NeutralMob)
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
				LevelingDataProvider.syncWith((ServerPlayer) event.getPlayer(), livingEntity, levelingData);
			});
		}
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if (LevelingApi.CAPABILITY == capability)
		{
			return lazyOptional.cast();
		}
		
		return LazyOptional.empty();
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		return lazyOptional.orElseThrow(NullPointerException::new).serializeNBT();
	}
	
	@Override
	public void deserializeNBT(CompoundTag compoundTag)
	{
		lazyOptional.orElseThrow(NullPointerException::new).deserializeNBT(compoundTag);
	}
	
	public static LazyOptional<ILevelingData> get(LivingEntity entity)
	{
		return entity.getCapability(LevelingApi.CAPABILITY);
	}
	
	public static void syncWith(ServerPlayer player, LivingEntity entity, ILevelingData levelingData)
	{
		NetworkDispatcher.networkChannel.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelingData(entity, levelingData));
	}
}
