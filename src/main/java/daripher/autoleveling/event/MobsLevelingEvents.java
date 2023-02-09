package daripher.autoleveling.event;

import java.lang.reflect.InvocationTargetException;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.data.EntitiesLevelingSettingsReloader;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
	@SubscribeEvent
	public static void applyLevelBonuses(EntityJoinLevelEvent event) {
		if (!shouldApplyLevelBonuses(event.getEntity()) && !event.loadedFromDisk()) {
			return;
		}

		var livingEntity = (LivingEntity) event.getEntity();
		var world = ((ServerLevel) livingEntity.level);
		var worldSpawnPos = world.getSharedSpawnPos();
		var distanceToWorldSpawn = Math.sqrt(worldSpawnPos.distSqr(livingEntity.blockPosition()));
		var entityLevel = getLevelForEntity(livingEntity, distanceToWorldSpawn);
		LevelingDataProvider.get(livingEntity).ifPresent(levelingData -> levelingData.setLevel(entityLevel));
		LevelingDataProvider.applyAttributeBonuses(livingEntity, entityLevel);
		LevelingDataProvider.addEquipment(livingEntity);
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

		var leveledMobsLootTableLocation = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
		var leveledMobsLootTable = event.getEntity().level.getServer().getLootTables().get(leveledMobsLootTableLocation);
		var lootContextBuilder = createLootContextBuilder(event.getEntity(), event.getSource());
		var lootContext = lootContextBuilder.create(LootContextParamSets.ENTITY);
		leveledMobsLootTable.getRandomItems(lootContext).forEach(event.getEntity()::spawnAtLocation);
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

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderEntityLevel(RenderNameTagEvent event) {
		if (!Config.COMMON.showLevel.get() || !LevelingDataProvider.canHaveLevel(event.getEntity())) {
			return;
		}

		var minecraft = Minecraft.getInstance();
		var entity = (LivingEntity) event.getEntity();

		if (shouldShowName(entity)) {
			var distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);

			if (ForgeHooksClient.isNameplateInRenderDistance(entity, distance)) {
				LevelingDataProvider.get(entity).ifPresent(levelingData -> {
					var entityLevel = levelingData.getLevel() + 1;
					var entityName = event.getContent();
					var font = minecraft.font;
					var levelString = Component.literal("" + entityLevel).withStyle(ChatFormatting.GREEN);
					var textX = -font.width(entityName) / 2 - 5 - font.width(levelString);
					var textY = entity.getBbHeight() + 0.5F;
					var textYShift = "deadmau5".equals(entityName.getString()) ? -10 : 0;
					var matrix4f = event.getPoseStack().last().pose();
					var backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
					var textAlpha = (int) (backgroundOpacity * 255.0F) << 24;
					var textColor = 553648127;
					var packedLight = event.getPackedLight();
					var multiBufferSource = event.getMultiBufferSource();
					event.getPoseStack().pushPose();
					event.getPoseStack().translate(0.0D, textY, 0.0D);
					event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
					event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
					font.drawInBatch(levelString, textX, textYShift, textColor, false, matrix4f, multiBufferSource, !entity.isDiscrete(), textAlpha, packedLight);

					if (!entity.isDiscrete()) {
						font.drawInBatch(levelString, textX, textYShift, -1, false, matrix4f, multiBufferSource, false, 0, packedLight);
					}

					event.getPoseStack().popPose();
				});
			}

			event.setResult(Event.Result.ALLOW);
		}
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

		if (levelBonus > 0) {
			monsterLevel += entity.getRandom().nextInt(levelBonus);
		}

		monsterLevel = Math.abs(monsterLevel);

		if (maxLevel > 0) {
			monsterLevel = Math.min(monsterLevel, maxLevel - 1);
		}

		var server = entity.getServer();
		var globalLevelingData = GlobalLevelingData.get(server);
		monsterLevel += globalLevelingData.getLevelBonus();
		return monsterLevel;
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean shouldShowName(LivingEntity entity) {
		var minecraft = Minecraft.getInstance();
		var clientPlayer = minecraft.player;
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(clientPlayer) && !entity.isVehicle()
				&& clientPlayer.hasLineOfSight(entity);
	}
}
