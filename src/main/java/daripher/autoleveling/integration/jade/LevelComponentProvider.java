package daripher.autoleveling.integration.jade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daripher.autoleveling.capability.LevelingDataProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	private static final Map<EntityType<?>, Boolean> SHOWN_LEVELS_CACHE = new HashMap<EntityType<?>, Boolean>();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
		Entity entity = entityAccessor.getEntity();
		EntityType<?> entityType = entity.getType();
		Boolean shouldShowLevel = SHOWN_LEVELS_CACHE.get(entityType);
		if (shouldShowLevel == null) {
			shouldShowLevel = LevelingDataProvider.canHaveLevel(entity) && LevelingDataProvider.shouldShowLevel(entity);
			SHOWN_LEVELS_CACHE.put(entityType, shouldShowLevel);
		}
		if (!shouldShowLevel) {
			return;
		}
		LevelingDataProvider.getLevelingData((LivingEntity) entity).ifPresent(levelingData -> {
			int level = levelingData.getLevel() + 1;
			tooltip.add(new TranslationTextComponent("jade.autoleveling.tooltip", level));
		});
	}
}
