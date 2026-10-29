package dev.customclaims.core.service;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.platform.PlatformHelperHolder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.server.MinecraftServer;

/** Sends the deliberately small, anonymous dedicated-server heartbeat. */
public final class MetricsService {
    private static final String SERVER_ID_FILE = "metrics/server-id.txt";
    private static final long HEARTBEAT_INTERVAL_MILLIS = Duration.ofMinutes(30).toMillis();
    private static final long ERROR_LOG_INTERVAL_MILLIS = Duration.ofHours(6).toMillis();

    private final ConfigManager config;
    private final DataStorageService storage;
    private ExecutorService executor;
    private HttpClient httpClient;
    private UUID serverId;
    private long nextHeartbeatAt;
    private long nextErrorLogAt;

    public MetricsService(ConfigManager config, DataStorageService storage) {
        this.config = config;
        this.storage = storage;
    }

    public void onServerStarted(MinecraftServer server) {
        if (!server.isDedicatedServer() || !config.metricsEnabled()) {
            return;
        }

        try {
            serverId = loadOrCreateServerId(server);
        } catch (RuntimeException exception) {
            logFailureSparingly("Metrics server id could not be loaded or saved", exception);
            return;
        }
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "customclaims-metrics");
            thread.setDaemon(true);
            return thread;
        });
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .executor(executor)
                .build();
        nextHeartbeatAt = 0;
    }

    public void onServerTick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        if (serverId == null || now < nextHeartbeatAt || !config.metricsEnabled()) {
            return;
        }

        nextHeartbeatAt = now + HEARTBEAT_INTERVAL_MILLIS;
        sendHeartbeat(server);
    }

    public void onServerStopping(MinecraftServer server) {
        serverId = null;
        httpClient = null;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private UUID loadOrCreateServerId(MinecraftServer server) {
        List<String> lines = storage.readLines(server, SERVER_ID_FILE);
        if (!lines.isEmpty()) {
            try {
                return UUID.fromString(lines.get(0).trim());
            } catch (IllegalArgumentException ignored) {
            }
        }

        UUID generated = UUID.randomUUID();
        storage.writeLines(server, SERVER_ID_FILE, List.of(generated.toString()));
        return generated;
    }

    private void sendHeartbeat(MinecraftServer server) {
        if (httpClient == null || serverId == null) {
            return;
        }

        String endpoint = config.metricsEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "CustomClaims-Warfare/" + versionOf("opac_warfare"))
                    .POST(HttpRequest.BodyPublishers.ofString(payload(server), StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .whenComplete((response, failure) -> {
                        if (failure != null) {
                            logFailureSparingly("Failed to send metrics heartbeat", failure);
                        }
                    });
        } catch (IllegalArgumentException | SecurityException exception) {
            logFailureSparingly("Invalid metrics configuration or request", exception);
        }
    }

    private String payload(MinecraftServer server) {
        return "{" +
                "\"server_id\":\"" + serverId + "\"," +
                "\"customclaims_version\":\"" + json(versionOf("opac_warfare")) + "\"," +
                "\"minecraft_version\":\"" + json(versionOf("minecraft")) + "\"," +
                "\"platform\":\"" + json(PlatformHelperHolder.getPlatform().getPlatformName()) + "\"," +
                "\"players_online\":" + server.getPlayerCount() + "," +
                "\"compatible_mods\":{" +
                "\"opac\":" + PlatformHelperHolder.getPlatform().isModLoaded("openpartiesandclaims") + "," +
                "\"create\":" + PlatformHelperHolder.getPlatform().isModLoaded("create") + "," +
                "\"cbc\":" + PlatformHelperHolder.getPlatform().isModLoaded("createbigcannons") + "," +
                "\"aeronautics_offroad\":" + (PlatformHelperHolder.getPlatform().isModLoaded("aeronautics") || PlatformHelperHolder.getPlatform().isModLoaded("offroad")) + "," +
                "\"xaero\":" + (PlatformHelperHolder.getPlatform().isModLoaded("xaerominimap") || PlatformHelperHolder.getPlatform().isModLoaded("xaeroworldmap")) + "," +
                "\"corpse\":" + PlatformHelperHolder.getPlatform().isModLoaded("corpse") +
                "}}";
    }

    private String versionOf(String modId) {
        return PlatformHelperHolder.getPlatform().getModVersion(modId);
    }

    private String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private synchronized void logFailureSparingly(String message, Throwable failure) {
        long now = System.currentTimeMillis();
        if (now < nextErrorLogAt) {
            return;
        }
        nextErrorLogAt = now + ERROR_LOG_INTERVAL_MILLIS;
        if (failure == null) {
            CustomClaimsCoreMod.LOGGER.warn("{}; telemetry will retry later", message);
        } else {
            CustomClaimsCoreMod.LOGGER.warn("{}; telemetry will retry later: {}", message, failure.toString());
        }
    }
}
