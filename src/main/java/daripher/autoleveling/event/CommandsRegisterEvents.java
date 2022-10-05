package daripher.autoleveling.event;

import daripher.autoleveling.command.CommandGlobalLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandsRegisterEvents
{	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		CommandGlobalLevel.register(event.getDispatcher());
	}
}
