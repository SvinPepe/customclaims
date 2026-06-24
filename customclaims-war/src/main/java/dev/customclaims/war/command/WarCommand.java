package dev.customclaims.war.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.service.WarOperationResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class WarCommand {
    private WarCommand() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("war")
                .then(Commands.literal("start")
                        .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                .hasPermission(source, CustomClaimsPermissions.WAR_START))
                        .executes(context -> start(context.getSource())))
                .then(Commands.literal("status")
                        .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                .hasPermission(source, CustomClaimsPermissions.WAR_STATUS))
                        .executes(context -> status(context.getSource()))));

        WarAdminCommand.register(dispatcher);
    }

    private static int start(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            CustomClaimsWarMod.services().afkTracker().markActive(player);
            WarOperationResult result = CustomClaimsWarMod.services().warManager().startWar(player);
            send(source, result);
            return result.success() ? Command.SINGLE_SUCCESS : 0;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can start a chunk war."));
            return 0;
        }
    }

    private static int status(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            CustomClaimsWarMod.services().afkTracker().markActive(player);
            WarOperationResult result = CustomClaimsWarMod.services().warManager().status(player);
            send(source, result);
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            WarOperationResult result = CustomClaimsWarMod.services().warManager().status(source.getServer());
            send(source, result);
            return Command.SINGLE_SUCCESS;
        }
    }

    private static void send(CommandSourceStack source, WarOperationResult result) {
        if (result.success()) {
            source.sendSuccess(() -> Component.literal(result.message()), false);
        } else {
            source.sendFailure(Component.literal(result.message()));
        }
    }
}
