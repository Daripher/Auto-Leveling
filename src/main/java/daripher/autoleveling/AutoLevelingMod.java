package daripher.autoleveling;

import java.util.UUID;

import daripher.autoleveling.config.Config;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod
{
	public static final String MOD_ID = "autoleveling";
	
	public AutoLevelingMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if (!entity.level.isClientSide)
			{
				ServerWorld level = ((ServerWorld) entity.level);
				
				if (entity instanceof IMob || entity instanceof IAngerable)
				{
					BlockPos spawnPos = level.getSharedSpawnPos();
					double distance = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
					applyAttributeBonusIfPossible(entity, Attributes.MOVEMENT_SPEED, Config.COMMON.movementSpeedBonus.get() * distance);
					applyAttributeBonusIfPossible(entity, Attributes.FLYING_SPEED, Config.COMMON.flyingSpeedBonus.get() * distance);
					applyAttributeBonusIfPossible(entity, Attributes.ATTACK_DAMAGE, Config.COMMON.attackDamageBonus.get() * distance);
					applyAttributeBonusIfPossible(entity, Attributes.ARMOR, Config.COMMON.armorBonus.get() * distance);
					applyAttributeBonusIfPossible(entity, Attributes.MAX_HEALTH, Config.COMMON.healthBonus.get() * distance);
				}
			}
		}
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
