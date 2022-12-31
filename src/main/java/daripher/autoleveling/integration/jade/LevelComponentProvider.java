package daripher.autoleveling.integration.jade;

import java.util.List;

import daripher.autoleveling.capability.LevelingDataProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum LevelComponentProvider implements IEntityComponentProvider {
	INSTANCE;

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
		Entity entity = entityAccessor.getEntity();

		if (!LevelingDataProvider.canHaveLevel(entity)) {
			return;
		}

		LevelingDataProvider.getLevelingData((LivingEntity) entity).ifPresent(levelingData -> {
			int level = levelingData.getLevel() + 1;
			tooltip.add(new TranslationTextComponent("jade.autoleveling.tooltip", level));
		});
	}
}
