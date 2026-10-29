package dev.customclaims.war.discord;

import com.mojang.logging.LogUtils;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;

public final class DiscordWebhookService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void sendWarDeclared(WarData war, String attackerName, String defenderName) {
        String url = WarConfig.DISCORD_WEBHOOK_URL.get();
        if (url == null || url.isBlank()) {
            return;
        }

        boolean ru = "ru_ru".equalsIgnoreCase(WarConfig.DISCORD_WEBHOOK_LANGUAGE.get());
        String title = ru ? "⚔️ Объявление осады" : "⚔️ Siege Declared";
        String description = ru
                ? "Началась подготовка к осаде сектора!"
                : "Sector siege preparation has begun!";

        int color = 0xF39C12; // Orange / Amber
        String json = buildEmbedJson(title, description, color, war, attackerName, defenderName, ru, null);
        postJson(url, json);
    }

    public static void sendWarActive(WarData war, String attackerName, String defenderName) {
        String url = WarConfig.DISCORD_WEBHOOK_URL.get();
        if (url == null || url.isBlank()) {
            return;
        }

        boolean ru = "ru_ru".equalsIgnoreCase(WarConfig.DISCORD_WEBHOOK_LANGUAGE.get());
        String title = ru ? "🔥 Штурм сектора начался!" : "🔥 Sector Assault Active!";
        String description = ru
                ? "Зона боевых действий активна! Защита снята, открыт огонь орудий CBC и буров Create."
                : "Combat zone active! Claims contested, cannon fire and drills enabled.";

        int color = 0xE74C3C; // Crimson Red
        String json = buildEmbedJson(title, description, color, war, attackerName, defenderName, ru, null);
        postJson(url, json);
    }

    public static void sendWarEnded(WarData war, String attackerName, String defenderName) {
        String url = WarConfig.DISCORD_WEBHOOK_URL.get();
        if (url == null || url.isBlank()) {
            return;
        }

        boolean ru = "ru_ru".equalsIgnoreCase(WarConfig.DISCORD_WEBHOOK_LANGUAGE.get());
        boolean attackerWon = war.progress() >= 100.0D;

        String title;
        String outcome;
        int color;
        if (attackerWon) {
            title = ru ? "🏆 Победа Атакующих!" : "🏆 Attackers Victorious!";
            outcome = ru
                    ? "Атакующие успешно захватили весь осадный сектор!"
                    : "Attackers have conquered the entire siege sector!";
            color = 0x2ECC71; // Emerald Green
        } else {
            title = ru ? "🛡️ Защитники отстояли сектор!" : "🛡️ Defenders Held the Line!";
            outcome = ru
                    ? "Осада отбита, сектор остаётся под защитой клана!"
                    : "Siege repelled, sector remains protected!";
            color = 0x3498DB; // Blue
        }

        String json = buildEmbedJson(title, outcome, color, war, attackerName, defenderName, ru, war.endReason());
        postJson(url, json);
    }

    private static String buildEmbedJson(
            String title,
            String description,
            int color,
            WarData war,
            String attackerName,
            String defenderName,
            boolean ru,
            String endReason
    ) {
        String fieldAttacker = ru ? "⚔️ Атака" : "⚔️ Attackers";
        String fieldDefender = ru ? "🛡️ Оборона" : "🛡️ Defenders";
        String fieldSector = ru ? "📍 Сектор" : "📍 Sector";
        String sectorVal = war.sectorChunks().size() + (ru ? " чанков (" : " chunks (") + war.targetChunk().levelId() + ")";

        StringBuilder fields = new StringBuilder();
        fields.append(String.format("{\"name\": \"%s\", \"value\": \"%s\", \"inline\": true},", escape(fieldAttacker), escape(attackerName)));
        fields.append(String.format("{\"name\": \"%s\", \"value\": \"%s\", \"inline\": true},", escape(fieldDefender), escape(defenderName)));
        fields.append(String.format("{\"name\": \"%s\", \"value\": \"%s\", \"inline\": false}", escape(fieldSector), escape(sectorVal)));

        if (endReason != null && !endReason.isBlank()) {
            String fieldReason = ru ? "Причина" : "Reason";
            fields.append(String.format(",{\"name\": \"%s\", \"value\": \"%s\", \"inline\": true}", escape(fieldReason), escape(endReason)));
        }

        return String.format(
                "{\"embeds\": [{"
                        + "\"title\": \"%s\","
                        + "\"description\": \"%s\","
                        + "\"color\": %d,"
                        + "\"fields\": [%s],"
                        + "\"footer\": {\"text\": \"OPaC Warfare 2.0\"},"
                        + "\"timestamp\": \"%s\""
                        + "}]}",
                escape(title),
                escape(description),
                color,
                fields,
                Instant.now().toString()
        );
    }

    private static void postJson(String url, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .whenComplete((response, throwable) -> {
                        if (throwable != null) {
                            LOGGER.warn("Failed to send Discord webhook: {}", throwable.getMessage());
                        } else if (response.statusCode() >= 400) {
                            LOGGER.warn("Discord webhook returned HTTP status {}", response.statusCode());
                        }
                    });
        } catch (Exception exception) {
            LOGGER.warn("Error initiating Discord webhook POST: {}", exception.getMessage());
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private DiscordWebhookService() {
    }
}
