package dev.customclaims.protection.command;

import com.mojang.brigadier.Command;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.network.ClientboundClaimRulesStatePayload;
import dev.customclaims.protection.network.ClaimRulesStateDto;
import dev.customclaims.protection.service.ClaimRuleUpdateResult;
import dev.customclaims.protection.service.ClaimRulesService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class ClaimRulesCommand {
    private ClaimRulesCommand() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("claimrules")
                .then(Commands.literal("limits")
                        .then(Commands.literal("me")
                                .executes(context -> limitsMe(context.getSource())))
                        .then(Commands.literal("reset")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.LIMITS_RESET))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> resetLimits(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))
                        .then(Commands.literal("resetall")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.LIMITS_RESET))
                                .executes(context -> resetAllLimits(context.getSource()))))
                .then(Commands.literal("explosions")
                        .then(Commands.literal("status")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_STATUS))
                                .executes(context -> ruleStatus(context.getSource(), ClaimRulesService.RULE_EXPLOSIONS)))
                        .then(Commands.literal("enable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_EXPLOSIONS, true)))
                        .then(Commands.literal("disable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_EXPLOSIONS, false))))
                .then(Commands.literal("create")
                        .then(Commands.literal("status")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.CREATE_STATUS))
                                .executes(context -> ruleStatus(context.getSource(), ClaimRulesService.RULE_CREATE)))
                        .then(Commands.literal("enable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.CREATE_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_CREATE, true)))
                        .then(Commands.literal("disable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.CREATE_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_CREATE, false))))
                .then(Commands.literal("assembly")
                        .then(Commands.literal("status")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.ASSEMBLY_STATUS))
                                .executes(context -> ruleStatus(context.getSource(), ClaimRulesService.RULE_ASSEMBLY)))
                        .then(Commands.literal("enable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.ASSEMBLY_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_ASSEMBLY, true)))
                        .then(Commands.literal("disable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.ASSEMBLY_TOGGLE))
                                .executes(context -> setRule(context.getSource(), ClaimRulesService.RULE_ASSEMBLY, false))))
                .then(Commands.literal("gui")
                        .executes(context -> openGui(context.getSource()))));
    }

    private static int limitsMe(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            source.sendSuccess(() -> Component.literal(CustomClaimsProtectionMod.services()
                    .foreignInteractionLimitService()
                    .limitsStatus(player)), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can inspect personal claim limits."));
            return 0;
        }
    }

    private static int resetLimits(CommandSourceStack source, ServerPlayer target) {
        CustomClaimsProtectionMod.services().foreignInteractionLimitService().reset(target.getUUID());
        source.sendSuccess(() -> Component.literal("Foreign claim limits reset for " + target.getGameProfile().getName() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetAllLimits(CommandSourceStack source) {
        CustomClaimsProtectionMod.services().foreignInteractionLimitService().resetAll();
        source.sendSuccess(() -> Component.literal("Foreign claim limits reset for all players."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int ruleStatus(CommandSourceStack source, String ruleId) {
        source.sendSuccess(() -> Component.literal(CustomClaimsProtectionMod.services()
                .claimRulesService()
                .statusMessage(source, ruleId)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setRule(CommandSourceStack source, String ruleId, boolean enabled) {
        ClaimRuleUpdateResult result = CustomClaimsProtectionMod.services()
                .claimRulesService()
                .setRule(source, ruleId, enabled);
        if (!result.success()) {
            source.sendFailure(Component.literal(result.message()));
            sendGuiRefreshIfOpen(source, result);
            return 0;
        }

        source.sendSuccess(() -> Component.literal(result.message()), result.changed());
        sendGuiRefreshIfOpen(source, result);
        return Command.SINGLE_SUCCESS;
    }

    private static int openGui(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (!NetworkRegistry.hasChannel(player.connection, ClientboundClaimRulesStatePayload.TYPE.id())) {
                source.sendFailure(Component.literal("Install CustomClaims Protection on the client to use /claimrules gui. Commands still work."));
                return 0;
            }

            PacketDistributor.sendToPlayer(player, new ClientboundClaimRulesStatePayload(
                    ClaimRulesStateDto.from(CustomClaimsProtectionMod.services().claimRulesService().stateFor(player)),
                    "",
                    true
            ));
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can open the claim rules GUI."));
            return 0;
        }
    }

    private static void sendGuiRefreshIfOpen(CommandSourceStack source, ClaimRuleUpdateResult result) {
        if (result.state() == null) {
            return;
        }
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (!NetworkRegistry.hasChannel(player.connection, ClientboundClaimRulesStatePayload.TYPE.id())) {
                return;
            }
            PacketDistributor.sendToPlayer(player, new ClientboundClaimRulesStatePayload(
                    ClaimRulesStateDto.from(result.state()),
                    result.message(),
                    false
            ));
        } catch (Exception ignored) {
            // Commands still work without a player/client payload.
        }
    }
}
