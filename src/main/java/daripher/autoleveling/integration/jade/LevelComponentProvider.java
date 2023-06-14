package daripher.autoleveling.integration.jade;

import daripher.autoleveling.event.MobsLevelingEvents;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
		var entity = entityAccessor.getEntity();
		var showLevel = MobsLevelingEvents.hasLevel(entity) && MobsLevelingEvents.shouldShowLevel(entity);
		if (!showLevel) return;
		var level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
		tooltip.add(new TranslatableComponent("jade.autoleveling.tooltip", level));
	}
}
