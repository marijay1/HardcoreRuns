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
    private final Logger logger;
    private long runStartTime;
    private boolean resetPending = false;
    private String currentRunId;

    public WorldManager(HardcoreRuns plugin, ConfigurationManager config, PlayerManager playerManager, TaskScheduler scheduler, BossBarManager bossBar, Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.cleanupService = new WorldCleanupService();
        this.playerManager = playerManager;
        this.scheduler = scheduler;
        this.bossBar = bossBar;
        this.logger = logger;
        loadPersistentState();
    }

    public void initializeWorlds() {
        World activeWorld = Bukkit.getWorld(config.getActiveWorld());
        if (activeWorld == null) {
            createNewWorld();
        } else {
            activeWorld.setDifficulty(Difficulty.HARD);
            activeWorld.setHardcore(true);
        }
        runStartTime = System.currentTimeMillis();
    }

    private void loadPersistentState() {
        currentRunId = config.getActiveWorld();
        if (Bukkit.getWorld(currentRunId) == null) {
            createNewWorld();
        }
        runStartTime = System.currentTimeMillis();
    }

    public void cleanupOldRuns() {
        cleanupService.cleanupOldRuns();
    }

    public void resetWorld(Player initiator) {
        config.incrementAttempts();

        initiator.sendMessage(ChatColor.GREEN + "World generation started...");
        World newWorld = createNewWorld();

        scheduler.runTask(() -> {
            teleportPlayers(newWorld);
            cleanupService.cleanupOldRuns();
            bossBar.startTimer();
            initiator.sendMessage(ChatColor.GREEN + "World reset complete!");
        });
    }

    public World createNewWorld() {
        try {
            currentRunId = "run_" + System.currentTimeMillis();

            WorldCreator wc = new WorldCreator(currentRunId)
                    .seed(new Random().nextLong())
                    .type(WorldType.NORMAL)
                    .environment(World.Environment.NORMAL)
                    .hardcore(true);

            config.setActiveWorld(currentRunId);
            return wc.createWorld();
        } catch (Exception e) {
            logger.severe("Failed to create new world: " + e.getMessage());
            throw new RuntimeException("World creation failed", e);
        }
    }

    private void teleportPlayers(World newWorld) {
        scheduler.runTaskLater(() -> {
            Location spawn = newWorld.getSpawnLocation();
            spawn.getChunk().load();

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.setMetadata("hardcore_run",
                        new FixedMetadataValue(plugin, currentRunId));
                playerManager.fullReset(player);
                player.teleport(spawn);
            });
        }, 20L);
    }

    public String getCurrentRunId() {
        return currentRunId;
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