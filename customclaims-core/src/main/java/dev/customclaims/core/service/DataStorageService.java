package dev.customclaims.core.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class DataStorageService {
    public List<String> readLines(MinecraftServer server, String relativeFile) {
        Path path = resolve(server, relativeFile);
        if (!Files.exists(path)) {
            return List.of();
        }

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read Custom Claims data file " + path, exception);
        }
    }

    public void writeLines(MinecraftServer server, String relativeFile, List<String> lines) {
        Path path = resolve(server, relativeFile);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write Custom Claims data file " + path, exception);
        }
    }

    public void appendLine(MinecraftServer server, String relativeFile, String line) {
        List<String> lines = new ArrayList<>(readLines(server, relativeFile));
        lines.add(line);
        writeLines(server, relativeFile, lines);
    }

    public Path resolve(MinecraftServer server, String relativeFile) {
        return server.getWorldPath(LevelResource.ROOT).resolve("customclaims").resolve(relativeFile);
    }
}
