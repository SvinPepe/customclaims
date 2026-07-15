package dev.customclaims.war.service;

import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.war.config.WarConfig;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;

/** Persists fixed attack and defense cooldown windows for every claim side. */
public final class WarCooldownService {
    private static final String SIDE_COOLDOWNS_FILE = "war/side-cooldowns.dat";

    private final DataStorageService dataStorageService;

    public WarCooldownService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    public CooldownResult checkAttacker(MinecraftServer server, ClaimSideId attackerSide) {
        return check(server, CooldownRole.ATTACKER, attackerSide, WarConfig.ATTACKER_COOLDOWN_SECONDS.get(),
                WarConfig.MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_COOLDOWN.get());
    }

    public CooldownResult checkDefender(MinecraftServer server, ClaimSideId defenderSide) {
        return check(server, CooldownRole.DEFENDER, defenderSide, WarConfig.DEFENDER_COOLDOWN_SECONDS.get(),
                WarConfig.MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_COOLDOWN.get());
    }

    public void recordStart(
            MinecraftServer server,
            ClaimSideId attackerSide,
            ClaimSideId defenderSide,
            Instant declaredAt
    ) {
        Map<WindowKey, CooldownWindow> windows = load(server);
        boolean changed = record(windows, CooldownRole.ATTACKER, attackerSide, WarConfig.ATTACKER_COOLDOWN_SECONDS.get(), declaredAt);
        changed |= record(windows, CooldownRole.DEFENDER, defenderSide, WarConfig.DEFENDER_COOLDOWN_SECONDS.get(), declaredAt);
        if (changed) {
            save(server, windows);
        }
    }

    private CooldownResult check(
            MinecraftServer server,
            CooldownRole role,
            ClaimSideId side,
            int cooldownSeconds,
            int limit
    ) {
        if (cooldownSeconds <= 0) {
            return new CooldownResult(true, 0, limit, 0L);
        }

        Instant now = Instant.now();
        Optional<CooldownWindow> window = currentWindow(load(server), role, side, cooldownSeconds, now);
        if (window.isEmpty()) {
            return new CooldownResult(true, 0, limit, 0L);
        }

        CooldownWindow value = window.get();
        long remainingSeconds = Math.max(1L, Duration.between(now, value.startedAt().plusSeconds(cooldownSeconds)).toSeconds());
        return new CooldownResult(value.used() < limit, value.used(), limit, remainingSeconds);
    }

    private boolean record(
            Map<WindowKey, CooldownWindow> windows,
            CooldownRole role,
            ClaimSideId side,
            int cooldownSeconds,
            Instant now
    ) {
        if (cooldownSeconds <= 0) {
            return false;
        }

        WindowKey key = new WindowKey(role, side.storageKey());
        Optional<CooldownWindow> existing = currentWindow(windows, role, side, cooldownSeconds, now);
        windows.put(key, existing
                .map(window -> new CooldownWindow(window.startedAt(), window.used() + 1))
                .orElseGet(() -> new CooldownWindow(now, 1)));
        return true;
    }

    private Optional<CooldownWindow> currentWindow(
            Map<WindowKey, CooldownWindow> windows,
            CooldownRole role,
            ClaimSideId side,
            int cooldownSeconds,
            Instant now
    ) {
        CooldownWindow window = windows.get(new WindowKey(role, side.storageKey()));
        if (window == null || !now.isBefore(window.startedAt().plusSeconds(cooldownSeconds))) {
            return Optional.empty();
        }
        return Optional.of(window);
    }

    private Map<WindowKey, CooldownWindow> load(MinecraftServer server) {
        Map<WindowKey, CooldownWindow> windows = new LinkedHashMap<>();
        for (String line : dataStorageService.readLines(server, SIDE_COOLDOWNS_FILE)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length != 4) {
                continue;
            }
            try {
                CooldownRole role = CooldownRole.fromStorageKey(parts[0]);
                String sideKey = decode(parts[1]);
                Instant startedAt = Instant.ofEpochMilli(Long.parseLong(parts[2]));
                int used = Math.max(0, Integer.parseInt(parts[3]));
                if (used > 0) {
                    windows.put(new WindowKey(role, sideKey), new CooldownWindow(startedAt, used));
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed historic rows and rewrite valid rows after the next successful start.
            }
        }
        return windows;
    }

    private void save(MinecraftServer server, Map<WindowKey, CooldownWindow> windows) {
        List<String> lines = windows.entrySet().stream()
                .filter(entry -> entry.getValue().used() > 0)
                .map(entry -> entry.getKey().role().storageKey()
                        + "|" + encode(entry.getKey().sideKey())
                        + "|" + entry.getValue().startedAt().toEpochMilli()
                        + "|" + entry.getValue().used())
                .toList();
        dataStorageService.writeLines(server, SIDE_COOLDOWNS_FILE, lines);
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public record CooldownResult(boolean allowed, int used, int limit, long remainingSeconds) {
    }

    private record WindowKey(CooldownRole role, String sideKey) {
    }

    private record CooldownWindow(Instant startedAt, int used) {
    }

    private enum CooldownRole {
        ATTACKER("attacker"),
        DEFENDER("defender");

        private final String storageKey;

        CooldownRole(String storageKey) {
            this.storageKey = storageKey;
        }

        private String storageKey() {
            return storageKey;
        }

        private static CooldownRole fromStorageKey(String key) {
            for (CooldownRole role : values()) {
                if (role.storageKey.equals(key)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown cooldown role: " + key);
        }
    }
}
