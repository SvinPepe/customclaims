package dev.customclaims.core.log;

import dev.customclaims.core.service.DataStorageService;
import java.time.Instant;
import net.minecraft.server.MinecraftServer;

public final class WarLogService {
    private static final String WAR_LOG = "logs/war.log";

    private final DataStorageService dataStorageService;

    public WarLogService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    public void log(MinecraftServer server, String message) {
        dataStorageService.appendLine(server, WAR_LOG, Instant.now() + " " + message);
    }
}
