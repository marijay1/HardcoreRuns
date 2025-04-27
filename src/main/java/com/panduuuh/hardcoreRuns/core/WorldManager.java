package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

public class WorldManager {
    private final HardcoreRuns plugin;
    private final ConfigurationManager config;
    private final WorldCleanupService cleanupService;
    private final PlayerManager playerManager;
    private long runStartTime;

    public WorldManager(HardcoreRuns plugin, ConfigurationManager config, PlayerManager playerManager) {
        this.plugin = plugin;
        this.config = config;
        this.cleanupService = new WorldCleanupService(plugin);
        this.playerManager = playerManager;
    }

    public void initializeWorlds() {
        Bukkit.getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.HARD);
            world.setHardcore(true);
        });
        runStartTime = System.currentTimeMillis();
    }

    public void cleanupOldRuns() {
        cleanupService.cleanupOldRuns();
    }

    public void resetWorld(Player initiator) {
        // Remove async task wrapper for world creation
        World newWorld = createNewWorld();

        // Handle async-safe operations
        Bukkit.getScheduler().runTask(plugin, () -> {
            teleportPlayers(newWorld);
            cleanupService.cleanupOldRuns();
            resetRunTimer();
        });
    }

    private World createNewWorld() {
        WorldCreator wc = new WorldCreator("run_" + System.currentTimeMillis())
                .seed(new Random().nextLong())
                .type(WorldType.NORMAL)
                .environment(World.Environment.NORMAL)
                .hardcore(true);

        // Explicitly set world border first to prevent async issues
        wc.generateStructures(true);
        return wc.createWorld();
    }

    private void teleportPlayers(World newWorld) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Location spawn = newWorld.getSpawnLocation();
            spawn.getChunk().load();

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.teleport(spawn);
                playerManager.fullReset(player);

                // Metadata handling
                player.setMetadata("teleporting", new FixedMetadataValue(plugin, true));
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        player.removeMetadata("teleporting", plugin), 20L);
            });
        }, 20L);
    }

    public void incrementAttempt() {
        config.incrementAttempts();
    }

    public long getRunStartTime() {
        return runStartTime;
    }

    private void resetRunTimer() {
        runStartTime = System.currentTimeMillis();
    }
}