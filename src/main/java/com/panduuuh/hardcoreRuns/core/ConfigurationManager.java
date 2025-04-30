package com.panduuuh.hardcoreRuns.core;

import org.bukkit.plugin.Plugin;
import java.util.UUID;

public class ConfigurationManager {
    private final Plugin plugin;
    private int attempts;

    public ConfigurationManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void load() {
        plugin.reloadConfig();
        attempts = plugin.getConfig().getInt("attempts", 1);
        if (attempts <= 0) {
            attempts = 1;
            save();
        }
        plugin.getLogger().info("Loaded attempt count from config: " + attempts);
    }

    public void save() {
        plugin.getConfig().set("attempts", attempts);
        plugin.saveConfig();
    }

    public int getPlayerAttempt(UUID playerId) {
        return plugin.getConfig().getInt("players." + playerId, -1);
    }

    public void setPlayerAttempt(UUID playerId, int attempt) {
        plugin.getConfig().set("players." + playerId, attempt);
        save();
    }

    public String getActiveWorld() {
        return plugin.getConfig().getString("active-world", "world");
    }

    public void setActiveWorld(String worldName) {
        plugin.getConfig().set("active-world", worldName);
        save();
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        attempts++;
        save();
    }

    public String getWebhookUrl() {
        return plugin.getConfig().getString("discord-webhook", "");
    }

    public double getSharedHealth() {
        return plugin.getConfig().getDouble("shared.health", 20.0);
    }

    public int getSharedFood() {
        return plugin.getConfig().getInt("shared.food", 20);
    }

    public float getSharedExp() {
        return (float) plugin.getConfig().getDouble("shared.exp", 0.0);
    }

    public int getSharedLevel() {
        return plugin.getConfig().getInt("shared.level", 0);
    }

    public void setSharedHealth(double health) {
        plugin.getConfig().set("shared.health", health);
    }

    public void setSharedFood(int food) {
        plugin.getConfig().set("shared.food", food);
    }

    public void setSharedExp(float exp) {
        plugin.getConfig().set("shared.exp", (double) exp);
    }

    public void setSharedLevel(int level) {
        plugin.getConfig().set("shared.level", level);
    }

    public boolean isHealthShared() {
        return plugin.getConfig().getBoolean("shared.enable-health", true);
    }

    public boolean isFoodShared() {
        return plugin.getConfig().getBoolean("shared.enable-food", true);
    }

    public boolean isExpShared() {
        return plugin.getConfig().getBoolean("shared.enable-exp", true);
    }

    public boolean isLevelShared() {
        return plugin.getConfig().getBoolean("shared.enable-level", true);
    }

    public boolean isInventoryShared() {
        return plugin.getConfig().getBoolean("shared.enable-inventory", true);
    }

    public String getSharedInventoryId() {
        String id = plugin.getConfig().getString("shared.inventory-id");
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            plugin.getConfig().set("shared.inventory-id", id);
            save();
        }
        return id;
    }
}