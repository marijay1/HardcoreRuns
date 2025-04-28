package com.panduuuh.hardcoreRuns.core;

import org.bukkit.plugin.Plugin;

public class BukkitLogger implements Logger {
    private final java.util.logging.Logger bukkitLogger;

    public BukkitLogger(Plugin plugin) {
        this.bukkitLogger = plugin.getLogger();
    }

    @Override
    public void info(String message) {
        bukkitLogger.info(message);
    }

    @Override
    public void warning(String message) {
        bukkitLogger.warning(message);
    }

    @Override
    public void severe(String message) {
        bukkitLogger.severe(message);
    }
}