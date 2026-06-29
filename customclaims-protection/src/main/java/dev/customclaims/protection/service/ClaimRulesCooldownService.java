package dev.customclaims.protection.service;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.PermissionService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public final class ClaimRulesCooldownService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STORAGE_FILE = "protection/claimrule-toggle-cooldowns.txt";

    private final DataStorageService dataStorageService;
    private final PermissionService permissionService;
    private final Map<String, Instant> lastToggles = new ConcurrentHashMap<>();

    private MinecraftServer loadedServer;

    public ClaimRulesCooldownService(DataStorageService dataStorageService, PermissionService permissionService) {
        this.dataStorageService = dataStorageService;
        this.permissionService = permissionService;
    }

    public long remainingSeconds(MinecraftServer server, PartyId partyId, String ruleId, Instant now) {
        ensureLoaded(server);
        int cooldownSeconds = ProtectionConfig.CLAIMRULES_TOGGLE_COOLDOWN_SECONDS.get();
        if (cooldownSeconds <= 0) {
            return 0L;
        }

        Instant lastToggle = lastToggles.get(key(partyId, ruleId));
        if (lastToggle == null) {
            return 0L;
        }

        long elapsed = Duration.between(lastToggle, now).getSeconds();
        return Math.max(0L, cooldownSeconds - elapsed);
    }

    public boolean isBypassed(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }
        return permissionService.hasPermission(player, CustomClaimsPermissions.ADMIN);
    }

    public boolean isBypassed(ServerPlayer player) {
        return permissionService.hasPermission(player, CustomClaimsPermissions.ADMIN);
    }

    public void recordToggle(MinecraftServer server, PartyId partyId, String ruleId, Instant now) {
        ensureLoaded(server);
        lastToggles.put(key(partyId, ruleId), now);
        save(server);
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loadedServer == server) {
            return;
        }

        synchronized (this) {
            if (loadedServer == server) {
                return;
            }

            lastToggles.clear();
            for (String line : dataStorageService.readLines(server, STORAGE_FILE)) {
                parseStorageLine(line).ifPresent(entry -> lastToggles.put(entry.key(), entry.lastToggle()));
            }
            loadedServer = server;
        }
    }

    private Optional<StoredCooldown> parseStorageLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return Optional.empty();
        }

        String[] parts = trimmed.split("=", 2);
        if (parts.length != 2 || !parts[0].contains("|")) {
            LOGGER.warn("Ignoring malformed CustomClaims claimrules cooldown line: {}", line);
            return Optional.empty();
        }

        try {
            return Optional.of(new StoredCooldown(parts[0].trim(), Instant.ofEpochSecond(Long.parseLong(parts[1].trim()))));
        } catch (NumberFormatException exception) {
            LOGGER.warn("Ignoring CustomClaims claimrules cooldown line with invalid timestamp: {}", line);
            return Optional.empty();
        }
    }

    private void save(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        lastToggles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue().getEpochSecond())
                .forEach(lines::add);
        dataStorageService.writeLines(server, STORAGE_FILE, lines);
    }

    private String key(PartyId partyId, String ruleId) {
        return partyId.value() + "|" + ruleId;
    }

    private record StoredCooldown(String key, Instant lastToggle) {
    }
}
