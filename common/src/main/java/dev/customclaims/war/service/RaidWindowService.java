package dev.customclaims.war.service;

import dev.customclaims.war.config.WarConfig;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public final class RaidWindowService {
    public boolean isWarStartBlockedNow() {
        if (!WarConfig.ENABLE_RAID_WINDOW.get()) {
            return false;
        }

        ZoneId zoneId = ZoneId.of(WarConfig.RAID_WINDOW_TIMEZONE.get());
        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime();
        return WarConfig.BLOCKED_WINDOWS.get().stream()
                .map(this::parseWindow)
                .flatMap(Optional::stream)
                .anyMatch(window -> window.contains(now));
    }

    private Optional<BlockedWindow> parseWindow(String raw) {
        String[] parts = raw.split("-", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BlockedWindow(LocalTime.parse(parts[0]), LocalTime.parse(parts[1])));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record BlockedWindow(LocalTime start, LocalTime end) {
        boolean contains(LocalTime time) {
            if (start.equals(end)) {
                return true;
            }
            if (start.isBefore(end)) {
                return !time.isBefore(start) && time.isBefore(end);
            }
            return !time.isBefore(start) || time.isBefore(end);
        }
    }
}
