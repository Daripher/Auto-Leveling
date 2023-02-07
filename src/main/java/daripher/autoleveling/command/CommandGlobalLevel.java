package daripher.autoleveling.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandGlobalLevel {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		var command = Commands.literal("autoleveling")
				.then(Commands.literal("addlevel")
						.then(Commands.argument("value", IntegerArgumentType.integer())
								.executes(CommandGlobalLevel::execute)));
		event.getDispatcher().register(command);
	}

	private static int execute(CommandContext<CommandSourceStack> ctx) {
		var globalLevelingData = GlobalLevelingData.get(ctx.getSource().getServer());
		var commandLevelBonus = ctx.getArgument("value", Integer.class);
		var oldLevelBonus = globalLevelingData.getLevelBonus();
		globalLevelingData.setLevel(oldLevelBonus + commandLevelBonus);
		return 1;
	}
}
