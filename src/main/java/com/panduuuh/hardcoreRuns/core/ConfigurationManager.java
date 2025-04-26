package com.panduuuh.hardcoreRuns.core;

import org.bukkit.plugin.Plugin;

public class ConfigurationManager {
    private final Plugin plugin;
    private int attempts;

    public ConfigurationManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void load() {
        attempts = plugin.getConfig().getInt("attempts", 0);
    }

    public void save() {
        plugin.getConfig().set("attempts", attempts);
        plugin.saveConfig();
    }

    public int getAttempts() { return attempts; }
    public void incrementAttempts() { attempts++; }
    public String getWebhookUrl() {
        return plugin.getConfig().getString("discord-webhook", "");
    }
}