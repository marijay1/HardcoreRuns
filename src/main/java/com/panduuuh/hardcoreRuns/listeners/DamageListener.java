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
        if (!(event.getEntity() instanceof Player)) return;
        if (event.isCancelled()) return;

        Player victim = (Player) event.getEntity();
        double damage = event.getFinalDamage();

        playerManager.handleDamage(victim, damage);
        broadcastDamageMessage(victim, damage);
    }

    private void broadcastDamageMessage(Player victim, double damage) {
        String message = ChatColor.WHITE + victim.getName() + " has taken " +
                ChatColor.RED + String.format("%.1f", damage / 2) + " ♥ " +
                ChatColor.RESET + "damage.";
        victim.getServer().broadcastMessage(message);
    }
}