package dev.customclaims.war.service;

import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public final class WarScoreboardService {
    private final Set<String> displayedEntries = new LinkedHashSet<>();
    private String lastObjectiveName;

    public void update(MinecraftServer server, Collection<WarData> wars) {
        if (!WarConfig.SCOREBOARD_SIDEBAR_ENABLED.get()) {
            clear(server);
            return;
        }

        Collection<WarData> activeWars = wars.stream()
                .filter(war -> war.state() == WarState.ACTIVE)
                .toList();
        if (activeWars.isEmpty()) {
            clear(server);
            return;
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = objective(scoreboard);
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);

        Set<String> currentEntries = new LinkedHashSet<>();
        for (WarData war : activeWars) {
            addLives(server, scoreboard, objective, currentEntries, "[A] ", war.attackerLives());
            addLives(server, scoreboard, objective, currentEntries, "[D] ", war.defenderLives());
        }

        for (String oldEntry : displayedEntries) {
            if (!currentEntries.contains(oldEntry)) {
                scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(oldEntry), objective);
            }
        }
        displayedEntries.clear();
        displayedEntries.addAll(currentEntries);
    }

    public void clear(MinecraftServer server) {
        String objectiveName = lastObjectiveName == null ? objectiveName() : lastObjectiveName;

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective != null) {
            if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == objective) {
                scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
            }
            scoreboard.removeObjective(objective);
        }
        displayedEntries.clear();
        lastObjectiveName = null;
    }

    private void addLives(
            MinecraftServer server,
            ServerScoreboard scoreboard,
            Objective objective,
            Set<String> currentEntries,
            String prefix,
            Map<UUID, Integer> lives
    ) {
        for (Map.Entry<UUID, Integer> entry : lives.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }

            String scoreboardName = prefix + player.getGameProfile().getName();
            currentEntries.add(scoreboardName);
            scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(scoreboardName), objective).set(entry.getValue());
        }
    }

    private Objective objective(ServerScoreboard scoreboard) {
        String objectiveName = objectiveName();
        if (lastObjectiveName != null && !lastObjectiveName.equals(objectiveName)) {
            Objective oldObjective = scoreboard.getObjective(lastObjectiveName);
            if (oldObjective != null) {
                scoreboard.removeObjective(oldObjective);
            }
            displayedEntries.clear();
        }
        lastObjectiveName = objectiveName;

        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective != null) {
            return objective;
        }
        return scoreboard.addObjective(
                objectiveName,
                ObjectiveCriteria.DUMMY,
                Component.literal("War Lives"),
                ObjectiveCriteria.RenderType.INTEGER,
                false,
                null
        );
    }

    private String objectiveName() {
        String configured = WarConfig.SCOREBOARD_OBJECTIVE.get().trim();
        if (configured.isBlank() || configured.length() > 16) {
            return "cc_war_lives";
        }
        return configured;
    }
}
