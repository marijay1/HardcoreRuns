package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {
    private final PlayerManager playerManager;

    public DamageListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!playerManager.getConfig().isHealthShared()) return;
        if (event.isCancelled()) return;

        // Skip if this damage is from sync
        if (playerManager.isProcessingDamage(victim.getUniqueId())) {
            return;
        }

        double damage = event.getFinalDamage();
        double heartsPerDamage = playerManager.getConfig().getDamageHeartsRatio();
        double hearts = damage / heartsPerDamage;

        double minThreshold = playerManager.getConfig().getMinDamageThreshold();
        if (hearts <= minThreshold) return;

        playerManager.handleDamage(victim, damage);
        broadcastHeartsLost(victim, hearts);
    }

    private void broadcastHeartsLost(Player victim, double hearts) {
        double roundedHearts = Math.round(hearts * 2) / 2.0;

        String heartsFormatted = roundedHearts % 1 == 0
                ? String.format("%.0f", roundedHearts)
                : String.format("%.1f", roundedHearts);

        String message = ChatColor.WHITE + victim.getName() + " has taken" +
                ChatColor.RED + " ❤ " + heartsFormatted +
                ChatColor.WHITE + " hearts";

        victim.getServer().broadcastMessage(message);
    }
}