package daripher.autoleveling.event;

import java.lang.reflect.InvocationTargetException;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.settings.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.settings.EntitiesLevelingSettingsReloader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
	@SubscribeEvent
	public static void applyLevelBonuses(EntityJoinLevelEvent event) {
		if (!shouldApplyLevelBonuses(event.getEntity()) || event.loadedFromDisk()) {
			return;
		}
		var entity = (LivingEntity) event.getEntity();
		var spawnPosition = entity.level.getSharedSpawnPos();
		var distanceToSpawn = Math.sqrt(spawnPosition.distSqr(entity.blockPosition()));
		var entityLevel = getLevelForEntity(entity, distanceToSpawn);
		LevelingDataProvider.get(entity).ifPresent(levelingData -> levelingData.setLevel(entityLevel));
		LevelingDataProvider.applyAttributeBonuses(entity, entityLevel);
		LevelingDataProvider.addEquipment(entity);
	}

	private static boolean shouldApplyLevelBonuses(Entity entity) {
		if (!LevelingDataProvider.canHaveLevel(entity)) {
			return false;
		}
		if (entity.level.isClientSide) {
			return false;
		}
		return true;
	}

	@SubscribeEvent
	public static void adjustExpirienceDrop(LivingExperienceDropEvent event) {
		if (!LevelingDataProvider.canHaveLevel(event.getEntity())) {
			return;
		}
		LevelingDataProvider.get(event.getEntity()).ifPresent(levelingData -> {
			var level = levelingData.getLevel() + 1;
			var originalExp = event.getOriginalExperience();
			var expBonus = Config.COMMON.expBonus.get() * level;
			event.setDroppedExperience((int) (originalExp + originalExp * expBonus));
		});
	}

	@SubscribeEvent
	public static void dropAdditionalLoot(LivingDropsEvent event) {
		if (!LevelingDataProvider.canHaveLevel(event.getEntity())) {
			return;
		}
		var lootTableId = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
		var lootTable = event.getEntity().level.getServer().getLootTables().get(lootTableId);
		var lootContextBuilder = createLootContextBuilder(event.getEntity(), event.getSource());
		var lootContext = lootContextBuilder.create(LootContextParamSets.ENTITY);
		lootTable.getRandomItems(lootContext).forEach(event.getEntity()::spawnAtLocation);
	}

	private static LootContext.Builder createLootContextBuilder(LivingEntity livingEntity, DamageSource damageSource) {
		var lastHurtByPlayerTime = (int) ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, livingEntity, "f_20889_");
		var createLootContextMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_7771_", boolean.class, DamageSource.class);
		try {
			return (LootContext.Builder) createLootContextMethod.invoke(livingEntity, lastHurtByPlayerTime > 0, damageSource);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getLevelForEntity(LivingEntity entity, double distanceFromSpawn) {
		var levelingSettings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
		if (levelingSettings == null) {
			var dimension = entity.level.dimension();
			levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
		}
		var entityLevel = levelingSettings.startingLevel() - 1;
		var maxLevel = levelingSettings.maxLevel();
		var randomLevelBonus = levelingSettings.randomLevelBonus() + 1;
		entityLevel += (int) (levelingSettings.levelsPerDistance() * distanceFromSpawn);
		if (randomLevelBonus > 0) {
			entityLevel += entity.getRandom().nextInt(randomLevelBonus);
		}
		if (maxLevel > 0) {
			entityLevel = Math.min(entityLevel, maxLevel - 1);
		}
		var server = entity.getServer();
		var globalLevelingData = GlobalLevelingData.get(server);
		entityLevel += globalLevelingData.getLevelBonus();
		if (entity.getY() < 64) {
			var deepness = 64 - entity.getY();
			entityLevel += levelingSettings.levelsPerDeepness() * deepness;
		}
		return entityLevel;
	}
}
