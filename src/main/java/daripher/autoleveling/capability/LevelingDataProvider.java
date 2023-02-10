package daripher.autoleveling.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
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
public class LevelingDataProvider implements ICapabilitySerializable<CompoundNBT> {
	private static final List<String> BLACKLISTED_NAMESPACES = new ArrayList<>();
	private static final List<String> WHITELISTED_NAMESPACES = new ArrayList<>();
	private static final Map<Attribute, Float> ATTRIBUTE_BONUSES = new HashMap<>();
	private static boolean whitelist_and_blacklist_initialized;
	private static boolean attribute_bonuses_initialized;
	private ILevelingData instance = LevelingApi.CAPABILITY.getDefaultInstance();

	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof LivingEntity) {
			event.addCapability(LevelingApi.CAPABILITY_ID, new LevelingDataProvider());
		}
	}

	@SubscribeEvent
	public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getTarget();

			LevelingDataProvider.getLevelingData(livingEntity).ifPresent(levelingData -> {
				LevelingDataProvider.syncWith((ServerPlayerEntity) event.getPlayer(), livingEntity, levelingData);
			});
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (LevelingApi.CAPABILITY == capability) {
			return (LazyOptional<T>) LazyOptional.of(() -> instance);
		}

		return LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) LevelingApi.CAPABILITY.writeNBT(instance, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		LevelingApi.CAPABILITY.readNBT(instance, null, nbt);
	}

	public static boolean canHaveLevel(Entity entity) {
		if (!whitelist_and_blacklist_initialized) {
			initializeBlacklistAndWhitelist();
			whitelist_and_blacklist_initialized = true;
		}

		if (!(entity instanceof LivingEntity)) {
			return false;
		}

		LivingEntity livingEntity = (LivingEntity) entity;

		if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
			return false;
		}

		if (entity.getType() == EntityType.PLAYER) {
			return false;
		}

		ResourceLocation entityId = ForgeRegistries.ENTITIES.getKey(entity.getType());

		if (BLACKLISTED_NAMESPACES.contains(entityId.getNamespace())) {
			return false;
		}

		if (!WHITELISTED_NAMESPACES.isEmpty() && WHITELISTED_NAMESPACES.contains(entityId.getNamespace())) {
			return true;
		}

		if (Config.COMMON.blacklistedMobs.get().contains(entityId.toString())) {
			return false;
		}

		if (!Config.COMMON.whitelistedMobs.get().isEmpty()) {
			return Config.COMMON.whitelistedMobs.get().contains(entityId.toString());
		}

		return true;
	}

	private static void initializeBlacklistAndWhitelist() {
		initializeList(Config.COMMON.whitelistedMobs.get(), WHITELISTED_NAMESPACES);
		initializeList(Config.COMMON.blacklistedMobs.get(), BLACKLISTED_NAMESPACES);
	}

	private static void initializeList(List<String> configList, List<String> namespacesList) {
		if (configList.isEmpty()) {
			return;
		}

		Predicate<? super String> namespaceFilter = s -> s.split(":").length == 2 && s.split(":")[1].equals("*");
		List<String> foundNamespaces = configList.stream().filter(namespaceFilter).collect(Collectors.toList());
		namespacesList.addAll(foundNamespaces);
	}

	public static LazyOptional<ILevelingData> getLevelingData(LivingEntity entity) {
		return entity.getCapability(LevelingApi.CAPABILITY);
	}

	public static void syncWith(ServerPlayerEntity player, LivingEntity entity, ILevelingData levelingData) {
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
		ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
		UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");

		if (attributeInstance == null) {
			return;
		}

		AttributeModifier modifier = attributeInstance.getModifier(modifierId);

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
		MinecraftServer server = entity.getServer();
		LootContext lootContext = createEquipmentLootContext(entity);

		Stream.of(EquipmentSlotType.values()).forEach(slot -> {
			LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
			equipmentTable.getRandomItems(lootContext).forEach(itemStack -> entity.setItemSlot(slot, itemStack));
		});
	}

	private static LootTable getEquipmentLootTableForSlot(MinecraftServer server, LivingEntity entity, EquipmentSlotType equipmentSlot) {
		ResourceLocation entityId = ForgeRegistries.ENTITIES.getKey(entity.getType());
		return server.getLootTables().get(new ResourceLocation(entityId.getNamespace(), "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName()));
	}

	private static LootContext createEquipmentLootContext(LivingEntity entity) {
		ServerWorld serverLevel = (ServerWorld) entity.level;
		Builder lootContextBuilder = new Builder(serverLevel).withRandom(entity.getRandom()).withParameter(LootParameters.THIS_ENTITY, entity)
				.withParameter(LootParameters.ORIGIN, entity.position());
		return lootContextBuilder.create(LootParameterSets.SELECTOR);
	}
}
