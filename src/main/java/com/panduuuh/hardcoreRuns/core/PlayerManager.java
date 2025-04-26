package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerManager {
    private final Set<UUID> processingDamage = new HashSet<>();
    private final ConfigurationManager config;
    private final HardcoreRuns plugin;
    private final TotemService totemService;

    public PlayerManager(HardcoreRuns plugin, ConfigurationManager config) {
        this.plugin = plugin;
        this.config = config;
        this.totemService = new TotemService();
    }

    public void handleDamage(Player source, double damage) {
        if (processingDamage.contains(source.getUniqueId())) return;

        processingDamage.add(source.getUniqueId());
        propagateDamage(source, damage);
        processingDamage.remove(source.getUniqueId());
    }

    private void propagateDamage(Player source, double damage) {
        Bukkit.getOnlinePlayers().parallelStream()
                .filter(p -> p != source)
                .forEach(p -> applySyncedDamage(p, damage));
    }

    private void applySyncedDamage(Player target, double damage) {
        processingDamage.add(target.getUniqueId());
        target.damage(damage);
        processingDamage.remove(target.getUniqueId());
    }

    public void syncNewPlayer(Player newPlayer) {
        Optional<Player> existing = Bukkit.getOnlinePlayers().stream()
                .map(p -> (Player) p)
                .filter(p -> p != newPlayer)
                .findFirst();

        existing.ifPresentOrElse(
                target -> syncToExistingPlayer(newPlayer, target),
                () -> initializeNewPlayer(newPlayer)
        );
    }

    private void syncToExistingPlayer(Player newPlayer, Player existing) {
        newPlayer.setHealth(existing.getHealth());
        newPlayer.setFoodLevel(existing.getFoodLevel());
        newPlayer.teleport(existing.getLocation());
    }

    private void initializeNewPlayer(Player player) {
        fullReset(player);
        player.teleport(Objects.requireNonNull(
                Bukkit.getWorld("world")).getSpawnLocation()
        );
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

    public void resetAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::fullReset);
    }

    private void clearNegativeEffects(Player player) {
        player.getActivePotionEffects()
                .forEach(e -> player.removePotionEffect(e.getType()));
    }
}