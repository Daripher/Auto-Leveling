package daripher.autoleveling.integration.jade;

import daripher.autoleveling.capability.LevelingDataProvider;
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

		if (!LevelingDataProvider.canHaveLevel(entity)) {
			return;
		}

		if (!LevelingDataProvider.shouldShowLevel(entity)) {
			return;
		}

		LevelingDataProvider.get((LivingEntity) entity).ifPresent(levelingData -> {
			var entityLevel = levelingData.getLevel() + 1;
			tooltip.add(new TranslatableComponent("jade.autoleveling.tooltip", entityLevel));
		});
	}
}
