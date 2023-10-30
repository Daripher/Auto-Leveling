package daripher.autoleveling.client;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    if (ModList.get().isLoaded("neat")) return;
    if (!(event.getEntity() instanceof LivingEntity entity)) return;
    boolean showLevel =
        MobsLevelingEvents.hasLevel(entity) && MobsLevelingEvents.shouldShowName(entity);
    if (!showLevel) return;
    Minecraft minecraft = Minecraft.getInstance();
    double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
    boolean isInRenderDistance = ForgeHooksClient.isNameplateInRenderDistance(entity, distance);
    if (!isInRenderDistance) return;
    int entityLevel = MobsLevelingEvents.getLevel(entity) + 1;
    Component entityName = event.getContent();
    MutableComponent entityLevelComponent =
        Component.translatable("autoleveling.level", entityLevel).withStyle(ChatFormatting.GREEN);
    event.getPoseStack().pushPose();
    event.getPoseStack().translate(0.0D, entity.getBbHeight() + 0.5F, 0.0D);
    event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
    Matrix4f renderPose = event.getPoseStack().last().pose();
    float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
    int textAlpha = (int) (backgroundOpacity * 255.0F) << 24;
    Font font = minecraft.font;
    Font.DisplayMode displayMode =
        !entity.isDiscrete() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
    int textX = -font.width(entityName) / 2 - 5 - font.width(entityLevelComponent);
    int textY = "deadmau5".equals(entityName.getString()) ? -10 : 0;
    font.drawInBatch(
        entityLevelComponent,
        textX,
        textY,
        553648127,
        false,
        renderPose,
        event.getMultiBufferSource(),
        displayMode,
        textAlpha,
        event.getPackedLight());
    font.drawInBatch(
        entityLevelComponent,
        textX,
        textY,
        -1,
        false,
        renderPose,
        event.getMultiBufferSource(),
        Font.DisplayMode.NORMAL,
        0,
        event.getPackedLight());
    event.setResult(Event.Result.ALLOW);
    event.getPoseStack().popPose();
  }
}
