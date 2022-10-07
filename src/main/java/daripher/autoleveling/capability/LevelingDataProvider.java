package daripher.autoleveling.capability;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.FORGE, modid = AutoLevelingMod.MOD_ID)
public class LevelingDataProvider implements ICapabilitySerializable<CompoundNBT>
{
	private static List<String> blacklisted_namespaces = new ArrayList<>();
	private static List<String> whitelisted_namespaces = new ArrayList<>();
	private static boolean initialized;
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
	
	public static boolean canHaveLevel(Entity entity)
	{
		if (!initialized)
		{
			if (!Config.COMMON.whitelistedMobs.get().isEmpty())
			{
				whitelisted_namespaces.addAll(Config.COMMON.whitelistedMobs.get().stream().filter(s -> s.split(":").length == 2 && s.split(":")[1].equals("*")).collect(ArrayList::new,
						(list, string) -> list.add(string.split(":")[0]), (list1, list2) -> list1.addAll(list2)));
			}
			
			if (!Config.COMMON.blacklistedMobs.get().isEmpty())
			{
				blacklisted_namespaces.addAll(Config.COMMON.blacklistedMobs.get().stream().filter(s -> s.split(":").length == 2 && s.split(":")[1].equals("*")).collect(ArrayList::new,
						(list, string) -> list.add(string.split(":")[0]), (list1, list2) -> list1.addAll(list2)));
			}
			
			initialized = true;
		}
		
		if (!(entity instanceof LivingEntity))
		{
			return false;
		}
		
		LivingEntity livingEntity = (LivingEntity) entity;
		
		if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) == null)
		{
			return false;
		}
		
		if (entity.getType() == EntityType.PLAYER)
		{
			return false;
		}
		
		ResourceLocation entityId = ForgeRegistries.ENTITIES.getKey(entity.getType());
		
		if (blacklisted_namespaces.contains(entityId.getNamespace()))
		{
			return false;
		}
		
		if (!whitelisted_namespaces.isEmpty())
		{
			if (whitelisted_namespaces.contains(entityId.getNamespace()))
			{
				return true;
			}
		}
		
		if (Config.COMMON.blacklistedMobs.get().contains(entityId.toString()))
		{
			return false;
		}
		
		if (!Config.COMMON.whitelistedMobs.get().isEmpty())
		{
			return Config.COMMON.whitelistedMobs.get().contains(entityId.toString());
		}
		
		return true;
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
