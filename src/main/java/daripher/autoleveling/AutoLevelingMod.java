package daripher.autoleveling;

import java.util.UUID;

import daripher.autoleveling.config.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod
{
	public static final String MOD_ID = "autoleveling";
	
	public AutoLevelingMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onBlocksRegistry(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if (entity instanceof Enemy || entity instanceof NeutralMob)
			{
				double distance = Math.sqrt(entity.distanceToSqr(0, entity.getY(), 0));
				applyAttributeBonusIfPossible(entity, Attributes.MOVEMENT_SPEED, Config.COMMON.movementSpeedBonus.get() * distance);
				applyAttributeBonusIfPossible(entity, Attributes.FLYING_SPEED, Config.COMMON.flyingSpeedBonus.get() * distance);
				applyAttributeBonusIfPossible(entity, Attributes.ATTACK_DAMAGE, Config.COMMON.attackDamageBonus.get() * distance);
				applyAttributeBonusIfPossible(entity, Attributes.ARMOR, Config.COMMON.armorBonus.get() * distance);
				applyAttributeBonusIfPossible(entity, Attributes.MAX_HEALTH, Config.COMMON.healthBonus.get() * distance);
			}
		}
	}
	
	private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus)
	{
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
		
		if (attributeInstance != null && attributeInstance.getModifier(modifierId) == null)
		{
			attributeInstance.addPermanentModifier(new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, Operation.MULTIPLY_TOTAL));
		}
	}
}
