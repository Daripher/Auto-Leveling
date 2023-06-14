package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	private static final ResourceLocation ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "level");

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
		var entity = entityAccessor.getEntity();
		var showLevel = MobsLevelingEvents.hasLevel(entity) && MobsLevelingEvents.shouldShowLevel(entity);
		if (!showLevel) return;
		var level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
		tooltip.add(Component.translatable("jade.autoleveling.tooltip", level));
	}
}
