package daripher.autoleveling.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandGlobalLevel
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("autoleveling").then(Commands.literal("addlevel").then(Commands.argument("value", IntegerArgumentType.integer()).executes(ctx ->
		{
			GlobalLevelingData data = GlobalLevelingData.get(ctx.getSource().getServer());
			data.setLevel(data.getLevelBonus() + ctx.getArgument("value", Integer.class));
			return 1;
		}))));
	}
}
