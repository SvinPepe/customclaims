package dev.customclaims.core.log;

import dev.customclaims.core.service.DataStorageService;
import java.time.Instant;
import net.minecraft.server.MinecraftServer;

public final class ActionLogService {
    private static final String ACTION_LOG = "logs/actions.log";

    private final DataStorageService dataStorageService;

    public ActionLogService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    public void log(MinecraftServer server, String message) {
        dataStorageService.appendLine(server, ACTION_LOG, Instant.now() + " " + message);
    }
}
