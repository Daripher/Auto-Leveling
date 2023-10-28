package daripher.autoleveling.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
  @Invoker("createLootContext")
  LootContext.Builder invokeCreateLootContext(boolean damagedByPlayer, DamageSource damageSource);

  @Accessor
  int getLastHurtByPlayerTime();
}
