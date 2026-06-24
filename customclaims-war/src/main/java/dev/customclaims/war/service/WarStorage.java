package dev.customclaims.war.service;

import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.war.model.WarData;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public final class WarStorage {
    private static final String ACTIVE_WARS_FILE = "war/active-wars.dat";

    private final DataStorageService dataStorageService;

    public WarStorage(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    public List<WarData> load(MinecraftServer server) {
        return dataStorageService.readLines(server, ACTIVE_WARS_FILE).stream()
                .map(WarData::fromStorageLine)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    public void save(MinecraftServer server, Collection<WarData> wars) {
        dataStorageService.writeLines(
                server,
                ACTIVE_WARS_FILE,
                wars.stream()
                        .filter(war -> !war.isTerminal())
                        .map(WarData::toStorageLine)
                        .toList()
        );
    }
}
