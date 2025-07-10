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
        if (health < 0 || health > 20) {
            plugin.getLogger().warning("Invalid health value: " + health + ". Clamping to valid range [0, 20].");
            health = Math.max(0, Math.min(20, health));
        }
        plugin.getConfig().set("shared.health", health);
    }

    public void setSharedFood(int food) {
        if (food < 0 || food > 20) {
            plugin.getLogger().warning("Invalid food value: " + food + ". Clamping to valid range [0, 20].");
            food = Math.max(0, Math.min(20, food));
        }
        plugin.getConfig().set("shared.food", food);
    }

    public void setSharedExp(float exp) {
        if (exp < 0 || exp > 1) {
            plugin.getLogger().warning("Invalid exp value: " + exp + ". Clamping to valid range [0, 1].");
            exp = Math.max(0, Math.min(1, exp));
        }
        plugin.getConfig().set("shared.exp", (double) exp);
    }

    public void setSharedLevel(int level) {
        if (level < 0) {
            plugin.getLogger().warning("Invalid level value: " + level + ". Setting to 0.");
            level = 0;
        }
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

    public double getDamageHeartsRatio() {
        return plugin.getConfig().getDouble("damage.hearts-per-damage", 2.0);
    }

    public double getMinDamageThreshold() {
        return plugin.getConfig().getDouble("damage.min-threshold", 0.05);
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