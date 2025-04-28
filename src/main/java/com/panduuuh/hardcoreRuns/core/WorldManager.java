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
    private final TaskScheduler scheduler;
    private final BossBarManager bossBar;
    private long runStartTime;
    private boolean resetPending = false;

    public WorldManager(HardcoreRuns plugin, ConfigurationManager config, PlayerManager playerManager, TaskScheduler scheduler, BossBarManager bossBar) {
        this.plugin = plugin;
        this.config = config;
        this.cleanupService = new WorldCleanupService();
        this.playerManager = playerManager;
        this.scheduler = scheduler;
        this.bossBar = bossBar;
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
        scheduler.runTask(() -> {
            teleportPlayers(newWorld);
            cleanupService.cleanupOldRuns();
            bossBar.startTimer();
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
        scheduler.runTaskLater(() -> {
            Location spawn = newWorld.getSpawnLocation();
            spawn.getChunk().load();

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.teleport(spawn);
                playerManager.fullReset(player);

                // Metadata handling
                player.setMetadata("teleporting", new FixedMetadataValue(plugin, true));
                scheduler.runTaskLater(() ->
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

    public boolean isResetPending() {
        return resetPending;
    }

    public void setResetPending(boolean resetPending) {
        this.resetPending = resetPending;
    }
}