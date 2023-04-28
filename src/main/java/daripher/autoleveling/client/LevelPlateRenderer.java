package daripher.autoleveling.client;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, value = Dist.CLIENT)
public class LevelPlateRenderer {
	@SubscribeEvent
	public static void renderEntityLevel(RenderNameTagEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		var entity = (LivingEntity) event.getEntity();
		if (!shouldShowName(entity)) {
			return;
		}
		var minecraft = Minecraft.getInstance();
		var distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
		var isInRenderDistance = ForgeHooksClient.isNameplateInRenderDistance(entity, distance);
		if (!isInRenderDistance) {
			return;
		}
		var levelingData = LevelingDataProvider.get(entity).orElse(null);
		if (levelingData == null) {
			return;
		}
		var entityLevel = levelingData.getLevel() + 1;
		var entityName = event.getContent();
		var entityLevelComponent = Component.translatable("autoleveling.level", entityLevel).withStyle(ChatFormatting.GREEN);
		event.getPoseStack().pushPose();
		event.getPoseStack().translate(0.0D, entity.getBbHeight() + 0.5F, 0.0D);
		event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
		event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
		var renderingPose = event.getPoseStack().last().pose();
		var backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
		var textAlpha = (int) (backgroundOpacity * 255.0F) << 24;
		var font = minecraft.font;
		var displayMode = !entity.isDiscrete() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
		var textX = -font.width(entityName) / 2 - 5 - font.width(entityLevelComponent);
		var textY = "deadmau5".equals(entityName.getString()) ? -10 : 0;
		font.drawInBatch(entityLevelComponent, textX, textY, 553648127, false, renderingPose, event.getMultiBufferSource(), displayMode, textAlpha, event.getPackedLight());
		font.drawInBatch(entityLevelComponent, textX, textY, -1, false, renderingPose, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, event.getPackedLight());
		event.setResult(Event.Result.ALLOW);
		event.getPoseStack().popPose();
	}

	private static boolean shouldShowName(LivingEntity entity) {
		if (!LevelingDataProvider.canHaveLevel(entity)) {
			return false;
		}
		if (!LevelingDataProvider.shouldShowLevel(entity)) {
			return false;
		}
		if (!Minecraft.renderNames()) {
			return false;
		}
		if (entity.isVehicle()) {
			return false;
		}
		var minecraft = Minecraft.getInstance();
		if (entity == minecraft.getCameraEntity()) {
			return false;
		}
		var clientPlayer = minecraft.player;
		if (entity.isInvisibleTo(clientPlayer)) {
			return false;
		}
		if (!clientPlayer.hasLineOfSight(entity)) {
			return false;
		}
		var alwaysShowLevel = Config.COMMON.alwaysShowLevel.get();
		var showLevelWhenLookingAt = Config.COMMON.showLevelWhenLookingAt.get();
		var lookingAtEntity = minecraft.crosshairPickEntity == entity;
		return alwaysShowLevel || showLevelWhenLookingAt && lookingAtEntity;
	}
}
