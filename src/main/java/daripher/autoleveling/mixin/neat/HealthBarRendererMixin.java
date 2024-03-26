package daripher.autoleveling.mixin.neat;

import com.mojang.blaze3d.vertex.PoseStack;
import daripher.autoleveling.client.LevelPlateRenderer;
import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
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
      MultiBufferSource buffer,
      Quaternionf cameraOrientation,
      CallbackInfo callback) {
    if (!MobsLevelingEvents.hasLevel(entity)) return;
    if (!MobsLevelingEvents.shouldShowLevel(entity)) return;
    if (!(entity instanceof LivingEntity living)) return;
    Minecraft minecraft = Minecraft.getInstance();
    String name = getName(entity);
    float nameLen = minecraft.font.width(name) * 0.5f;
    float halfSize = Math.max(NeatConfig.instance.plateSize(), nameLen / 2f + 10f);
    Component level = LevelPlateRenderer.getLevelComponent(living);
    poseStack.pushPose();
    poseStack.translate(0d, entity.getBbHeight() + NeatConfig.instance.heightAbove(), 0d);
    poseStack.mulPose(cameraOrientation);
    poseStack.scale(-0.0267f, -0.0267f, 0.0267f);
    poseStack.translate(-halfSize - minecraft.font.width(level) / 2f - 5, -4.5d, 0d);
    poseStack.scale(0.5f, 0.5f, 0.5f);
    Matrix4f pose = poseStack.last().pose();
    Font.DisplayMode mode = Font.DisplayMode.NORMAL;
    int light = 15728880;
    LevelPlateRenderer.renderLevel(minecraft, level, 0, 0, pose, buffer, mode, light);
    poseStack.popPose();
  }

  @Nonnull
  private static String getName(Entity entity) {
    if (!entity.hasCustomName()) return entity.getDisplayName().getString();
    return ChatFormatting.ITALIC + Objects.requireNonNull(entity.getCustomName()).getString();
  }
}
