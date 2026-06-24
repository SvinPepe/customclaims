package dev.customclaims.protection.command;

import com.mojang.brigadier.Command;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ClaimRulesCommand {
    private ClaimRulesCommand() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("claimrules")
                .then(Commands.literal("limits")
                        .then(Commands.literal("me")
                                .executes(context -> limitsMe(context.getSource()))))
                .then(Commands.literal("explosions")
                        .then(Commands.literal("status")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_STATUS))
                                .executes(context -> explosionStatus(context.getSource())))
                        .then(Commands.literal("enable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE))
                                .executes(context -> setExplosionProtection(context.getSource(), true)))
                        .then(Commands.literal("disable")
                                .requires(source -> CustomClaimsCoreMod.services().permissionService()
                                        .hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE))
                                .executes(context -> setExplosionProtection(context.getSource(), false)))));
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

    private static int explosionStatus(CommandSourceStack source) {
        return withPlayerParty(source, party -> {
            boolean protectedFromExplosions = CustomClaimsProtectionMod.services()
                    .explosionProtectionService()
                    .isPartyExplosionProtectionEnabled(party);
            source.sendSuccess(() -> Component.literal(
                    "Explosion protection for " + party + ": " + (protectedFromExplosions ? "enabled" : "disabled")
            ), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int setExplosionProtection(CommandSourceStack source, boolean enabled) {
        return withPlayerParty(source, party -> {
            CustomClaimsProtectionMod.services().explosionProtectionService().setPartyExplosionProtection(party, enabled);
            source.sendSuccess(() -> Component.literal(
                    "Explosion protection for " + party + " is now " + (enabled ? "enabled" : "disabled")
            ), true);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int withPlayerParty(CommandSourceStack source, PartyCommand action) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            return CustomClaimsCoreMod.services().partyService().getPlayerParty(player)
                    .map(action::run)
                    .orElseGet(() -> {
                        source.sendFailure(Component.literal("You must be in a party."));
                        return 0;
                    });
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can manage party claim rules."));
            return 0;
        }
    }

    @FunctionalInterface
    private interface PartyCommand {
        int run(PartyId partyId);
    }
}
