package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerManager {
    private final Set<UUID> processingDamage = new HashSet<>();
    private final ConfigurationManager config;
    private final HardcoreRuns plugin;
    private final TaskScheduler scheduler;
    private final Logger logger;
    private final TotemService totemService;
    private final SharedInventoryManager sharedInventoryManager;
    private WorldManager worldManager;

    public PlayerManager(HardcoreRuns plugin, ConfigurationManager config, TaskScheduler scheduler, Logger logger, SharedInventoryManager sharedInventoryManager) {
        this.plugin = plugin;
        this.config = config;
        this.scheduler = scheduler;
        this.logger = logger;
        this.totemService = new TotemService();
        this.sharedInventoryManager = sharedInventoryManager;
    }

    public void handleDamage(Player source, double damage) {
        if (processingDamage.contains(source.getUniqueId())) return;

        processingDamage.add(source.getUniqueId());
        propagateHealthChange(source, source.getHealth() - damage); // Track net health
        processingDamage.remove(source.getUniqueId());
    }

    public void handleHealing(Player source, double newHealth) {
        if (processingDamage.contains(source.getUniqueId())) return;

        processingDamage.add(source.getUniqueId());
        propagateHealthChange(source, newHealth);
        processingDamage.remove(source.getUniqueId());
    }

    private void propagateHealthChange(Player source, double newHealth) {
        if (!config.isHealthShared()) return;
        double clampedHealth = Math.max(0.0, Math.min(20.0, newHealth));
        config.setSharedHealth(clampedHealth);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != source)
                .forEach(p -> {
                    scheduler.runTask(() -> {
                        if (processingDamage.contains(p.getUniqueId())) return;
                        processingDamage.add(p.getUniqueId());
                        p.setHealth(clampedHealth);
                        processingDamage.remove(p.getUniqueId());
                    });
                });
    }

    public void syncNewPlayer(Player newPlayer) {
        if (newPlayer == null) {
            logger.warning("Attempted to sync null player");
            return;
        }
        
        UUID playerId = newPlayer.getUniqueId();
        int currentAttempt = config.getAttempts();
        int lastAttempt = config.getPlayerAttempt(playerId);

        logger.info("Player " + newPlayer.getName() + " joining - Current attempt: " +
                currentAttempt + ", Last player attempt: " + lastAttempt);

        if (currentAttempt != lastAttempt) {
            fullReset(newPlayer);
            config.setPlayerAttempt(playerId, currentAttempt);
            newPlayer.setMetadata("hardcore_attempt",
                    new FixedMetadataValue(plugin, currentAttempt));
            logger.info("Updated player " + newPlayer.getName() + " to attempt #" + currentAttempt);

            World targetWorld = Bukkit.getWorld(worldManager.getCurrentRunId());
            if (targetWorld == null) {
                targetWorld = worldManager.createNewWorld();
            }
            newPlayer.teleport(targetWorld.getSpawnLocation());
        } else {
            logger.info("Player " + newPlayer.getName() + " already on current attempt #" + currentAttempt);
            World currentWorld = newPlayer.getWorld();
            String currentRunId = worldManager.getCurrentRunId();
            if (!currentWorld.getName().equals(currentRunId)) {
                World targetWorld = Bukkit.getWorld(currentRunId);
                if (targetWorld != null) {
                    newPlayer.teleport(targetWorld.getSpawnLocation());
                } else {
                    logger.warning("Current run world not found: " + currentRunId);
                }
            }
        }

        if (Bukkit.getOnlinePlayers().size() == 1) {
            if (config.isHealthShared()) newPlayer.setHealth(config.getSharedHealth());
            if (config.isFoodShared()) newPlayer.setFoodLevel(config.getSharedFood());
            if (config.isExpShared()) newPlayer.setExp(config.getSharedExp());
            if (config.isLevelShared()) newPlayer.setLevel(config.getSharedLevel());
            if (config.isInventoryShared()) {
                sharedInventoryManager.syncPlayerWithSharedInventory(newPlayer);
            }
        } else {
            Player existing = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p != newPlayer)
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                syncToExistingPlayer(newPlayer, existing);
            }
        }
    }

    private void syncToExistingPlayer(Player newPlayer, Player existing) {
        if (newPlayer == null || existing == null) {
            logger.warning("Cannot sync players: one or both players are null");
            return;
        }
        
        if (config.isHealthShared()) newPlayer.setHealth(existing.getHealth());
        if (config.isFoodShared()) newPlayer.setFoodLevel(existing.getFoodLevel());
        if (config.isExpShared()) newPlayer.setExp(existing.getExp());
        if (config.isLevelShared()) newPlayer.setLevel(existing.getLevel());
        if (config.isInventoryShared()) {
            sharedInventoryManager.syncPlayerWithSharedInventory(newPlayer);
        }
    }

    public void fullReset(Player player) {
        if (player == null) {
            logger.warning("Attempted to reset null player");
            return;
        }
        
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.getActivePotionEffects().forEach(e ->
                player.removePotionEffect(e.getType()));
    }

    public boolean isProcessingDamage(UUID playerId) {
        return processingDamage.contains(playerId);
    }

    private void clearNegativeEffects(Player player) {
        player.getActivePotionEffects()
                .forEach(e -> player.removePotionEffect(e.getType()));
    }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public HardcoreRuns getPlugin() {
        return plugin;
    }

    public ConfigurationManager getConfig() {
        return config;
    }
}