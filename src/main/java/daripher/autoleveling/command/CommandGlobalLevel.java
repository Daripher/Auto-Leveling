package daripher.autoleveling.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class CommandGlobalLevel {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("autoleveling")
				.then(Commands.literal("addlevel")
						.then(Commands.argument("value", IntegerArgumentType.integer())
								.executes(CommandGlobalLevel::execute)));
		event.getDispatcher().register(command);
	}

	private static int execute(CommandContext<CommandSourceStack> ctx) {
		var globalLevelingData = GlobalLevelingData.get(ctx.getSource().getServer());
		globalLevelingData.setLevel(globalLevelingData.getLevelBonus() + ctx.getArgument("value", Integer.class));
		return 1;
	}
}
