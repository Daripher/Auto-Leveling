package daripher.autoleveling.integration.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.LivingEntity;

@WailaPlugin
public class AutoLevelingWailaPlugin implements IWailaPlugin {
  @Override
  public void register(IRegistrar registration) {
    registration.registerComponentProvider(
        LevelComponentProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
  }
}
