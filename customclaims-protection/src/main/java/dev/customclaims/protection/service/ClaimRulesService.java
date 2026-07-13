package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.ClaimSideDisplayInfo;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.PartyService;
import dev.customclaims.core.service.PermissionService;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ClaimRulesService {
    public static final String RULE_EXPLOSIONS = "explosions";
    public static final String RULE_CREATE = "create";
    public static final String RULE_ASSEMBLY = "assembly";

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
        ensureAssemblyMigration(player.server);
        boolean canToggleExplosions = permissionService.hasPermission(player, CustomClaimsPermissions.EXPLOSIONS_TOGGLE);
        boolean canToggleCreate = permissionService.hasPermission(player, CustomClaimsPermissions.CREATE_TOGGLE);
        boolean canToggleAssembly = permissionService.hasPermission(player, CustomClaimsPermissions.ASSEMBLY_TOGGLE);

        Instant now = Instant.now();
        ClaimSideId sideId = partyService.getPlayerSide(player);
        boolean bypassed = cooldownService.isBypassed(player);
        return new ClaimRulesState(
                true,
                sideId,
                sideLabel(player.createCommandSourceStack(), sideId),
                explosionProtectionService.isExplosionProtectionEnabled(player.server, sideId),
                createMachinesProtectionService.isCreateMachinesEnabled(player.server, sideId),
                createMachinesProtectionService.isAssemblyEnabled(player.server, sideId),
                bypassed ? 0L : cooldownService.remainingSeconds(player.server, sideId, RULE_EXPLOSIONS, now),
                bypassed ? 0L : cooldownService.remainingSeconds(player.server, sideId, RULE_CREATE, now),
                bypassed ? 0L : cooldownService.remainingSeconds(player.server, sideId, RULE_ASSEMBLY, now),
                canToggleExplosions,
                canToggleCreate,
                canToggleAssembly
        );
    }

    public ClaimRuleUpdateResult setRule(CommandSourceStack source, String ruleId, boolean enabled) {
        String normalizedRule = normalizeRule(ruleId);
        String permission = switch (normalizedRule) {
            case RULE_EXPLOSIONS -> CustomClaimsPermissions.EXPLOSIONS_TOGGLE;
            case RULE_CREATE -> CustomClaimsPermissions.CREATE_TOGGLE;
            case RULE_ASSEMBLY -> CustomClaimsPermissions.ASSEMBLY_TOGGLE;
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
            return ClaimRuleUpdateResult.failure("Only players can manage claim rules.", currentState(source));
        }

        ensureAssemblyMigration(source.getServer());
        ClaimSideId sideId = partyService.getPlayerSide(player);
        boolean current = currentValue(source, sideId, normalizedRule);
        Instant now = Instant.now();
        if (!cooldownService.isBypassed(source)) {
            long remaining = cooldownService.remainingSeconds(source.getServer(), sideId, normalizedRule, now);
            if (remaining > 0L) {
                return ClaimRuleUpdateResult.failure(
                        "This claim rule was changed recently. Try again in " + formatDuration(remaining) + ".",
                        stateFor(player)
                );
            }
        }
        if (current == enabled) {
            return ClaimRuleUpdateResult.success(false, alreadyMessage(source, sideId, normalizedRule, enabled), stateFor(player));
        }

        boolean saved = switch (normalizedRule) {
            case RULE_EXPLOSIONS -> explosionProtectionService.setExplosionProtection(source.getServer(), sideId, enabled);
            case RULE_CREATE -> createMachinesProtectionService.setCreateMachinesEnabled(source.getServer(), sideId, enabled);
            case RULE_ASSEMBLY -> createMachinesProtectionService.setAssemblyEnabled(source.getServer(), sideId, enabled);
            default -> false;
        };
        if (!saved) {
            return ClaimRuleUpdateResult.failure("Failed to save claim rule for " + sideLabel(source, sideId) + ".", stateFor(player));
        }

        if (!cooldownService.isBypassed(source)) {
            cooldownService.recordToggle(source.getServer(), sideId, normalizedRule, now);
        }

        return ClaimRuleUpdateResult.success(true, changedMessage(source, sideId, normalizedRule, enabled), stateFor(player));
    }

    public String statusMessage(CommandSourceStack source, String ruleId) {
        String normalizedRule = normalizeRule(ruleId);
        try {
            ServerPlayer player = source.getPlayerOrException();
            ensureAssemblyMigration(source.getServer());
            ClaimSideId sideId = partyService.getPlayerSide(player);
            boolean enabled = currentValue(source, sideId, normalizedRule);
            return switch (normalizedRule) {
                case RULE_EXPLOSIONS -> "Explosion protection for " + sideLabel(source, sideId) + ": "
                        + (enabled ? "enabled" : "disabled") + cooldownSuffix(source, sideId, normalizedRule);
                case RULE_CREATE -> "Create and Offroad mining for " + sideLabel(source, sideId) + ": "
                        + (enabled ? "allowed" : "blocked") + cooldownSuffix(source, sideId, normalizedRule);
                case RULE_ASSEMBLY -> "Create and Sable assembly for " + sideLabel(source, sideId) + ": "
                        + (enabled ? "allowed" : "blocked") + cooldownSuffix(source, sideId, normalizedRule);
                default -> "Unknown claim rule: " + ruleId;
            };
        } catch (Exception exception) {
            return "Only players can inspect claim rules.";
        }
    }

    public ClaimRulesState currentState(CommandSourceStack source) {
        try {
            return stateFor(source.getPlayerOrException());
        } catch (Exception exception) {
            return ClaimRulesState.noSide(
                    permissionService.hasPermission(source, CustomClaimsPermissions.EXPLOSIONS_TOGGLE),
                    permissionService.hasPermission(source, CustomClaimsPermissions.CREATE_TOGGLE),
                    permissionService.hasPermission(source, CustomClaimsPermissions.ASSEMBLY_TOGGLE)
            );
        }
    }

    public String sideLabel(CommandSourceStack source, ClaimSideId sideId) {
        return partyService.describeSide(source.getServer(), sideId)
                .map(ClaimRulesService::formatSide)
                .orElse(sideId.shortLabel());
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

    private boolean currentValue(CommandSourceStack source, ClaimSideId sideId, String ruleId) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> explosionProtectionService.isExplosionProtectionEnabled(source.getServer(), sideId);
            case RULE_CREATE -> createMachinesProtectionService.isCreateMachinesEnabled(source.getServer(), sideId);
            case RULE_ASSEMBLY -> createMachinesProtectionService.isAssemblyEnabled(source.getServer(), sideId);
            default -> false;
        };
    }

    private void ensureAssemblyMigration(MinecraftServer server) {
        if (createMachinesProtectionService.consumeAssemblyCooldownMigration(server)) {
            cooldownService.copyRuleCooldowns(server, RULE_CREATE, RULE_ASSEMBLY);
        }
    }

    private String cooldownSuffix(CommandSourceStack source, ClaimSideId sideId, String ruleId) {
        if (cooldownService.isBypassed(source)) {
            return "";
        }

        long remaining = cooldownService.remainingSeconds(source.getServer(), sideId, ruleId, Instant.now());
        if (remaining <= 0L) {
            return "";
        }
        return " (toggle cooldown: " + formatDuration(remaining) + ")";
    }

    private String alreadyMessage(CommandSourceStack source, ClaimSideId sideId, String ruleId, boolean enabled) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> "Explosion protection for " + sideLabel(source, sideId) + " is already "
                    + (enabled ? "enabled" : "disabled") + ".";
            case RULE_CREATE -> "Create and Offroad mining for " + sideLabel(source, sideId) + " is already "
                    + (enabled ? "allowed" : "blocked") + ".";
            case RULE_ASSEMBLY -> "Create and Sable assembly for " + sideLabel(source, sideId) + " is already "
                    + (enabled ? "allowed" : "blocked") + ".";
            default -> "Claim rule is already set.";
        };
    }

    private String changedMessage(CommandSourceStack source, ClaimSideId sideId, String ruleId, boolean enabled) {
        return switch (ruleId) {
            case RULE_EXPLOSIONS -> "Explosion protection for " + sideLabel(source, sideId) + " is now "
                    + (enabled ? "enabled" : "disabled") + ".";
            case RULE_CREATE -> "Create and Offroad mining for " + sideLabel(source, sideId) + " is now "
                    + (enabled ? "allowed" : "blocked") + ".";
            case RULE_ASSEMBLY -> "Create and Sable assembly for " + sideLabel(source, sideId) + " is now "
                    + (enabled ? "allowed" : "blocked") + ".";
            default -> "Claim rule updated.";
        };
    }

    private String normalizeRule(String ruleId) {
        return ruleId.toLowerCase(Locale.ROOT);
    }

    private static String formatSide(ClaimSideDisplayInfo info) {
        String prefix = info.id().isParty() ? "Nation: " : "Personal claims: ";
        return prefix + info.name() + " (owner: " + info.ownerName() + ")";
    }
}
