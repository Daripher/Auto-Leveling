package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsAttributesEvents {
	@SubscribeEvent
	public static void attachMobsAttributes(EntityAttributeModificationEvent event) {
		event.getTypes().forEach(entityType -> {
			event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
			event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
		});
	}

	@SubscribeEvent
	public static void applyRangedDamageBonus(LivingHurtEvent event) {
		var damageSource = event.getSource();
		var attackingEntity = damageSource.getEntity();

		if (attackingEntity instanceof LivingEntity) {
			var attackingLivingEntity = (LivingEntity) attackingEntity;
			var isProjectileDamage = damageSource.isProjectile();
			var canApplyProjectileDamageBonus = attackingLivingEntity.getAttribute(AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get()) != null;

			if (isProjectileDamage && canApplyProjectileDamageBonus) {
				var damageBonus = (float) attackingLivingEntity.getAttributeValue(AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
				event.setAmount(event.getAmount() + event.getAmount() * damageBonus);
			}

			var isExplosionDamage = damageSource.isExplosion();
			var canApplyExplosionDamageBonus = attackingLivingEntity.getAttribute(AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get()) != null;

			if (isExplosionDamage && canApplyExplosionDamageBonus) {
				var damageBonus = (float) attackingLivingEntity.getAttributeValue(AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
				event.setAmount(event.getAmount() + event.getAmount() * damageBonus);
			}
		}
	}
}
