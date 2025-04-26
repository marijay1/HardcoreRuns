package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceListener implements Listener {
    private final PlayerManager playerManager;

    public ExperienceListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        playerManager.syncExperience(player);
    }
}