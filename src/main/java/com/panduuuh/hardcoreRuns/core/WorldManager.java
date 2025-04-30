package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.Random;

public class WorldManager {
    private final HardcoreRuns plugin;
    private final ConfigurationManager config;
    private final WorldCleanupService cleanupService;
    private final PlayerManager playerManager;
    private final TaskScheduler scheduler;
    private final BossBarManager bossBar;
    private final Logger logger;
    private final SharedInventoryManager sharedInventoryManager;
    private long runStartTime;
    private boolean resetPending = false;
    private String currentRunId;

    public WorldManager(HardcoreRuns plugin, ConfigurationManager config, PlayerManager playerManager,
                        TaskScheduler scheduler, BossBarManager bossBar, Logger logger,
                        SharedInventoryManager sharedInventoryManager) {
        this.plugin = plugin;
        this.config = config;
        this.cleanupService = new WorldCleanupService();
        this.playerManager = playerManager;
        this.scheduler = scheduler;
        this.bossBar = bossBar;
        this.logger = logger;
        this.sharedInventoryManager = sharedInventoryManager;
        loadPersistentState();
    }

    public void initializeWorlds() {
        String savedWorldName = config.getActiveWorld();
        logger.info("Attempting to load saved world: " + savedWorldName);

        if (savedWorldName == null || savedWorldName.isEmpty()) {
            logger.warning("No valid world found in config, creating new world");
            createNewWorld();
            return;
        }

        World activeWorld = Bukkit.getWorld(savedWorldName);
        if (activeWorld == null) {
            File worldFolder = new File(Bukkit.getWorldContainer(), savedWorldName);
            if (worldFolder.exists() && worldFolder.isDirectory()) {
                logger.info("Found world directory, loading world: " + savedWorldName);
                WorldCreator creator = new WorldCreator(savedWorldName);
                activeWorld = creator.createWorld();
            }
        }

        if (activeWorld == null) {
            logger.warning("Could not load world " + savedWorldName + ", creating new world");
            createNewWorld();
        } else {
            currentRunId = savedWorldName;
            activeWorld.setDifficulty(Difficulty.HARD);
            activeWorld.setHardcore(true);
            logger.info("Successfully loaded world: " + currentRunId);
        }

        runStartTime = System.currentTimeMillis();
    }

    private void loadPersistentState() {
        currentRunId = config.getActiveWorld();
        logger.info("Loaded run ID from config: " + currentRunId);

        runStartTime = System.currentTimeMillis();
    }

    public void cleanupOldRuns() {
        cleanupService.cleanupOldRuns();
    }

    public void resetWorld(Player initiator) {
        config.incrementAttempts();
        updateAllPlayerAttempts();

        config.setSharedHealth(20.0);
        config.setSharedFood(20);
        config.setSharedExp(0.0f);
        config.setSharedLevel(0);

        if (sharedInventoryManager != null) {
            sharedInventoryManager.reset();
        }

        config.save();

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

    private void updateAllPlayerAttempts() {
        int currentAttempt = config.getAttempts();
        Bukkit.getOnlinePlayers().forEach(player ->
                config.setPlayerAttempt(player.getUniqueId(), currentAttempt));
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