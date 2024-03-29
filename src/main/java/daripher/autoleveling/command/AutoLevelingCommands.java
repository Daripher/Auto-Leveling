package daripher.autoleveling.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class AutoLevelingCommands {
  @SubscribeEvent
  public static void onRegisterCommands(RegisterCommandsEvent event) {
    LiteralArgumentBuilder<CommandSourceStack> addGlobalLevelCommand =
        Commands.literal("autoleveling")
            .then(
                Commands.literal("level")
                    .then(
                        Commands.literal("add")
                            .then(
                                Commands.argument("value", IntegerArgumentType.integer())
                                    .executes(AutoLevelingCommands::executeAddLevelCommand))))
            .requires(AutoLevelingCommands::hasPermission);
    event.getDispatcher().register(addGlobalLevelCommand);
  }

  private static int executeAddLevelCommand(CommandContext<CommandSourceStack> ctx) {
    MinecraftServer server = ctx.getSource().getServer();
    GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
    Integer levelBonus = ctx.getArgument("value", Integer.class);
    int oldLevelBonus = globalLevelingData.getLevelBonus();
    globalLevelingData.setLevel(oldLevelBonus + levelBonus);
    return 1;
  }

  private static boolean hasPermission(CommandSourceStack commandSourceStack) {
    return commandSourceStack.hasPermission(2);
  }
}
