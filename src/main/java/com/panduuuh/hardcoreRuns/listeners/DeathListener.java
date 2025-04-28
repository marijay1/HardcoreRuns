package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.DiscordService;
import com.panduuuh.hardcoreRuns.core.NotificationService;
import com.panduuuh.hardcoreRuns.core.PlayerManager;
import com.panduuuh.hardcoreRuns.core.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final PlayerManager playerManager;
    private final WorldManager worldManager;
    private final DiscordService discordService;
    private final NotificationService notifications;

    public DeathListener(PlayerManager playerManager, WorldManager worldManager,
                         DiscordService discordService, NotificationService notifications) {
        this.playerManager = playerManager;
        this.worldManager = worldManager;
        this.discordService = discordService;
        this.notifications = notifications;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        long timeAlive = System.currentTimeMillis() - worldManager.getRunStartTime();

        if (playerManager.handleTeamTotemActivation()) {
            playerManager.reviveAllPlayers();
            event.setDeathMessage(null);
            return;
        }

        if (worldManager.isResetPending()) {
            checkAllPlayersDead(player, timeAlive, event.getDeathMessage());
            return;
        }

        worldManager.setResetPending(true);
        worldManager.incrementAttempt();

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.isOnline() && !p.isDead() && p != player)
                .forEach(p -> p.setHealth(0));

        Bukkit.getScheduler().runTaskLater(playerManager.getPlugin(), () -> {
            checkAllPlayersDead(player, timeAlive, event.getDeathMessage());
        }, 1L);
    }

    private void checkAllPlayersDead(Player player, long timeAlive, String deathMessage) {
        boolean allDead = Bukkit.getOnlinePlayers().stream()
                .allMatch(p -> p.isDead() || !p.isOnline());

        if (allDead) {
            discordService.sendDeathAlert(player, timeAlive, deathMessage);
            Bukkit.getOnlinePlayers().forEach(notifications::promptWorldReset);
            worldManager.setResetPending(false);
        }
    }
}