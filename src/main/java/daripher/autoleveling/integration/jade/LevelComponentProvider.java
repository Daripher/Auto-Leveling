package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LevelComponentProvider implements IEntityComponentProvider
{
	INSTANCE;
	
	private static final ResourceLocation ID = new ResourceLocation(AutoLevelingMod.MOD_ID, "level");
	
	@Override
	public ResourceLocation getUid()
	{
		return ID;
	}
	
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig)
	{
		Entity entity = entityAccessor.getEntity();
		
		if (!LevelingDataProvider.canHaveLevel(entity))
		{
			return;
		}
		
		LevelingDataProvider.get((LivingEntity) entity).ifPresent(levelingData ->
		{
			int level = levelingData.getLevel() + 1;
			tooltip.add(Component.translatable("jade.autoleveling.tooltip", level));
		});
	}
}
