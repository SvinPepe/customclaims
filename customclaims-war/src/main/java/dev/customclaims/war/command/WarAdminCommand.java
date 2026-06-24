package dev.customclaims.war.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.service.WarOperationResult;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class WarAdminCommand {
    private WarAdminCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("waradmin")
                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                        .hasPermission(source, CustomClaimsPermissions.WAR_ADMIN))
                .then(Commands.literal("list")
                        .executes(context -> list(context.getSource())))
                .then(Commands.literal("stop")
                        .then(Commands.literal("here")
                                .executes(context -> stopHere(context.getSource())))
                        .then(Commands.literal("chunk")
                                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                        .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                                .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                                                        .executes(context -> stopChunk(
                                                                context.getSource(),
                                                                ResourceLocationArgument.getId(context, "dimension").toString(),
                                                                IntegerArgumentType.getInteger(context, "chunkX"),
                                                                IntegerArgumentType.getInteger(context, "chunkZ")
                                                        )))))))
                .then(Commands.literal("skipprep")
                        .then(Commands.literal("here")
                                .executes(context -> skipPrepHere(context.getSource())))
                        .then(Commands.literal("chunk")
                                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                        .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                                .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                                                        .executes(context -> skipPrepChunk(
                                                                context.getSource(),
                                                                ResourceLocationArgument.getId(context, "dimension").toString(),
                                                                IntegerArgumentType.getInteger(context, "chunkX"),
                                                                IntegerArgumentType.getInteger(context, "chunkZ")
                                                        )))))))
                .then(Commands.literal("setprogress")
                        .then(Commands.literal("here")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D, 100.0D))
                                        .executes(context -> setProgressHere(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "value")
                                        ))))
                        .then(Commands.literal("chunk")
                                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                        .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                                .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D, 100.0D))
                                                                .executes(context -> setProgressChunk(
                                                                        context.getSource(),
                                                                        ResourceLocationArgument.getId(context, "dimension").toString(),
                                                                        IntegerArgumentType.getInteger(context, "chunkX"),
                                                                        IntegerArgumentType.getInteger(context, "chunkZ"),
                                                                        DoubleArgumentType.getDouble(context, "value")
                                                                )))))))));
    }

    private static int list(CommandSourceStack source) {
        WarOperationResult result = CustomClaimsWarMod.services().warManager().adminList(source.getServer());
        send(source, result);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopHere(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            WarOperationResult result = CustomClaimsWarMod.services()
                    .warManager()
                    .adminStopChunk(source.getServer(), ChunkPosKey.from(player.serverLevel(), player.chunkPosition()));
            send(source, result);
            return result.success() ? Command.SINGLE_SUCCESS : 0;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can use /waradmin stop here."));
            return 0;
        }
    }

    private static int stopChunk(CommandSourceStack source, String dimension, int chunkX, int chunkZ) {
        WarOperationResult result = CustomClaimsWarMod.services()
                .warManager()
                .adminStopChunk(source.getServer(), new ChunkPosKey(dimension, chunkX, chunkZ));
        send(source, result);
        return result.success() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int skipPrepHere(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            WarOperationResult result = CustomClaimsWarMod.services()
                    .warManager()
                    .adminSkipPreparationChunk(source.getServer(), ChunkPosKey.from(player.serverLevel(), player.chunkPosition()));
            send(source, result);
            return result.success() ? Command.SINGLE_SUCCESS : 0;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can use /waradmin skipprep here."));
            return 0;
        }
    }

    private static int skipPrepChunk(CommandSourceStack source, String dimension, int chunkX, int chunkZ) {
        WarOperationResult result = CustomClaimsWarMod.services()
                .warManager()
                .adminSkipPreparationChunk(source.getServer(), new ChunkPosKey(dimension, chunkX, chunkZ));
        send(source, result);
        return result.success() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int setProgressHere(CommandSourceStack source, double value) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            WarOperationResult result = CustomClaimsWarMod.services()
                    .warManager()
                    .adminSetProgressChunk(source.getServer(), ChunkPosKey.from(player.serverLevel(), player.chunkPosition()), value);
            send(source, result);
            return result.success() ? Command.SINGLE_SUCCESS : 0;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can use /waradmin setprogress here."));
            return 0;
        }
    }

    private static int setProgressChunk(CommandSourceStack source, String dimension, int chunkX, int chunkZ, double value) {
        WarOperationResult result = CustomClaimsWarMod.services()
                .warManager()
                .adminSetProgressChunk(source.getServer(), new ChunkPosKey(dimension, chunkX, chunkZ), value);
        send(source, result);
        return result.success() ? Command.SINGLE_SUCCESS : 0;
    }

    private static void send(CommandSourceStack source, WarOperationResult result) {
        if (result.success()) {
            source.sendSuccess(() -> Component.literal(result.message()), false);
        } else {
            source.sendFailure(Component.literal(result.message()));
        }
    }
}
