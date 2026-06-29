package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.PartyService;
import dev.customclaims.core.service.PermissionService;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class ClaimRulesService {
    public static final String RULE_EXPLOSIONS = "explosions";
    public static final String RULE_CREATE = "create";

    private final PartyService partyService;
    private final PermissionService permissionService;
    private final ExplosionProtectionService explosionProtectionService;
    private final CreateMachinesProtectionService createMachinesProtectionService;
    private final ClaimRulesCooldownService cooldownService;

    public ClaimRulesService(
            PartyService partyService,
            PermissionService permissionService,
            ExplosionProtectionService explosionProtectionService,
            CreateMachinesProtectionService createMachinesProtectionService,
            ClaimRulesCooldownService cooldownService
    ) {
        this.partyService = partyService;
        this.permissionService = permissionService;
        this.explosionProtectionService = explosionProtectionService;
        this.createMachinesProtectionService = createMachinesProtectionService;
        this.cooldownService = cooldownService;
    }

    public ClaimRulesState stateFor(ServerPlayer player) {
        boolean canToggleExplosions = permissionService.hasPermission(player, CustomClaimsPermissions.EXPLOSIONS_TOGGLE);
        boolean canToggleCreate = permissionService.hasPermission(player, CustomClaimsPermissions.CREATE_TOGGLE);

        Optional<PartyId> party = partyService.getPlayerParty(player);
        if (party.isEmpty()) {
            return ClaimRulesState.noParty(canToggleExplosions, canToggleCreate);
        }

        Instant now = Instant.now();
        PartyId partyId = party.get();
        boolean bypassed = cooldownService.isBypassed(player);
        return new ClaimRulesState(
                true,
                partyId,
                partyLabel(player.createCommandSourceStack(), partyId),
                explosionProtectionService.isPartyExplosionProtectionEnabled(player.server, partyId),
                createMachinesProtectionService.isPartyCreateMachinesEnabled(player.server, partyId),
                bypassed ? 0L : cooldownService.remainingSeconds(player.server, partyId, RULE_EXPLOSIONS, now),
                bypassed ? 0L : cooldownService.remainingSeconds(player.server, partyId, RULE_CREATE, now),
                canToggleExplosions,
                canToggleCreate
        );
    }

    public ClaimRuleUpdateResult setRule(CommandSourceStack source, String ruleId, boolean enabled) {
        String normalizedRule = normalizeRule(ruleId);
        String permission = switch (normalizedRule) {
            case RULE_EXPLOSIONS -> CustomClaimsPermissions.EXPLOSIONS_TOGGLE;
            case RULE_CREATE -> CustomClaimsPermissions.CREATE_TOGGLE;
            default -> "";
        };
        if (permission.isEmpty()) {
            return ClaimRuleUpdateResult.failure("Unknown claim rule: " + ruleId, currentState(source));
        }
        if (!permissionService.hasPermission(source, permission)) {
            return ClaimRuleUpdateResult.failure("You do not have permission to change this claim rule.", currentState(source));
        }

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            return ClaimRuleUpdateResult.failure("Only players can manage party claim rules.", currentState(source));
        }

        Optional<PartyId> party = partyService.getPlayerParty(player);
        if (party.isEmpty()) {
            return ClaimRuleUpdateResult.failure("You must be in a party.", stateFor(player));
        }

        PartyId partyId = party.get();
        boolean current = currentValue(source, partyId, normalizedRule);
        Instant now = Instant.now();
        if (!cooldownService.isBypassed(source)) {
            long remaining = cooldownService.remainingSeconds(source.getServer(), partyId, normalizedRule, now);
            if (remaining > 0L) {
                return ClaimRuleUpdateResult.failure(
                        "This claim rule was changed recently. Try again in " + formatDuration(remaining) + ".",
                        stateFor(player)
                );
            }
        }
        if (current == enabled) {
            return ClaimRuleUpdateResult.success(false, alreadyMessage(source, partyId, normalizedRule, enabled), stateFor(player));
        }

        boolean saved = switch (normalizedRule) {
            case RULE_EXPLOSIONS -> explosionProtectionService.setPartyExplosionProtection(source.getServer(), partyId, enabled);
            case RULE_CREATE -> createMachinesProtectionService.setPartyCreateMachinesEnabled(source.getServer(), partyId, enabled);
            default -> false;
        };
        if (!saved) {
            return ClaimRuleUpdateResult.failure("Failed to save claim rule for " + partyLabel(source, partyId) + ".", stateFor(player));
        }

        if (!cooldownService.isBypassed(source)) {
            cooldownService.recordToggle(source.getServer(), partyId, normalizedRule, now);
        }

        return ClaimRuleUpdateResult.success(true, changedMessage(source, partyId, normalizedRule, enabled), stateFor(player));
    }

    public String statusMessage(CommandSourceStack source, String ruleId) {
        String normalizedRule = normalizeRule(ruleId);
        try {
            ServerPlayer player = source.getPlayerOrException();
            Optional<PartyId> party = partyService.getPlayerParty(player);
            if (party.isEmpty()) {
                return "You must be in a party.";
            }

            PartyId partyId = party.get();
            boolean enabled = currentValue(source, partyId, normalizedRule);
            return switch (normalizedRule) {
                case RULE_EXPLOSIONS -> "Explosion protection for " + partyLabel(source, partyId) + ": "
                        + (enabled ? "enabled" : "disabled") + cooldownSuffix(source, partyId, normalizedRule);
                case RULE_CREATE -> "Create machines for " + partyLabel(source, partyId) + ": "
                        + (enabled ? "allowed" : "blocked") + cooldownSuffix(source, partyId, normalizedRule);
                default -> "Unknown claim rule: " + ruleId;
            };
        } catch (Exception exception) {
            return "Only players can inspect party claim rules.";
        }
    }

    public ClaimRulesState currentState(CommandSourceStack source) {
        try {
            return stateFor(source.getPlayerOrException());
        } catch (Exception exception) {
            return ClaimRulesState.noParty(
                    permissionService.hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE),
                    permissionService.hasPermission(source, CustomClaimsPermissions.CREATE_TOGGLE)
            );
        }
    }

    public String partyLabel(CommandSourceStack source, PartyId partyId) {
        return partyService.describeParty(source.getServer(), partyId)
                .map(ClaimRulesService::formatParty)
                .orElse(partyId.toString());
    }

    public static String formatDuration(long seconds) {
        Duration duration = Duration.ofSeconds(Math.max(0L, seconds));
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long remainingSeconds = duration.toSecondsPart();
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0L) {
            return minutes + "m " + remainingSeconds + "s";
        }
        return remainingSeconds + "s";
    }

    private boolean currentValue(CommandSourceStack source, PartyId partyId, String ruleId) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> explosionProtectionService.isPartyExplosionProtectionEnabled(source.getServer(), partyId);
            case RULE_CREATE -> createMachinesProtectionService.isPartyCreateMachinesEnabled(source.getServer(), partyId);
            default -> false;
        };
    }

    private String cooldownSuffix(CommandSourceStack source, PartyId partyId, String ruleId) {
        if (cooldownService.isBypassed(source)) {
            return "";
        }

        long remaining = cooldownService.remainingSeconds(source.getServer(), partyId, ruleId, Instant.now());
        if (remaining <= 0L) {
            return "";
        }
        return " (toggle cooldown: " + formatDuration(remaining) + ")";
    }

    private String alreadyMessage(CommandSourceStack source, PartyId partyId, String ruleId, boolean enabled) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> "Explosion protection for " + partyLabel(source, partyId) + " is already "
                    + (enabled ? "enabled" : "disabled") + ".";
            case RULE_CREATE -> "Create machines for " + partyLabel(source, partyId) + " are already "
                    + (enabled ? "allowed" : "blocked") + ".";
            default -> "Claim rule is already set.";
        };
    }

    private String changedMessage(CommandSourceStack source, PartyId partyId, String ruleId, boolean enabled) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> "Explosion protection for " + partyLabel(source, partyId) + " is now "
                    + (enabled ? "enabled" : "disabled") + ".";
            case RULE_CREATE -> "Create machines for " + partyLabel(source, partyId) + " are now "
                    + (enabled ? "allowed" : "blocked") + ".";
            default -> "Claim rule updated.";
        };
    }

    private String normalizeRule(String ruleId) {
        return ruleId.toLowerCase(Locale.ROOT);
    }

    private static String formatParty(PartyDisplayInfo info) {
        return info.name() + " (owner: " + info.ownerName() + ")";
    }
}
