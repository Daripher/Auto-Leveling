package daripher.autoleveling.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.mojang.math.Matrix4f;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.data.EntitiesLevelingSettingsReloader;
import daripher.autoleveling.data.LevelingSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
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
public class MobsLevelingEvents
{
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if (!entity.level.isClientSide)
			{
				ServerLevel level = ((ServerLevel) entity.level);				
				BlockPos spawnPos = level.getSharedSpawnPos();
				double distance = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
				int monsterLevel = getLevelForEntity(entity, distance);
				LevelingDataProvider.get(entity).ifPresent(levelingData -> levelingData.setLevel(monsterLevel));
				applyAttributeBonusIfPossible(entity, Attributes.MOVEMENT_SPEED, Config.COMMON.movementSpeedBonus.get() * monsterLevel);
				applyAttributeBonusIfPossible(entity, Attributes.FLYING_SPEED, Config.COMMON.flyingSpeedBonus.get() * monsterLevel);
				applyAttributeBonusIfPossible(entity, Attributes.ATTACK_DAMAGE, Config.COMMON.attackDamageBonus.get() * monsterLevel);
				applyAttributeBonusIfPossible(entity, Attributes.ARMOR, Config.COMMON.armorBonus.get() * monsterLevel);
				applyAttributeBonusIfPossible(entity, Attributes.MAX_HEALTH, Config.COMMON.healthBonus.get() * monsterLevel);
			}
		}
	}
	
	@SubscribeEvent
	public static void onLivingExperienceDrop(LivingExperienceDropEvent event)
	{
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			LevelingDataProvider.get(event.getEntityLiving()).ifPresent(levelingData ->
			{
				int level = levelingData.getLevel() + 1;
				int exp = event.getOriginalExperience();
				double expBonus = Config.COMMON.expBonus.get() * level;
				event.setDroppedExperience((int) (exp + exp * expBonus));
			});
		}
	}
	
	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event)
	{
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			ResourceLocation resourcelocation = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
			LootTable loottable = event.getEntity().level.getServer().getLootTables().get(resourcelocation);
			int lastHurtByPlayerTime = ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, event.getEntityLiving(), "f_20889_");
			Method createLootContext = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_7771_", boolean.class, DamageSource.class);
			LootContext.Builder lootcontext$builder;
			
			try
			{
				lootcontext$builder = (LootContext.Builder) createLootContext.invoke(event.getEntity(), lastHurtByPlayerTime > 0, event.getSource());
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				e.printStackTrace();
				return;
			}
			
			LootContext ctx = lootcontext$builder.create(LootContextParamSets.ENTITY);
			loottable.getRandomItems(ctx).forEach(event.getEntity()::spawnAtLocation);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderNameplate(RenderNameplateEvent event)
	{
		if (!Config.COMMON.showLevel.get())
		{
			return;
		}
		
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			Minecraft minecraft = Minecraft.getInstance();
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if (shouldShowName(entity))
			{
				event.setResult(Event.Result.ALLOW);
				double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
				
				if (ForgeHooksClient.isNameplateInRenderDistance(entity, distance))
				{
					LevelingDataProvider.get(entity).ifPresent(levelingData ->
					{
						int level = levelingData.getLevel() + 1;
						Component entityName = event.getContent();
						Component levelString = new TextComponent("" + level).withStyle(ChatFormatting.GREEN);
						float y = entity.getBbHeight() + 0.5F;
						int yShift = "deadmau5".equals(entityName.getString()) ? -10 : 0;
						event.getPoseStack().pushPose();
						event.getPoseStack().translate(0.0D, y, 0.0D);
						event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
						event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
						Matrix4f matrix4f = event.getPoseStack().last().pose();
						float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
						int alpha = (int) (backgroundOpacity * 255.0F) << 24;
						Font font = minecraft.font;
						float x = -font.width(entityName) / 2 - 5 - font.width(levelString);
						font.drawInBatch(levelString, x, yShift, 553648127, false, matrix4f, event.getMultiBufferSource(), !entity.isDiscrete(), alpha, event.getPackedLight());
						
						if (!entity.isDiscrete())
						{
							font.drawInBatch(levelString, x, yShift, -1, false, matrix4f, event.getMultiBufferSource(), false, 0, event.getPackedLight());
						}
						
						event.getPoseStack().popPose();
					});
				}
			}
		}
	}
	
	private static int getLevelForEntity(LivingEntity entity, double distanceFromSpawn)
	{
		LevelingSettings levelingSettings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
		
		if (levelingSettings == null)
		{
			ResourceKey<Level> dimension = entity.level.dimension();
			levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
		}
		
		int monsterLevel = (int) (levelingSettings.levelsPerDistance() * distanceFromSpawn);
		int maxLevel = levelingSettings.maxLevel();
		int levelBonus = levelingSettings.randomLevelBonus() + 1;
		monsterLevel += levelingSettings.startingLevel() - 1;
		
		if (levelBonus > 0)
		{
			monsterLevel += entity.getRandom().nextInt(levelBonus);
		}
		
		monsterLevel = Math.abs(monsterLevel);
		
		if (maxLevel > 0)
		{
			monsterLevel = Math.min(monsterLevel, maxLevel - 1);
		}
		
		return monsterLevel;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected static boolean shouldShowName(LivingEntity entity)
	{
		Minecraft minecraft = Minecraft.getInstance();
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(minecraft.player) && !entity.isVehicle() && minecraft.player.hasLineOfSight(entity);
	}
	
	private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus)
	{
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
		
		if (attributeInstance != null && attributeInstance.getModifier(modifierId) == null)
		{
			attributeInstance.addPermanentModifier(new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, Operation.MULTIPLY_TOTAL));
			
			if (attribute == Attributes.MAX_HEALTH)
			{
				entity.heal(entity.getMaxHealth());
			}
		}
	}
}
