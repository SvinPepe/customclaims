package dev.customclaims.core.service;

import dev.customclaims.core.CustomClaimsCoreMod;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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

    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
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

    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long now = System.currentTimeMillis();
        if (serverId == null || now < nextHeartbeatAt || !config.metricsEnabled()) {
            return;
        }

        nextHeartbeatAt = now + HEARTBEAT_INTERVAL_MILLIS;
        sendHeartbeat(server);
    }

    public void onServerStopping(ServerStoppingEvent event) {
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
                return UUID.fromString(lines.getFirst().trim());
            } catch (IllegalArgumentException exception) {
                logFailureSparingly("Stored metrics server id is invalid; replacing it", exception);
            }
        }

        UUID id = UUID.randomUUID();
        storage.writeLines(server, SERVER_ID_FILE, List.of(id.toString()));
        return id;
    }

    private void sendHeartbeat(MinecraftServer server) {
        String endpoint = config.metricsEndpoint().trim();
        if (endpoint.isEmpty() || httpClient == null) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload(server), StandardCharsets.UTF_8))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .whenComplete((response, failure) -> {
                        if (failure != null) {
                            logFailureSparingly("Metrics heartbeat failed", failure);
                        } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                            logFailureSparingly("Metrics endpoint returned HTTP " + response.statusCode(), null);
                        }
                    });
        } catch (RuntimeException exception) {
            logFailureSparingly("Metrics heartbeat could not be prepared", exception);
        }
    }

    private String payload(MinecraftServer server) {
        ModList mods = ModList.get();
        return "{" +
                "\"server_id\":\"" + serverId + "\"," +
                "\"customclaims_version\":\"" + json(versionOf("customclaims_core")) + "\"," +
                "\"minecraft_version\":\"" + json(versionOf("minecraft")) + "\"," +
                "\"neoforge_version\":\"" + json(versionOf("neoforge")) + "\"," +
                "\"players_online\":" + server.getPlayerCount() + "," +
                "\"compatible_mods\":{" +
                "\"opac\":" + mods.isLoaded("openpartiesandclaims") + "," +
                "\"create\":" + mods.isLoaded("create") + "," +
                "\"cbc\":" + mods.isLoaded("createbigcannons") + "," +
                "\"aeronautics_offroad\":" + (mods.isLoaded("aeronautics") || mods.isLoaded("offroad")) + "," +
                "\"xaero\":" + (mods.isLoaded("xaerominimap") || mods.isLoaded("xaeroworldmap")) + "," +
                "\"corpse\":" + mods.isLoaded("corpse") +
                "}}";
    }

    private String versionOf(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
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
