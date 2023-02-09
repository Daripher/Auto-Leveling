package daripher.autoleveling.integration.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.world.entity.LivingEntity;

@WailaPlugin
public class AutoLevelingWailaPlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerComponentProvider(LevelComponentProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
	}
}
