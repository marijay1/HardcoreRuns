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
}