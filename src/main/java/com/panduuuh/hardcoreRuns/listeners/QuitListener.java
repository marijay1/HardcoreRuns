package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.ConfigurationManager;
import com.panduuuh.hardcoreRuns.core.SharedInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final ConfigurationManager config;
    private final SharedInventoryManager sharedInventory;

    public QuitListener(ConfigurationManager config, SharedInventoryManager sharedInventory) {
        this.config = config;
        this.sharedInventory = sharedInventory;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() - 1 == 0) {
            config.save();
            sharedInventory.saveSharedInventories();
        }
    }
}