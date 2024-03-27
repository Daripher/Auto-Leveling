package daripher.autoleveling.client;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.event.MobsLevelingEvents;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, value = Dist.CLIENT)
public class LevelPlateRenderer {
  @SubscribeEvent
  public static void renderEntityLevel(RenderNameTagEvent event) {
    if (!shouldRender(event)) return;
    LivingEntity entity = (LivingEntity) event.getEntity();
    Minecraft minecraft = Minecraft.getInstance();
    event.getPoseStack().pushPose();
    event.getPoseStack().translate(0d, entity.getBbHeight() + 0.5f, 0d);
    event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    event.getPoseStack().scale(-0.025f, -0.025f, 0.025f);
    Matrix4f pose = event.getPoseStack().last().pose();
    Font.DisplayMode mode = getDisplayMode(entity);
    Component level = getLevelComponent(entity);
    Component name = event.getContent();
    int nameplateWidth = minecraft.font.width(name);
    int levelWidth = minecraft.font.width(level);
    MultiBufferSource buffer = event.getMultiBufferSource();
    int light = event.getPackedLight();
    LevelPlatePos pos = Config.CLIENT.levelTextPosition.get();
    float textX = getTextX(levelWidth, nameplateWidth, pos);
    float textY = getTextY(minecraft.font.lineHeight, pos);
    if (name.getString().equals("deadmau5")) {
      textY -= 10;
    }
    renderLevel(level, textX, textY, pose, buffer, mode, light);
    event.setResult(Event.Result.ALLOW);
    event.getPoseStack().popPose();
  }

  @Nonnull
  private static Font.DisplayMode getDisplayMode(LivingEntity entity) {
    return !entity.isDiscrete() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
  }

  private static boolean shouldRender(RenderNameTagEvent event) {
    if (ModList.get().isLoaded("neat")) return false;
    if (!(event.getEntity() instanceof LivingEntity entity)) return false;
    if (!MobsLevelingEvents.shouldShowName(entity)) return false;
    Minecraft minecraft = Minecraft.getInstance();
    double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
    return ForgeHooksClient.isNameplateInRenderDistance(entity, distance);
  }

  public static void renderLevel(
      Component component,
      float x,
      float y,
      Matrix4f pose,
      MultiBufferSource buffer,
      Font.DisplayMode mode,
      int light) {
    Minecraft minecraft = Minecraft.getInstance();
    float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25f);
    int alpha = (int) (backgroundOpacity * 255f) << 24;
    Font font = minecraft.font;
    font.drawInBatch(component, x, y, 0xffffff, false, pose, buffer, mode, alpha, light);
    font.drawInBatch(component, x, y, -1, false, pose, buffer, Font.DisplayMode.NORMAL, 0, light);
  }

  @Nonnull
  public static Component getLevelComponent(LivingEntity entity) {
    int entityLevel = MobsLevelingEvents.getLevel(entity) + 1;
    int color = Config.Client.getLevelTextColor();
    Style style = Style.EMPTY.withColor(color);
    return Component.translatable("autoleveling.level", entityLevel).withStyle(style);
  }

  public static float getTextX(float levelWidth, float nameplateWidth, LevelPlatePos pos) {
    float posX =
        switch (pos) {
          case LEFT -> -nameplateWidth / 2 - 3 - levelWidth;
          case RIGHT -> nameplateWidth / 2 + 3;
          case TOP, BOTTOM -> -levelWidth / 2;
          case TOP_LEFT, BOTTOM_LEFT -> -nameplateWidth / 2;
          case TOP_RIGHT, BOTTOM_RIGHT -> nameplateWidth / 2 - levelWidth;
        };
    return posX + Config.CLIENT.levelTextShiftX.get();
  }

  public static float getTextY(float nameplateHeight, LevelPlatePos pos) {
    float posY =
        switch (pos) {
          case LEFT, RIGHT -> 0f;
          case TOP, TOP_RIGHT, TOP_LEFT -> -nameplateHeight - 3;
          case BOTTOM, BOTTOM_RIGHT, BOTTOM_LEFT -> nameplateHeight + 3;
        };
    return posY + Config.CLIENT.levelTextShiftY.get();
  }
}
