package daripher.autoleveling.client;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.event.MobsLevelingEvents;
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
		if (!(event.getEntity() instanceof LivingEntity)) return;
		var entity = (LivingEntity) event.getEntity();
		var showLevel = MobsLevelingEvents.hasLevel(entity) && MobsLevelingEvents.shouldShowName(entity);
		if (!showLevel) return;
		var minecraft = Minecraft.getInstance();
		var distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
		var isInRenderDistance = ForgeHooksClient.isNameplateInRenderDistance(entity, distance);
		if (!isInRenderDistance) return;
		var entityLevel = MobsLevelingEvents.getLevel(entity) + 1;
		var entityName = event.getContent();
		var entityLevelComponent = Component.translatable("autoleveling.level", entityLevel).withStyle(ChatFormatting.GREEN);
		event.getPoseStack().pushPose();
		event.getPoseStack().translate(0.0D, entity.getBbHeight() + 0.5F, 0.0D);
		event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
		event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
		var renderPose = event.getPoseStack().last().pose();
		var backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
		var textAlpha = (int) (backgroundOpacity * 255.0F) << 24;
		var font = minecraft.font;
		var displayMode = !entity.isDiscrete() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
		var textX = -font.width(entityName) / 2 - 5 - font.width(entityLevelComponent);
		var textY = "deadmau5".equals(entityName.getString()) ? -10 : 0;
		font.drawInBatch(entityLevelComponent, textX, textY, 553648127, false, renderPose, event.getMultiBufferSource(), displayMode, textAlpha, event.getPackedLight());
		font.drawInBatch(entityLevelComponent, textX, textY, -1, false, renderPose, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, event.getPackedLight());
		event.setResult(Event.Result.ALLOW);
		event.getPoseStack().popPose();
	}
}
