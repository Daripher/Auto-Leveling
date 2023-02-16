package daripher.autoleveling.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.FORGE, modid = AutoLevelingMod.MOD_ID)
public class LevelingDataProvider implements ICapabilitySerializable<CompoundTag> {
	private static final List<String> BLACKLISTED_NAMESPACES = new ArrayList<>();
	private static final List<String> WHITELISTED_NAMESPACES = new ArrayList<>();
	private static final Map<Attribute, Float> ATTRIBUTE_BONUSES = new HashMap<>();
	private static boolean whitelist_and_blacklist_initialized;
	private static boolean attribute_bonuses_initialized;
	private LazyOptional<ILevelingData> lazyOptional = LazyOptional.of(LevelingData::new);

	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof LivingEntity) {
			event.addCapability(LevelingApi.CAPABILITY_ID, new LevelingDataProvider());
		}
	}

	@SubscribeEvent
	public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof LivingEntity) {
			var livingEntity = (LivingEntity) event.getTarget();

			LevelingDataProvider.get(livingEntity).ifPresent(levelingData -> {
				LevelingDataProvider.syncWith((ServerPlayer) event.getEntity(), livingEntity, levelingData);
			});
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (LevelingApi.CAPABILITY == capability) {
			return lazyOptional.cast();
		}

		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return lazyOptional.orElseThrow(NullPointerException::new).serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag compoundTag) {
		lazyOptional.orElseThrow(NullPointerException::new).deserializeNBT(compoundTag);
	}

	public static boolean canHaveLevel(Entity entity) {
		if (!whitelist_and_blacklist_initialized) {
			Predicate<String> namespacePredicate = s -> s.split(":").length == 2 && s.split(":")[1].equals("*");
			Config.COMMON.whitelistedMobs.get().stream().filter(namespacePredicate).map(s -> s.split(":")[0]).forEach(WHITELISTED_NAMESPACES::add);
			Config.COMMON.blacklistedMobs.get().stream().filter(namespacePredicate).map(s -> s.split(":")[0]).forEach(BLACKLISTED_NAMESPACES::add);
			whitelist_and_blacklist_initialized = true;
		}

		if (!(entity instanceof LivingEntity)) {
			return false;
		}

		var livingEntity = (LivingEntity) entity;

		if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
			return false;
		}

		if (entity.getType() == EntityType.PLAYER) {
			return false;
		}

		var entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

		if (BLACKLISTED_NAMESPACES.contains(entityId.getNamespace())) {
			return false;
		}

		if (!WHITELISTED_NAMESPACES.isEmpty()) {
			if (WHITELISTED_NAMESPACES.contains(entityId.getNamespace())) {
				return true;
			}
		}

		if (Config.COMMON.blacklistedMobs.get().contains(entityId.toString())) {
			return false;
		}

		if (!Config.COMMON.whitelistedMobs.get().isEmpty()) {
			return Config.COMMON.whitelistedMobs.get().contains(entityId.toString());
		}

		return true;
	}

	public static LazyOptional<ILevelingData> get(LivingEntity entity) {
		return entity.getCapability(LevelingApi.CAPABILITY);
	}

	public static void syncWith(ServerPlayer player, LivingEntity entity, ILevelingData levelingData) {
		NetworkDispatcher.networkChannel.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelingData(entity, levelingData));
	}

	public static void applyAttributeBonuses(LivingEntity entity, int level) {
		if (!attribute_bonuses_initialized) {
			Config.COMMON.attributesBonuses.get().forEach(attributeBonusConfig -> {
				var attributeId = new ResourceLocation((String) attributeBonusConfig.get(0));
				var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
				var attributeBonus = ((Double) attributeBonusConfig.get(1)).floatValue();

				if (attribute == null) {
					AutoLevelingMod.LOGGER.error("Attribute '" + attributeId + "' can not be found!");
				} else {
					ATTRIBUTE_BONUSES.put(attribute, attributeBonus);
				}
			});

			attribute_bonuses_initialized = true;
		}

		ATTRIBUTE_BONUSES.forEach((attribute, bonus) -> {
			applyAttributeBonusIfPossible(entity, attribute, bonus * level);
		});
	}

	private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus) {
		var attributeInstance = entity.getAttribute(attribute);
		var modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");

		if (attributeInstance == null) {
			return;
		}

		var modifier = attributeInstance.getModifier(modifierId);

		if (modifier == null || modifier.getAmount() != bonus) {
			if (modifier != null) {
				attributeInstance.removeModifier(modifier);
			}

			attributeInstance.addPermanentModifier(new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, Operation.MULTIPLY_TOTAL));

			if (attribute == Attributes.MAX_HEALTH) {
				entity.heal(entity.getMaxHealth());
			}
		}
	}

	public static void addEquipment(LivingEntity entity) {
		var server = entity.getLevel().getServer();
		var lootContext = createEquipmentLootContext(entity);

		Stream.of(EquipmentSlot.values()).forEach(slot -> {
			var equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
			equipmentTable.getRandomItems(lootContext).forEach(itemStack -> entity.setItemSlot(slot, itemStack));
		});
	}

	private static LootTable getEquipmentLootTableForSlot(MinecraftServer server, LivingEntity entity, EquipmentSlot equipmentSlot) {
		var entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		return server.getLootTables().get(new ResourceLocation(entityId.getNamespace(), "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName()));
	}

	private static LootContext createEquipmentLootContext(LivingEntity entity) {
		var serverLevel = (ServerLevel) entity.level;
		var lootContextBuilder = new Builder(serverLevel)
				.withRandom(entity.getRandom())
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position());
		return lootContextBuilder.create(LootContextParamSets.SELECTOR);
	}
}
