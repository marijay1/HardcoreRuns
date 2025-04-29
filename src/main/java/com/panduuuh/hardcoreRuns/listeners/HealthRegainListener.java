package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HealthRegainListener implements Listener {
    private final PlayerManager playerManager;

    public HealthRegainListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (playerManager.isProcessingDamage(player.getUniqueId())) return;
        if (!playerManager.getConfig().isHealthShared()) return;

        double newHealth = Math.min(20.0, player.getHealth() + event.getAmount());
        playerManager.handleHealing(player, newHealth);
    }
}