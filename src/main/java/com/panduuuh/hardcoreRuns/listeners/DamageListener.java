package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {
    private static final double HEARTS_PER_DAMAGE = 2.0; // 1 heart = 2 damage points
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
        double heartsLost = damage / HEARTS_PER_DAMAGE;

        playerManager.handleDamage(victim, damage);
        broadcastHeartsLost(victim, heartsLost);
    }

    private void broadcastHeartsLost(Player victim, double hearts) {
        String heartsFormatted = String.format("%.1f", hearts);
        String message = ChatColor.WHITE + victim.getName() + " has taken " +
                ChatColor.RED + " ❤ " + heartsFormatted +
                ChatColor.WHITE + " hearts";

        victim.getServer().broadcastMessage(message);
    }
}