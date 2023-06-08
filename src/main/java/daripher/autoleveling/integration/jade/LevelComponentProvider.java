package daripher.autoleveling.integration.jade;

import java.util.HashMap;
import java.util.Map;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	private static final Map<EntityType<?>, Boolean> SHOWN_LEVELS_CACHE = new HashMap<EntityType<?>, Boolean>();
	private static final ResourceLocation ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "level");

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

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
			var level = levelingData.getLevel() + 1;
			tooltip.add(Component.translatable("jade.autoleveling.tooltip", level));
		});
	}
}
