package daripher.autoleveling.integration.jade;

import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.List;
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
  public void appendBody(
      List<ITextComponent> tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
    Entity entity = entityAccessor.getEntity();
    boolean showLevel =
        MobsLevelingEvents.hasLevel(entity) && MobsLevelingEvents.shouldShowLevel(entity);
    if (!showLevel) return;
    int level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
    tooltip.add(new TranslationTextComponent("jade.autoleveling.tooltip", level));
  }
}
