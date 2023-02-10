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
import net.minecraft.network.chat.TextComponent;
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
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
	@SubscribeEvent
	public static void applyLevelBonuses(EntityJoinWorldEvent event) {
		if (!shouldApplyLevelBonuses(event.getEntity()) || event.loadedFromDisk()) {
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

		LevelingDataProvider.get(event.getEntityLiving()).ifPresent(levelingData -> {
			var entityLevel = levelingData.getLevel() + 1;
			var originalExpirience = event.getOriginalExperience();
			var expirienceBonus = Config.COMMON.expBonus.get() * entityLevel;
			event.setDroppedExperience((int) (originalExpirience + originalExpirience * expirienceBonus));
		});
	}

	@SubscribeEvent
	public static void dropAdditionalLoot(LivingDropsEvent event) {
		if (!LevelingDataProvider.canHaveLevel(event.getEntity())) {
			return;
		}

		var lootTableLocation = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
		var lootTable = event.getEntity().level.getServer().getLootTables().get(lootTableLocation);
		var lootContextBuilder = createLootContextBuilder(event.getEntityLiving(), event.getSource());
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

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderEntityLevel(RenderNameplateEvent event) {
		if (!Config.COMMON.showLevel.get() || !LevelingDataProvider.canHaveLevel(event.getEntity())) {
			return;
		}

		var minecraft = Minecraft.getInstance();
		var entity = (LivingEntity) event.getEntity();

		if (shouldShowName(entity)) {
			event.setResult(Event.Result.ALLOW);
			double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);

			if (ForgeHooksClient.isNameplateInRenderDistance(entity, distance)) {
				LevelingDataProvider.get(entity).ifPresent(levelingData -> {
					var entityLevel = levelingData.getLevel() + 1;
					var entityName = event.getContent();
					var levelString = new TextComponent("" + entityLevel).withStyle(ChatFormatting.GREEN);
					var textY = entity.getBbHeight() + 0.5F;
					var textOffsetY = "deadmau5".equals(entityName.getString()) ? -10 : 0;
					event.getPoseStack().pushPose();
					event.getPoseStack().translate(0.0D, textY, 0.0D);
					event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
					event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
					var pose = event.getPoseStack().last().pose();
					var backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
					var alpha = (int) (backgroundOpacity * 255.0F) << 24;
					var font = minecraft.font;
					var textX = -font.width(entityName) / 2 - 5 - font.width(levelString);
					var multiBufferSource = event.getMultiBufferSource();
					var packedLight = event.getPackedLight();
					font.drawInBatch(levelString, textX, textOffsetY, 553648127, false, pose, multiBufferSource, !entity.isDiscrete(), alpha, packedLight);

					if (!entity.isDiscrete()) {
						font.drawInBatch(levelString, textX, textOffsetY, -1, false, pose, multiBufferSource, false, 0, packedLight);
					}

					event.getPoseStack().popPose();
				});
			}
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
		
		if(entity.getY() < 64) {
			var deepness = 64 - entity.getY();
			monsterLevel += levelingSettings.levelsPerDeepness() * deepness;
		}
		
		return monsterLevel;
	}

	@OnlyIn(Dist.CLIENT)
	protected static boolean shouldShowName(LivingEntity entity) {
		var minecraft = Minecraft.getInstance();
		var clientPlayer = minecraft.player;
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(clientPlayer) && !entity.isVehicle() && clientPlayer.hasLineOfSight(entity);
	}
}
