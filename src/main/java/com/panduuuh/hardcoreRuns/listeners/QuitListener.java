package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final ConfigurationManager config;

    public QuitListener(ConfigurationManager config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() - 1 == 0) {
            config.save();
        }
    }
}