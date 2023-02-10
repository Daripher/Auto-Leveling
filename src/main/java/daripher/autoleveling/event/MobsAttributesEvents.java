package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsAttributesEvents {
	@SubscribeEvent
	public static void applyAttributeDamageBonus(LivingHurtEvent event) {
		DamageSource damageSource = event.getSource();
		Entity attackingEntity = damageSource.getEntity();

		if (attackingEntity instanceof LivingEntity) {
			LivingEntity attackingLivingEntity = (LivingEntity) attackingEntity;
			boolean isProjectileDamage = damageSource.isProjectile();
			boolean canApplyProjectileDamageBonus = attackingLivingEntity.getAttribute(AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get()) != null;

			if (isProjectileDamage && canApplyProjectileDamageBonus) {
				float damageBonus = (float) attackingLivingEntity.getAttributeValue(AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
				event.setAmount(event.getAmount() * damageBonus);
			}

			boolean isExplosionDamage = damageSource.isExplosion();
			boolean canApplyExplosionDamageBonus = attackingLivingEntity.getAttribute(AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get()) != null;

			if (isExplosionDamage && canApplyExplosionDamageBonus) {
				float damageBonus = (float) attackingLivingEntity.getAttributeValue(AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
				event.setAmount(event.getAmount() * damageBonus);
			}
		}
	}

	@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void attachMobsAttributes(EntityAttributeModificationEvent event) {
			event.getTypes().forEach(entityType -> {
				event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
				event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
			});
		}
	}
}