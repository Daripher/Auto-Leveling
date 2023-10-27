package daripher.autoleveling.mixin.client;

import daripher.autoleveling.client.LeveledMobsTextures;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
  @Shadow protected M model;

  @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
  protected void injectGetRenderType(
      T entity,
      boolean visible,
      boolean invisibleToPlayer,
      boolean glowing,
      CallbackInfoReturnable<RenderType> callbackInfo) {
    var level = MobsLevelingEvents.getLevel(entity);
    var textureLocation = LeveledMobsTextures.get(entity.getType(), level + 1);
    if (textureLocation == null) return;
    if (invisibleToPlayer)
      callbackInfo.setReturnValue(RenderType.itemEntityTranslucentCull(textureLocation));
    else if (visible) callbackInfo.setReturnValue(model.renderType(textureLocation));
    else callbackInfo.setReturnValue(glowing ? RenderType.outline(textureLocation) : null);
  }
}
