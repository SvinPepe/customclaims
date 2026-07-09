package dev.customclaims.war.service;

import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.war.config.WarConfig;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;

public final class DailyWarStartLimitService {
    private static final String DAILY_ATTACKER_STARTS_FILE = "war/daily-starts.dat";
    private static final String DAILY_ACCEPTED_STARTS_FILE = "war/daily-accepted-starts.dat";

    private final DataStorageService dataStorageService;

    public DailyWarStartLimitService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    public DailyStartLimitResult checkAttacker(MinecraftServer server, ClaimSideId attackerSide) {
        return check(
                server,
                DAILY_ATTACKER_STARTS_FILE,
                attackerSide,
                WarConfig.MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_DAY.get()
        );
    }

    public DailyStartLimitResult checkDefender(MinecraftServer server, ClaimSideId defenderSide) {
        return check(
                server,
                DAILY_ACCEPTED_STARTS_FILE,
                defenderSide,
                WarConfig.MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_DAY.get()
        );
    }

    public void recordStart(MinecraftServer server, ClaimSideId attackerSide, ClaimSideId defenderSide) {
        record(
                server,
                DAILY_ATTACKER_STARTS_FILE,
                attackerSide,
                WarConfig.MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_DAY.get()
        );
        record(
                server,
                DAILY_ACCEPTED_STARTS_FILE,
                defenderSide,
                WarConfig.MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_DAY.get()
        );
    }

    private DailyStartLimitResult check(MinecraftServer server, String file, ClaimSideId side, int limit) {
        LocalDate day = currentDay();
        if (limit <= 0) {
            return new DailyStartLimitResult(true, 0, limit, day);
        }

        int used = load(server, file, day).getOrDefault(side.storageKey(), 0);
        return new DailyStartLimitResult(used < limit, used, limit, day);
    }

    private void record(MinecraftServer server, String file, ClaimSideId side, int limit) {
        if (limit <= 0) {
            return;
        }

        LocalDate day = currentDay();
        Map<String, Integer> counts = load(server, file, day);
        counts.merge(side.storageKey(), 1, Integer::sum);
        save(server, file, day, counts);
    }

    private LocalDate currentDay() {
        ZoneId zoneId = ZoneId.of(WarConfig.RAID_WINDOW_TIMEZONE.get());
        return LocalDate.now(zoneId);
    }

    private Map<String, Integer> load(MinecraftServer server, String file, LocalDate currentDay) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String line : dataStorageService.readLines(server, file)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length != 3 || !parts[0].equals(currentDay.toString())) {
                continue;
            }
            try {
                String sideKey = decode(parts[1]);
                int count = Math.max(0, Integer.parseInt(parts[2]));
                counts.put(sideKey, count);
            } catch (RuntimeException ignored) {
                // Ignore malformed historic rows and rewrite only valid current-day rows on save.
            }
        }
        return counts;
    }

    private void save(MinecraftServer server, String file, LocalDate day, Map<String, Integer> counts) {
        List<String> lines = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> day + "|" + encode(entry.getKey()) + "|" + entry.getValue())
                .toList();
        dataStorageService.writeLines(server, file, lines);
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

    public record DailyStartLimitResult(boolean allowed, int used, int limit, LocalDate day) {
    }
}