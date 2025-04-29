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
    private WorldManager worldManager;

    public PlayerManager(HardcoreRuns plugin, ConfigurationManager config, TaskScheduler scheduler, Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.scheduler = scheduler;
        this.logger = logger;
        this.totemService = new TotemService();
    }

    public void handleDamage(Player source, double damage) {
        if (processingDamage.contains(source.getUniqueId())) return;

        processingDamage.add(source.getUniqueId());
        propagateDamage(source, damage);
        processingDamage.remove(source.getUniqueId());
    }

    private void propagateDamage(Player source, double damage) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != source)
                .forEach(p -> {
                    scheduler.runTask(() -> {
                        applySyncedDamage(p, damage);
                    });
                });
    }

    private void applySyncedDamage(Player target, double damage) {
        if (processingDamage.contains(target.getUniqueId())) return;
        if (!Bukkit.isPrimaryThread()) {
            logger.warning("Attempted async damage application!");
            return;
        }

        processingDamage.add(target.getUniqueId());
        target.damage(damage);
        processingDamage.remove(target.getUniqueId());
    }

    public void syncNewPlayer(Player newPlayer) {
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
        } else {
            logger.info("Player " + newPlayer.getName() + " already on current attempt #" + currentAttempt);
        }

        World targetWorld = Bukkit.getWorld(worldManager.getCurrentRunId());
        if (targetWorld == null) {
            targetWorld = worldManager.createNewWorld();
        }
        newPlayer.teleport(targetWorld.getSpawnLocation());
    }

    private void syncToExistingPlayer(Player newPlayer, Player existing) {
        newPlayer.setHealth(existing.getHealth());
        newPlayer.setFoodLevel(existing.getFoodLevel());
        newPlayer.teleport(existing.getLocation());
    }

    private void initializeNewPlayer(Player player) {
        World targetWorld = Bukkit.getWorld(worldManager.getCurrentRunId());
        if (targetWorld == null) {
            targetWorld = Bukkit.getWorlds().get(0); // Fallback only if no runs exist
        }
        player.teleport(targetWorld.getSpawnLocation());
    }

    public boolean handleTeamTotemActivation() {
        if (!totemService.teamHasTotems()) return false;

        Bukkit.getOnlinePlayers().forEach(this::applyTotemEffects);
        return true;
    }

    private void applyTotemEffects(Player player) {
        totemService.consumeTotem(player);
        player.setHealth(4);
        player.setFoodLevel(20);
        player.addPotionEffects(Arrays.asList(
                new PotionEffect(PotionEffectType.REGENERATION, 100, 1),
                new PotionEffect(PotionEffectType.RESISTANCE, 100, 1)
        ));
        clearNegativeEffects(player);
    }

    public void reviveAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::applyTotemEffects);
    }

    public void syncFoodLevel(Player source, int newLevel) {
        if (source.hasMetadata("syncing_food")) return;

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p != source) {
                p.setMetadata("syncing_food", new FixedMetadataValue(plugin, true));
                p.setFoodLevel(newLevel);
                p.removeMetadata("syncing_food", plugin);
            }
        });
    }

    public void syncExperience(Player source) {
        if (source.hasMetadata("syncing_exp")) return;

        float exp = source.getExp();
        int level = source.getLevel();

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p != source) {
                p.setMetadata("syncing_exp", new FixedMetadataValue(plugin, true));
                p.setExp(exp);
                p.setLevel(level);
                p.removeMetadata("syncing_exp", plugin);
            }
        });
    }

    public void fullReset(Player player) {
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
}