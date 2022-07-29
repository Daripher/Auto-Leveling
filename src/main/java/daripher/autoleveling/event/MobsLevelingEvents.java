package daripher.autoleveling.event;

import java.util.UUID;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
				ServerWorld serverWorld = ((ServerWorld) entity.level);
				BlockPos spawnPos = serverWorld.getSharedSpawnPos();
				double distance = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
				int monsterLevel = getLevelForEntity(distance);
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
						ITextComponent entityName = event.getContent();
						ITextComponent levelString = new StringTextComponent("" + level).withStyle(TextFormatting.GREEN);
						float y = entity.getBbHeight() + 0.5F;
						int yShift = "deadmau5".equals(entityName.getString()) ? -10 : 0;
						event.getMatrixStack().pushPose();
						event.getMatrixStack().translate(0.0D, y, 0.0D);
						event.getMatrixStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
						event.getMatrixStack().scale(-0.025F, -0.025F, 0.025F);
						Matrix4f matrix4f = event.getMatrixStack().last().pose();
						float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
						int alpha = (int) (backgroundOpacity * 255.0F) << 24;
						FontRenderer font = minecraft.font;
						float x = -font.width(entityName) / 2 - 5 - font.width(levelString);
						font.drawInBatch(levelString, x, yShift, 553648127, false, matrix4f, event.getRenderTypeBuffer(), !entity.isDiscrete(), alpha, event.getPackedLight());
						
						if (!entity.isDiscrete())
						{
							font.drawInBatch(levelString, x, yShift, -1, false, matrix4f, event.getRenderTypeBuffer(), false, 0, event.getPackedLight());
						}
						
						event.getMatrixStack().popPose();
					});
				}
			}
		}
	}
	
	private static int getLevelForEntity(double distanceFromSpawn)
	{
		int monsterLevel = (int) (Config.COMMON.levelBonus.get() * distanceFromSpawn);
		int maxLevel = Config.COMMON.maxLevel.get();
		
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
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(minecraft.player) && !entity.isVehicle() && minecraft.player.canSee(entity);
	}
	
	private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus)
	{
		ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
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
