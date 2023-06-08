package daripher.autoleveling.integration.jade;

import java.util.HashMap;
import java.util.Map;

import daripher.autoleveling.capability.LevelingDataProvider;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	private static final Map<EntityType<?>, Boolean> SHOWN_LEVELS_CACHE = new HashMap<EntityType<?>, Boolean>();


	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
		var entity = entityAccessor.getEntity();
		var entityType = entity.getType();
		var shouldShowLevel = SHOWN_LEVELS_CACHE.get(entityType);
		if (shouldShowLevel == null) {
			shouldShowLevel = LevelingDataProvider.canHaveLevel(entity) && LevelingDataProvider.shouldShowLevel(entity);
			SHOWN_LEVELS_CACHE.put(entityType, shouldShowLevel);
		}
		if (!shouldShowLevel) {
			return;
		}
		LevelingDataProvider.get((LivingEntity) entity).ifPresent(levelingData -> {
			var entityLevel = levelingData.getLevel() + 1;
			tooltip.add(new TranslatableComponent("jade.autoleveling.tooltip", entityLevel));
		});
	}
}
