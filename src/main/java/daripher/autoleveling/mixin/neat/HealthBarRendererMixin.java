package daripher.autoleveling.mixin.neat;

import com.mojang.blaze3d.matrix.MatrixStack;
import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.neat.HealthBarRenderer;
import vazkii.neat.NeatConfig;

@Mixin(value = HealthBarRenderer.class, remap = false)
public class HealthBarRendererMixin {
  @Inject(
      method = "renderEntity",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/FontRenderer;drawInBatch(Ljava/lang/String;FFIZLnet/minecraft/util/math/vector/Matrix4f;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ZII)I",
              remap = true,
              shift = At.Shift.AFTER,
              ordinal = 0))
  private void renderLevel(
      Minecraft mc,
      MatrixStack matrixStack,
      IRenderTypeBuffer.Impl buffer,
      ActiveRenderInfo renderInfo,
      LivingEntity entity,
      int light,
      ItemStack icon,
      boolean boss,
      CallbackInfo callback) {
    if (!MobsLevelingEvents.hasLevel(entity)) return;
    if (!MobsLevelingEvents.shouldShowLevel(entity)) return;
    int entityLevel = MobsLevelingEvents.getLevel(entity) + 1;
    String name =
        entity.hasCustomName()
            ? TextFormatting.ITALIC + Objects.requireNonNull(entity.getCustomName()).getString()
            : entity.getDisplayName().getString();
    float size = (float) NeatConfig.plateSize;
    if (entity.hasCustomName()) {
      name = TextFormatting.ITALIC + name;
    }
    float namel = (float) mc.font.width(name) * 0.5f;
    if (namel + 20.0F > size * 2.0F) {
      size = namel / 2.0F + 10.0F;
    }
    String levelText = new TranslationTextComponent("autoleveling.level", entityLevel).getString();
    mc.font.drawInBatch(
        levelText,
        size * 4 - mc.font.width(levelText),
        0.0F,
        16777215,
        false,
        matrixStack.last().pose(),
        buffer,
        false,
        0,
        light);
  }
}
