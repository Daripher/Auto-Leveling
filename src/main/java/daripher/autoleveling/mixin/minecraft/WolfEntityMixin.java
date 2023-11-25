package daripher.autoleveling.mixin.minecraft;

import java.util.Objects;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity {
  @SuppressWarnings("DataFlowIssue")
  protected WolfEntityMixin() {
    super(null, null);
  }

  @Redirect(
      method = "setTame(Z)V",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/entity/passive/WolfEntity;setHealth(F)V",
              ordinal = 0))
  private void setTame_inject1(WolfEntity entity, float health) {}

  @Inject(method = "setTame(Z)V", at = @At("HEAD"))
  private void setTame_inject2(boolean tamed, CallbackInfo callbackInfo) {
    if (tamed) {
      float healthPercentage = getHealth() / getMaxHealth();
      Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20);
      setHealth(getMaxHealth() * healthPercentage);
    }
  }
}
