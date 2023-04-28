package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingAttributes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsAttributesEvents {
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

	@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void attachDamageBonusAttributes(EntityAttributeModificationEvent event) {
			event.getTypes().forEach(entityType -> {
				event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER.get());
				event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER.get());
			});
		}
	}
}
