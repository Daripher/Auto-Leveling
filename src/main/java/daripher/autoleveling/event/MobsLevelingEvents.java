package daripher.autoleveling.event;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.settings.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.settings.EntitiesLevelingSettingsReloader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
	private static final String LEVEL_TAG = "LEVEL";
	
	@SubscribeEvent
	public static void applyLevelBonuses(EntityJoinLevelEvent event) {
		if (!shouldSetLevel(event.getEntity()) || event.loadedFromDisk()) return;
		var entity = (LivingEntity) event.getEntity();
		var spawnPos = entity.level.getSharedSpawnPos();
		var distanceToSpawn = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
		var level = getLevelForEntity(entity, distanceToSpawn);
		setLevel(entity, level);
		applyAttributeBonuses(entity);
		addEquipment(entity);
	}

	@SubscribeEvent
	public static void adjustExpirienceDrop(LivingExperienceDropEvent event) {
		if (!hasLevel(event.getEntity())) return;
		var level = getLevel(event.getEntity()) + 1;
		var originalExp = event.getOriginalExperience();
		var expBonus = Config.COMMON.expBonus.get() * level;
		event.setDroppedExperience((int) (originalExp + originalExp * expBonus));
	}

	@SubscribeEvent
	public static void dropAdditionalLoot(LivingDropsEvent event) {
		if (!hasLevel(event.getEntity())) return;
		var lootTableId = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
		var lootTable = event.getEntity().level.getServer().getLootTables().get(lootTableId);
		var lootContext = createLootContext(event.getEntity(), event.getSource());
		lootTable.getRandomItems(lootContext).forEach(event.getEntity()::spawnAtLocation);
	}

	@SubscribeEvent
	public static void reloadSettings(AddReloadListenerEvent event) {
		event.addListener(new DimensionsLevelingSettingsReloader());
		event.addListener(new EntitiesLevelingSettingsReloader());
	}

	@SubscribeEvent
	public static void syncEntityLevel(PlayerEvent.StartTracking event) {
		if (!hasLevel(event.getTarget())) return;
		var entity = (LivingEntity) event.getTarget();
		NetworkDispatcher.network_channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new SyncLevelingData(entity));
	}

	@SubscribeEvent
	public static void applyAttributesDamageBonus(LivingHurtEvent event) {
		var damageSource = event.getSource();
		if (!(damageSource.getEntity() instanceof LivingEntity)) {
			return;
		}
		var attackingEntity = (LivingEntity) damageSource.getEntity();
		var damageBonus = getDamageBonus(damageSource, attackingEntity);
		if (damageBonus > 1F) {
			event.setAmount(event.getAmount() * damageBonus);
		}
	}

	public static float getDamageBonus(DamageSource damageSource, LivingEntity attackingEntity) {
		var damageBonus = 1F;
		var isProjectileDamage = damageSource.is(DamageTypeTags.IS_PROJECTILE);
		if (isProjectileDamage) {
			damageBonus = getAttributeValue(attackingEntity, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER.get());
		}
		var isExplosionDamage = damageSource.is(DamageTypeTags.IS_EXPLOSION);
		if (isExplosionDamage) {
			damageBonus = getAttributeValue(attackingEntity, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER.get());
		}
		return damageBonus;
	}

	private static float getAttributeValue(LivingEntity entity, Attribute damageBonusAttribute) {
		if (entity.getAttribute(damageBonusAttribute) == null) {
			return 0F;
		}
		return (float) entity.getAttribute(damageBonusAttribute).getValue();
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean shouldShowName(LivingEntity entity) {
		if (!hasLevel(entity)) return false;
		if (!shouldShowLevel(entity)) return false;
		var minecraft = Minecraft.getInstance();
		var alwaysShowLevel = Config.COMMON.alwaysShowLevel.get();
		var showLevelWhenLookingAt = Config.COMMON.showLevelWhenLookingAt.get();
		if (!alwaysShowLevel && !(showLevelWhenLookingAt && minecraft.crosshairPickEntity == entity)) return false;
		var clientPlayer = minecraft.player;
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(clientPlayer) && !entity.isVehicle() && clientPlayer.hasLineOfSight(entity);
	}

	private static boolean shouldSetLevel(Entity entity) {
		if (entity.level.isClientSide) return false;
		return canHaveLevel(entity);
	}
	
	private static int getLevelForEntity(LivingEntity entity, double distanceFromSpawn) {
		var levelingSettings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
		if (levelingSettings == null) {
			var dimension = entity.level.dimension();
			levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
		}
		var monsterLevel = (int) (levelingSettings.levelsPerDistance() * distanceFromSpawn);
		var maxLevel = levelingSettings.maxLevel();
		var levelBonus = levelingSettings.randomLevelBonus() + 1;
		monsterLevel += levelingSettings.startingLevel() - 1;
		if (levelBonus > 0) monsterLevel += entity.getRandom().nextInt(levelBonus);
		monsterLevel = Math.abs(monsterLevel);
		if (maxLevel > 0) monsterLevel = Math.min(monsterLevel, maxLevel - 1);
		var server = entity.getServer();
		var globalLevelingData = GlobalLevelingData.get(server);
		monsterLevel += globalLevelingData.getLevelBonus();
		if (entity.getY() < 64) {
			var deepness = 64 - entity.getY();
			monsterLevel += levelingSettings.levelsPerDeepness() * deepness;
		}
		return monsterLevel;
	}

	public static void applyAttributeBonuses(LivingEntity entity) {
		var level = getLevel(entity);
		Config.getAttributeBonuses().forEach((attribute, bonus) -> {
			applyAttributeBonusIfPossible(entity, attribute, bonus * level);
		});
	}

	private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus) {
		var attributeInstance = entity.getAttribute(attribute);
		var modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
		if (attributeInstance == null) return;
		var modifier = attributeInstance.getModifier(modifierId);
		if (modifier != null && modifier.getAmount() == bonus) return;
		if (modifier != null) attributeInstance.removeModifier(modifier);
		modifier = new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, Operation.MULTIPLY_TOTAL);
		attributeInstance.addPermanentModifier(modifier);
		if (attribute == Attributes.MAX_HEALTH) entity.heal(entity.getMaxHealth());
	}

	public static void addEquipment(LivingEntity entity) {
		for (var slot : EquipmentSlot.values()) {
			var server = entity.getLevel().getServer();
			var equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
			if (equipmentTable == LootTable.EMPTY) continue;
			var lootContext = createEquipmentLootContext(entity);
			equipmentTable.getRandomItems(lootContext).forEach(itemStack -> entity.setItemSlot(slot, itemStack));
		}
	}

	private static LootTable getEquipmentLootTableForSlot(MinecraftServer server, LivingEntity entity, EquipmentSlot equipmentSlot) {
		var entityId = EntityType.getKey(entity.getType());
		var lootTableId = new ResourceLocation(entityId.getNamespace(), "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName());
		return server.getLootTables().get(lootTableId);
	}


	private static LootContext createLootContext(LivingEntity livingEntity, DamageSource damageSource) {
		var lastHurtByPlayerTime = (int) ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, livingEntity, "f_20889_");
		var createLootContextMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_7771_", boolean.class, DamageSource.class);
		try {
			var builder = (LootContext.Builder) createLootContextMethod.invoke(livingEntity, lastHurtByPlayerTime > 0, damageSource);
			return builder.create(LootContextParamSets.ENTITY);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static LootContext createEquipmentLootContext(LivingEntity entity) {
		return new Builder((ServerLevel) entity.level)
				.withRandom(entity.getRandom())
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.create(LootContextParamSets.SELECTOR);
	}

	private static boolean canHaveLevel(Entity entity) {
		if (!(entity instanceof LivingEntity)) return false;
		var livingEntity = (LivingEntity) entity;
		if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) == null) return false;
		if (entity.getType() == EntityType.PLAYER) return false;
		var entityId = EntityType.getKey(entity.getType());
		var entityNamespace = entityId.getNamespace();
		var blacklistedMobs = Config.COMMON.blacklistedMobs.get();
		if (blacklistedMobs.contains(entityNamespace + ":*")) return false;
		var whitelistedMobs = Config.COMMON.whitelistedMobs.get();
		if (whitelistedMobs.contains(entityNamespace + ":*")) return true;
		if (blacklistedMobs.contains(entityId.toString())) return false;
		if (!whitelistedMobs.isEmpty()) return whitelistedMobs.contains(entityId.toString());
		return true;
	}

	public static boolean shouldShowLevel(Entity entity) {
		var entityId = EntityType.getKey(entity.getType());
		var blacklist = Config.COMMON.blacklistedShownLevels.get();
		if (blacklist.contains(entityId.toString())) return false;
		var namespace = entityId.getNamespace();
		return !blacklist.contains(namespace + ":*");
	}

	public static boolean hasLevel(Entity entity) {
		return entity.getPersistentData().contains(LEVEL_TAG);
	}
	
	public static int getLevel(LivingEntity entity) {
		return entity.getPersistentData().getInt(LEVEL_TAG);
	}

	public static void setLevel(LivingEntity entity, int level) {
		entity.getPersistentData().putInt(LEVEL_TAG, level);
	}
}
