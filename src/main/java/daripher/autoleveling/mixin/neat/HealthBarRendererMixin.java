package daripher.autoleveling.mixin.neat;

import com.mojang.blaze3d.vertex.PoseStack;
import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.neat.HealthBarRenderer;
import vazkii.neat.NeatConfig;

@Mixin(value = HealthBarRenderer.class, remap = false)
public class HealthBarRendererMixin {
  @Inject(method = "hookRender", at = @At("TAIL"))
  private static void renderLevel(
      Entity entity,
      PoseStack poseStack,
      MultiBufferSource buffers,
      Quaternionf cameraOrientation,
      CallbackInfo callback) {
    if (!MobsLevelingEvents.shouldShowLevel(entity)) return;
    Minecraft mc = Minecraft.getInstance();
    int entityLevel = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
    String name =
        entity.hasCustomName()
            ? ChatFormatting.ITALIC + Objects.requireNonNull(entity.getCustomName()).getString()
            : entity.getDisplayName().getString();
    float nameLen = mc.font.width(name) * 0.5f;
    float halfSize = Math.max((float) NeatConfig.instance.plateSize(), nameLen / 2f + 10f);
    String levelText = Component.translatable("autoleveling.level", entityLevel).getString();
    poseStack.pushPose();
    poseStack.translate(0d, entity.getBbHeight() + NeatConfig.instance.heightAbove(), 0d);
    poseStack.mulPose(cameraOrientation);
    poseStack.pushPose();
    poseStack.scale(-0.0267f, -0.0267f, 0.0267f);
    poseStack.translate(halfSize - mc.font.width(levelText) / 2f, -4.5d, 0d);
    poseStack.scale(0.5f, 0.5f, 0.5f);
    mc.font.drawInBatch(
        levelText,
        0f,
        0f,
        16777215,
        false,
        poseStack.last().pose(),
        buffers,
        Font.DisplayMode.NORMAL,
        0,
        15728880);
    poseStack.popPose();
    poseStack.popPose();
  }
}
