package daripher.autoleveling.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandGlobalLevel {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> addLevelCommand = Commands.literal("autoleveling")
				.then(Commands.literal("addlevel").then(Commands.argument("value", IntegerArgumentType.integer()).executes(executeAddLevelCommand())));
		dispatcher.register(addLevelCommand);
	}

	private static Command<CommandSource> executeAddLevelCommand() {
		return ctx -> {
			GlobalLevelingData levelingData = GlobalLevelingData.get(ctx.getSource().getServer());
			levelingData.setLevel(levelingData.getLevelBonus() + ctx.getArgument("value", Integer.class));
			return 1;
		};
	}
}
