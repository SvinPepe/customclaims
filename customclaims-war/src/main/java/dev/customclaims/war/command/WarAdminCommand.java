package dev.customclaims.war.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.service.WarOperationResult;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class WarAdminCommand {
    private WarAdminCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("waradmin")
                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                        .hasPermission(source, CustomClaimsPermissions.WAR_ADMIN))
                .then(Commands.literal("stop")
                        .then(Commands.argument("warId", StringArgumentType.word())
                                .executes(context -> stop(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "warId")
                                ))))
                .then(Commands.literal("setprogress")
                        .then(Commands.argument("warId", StringArgumentType.word())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D, 100.0D))
                                        .executes(context -> setProgress(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "warId"),
                                                DoubleArgumentType.getDouble(context, "value")
                                        ))))));
    }

    private static int stop(CommandSourceStack source, String rawWarId) {
        return parseWarId(source, rawWarId, warId -> CustomClaimsWarMod.services().warManager().adminStop(source.getServer(), warId));
    }

    private static int setProgress(CommandSourceStack source, String rawWarId, double value) {
        return parseWarId(source, rawWarId, warId -> CustomClaimsWarMod.services().warManager().adminSetProgress(source.getServer(), warId, value));
    }

    private static int parseWarId(CommandSourceStack source, String rawWarId, WarAdminAction action) {
        try {
            WarOperationResult result = action.run(UUID.fromString(rawWarId));
            send(source, result);
            return result.success() ? Command.SINGLE_SUCCESS : 0;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid war id: " + rawWarId));
            return 0;
        }
    }

    private static void send(CommandSourceStack source, WarOperationResult result) {
        if (result.success()) {
            source.sendSuccess(() -> Component.literal(result.message()), false);
        } else {
            source.sendFailure(Component.literal(result.message()));
        }
    }

    @FunctionalInterface
    private interface WarAdminAction {
        WarOperationResult run(UUID warId);
    }
}
