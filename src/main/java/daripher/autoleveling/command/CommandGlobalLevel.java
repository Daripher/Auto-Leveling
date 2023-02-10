package daripher.autoleveling.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandGlobalLevel {
	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("autoleveling")
				.then(Commands.literal("addlevel")
						.then(Commands.argument("value", IntegerArgumentType.integer())
								.executes(CommandGlobalLevel::execute)));
		event.getDispatcher().register(command);
	}

	private static int execute(CommandContext<CommandSource> ctx) {
		GlobalLevelingData globalLevelingData = GlobalLevelingData.get(ctx.getSource().getServer());
		globalLevelingData.setLevel(globalLevelingData.getLevelBonus() + ctx.getArgument("value", Integer.class));
		return 1;
	}
}
