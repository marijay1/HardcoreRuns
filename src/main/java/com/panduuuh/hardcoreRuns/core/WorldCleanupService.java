package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorldCleanupService {
    private final HardcoreRuns plugin;

    public WorldCleanupService(HardcoreRuns plugin) {
        this.plugin = plugin;
    }

    // Renamed and modified cleanup method
    public void cleanupOldRuns() {
        File worldContainer = Bukkit.getWorldContainer();
        List<File> runWorlds = getSortedRunWorlds(worldContainer);

        // Keep only the 2 most recent worlds
        if (runWorlds.size() > 2) {
            List<File> toDelete = runWorlds.subList(0, runWorlds.size() - 2);
            toDelete.forEach(this::deleteWorld);
        }
    }

    private List<File> getSortedRunWorlds(File worldContainer) {
        return Arrays.stream(worldContainer.listFiles())
                .filter(f -> f.getName().startsWith("run_"))
                .sorted((f1, f2) -> {
                    long t1 = Long.parseLong(f1.getName().substring(4));
                    long t2 = Long.parseLong(f2.getName().substring(4));
                    return Long.compare(t1, t2); // Oldest first
                })
                .collect(Collectors.toList());
    }

    private void deleteWorld(File worldFolder) {
        World world = Bukkit.getWorld(worldFolder.getName());
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }

        deleteDirectory(worldFolder);
    }

    private void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                file.delete();
            }
        }
        directory.delete();
    }
}